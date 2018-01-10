import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Project : prr_labo2
 * Date : 14.12.17
 * Authors : Antoine Friant, Michela Zucca
 * <p>
 * Algorithme d'élection en anneau avec panne des sites possibles (et gérées).
 * - En cas de reprise de panne d'un site, l'élection continue en excluant le site en panne
 * - En cas de panne d'un site entre la réception d'un message et l'envoi du suivant, l'élection est relancée
 * - En cas de panne de l'élu une nouvelle élection est démarrée
 *
 * L'algorithme suppose que la connexion réseau entre les sites est entièrement fiable, et que les sites
 * sont numérotés dans l'ordre de 0 à numberOfSites - 1, sans "trou".
 */
public class AlgoElection implements Runnable {

    enum Phase {ANNOUNCE, RESULT}

    private static final long CHECKOUT_TIMEOUT = 200; // Timeout de l'attente de quittance en ms
    private static final long ELECTION_TIMEOUT = 2000; // Timeout de l'attente du message de résultat en ms
    private final byte SITE_COUNT;                    // nombre total de sites
    private Site me;                                // Site courant
    private byte neighbour;                         // Site voisin
    private byte electedSite;                       // Site élu actuel (accès concurrents => protégé par des synchronised)
    private UDPController udpController;            // Envoi/réception de messages
    private Phase phase;                            // Etape de l'algorithme
    private Message lastSentMessage;                // Dernier message envoyé (pour nouvelle tentative en cas de panne)
    private long timeOfLastSentMessage;             // Heure du dernier message envoyé (pour gérer le timeout)
    private boolean waitingForCheckout = false;     // vrai si on attend une quittance (pour timeout)
    private boolean waitingForResult = false;       // vrai si on attend un message de type résultat (pour timeout)

    /**
     * Constructeur
     *
     * @param num numéro du site local
     * @param udpController contrôleur UDP à utiliser pour les élections
     */
    public AlgoElection(byte num, UDPController udpController, byte numberOfSites) {
        this.SITE_COUNT = numberOfSites;
        this.me = new Site(num, udpController.getAptitude());
        this.udpController = udpController;
        this.electedSite = num;
        System.out.println("***** Site : " + me.getNoSite() + " aptitude : " + me.getAptitude() + " started *****");
    }

    /**
     * Lancement d'une élection
     */
    public void startElection() {
        System.out.println("Election start");

        // réinitialise le numéro du site voisin (ignore les pannes précédentes)
        neighbour = (byte) ((me.getNoSite() + 1) % SITE_COUNT);

        // envoie la première annonce
        SortedSet<Site> election = new TreeSet<>();
        election.add(me);
        send(neighbour, new Message(Message.MessageType.ANNOUNCE, election));
        waitingForResult = true;

        phase = Phase.ANNOUNCE;
    }

    /**
     * Annonce reçue d'un autre site, traitement de l'information.
     * @param message annonce à traiter
     */
    private void annoucement(Message message) {
        waitingForResult = true;
        if (message.getSites().contains(me)) { // si je suis déjà dans la liste ...
            // change l'élu du site
            synchronized (this) {
                electedSite = message.getResultByte();
                System.out.println("Elected site : " + electedSite);
            }

            // envoie le message de résultat contenant l'élu et vu par moi
            SortedSet<Site> seenBy = new TreeSet<>();
            seenBy.add(me); // vu par moi
            Message messageResult = new Message(Message.MessageType.RESULT, seenBy);
            messageResult.setResultByte(message.getResultByte()); // site élu
            send(neighbour, messageResult);
            phase = Phase.RESULT;

        } else {
            // s'ajoute à la liste des candidats
            message.getSites().add(me);

            // réinitialise le numéro du site voisin (ignore les pannes précédentes), puis fait passer l'annonce
            neighbour = (byte) ((me.getNoSite() + 1) % SITE_COUNT);
            send(neighbour, message);
            phase = Phase.ANNOUNCE;
        }
    }

    /**
     * Réception du résultat de l'élection
     * @param message résultat à traiter
     */
    private void result(Message message) {
        waitingForResult = false;
        if (!message.getSites().contains(me)) {
            // le résultat n'a pas fait un tour complet ...
            if (phase == Phase.RESULT && getElectedSite() != message.getResultByte()) {
                // 2 elections en cours.. redémarre l'élection
                startElection();
            } else if (phase == Phase.ANNOUNCE) {
                // sauvegarde le résultat de l'élection
                synchronized (this) {
                    electedSite = message.getResultByte();
                    System.out.println("END - Elected site : " + message.getResultByte());
                }

                // s'ajoute à la liste des sites ayant vu le résultat, et l'envoie au suivant
                message.getSites().add(me);
                send(neighbour, message);
                phase = Phase.RESULT;
            }
        }
    }

    /**
     * Envoie un message d'annnonce ou de résultat de façon résistante aux pannes (prépare les timeouts)
     * @param dest numéro du site destinataire
     * @param message message à envoyer
     */
    private void send(byte dest, Message message) {
        System.out.println("SEND => " + message.toString() + " to site " + dest);

        // sauvegarde le message s'il faut le renvoyer en cas de panne
        lastSentMessage = message;

        // exige un checkout avant le timeout
        waitingForCheckout = true;
        timeOfLastSentMessage = System.currentTimeMillis();

        // effectue l'envoi
        udpController.send(dest, message);
    }

    /**
     * Retourne le numéro de l'élu actuel (thread safe)
     *
     * @return l'élu
     */
    public byte getElectedSite() {
        synchronized (this) {
            return electedSite;
        }
    }

    @Override
    public void run() {
        // Lance une élection immédiatement pour s'annoncer
        startElection();

        // Boucle d'exécution de l'algorithme d'élection
        while (true) {
            Message message;
            try {
                // attend de recevoir le message suivant (lance SocketTimeoutException si l'attente est trop longue)
                message = udpController.listen();
                System.out.println("RECEIVE => " + message.toString());

                if (message.getMessageType() == Message.MessageType.CHECKOUT) {
                    // quittance reçue
                    waitingForCheckout = false;
                } else if (message.getMessageType() == Message.MessageType.ANNOUNCE) {
                    // annonce reçue
                    annoucement(message);
                } else if (message.getMessageType() == Message.MessageType.RESULT) {
                    // resultat reçu
                    result(message);
                }
            } catch (SocketTimeoutException e) {
                // Simple timeout de réception, rien à faire.
                // Ce timeout sert à éxécuter la clause "finally" périodiquement, même si rien n'est reçu.
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // vérifie les timeouts
                if (waitingForCheckout && System.currentTimeMillis() - timeOfLastSentMessage > CHECKOUT_TIMEOUT) {
                    System.out.println("TIMEOUT - Checkout expected but no received, neighbour skipped");
                    // aucune quittance reçue dans le temps imparti => relance le message au site suivant

                    // retire le site en panne de la liste
                    lastSentMessage.getSites().removeIf((s) -> s.getNoSite() == neighbour);

                    // saute le site en panne
                    neighbour = (byte) ((neighbour + 1) % SITE_COUNT);

                    // renvoie le message au site suivant
                    send(neighbour, lastSentMessage);

                } else if (waitingForResult && System.currentTimeMillis() - timeOfLastSentMessage > ELECTION_TIMEOUT) {
                    System.out.println("TIMEOUT - Result expected but no received, restarting election ...");

                    // aucun message de résultat reçu dans le temps imparti => relance l'élection
                    startElection();
                }
            }
        }
    }
}
