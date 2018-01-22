import java.util.Random;

/**
 * Project : prr_labo4
 * Author(s) : Antoine Friant, Michela Zucca
 * Date : 11.01.18
 *
 * Cette classe représente un travail réparti quelconque pouvant exiger du travail d'un autre site.
 */
public class Worker implements Runnable {
    private static final int TASK_MAX_TIME = 2000;              // temps d'exécution maximal d'une tâche
    private static final double TASK_REQUEST_PROBABILITY = 0.6; // probabilité d'envoyer une requête de travail

    private final Object STOP_REQUEST_MUTEX = new Object();     // mutex d'écriture pour la requête d'arret du travail

    private UDPController udpController;                        // controleur UDP pour l'envoi de requêtes
    private boolean stopRequested = false;                      // demande d'arrêt reçue ou pas
    private Thread thread;                                      // thread dédié à ce Worker

    /**
     * Constructeur. Lance immédiatement la tâche sur un thread dédié
     * @param udpController controleur UDP
     */
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

    /**
     * Accède à stopRequested de façon thread safe
     * @return true si et seulement si une demande d'arrêt a été faite
     */
    private boolean isStopRequested() {
        synchronized (STOP_REQUEST_MUTEX) {
            return stopRequested;
        }
    }

    @Override
    public void run() {
        Random rand = new Random();
        try {
            while (!isStopRequested()) {
                //System.out.println("Working ...");
                // Effectue un travail quelconque, ici représenté par une attente variable
                Thread.sleep(rand.nextInt(TASK_MAX_TIME));

                if (!isStopRequested() && rand.nextDouble() < TASK_REQUEST_PROBABILITY) {
                    // choisit un autre site au hasard
                    byte j = (byte) rand.nextInt(udpController.getSiteCount() - 1);
                    if (j >= udpController.getSiteId()) {
                        j++;
                    }

                    // lance un nouveau travail sur le site j
                    udpController.send(j, new Message(Message.MessageType.REQUEST, udpController.getSiteId()));
                    System.out.println("Sent task request to " + j);
                } else {
//                    System.out.println("Task over");
                    break;
                }
            }

            if (isStopRequested()) {
                System.out.println("Task stopped on request");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
