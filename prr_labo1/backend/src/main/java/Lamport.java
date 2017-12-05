import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Project : prr_labo1
 * Date : 16.11.17
 */

public class Lamport {
    private int numSite;
    private int nbSite;
    private long clockLogical;
    private boolean scAccorde;
    private ArrayList<Message> messageFile;
    private ArrayList<Integer> siteAdressFile;
    // Mutex
    private Lock waitDemande = new ReentrantLock();
    private Lock waitSC = new ReentrantLock();

    public Lamport(int numSite, int nbSite) {
        this.numSite = numSite;
        this.nbSite = nbSite;
        this.clockLogical = 0;
        this.scAccorde = false;
        this.messageFile = new ArrayList<Message>();
        this.siteAdressFile = new ArrayList<Integer>();
        initMessageFile();
        initSiteAdressFile();
    }

    /**
     * Initialisation de la file des messages des N sites
     */
    private void initMessageFile() {
        for (int i = 0; i < nbSite; i++) {
            messageFile.add(new Message(Message.TYPE.LIBERE, 0, i));
        }
    }

    /**
     * Initialisation des adresses des sites
     */
    private void initSiteAdressFile() {
        for (int i = 0; i < nbSite; i++) {
            siteAdressFile.add(i);
        }
    }

    /**
     * Retourne si le site actuel peut accéder à la section critique
     *
     * @param me numéro du site
     * @return
     */
    private boolean permission(int me) {
        boolean accord = true;
        for (int i = 0; i < siteAdressFile.size(); i++) {
            if (i != me) {
                accord = (messageFile.get(me).getEstampille() < messageFile.get(i).getEstampille()) ||
                        (messageFile.get(me).getEstampille() == messageFile.get(i).getEstampille() && me < i);
            }
        }
        return accord;
    }

    /**
     * Envoi un message "msg" à un site "dest"
     *
     * @param msg
     * @param dest
     */
    public void envoi(Message msg, int dest) {
        // Envoyer au site dest, la requete et mon num de site
        // Utiliser RMI pour faire le lien entre site numéro i et son adresse
    }

    /**
     * Demande d'entrer en section critique
     *
     * @throws InterruptedException
     */
    public void demande() throws InterruptedException {
        // Demande d'accès, 1 seul à la fois
        this.waitDemande.lock();

        // Maj horloge interne
        this.clockLogical += 1;
        // Enregistre la requête dans sa liste
        Message req = new Message(Message.TYPE.REQUETE, clockLogical, numSite);
        messageFile.set(this.numSite, req);
        // Signaler à tous les autres sites la nouvelle requête
        for (int i = 0; i < siteAdressFile.size(); i++) {
            if (i != numSite) {
                envoi(req, i);
            }
        }
        scAccorde = permission(numSite);

        // Attente d'autorisation à la section critique
        if(!scAccorde){
            this.waitSC.lock();
        }
    }

    /**
     * Fin de la section critique, libère l'accès.
     */
    public void fin() {
        // Enregistre la requête dans sa liste
        Message req = new Message(Message.TYPE.LIBERE, clockLogical, numSite);
        messageFile.set(this.numSite, req);
        // Signaler à tous les autres sites la nouvelle requête
        for (int i = 0; i < siteAdressFile.size(); i++) {
            if (i != numSite) {
                envoi(req, i);
            }
        }
        scAccorde = false;
        // Relache une éventuelle demande
        this.waitDemande.unlock();
    }

    /**
     * Traitement des messages reçus du type REQUETE, LIBERE et QUITTANCE
     *
     * @param msg message à analyser
     */
    public void recoit(Message msg) {
        // Maj de l'horloge logique
        clockLogical = Math.max(clockLogical, msg.getEstampille()) + 1;
        switch (msg.getType()) {
            case REQUETE:
                messageFile.set(msg.getOriginSite(), msg);
                envoi(new Message(Message.TYPE.QUITTANCE, clockLogical, numSite), msg.getOriginSite());
                break;
            case LIBERE:
                messageFile.set(msg.getOriginSite(), msg);
                break;
            case QUITTANCE:
                if (messageFile.get(msg.originSite).getType() != Message.TYPE.REQUETE) {
                    messageFile.set(msg.getOriginSite(), msg);
                }
                break;
        }
        // Vérifie l'accès à la section critique
        scAccorde = (messageFile.get(numSite).getType() == Message.TYPE.REQUETE) && permission(numSite);

        // Libère une éventuelle attente d'accès
        if(scAccorde){
            this.waitSC.unlock();
        }
    }

}