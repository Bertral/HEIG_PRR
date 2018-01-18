import java.util.Random;

/**
 * Project : prr_labo4
 * Author(s) : Antoine Friant, Michela Zucca
 * Date : 11.01.18
 */
public class Worker implements Runnable {
    private static final int TASK_MAX_TIME = 2000;
    private static final double TASK_REQUEST_PROBABILITY = 0.5;

    private final Object STOP_REQUEST_MUTEX = new Object();
    private final Object RUNNING_MUTEX = new Object();

    private UDPController udpController;
    private boolean running = true;
    private boolean stopRequested = false;
    private Thread thread;

    public Worker(UDPController udpController) {
        this.udpController = udpController;
        this.thread = new Thread(this);
        thread.start();
    }

    /**
     * Interdit au Worker de lancer ou demander une nouvelle tâche.
     */
    public void requestStop() {
        synchronized (STOP_REQUEST_MUTEX) {
            stopRequested = true;
        }
    }

    /**
     * Attend la mort du thread.
     * Cette méthode ne permet pas d'arrêter le Worker (pour cela, appeler requestStop())
     */
    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isStopRequested() {
        synchronized (STOP_REQUEST_MUTEX) {
            return stopRequested;
        }
    }

    public boolean isRunning() {
        synchronized (RUNNING_MUTEX) {
            return running;
        }
    }

    @Override
    public void run() {
        Random rand = new Random();
        try {
            while (!isStopRequested()) {
                System.out.println("Working ...");
                Thread.sleep(rand.nextInt(TASK_MAX_TIME));

                if (!isStopRequested() && rand.nextDouble() < TASK_REQUEST_PROBABILITY) {
                    // choisit un autre site au hasard
                    byte j = (byte) rand.nextInt(udpController.getSiteCount() - 1);
                    if (j >= udpController.getSiteId()) {
                        j++;
                    }

                    // lance un nouveau travail sur le site j
                    udpController.send(j, new Message(Message.MessageType.REQUETE, udpController.getSiteId()));
                    System.out.println("Sent task request to " + j);
                } else {
                    System.out.println("Task over");
                    break;
                }
            }

            if (isStopRequested()) {
                System.out.println("Task stopped on request");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (RUNNING_MUTEX) {
            running = false;
        }
    }
}
