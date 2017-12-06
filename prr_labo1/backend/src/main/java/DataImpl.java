/**
 * Project : prr_labo2
 * Date : 16.11.17
 * Auteurs : Antoine Friant et Michela Zucca
 *
 * Objet contenant
 * Gestionnaire du site en charge de maintenir la cohérence de la donnée <value>
 *     - Utilisation de RMI pour la communication entre les sites et les tâches applicatives
 *     - Utilisation de l'algorithme de Lamport pour le partage de la section critique
 *          - Utilisation d'une file de message pour stocker l'état des autres sites.
 *          - Horloge logique recalibré à chaque envoi de <request> ou de réception d'un message
 *          - Messages :
 *              Request : demande émise pour un souhait d'entrer en section critique,
 *                        doit être suivi de la réception d'une quittance des autres sites.
 *              Receipt : quittance émise à la réception d'une demande d'exclusion
 *              Release : message émis à la fin de la section critique et envoyé à tous les autres sites.
 *          - Règle d'accès à une section critique lors d'une demande (request) :
 *              - Cas 1 : estampille(i) < estampille(j) : accès à la section critique pour i
 *              - Cas 2 : estampile(i) = estampille(j) et i < j : accès à la section critique pour i
 *
 * Remarques : pour faciliter la vérification du fonctionnement de l'algorithme des messages sont afficher pour indiquer
 * les différentes actions réalisées côté serveur.
 *  - Demande d'accès à la section ctirique
 *      - Attente
 *      - Accès
 *      - Fin
 *  - Messages échangés < TYPE, estampille, site d'origine>
 *  - Accès en lecture : affichage de la valeur retournée
 *  - Accès en écriture : Affichage de la valeur modifiée
 */

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Properties;

public class DataImpl extends UnicastRemoteObject implements Data {
    private static int value;                       // Valeur globale
    private int numSite;                            // Id du site (0 à n-1)
    private int nbSite;                             // Nombre de sites (n)
    private long clockLogical;                      // Horloge logique
    private boolean scGrant;                        // Possibilité d'accès à la variable globale
    private ArrayList<Message> messageFile;         // Messages reçus par les sites (un site par index)
    private ArrayList<Integer> siteAdressFile;      // Adresses des sites
    private boolean waitAccesSC;                    // Vrai si le client est en attente (condition d'attente)
    // Objets de synchronisations
    private static final Object lockFile = new Object();
    private static final Object lockClock = new Object();
    private static final Object lockValue = new Object();
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
     * @param numSite numéro du site
     * @param nbSite nombre de site
     */
    public void init(int numSite, int nbSite) {
        this.value = 0;
        this.numSite = numSite;
        this.nbSite = nbSite;
        this.clockLogical = 0;
        this.scGrant = false;
        this.waitAccesSC = false;
        this.messageFile = new ArrayList<Message>();
        this.siteAdressFile = new ArrayList<Integer>();

        initMessageFile();
        initSiteAdressFile();
    }

    /**
     * Implémentaiton de l'interface RMI
     * Lecture de la valeur globale.
     * @return valeur
     * @throws RemoteException
     */
    @Override
    public int getValue() throws RemoteException {
        int i;
        synchronized (lockValue){
            System.out.println("Read value : " + value);
             i = value;
        }
        return i;
    }

    /**
     * Implémentaiton de l'interface RMI
     * Demande d'acquisition du mutex, envoi du message <Request> méthode bloquante.
     * @throws RemoteException
     */
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

    /**
     * Implémentaiton de l'interface RMI
     * Relachement du mutex, envoi du message <Release>
     * @throws RemoteException
     */
    @Override
    public void releaseMutex() throws RemoteException {
        System.out.println("Releasing mutex");
        end();
    }

    /**
     * Implémentaiton de l'interface RMI
     * Ecriture d'une nouvelle valeur pour la variable globale
     * @param value
     * @throws RemoteException
     */
    @Override
    public void setValue(int value) throws RemoteException {
       System.out.println("Write value : " + value);
       synchronized (lockValue) {
           this.value = value;
       }
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
        synchronized (lockFile) {
            for (int i = 0; i < siteAdressFile.size(); i++) {
                if (i != me) {
                    accord = accord && ((messageFile.get(me).getStamp() < messageFile.get(i).getStamp()
                            || (messageFile.get(me).getStamp() == messageFile.get(i).getStamp() && me < i)));
                }
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
            System.out.println("Send message to site : " + dest
                    +" < type: " + msg.type + ", stamp: "+msg.stamp +", origin: " +msg.originSite
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
    private void demande() throws InterruptedException {
        // Maj horloge interne
        synchronized (lockClock) {
            this.clockLogical += 1;
        }

        // Enregistre la requête dans sa liste
        Message req = new Message(Message.TYPE.REQUEST, clockLogical, numSite);
        synchronized (lockFile) {
            messageFile.set(this.numSite, req);
        }
        // Signaler à tous les autres sites la nouvelle requête
        for (int i = 0; i < siteAdressFile.size(); i++) {
            if (i != numSite) {
                send(req, i);
            }
        }
        scGrant = permission(numSite);
        if (!scGrant) {
            System.out.println("** Wait SC");
            waitAccesSC = true;
            synchronized (this) {
                wait();
            }
            scGrant = true;
        }
        System.out.println("** Acces SC");
    }

    /**
     * Fin de la section critique, libère l'accès.
     */
    private void end() throws RemoteException {
        // Enregistre la requête dans sa liste
        Message msg = new MessageRelease(clockLogical, numSite, getValue());
        synchronized (lockFile) {
            messageFile.set(this.numSite, msg);
        }

        // Signaler à tous les autres sites la nouvelle requête
        for (int i = 0; i < siteAdressFile.size(); i++) {
            if (i != numSite) {
                send(msg, i);
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
        synchronized (lockClock) {
            clockLogical = Math.max(clockLogical, msg.getStamp()) + 1;
        }

        System.out.println("Received message < type: " + msg.type + ", stamp: " + msg.stamp
                +", origin: " + msg.getOriginSite()+" >");

        switch (msg.getType()) {
            case REQUEST:
                synchronized (lockFile) {
                    messageFile.set(msg.getOriginSite(), msg);
                }
                send(new Message(Message.TYPE.RECEIPT, clockLogical, numSite), msg.getOriginSite());
                break;
            case RELEASE:
                try {
                    synchronized (lockFile) {
                        messageFile.set(msg.getOriginSite(), msg);
                    }
                    setValue(((MessageRelease) msg).getNewValue());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case RECEIPT:
                synchronized (lockFile) {
                    if (messageFile.get(msg.originSite).getType() != Message.TYPE.REQUEST) {
                        messageFile.set(msg.getOriginSite(), msg);
                    }
                }
                break;
        }
        // Vérifie l'accès à la section critique
        Message.TYPE t;
        synchronized (lockFile) {
            t = messageFile.get(numSite).getType();
        }
        scGrant = (t == Message.TYPE.REQUEST) && permission(numSite);

        synchronized (lockFile) {
            System.out.println("Client authorization  : " + scGrant + ", req : " + messageFile.get(numSite).getType());
        }
        if (scGrant && waitAccesSC) {
            System.out.println("** Release client");
            waitAccesSC = false;
            synchronized (this) {
                notify();
            }
        }

        // affiche état
        synchronized (lockFile) {
            System.out.println("State sites : ");
            for (Message m : messageFile) {
                System.out.println("< " + m.type + ", " + m.stamp + ", " + m.originSite + " >");
            }
        }
    }
}
