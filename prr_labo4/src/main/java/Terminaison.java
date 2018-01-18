import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Project : prr_labo4
 * Date : 11.01.18
 * Authors : Antoine Friant, Michela Zucca
 *
 * Cette classe implémente l'algorithme de terminaison en anneau de tâches réparties.
 */
public class Terminaison implements Runnable {
    enum T_Etat {actif, inactif}

    private T_Etat etat;                                            // état actif/inactif des tâches
    private byte localSiteId;                                       // numéro du site local
    private byte N;                                                 // nombre de sites
    private UDPController udpController;                            // controleur UDP, responsable des communications
    private List<Worker> workers = new CopyOnWriteArrayList<>();    // liste des tâches actives (thread safe)
    private AtomicBoolean isRunning = new AtomicBoolean(true);   // condition d'arrêt du thread (thread safe)

    /**
     * Constructeur
     * @param udpController controleur UDP pour la communication
     */
    public Terminaison(UDPController udpController) {
        this.etat = T_Etat.actif;
        this.udpController = udpController;
        this.localSiteId = udpController.getSiteId();
        this.N = udpController.getSiteCount();
    }

    /**
     * Lance une nouvelle tâche
     */
    public void newTask() {
        workers.add(new Worker(udpController));
    }

    /**
     * Acccesseur de l'état du _THREAD_ (pas de la terminaison)
     * @return boolean
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * Demande l'arrêt du travail sur tous les sites
     */
    public void requestStop() {
        if (isRunning.get()) {
            processMessage(new Message(Message.MessageType.TOKEN, localSiteId));
        }
    }

    /**
     * Coeur de l'algorithme de terminaison en anneau, traite les messages des autres sites
     * @param msg Message reçu
     */
    private void processMessage(Message msg) {
        byte neightbour = (byte) ((localSiteId + 1) % N);

        switch (msg.getMessageType()) {
            case REQUEST:
                if (etat != T_Etat.inactif) {
                    //System.out.println("Received REQUEST to " + msg.getOriginSite());
                    // on est actif et on reçoit une requête de travail => lance une tâche
                    newTask();
                } else {
                    // on est déjà désactivé, on ne peut donc pas relancer de tâche
                    System.out.println("Impossible to start new Task");
                }
                break;
            case TOKEN:
                if (etat == T_Etat.inactif) {
                    // Envoyer END au voisin
                    udpController.send(neightbour, new Message(Message.MessageType.END, msg.getOriginSite()));
                } else {
                    for (Worker w : workers) {
                        // demande l'arrêt de toutes les tâches
                        w.requestStop();
                    }

                    for (Worker w : workers) {
                        // attend la fin des threads
                        w.join();
                    }

                    // Passe en inactif et on transmet le jeton
                    etat = T_Etat.inactif;
                    System.out.println("Send TOKEN to " + neightbour + " origin : " + msg.getOriginSite());
                    udpController.send(neightbour, new Message(Message.MessageType.TOKEN, msg.getOriginSite()));
                }
                break;
            case END:
                System.out.println("Received END, origin : " + msg.getOriginSite());
                if (localSiteId != msg.getOriginSite()) {
                    // Transmet le jeton au voisin
                    System.out.println("Send END to " + neightbour);
                    udpController.send(neightbour, new Message(Message.MessageType.END, msg.getOriginSite()));
                }

                // arrête la boucle d'exécution du thread
                isRunning.set(false);
                break;
        }
    }

    @Override
    public void run() {
        // boucle d'exécution, reçoit les messages
        while (isRunning.get()) {
            try {
                processMessage(udpController.listen());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
