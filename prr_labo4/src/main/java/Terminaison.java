import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Project : prr_labo4
 * Date : 11.01.18
 * Authors : Antoine Friant, Michela Zucca
 */

public class Terminaison implements Runnable {
    enum T_Etat {actif, inactif}

    private T_Etat etat;
    private byte moi;
    private byte N;
    private UDPController application;
    private List<Worker> workers = new LinkedList<>();
    public AtomicBoolean isRunning = new AtomicBoolean(true);

    public Terminaison(UDPController application, byte proc, byte N) {
        this.etat = T_Etat.actif;
        this.moi = proc;
        this.application = application;
        this.N = N;
    }

    public void newTask() {
        workers.add(new Worker(application));
    }

    public void requestStop() {
        if(isRunning.get()) {
            travail(new Message(Message.MessageType.JETON, moi));
        }
    }

    private void travail(Message msg) {
        byte neightbour = (byte) ((moi +1) % N);

        switch (msg.getMessageType()) {
            case REQUETE:
                if(etat != T_Etat.inactif) {
                    //System.out.println("Received REQUETE to " + msg.getOriginSite());
                    newTask();
                }else{
                    System.out.println("Impossible to start new Task");
                }
                break;
            case JETON:
                if (etat == T_Etat.inactif) {
                    // Envoyer fin au voisin
                    application.send(neightbour, new Message(Message.MessageType.FIN, msg.getOriginSite()));
                } else {

                    // Vérifier si les workers sont terminés
                    for (Worker w : workers) {
                        // demande l'arrêt
                        w.requestStop();
                    }

                    for (Worker w : workers) {
                        // attend la fin des threads
                        if (w.isRunning()) {
                            w.join(); // rejoint pour attendre qu'il finisse, sinon il ne relancera pas de travail
                        }
                    }

                    // Passe en inactif et on transmet le jeton
                    etat = T_Etat.inactif;
                    System.out.println("Send JETON to " + neightbour + " origin : "+ msg.getOriginSite());
                    application.send(neightbour, new Message(Message.MessageType.JETON, msg.getOriginSite()));
                }
                break;
            case FIN:
                System.out.println("Received FIN to " + msg.getOriginSite());
                if (moi != msg.getOriginSite()) {
                    // Transmet le jeton au voisi
                    System.out.println("Send FIN to "+ neightbour);
                    application.send(neightbour, new Message(Message.MessageType.FIN, msg.getOriginSite()));
                }
                isRunning.set(false);
                break;
        }
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            try {
                travail(application.listen());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
