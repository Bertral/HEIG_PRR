import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Project : prr_labo1
 * Date : 16.11.17
 */
public class DataImpl extends UnicastRemoteObject implements Data {
    private int value = 0;
    private int numSite;
    private int nbSite;
    private long clockLogical;
    private boolean scAccorde;
    private ArrayList<Message> messageFile;
    private ArrayList<Integer> siteAdressFile;
    private boolean waitClient = false;
    private final Object lock = new Object();


    protected DataImpl() throws RemoteException {
        super();
    }

    public void init(int numSite, int nbSite) {
        this.numSite = numSite;
        this.nbSite = nbSite;
        this.clockLogical = 0;
        this.scAccorde = false;
        this.messageFile = new ArrayList<Message>();
        this.siteAdressFile = new ArrayList<Integer>();
        initMessageFile();
        initSiteAdressFile();
    }

    public int getValue() throws RemoteException {
        System.out.println("Sending value : " + value);
        return value;
    }

    public void lockMutex() throws RemoteException {
        System.out.println("Locking mutex");
        try {
            demande();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    public void releaseMutex() throws RemoteException {
        System.out.println("Releasing mutex");
        fin();
    }

    public void setValue(int value) throws RemoteException {
        System.out.println("Setting value : " + value);
        this.value = value;
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
                accord = accord && ((messageFile.get(me).getEstampille() < messageFile.get(i).getEstampille()
                        || (messageFile.get(me).getEstampille() == messageFile.get(i).getEstampille() && me < i)));
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
    private void envoi(Message msg, int dest) {
        // Envoyer au site dest, la requete et mon num de site
        // Utiliser RMI pour faire le lien entre site numéro i et son adresse
        try {
            System.out.println("Envoi : " + msg.type + " a " + dest);
            // récupération de la liste des serveurs
            Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("sites.properties"));

            Remote r = Naming.lookup(properties.getProperty("" + dest));
            Data data = (Data) r;
            data.recoit(msg);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error envoi");
        }
    }

    /**
     * Demande d'entrer en section critique
     *
     * @throws InterruptedException
     */
    synchronized
    private void demande() throws InterruptedException {
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
            System.out.println(scAccorde);
            if (!scAccorde) {
                System.out.println("wait sc");
                waitClient = true;
                wait();
            }
            System.out.println("acces sc - fin demande");
    }

    /**
     * Fin de la section critique, libère l'accès.
     */
    private void fin() {
        // Enregistre la requête dans sa liste
        Message req = new MessageLibere(Message.TYPE.LIBERE, clockLogical, numSite, value);
        messageFile.set(this.numSite, req);

        // Signaler à tous les autres sites la nouvelle requête
        for (int i = 0; i < siteAdressFile.size(); i++) {
            if (i != numSite) {
                envoi(req, i);
            }
        }
        scAccorde = false;
    }

    /**
     * Traitement des messages reçus du type REQUETE, LIBERE et QUITTANCE
     *
     * @param msg message à analyser
     */

    public void recoit(Message msg) {
        // Maj de l'horloge logique
        clockLogical = Math.max(clockLogical, msg.getEstampille()) + 1;

        System.out.println("Recoit " + msg.type + " de " + msg.getOriginSite());

        switch (msg.getType()) {
            case REQUETE:
                messageFile.set(msg.getOriginSite(), msg);
                envoi(new Message(Message.TYPE.QUITTANCE, clockLogical, numSite), msg.getOriginSite());
                break;
            case LIBERE:
                try {
                    messageFile.set(msg.getOriginSite(), msg);
                    setValue(((MessageLibere) msg).getNewValue());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case QUITTANCE:
                if (messageFile.get(msg.originSite).getType() != Message.TYPE.REQUETE) {
                    messageFile.set(msg.getOriginSite(), msg);
                }
                break;
        }
        // Vérifie l'accès à la section critique
        scAccorde = (messageFile.get(numSite).getType() == Message.TYPE.REQUETE) && permission(numSite);


            if (scAccorde && waitClient) {
                System.out.println("relache client");
                waitClient = false;
                synchronized (this) {
                    notify();
                }
            }


        // affiche état
        for(Message m : messageFile){
            System.out.println(m.type + " " + m.estampille + " "+ m.originSite );
        }
    }
}
