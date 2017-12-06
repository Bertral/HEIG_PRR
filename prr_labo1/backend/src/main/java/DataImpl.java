import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Project : prr_labo2
 * Date : 16.11.17
 * Auteurs : Antoine Friant et Michela Zucca
 * <p>
 * Objet contenant
 */
public class DataImpl extends UnicastRemoteObject implements Data {
    private int value = 0;                          // Valeur globale
    private int numSite;                            // Id du site (0 à n-1)
    private int nbSite;                             // Nombre de sites (n)
    private long clockLogical;                      // Horloge logique
    private boolean scGrant;                      // Possibilité d'accès à la variable globale
    private ArrayList<Message> messageFile;         // Messages reçus par les sites (un site par index)
    private ArrayList<Integer> siteAdressFile;      // Adresses des sites
    private boolean waitClient;                     // Vrai si le client est en attente (condition d'attente)

    /**
     * Constructeur
     *
     * @throws RemoteException
     */
    protected DataImpl() throws RemoteException {
        super();
    }

    /**
     * Initialise les données nécessaires au fonctionnement de l'algorithme de Lamport
     *
     * @param numSite
     * @param nbSite
     */
    public void init(int numSite, int nbSite) {
        this.numSite = numSite;
        this.nbSite = nbSite;
        this.clockLogical = 0;
        this.scGrant = false;
        this.waitClient = false;
        this.messageFile = new ArrayList<Message>();
        this.siteAdressFile = new ArrayList<Integer>();
        initMessageFile();
        initSiteAdressFile();
    }

    // RMI
    @Override
    public int getValue() throws RemoteException {
        System.out.println("Sending value : " + value);
        return value;
    }

    // RMI
    @Override
    public void lockMutex() throws RemoteException {
        System.out.println("Locking mutex");
        try {
            demande();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    // RMI
    @Override
    public void releaseMutex() throws RemoteException {
        System.out.println("Releasing mutex");
        end();
    }

    // RMI
    @Override
    public void setValue(int value) throws RemoteException {
        System.out.println("Setting value : " + value);
        this.value = value;
    }

    /**
     * Initialisation de la file des messages des N sites
     */
    private void initMessageFile() {
        for (int i = 0; i < nbSite; i++) {
            messageFile.add(new Message(Message.TYPE.RELEASE, 0, i));
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
     * @return droit d'accès à la section critique
     */
    private boolean permission(int me) {
        boolean accord = true;

        for (int i = 0; i < siteAdressFile.size(); i++) {
            if (i != me) {
                accord = accord && ((messageFile.get(me).getStamp() < messageFile.get(i).getStamp()
                        || (messageFile.get(me).getStamp() == messageFile.get(i).getStamp() && me < i)));
            }
        }
        return accord;
    }

    /**
     * Envoi un message "msg" à un site "dest" identifié par son numéro
     *
     * @param msg message
     * @param dest destinataire
     */
    private void send(Message msg, int dest) {
        try {
            System.out.println("Envoi message au site : " + dest
                    +" < type: " + msg.type + ", estampille: "+msg.stamp +", origine: " +msg.originSite
                    + (msg.type == Message.TYPE.RELEASE ? ((MessageRelease)msg).getNewValue()+" >" : " >"));
            // récupération de la liste des serveurs
            Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("sites.properties"));

            Remote r = Naming.lookup(properties.getProperty("" + dest));
            Data data = (Data) r;
            data.receive(msg);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Send error");
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
        Message req = new Message(Message.TYPE.REQUEST, clockLogical, numSite);
        messageFile.set(this.numSite, req);
        // Signaler à tous les autres sites la nouvelle requête
        for (int i = 0; i < siteAdressFile.size(); i++) {
            if (i != numSite) {
                send(req, i);
            }
        }
        scGrant = permission(numSite);
        if (!scGrant) {
            System.out.println("** Wait SC");
            waitClient = true;
            wait();
            scGrant = true;
        }
        System.out.println("** Acces SC");
    }

    /**
     * Fin de la section critique, libère l'accès.
     */
    private void end() {
        // Enregistre la requête dans sa liste
        Message req = new MessageRelease(clockLogical, numSite, value);
        messageFile.set(this.numSite, req);

        // Signaler à tous les autres sites la nouvelle requête
        for (int i = 0; i < siteAdressFile.size(); i++) {
            if (i != numSite) {
                send(req, i);
            }
        }
        scGrant = false;
    }

    /**
     * Traitement des messages reçus du type REQUEST, RELEASE et RECEIPT
     * Cette méthode est exposée par RMI pour la communication entre serveurs
     * @param msg message à analyser
     */
    @Override
    public void receive(Message msg) {
        // Maj de l'horloge logique
        clockLogical = Math.max(clockLogical, msg.getStamp()) + 1;

        System.out.println("Recoit message < type: " + msg.type + ", stamp: " + msg.stamp +", origine: " + msg.getOriginSite()+" >");

        switch (msg.getType()) {
            case REQUEST:
                messageFile.set(msg.getOriginSite(), msg);
                send(new Message(Message.TYPE.RECEIPT, clockLogical, numSite), msg.getOriginSite());
                break;
            case RELEASE:
                try {
                    messageFile.set(msg.getOriginSite(), msg);
                    setValue(((MessageRelease) msg).getNewValue());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case RECEIPT:
                if (messageFile.get(msg.originSite).getType() != Message.TYPE.REQUEST) {
                    messageFile.set(msg.getOriginSite(), msg);
                }
                break;
        }
        // Vérifie l'accès à la section critique
        scGrant = (messageFile.get(numSite).getType() == Message.TYPE.REQUEST) && permission(numSite);

        System.out.println("Autorisation client : " + scGrant + ", req : "+ messageFile.get(numSite).getType());
        if (scGrant && waitClient) {
            System.out.println("** Relache un client");
            waitClient = false;
            synchronized (this) {
                notify();
            }
        }

        // affiche état
        System.out.println("Etat de la file : ");
        for (Message m : messageFile) {
            System.out.println("< " + m.type + ", " + m.stamp + ", " + m.originSite+" >");
        }
    }
}
