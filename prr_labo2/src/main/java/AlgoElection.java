import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * Algorithme d'élection en anneau avec panne des sites possibles.
 * Une seule élection peut avoir lieu
 * - En cas de reprise de panne un site ne peut pas interrompre une élection
 * - En cas de panne de l'élu une nouvelle élection est démarrée
 */
public class AlgoElection implements Runnable {

    enum Phase {ANNOUNCE, RESULT}

    private static final long CHECKOUT_TIMEOUT = 200;
    private static final long ELECTION_TIMEOUT = 2000;
    private Site me; // Site courant
    private byte neighbour; // Site voisin
    private byte coordinator; // Site élu
    private UDPController udpController;
    private Phase phase;
    private Message lastSentMessage;
    private long timeOfLastSentMessage;
    private boolean waitingForCheckout = false;
    private boolean waitingForResult = false;

    /**
     * Constructeur
     *
     * @param num numéro du site local
     */
    public AlgoElection(byte num, UDPController udpController) {
        this.me = new Site(num, udpController.getAptitude());
        this.udpController = udpController;
        this.coordinator = num;
        System.out.println("***** Site : " + me.getNoSite() + " aptitude : " + me.getAptitude() + " started *****");
        start();
    }


    /**
     * Lancement d'une élection
     */
    public void start() {
        System.out.println("Election start");
        // réinitialise le voisin
        neighbour = (byte) ((me.getNoSite() + 1) % Main.getSiteCount());

        // envoie le premier message
        SortedSet<Site> election = new TreeSet<>();
        election.add(me);
        send(neighbour, new Message(Message.MessageType.ANNOUNCE, election));
        waitingForResult = true;

        phase = Phase.ANNOUNCE;
    }

    /**
     * Annonce reçu d'un autre site, traitement de l'information.
     *
     */
    private void annoucement(Message message) {
        waitingForResult = true;
        if (message.getSites().contains(me)) {
            // si je suis déjà dans la liste ...
            // change l'élu du site
            synchronized (this) {
                coordinator = message.getResultByte();
                System.out.println("Elected site : " + coordinator);
            }

            // envoie le message de résultat contenant l'élu
            SortedSet<Site> seenBy = new TreeSet<>();
            seenBy.add(me);
            Message messageResult = new Message(Message.MessageType.RESULT, seenBy);
            messageResult.setResultByte(message.getResultByte());
            send(neighbour, messageResult);
            phase = Phase.RESULT;

        } else {
            // s'ajoute à la liste
            message.getSites().add(me);

            // réinitialise le numéro du site voisin, puis fait passer l'annonce
            neighbour = (byte) ((me.getNoSite() + 1) % Main.getSiteCount());
            send(neighbour, message);
            phase = Phase.ANNOUNCE;
        }
    }

    /**
     * Réception du résultat de l'élection
     *
     */
    private void resultat(Message message) {
        waitingForResult = false;
        if (!message.getSites().contains(me)) {
            if (phase == Phase.RESULT && getCoordinator() != message.getResultByte()) {
                // 2 elections en cours.. redémarre l'élection
                start();
            } else if (phase == Phase.ANNOUNCE) {
                // inscrit le résultat de l'élection
                synchronized (this) {
                    coordinator = message.getResultByte();
                    System.out.println("Elected site : " + message.getResultByte());
                }

                // s'ajoute à la liste des sites ayant reçu le message, et l'envoie au suivant
                message.getSites().add(me);
                send(neighbour, message);
                phase = Phase.RESULT;
            }
        }
    }

    private void send(byte dest, Message message) {
        System.out.println("Site : " + me.getNoSite() + " SEND => " + message.toString() + " to site " + dest);
        lastSentMessage = message;
        waitingForCheckout = true;
        timeOfLastSentMessage = System.currentTimeMillis();
        udpController.send(dest, message);
    }

    /**
     * Retourne l'élu actuelle connu
     *
     * @return l'élu
     */
    public byte getCoordinator() {
        synchronized (this) {
            return coordinator;
        }
    }

    @Override
    public void run() {
        // boucle d'exécution de l'élection
        while (true) {
            Message message = null;
            try {
                message = udpController.listen();

                System.out.println("Site : " + message.getMessageType().toString() + " RECEIVED => " + message
                        .toString());
                if (message.getMessageType() == Message.MessageType.CHECKOUT) {
                    waitingForCheckout = false;
                } else if (message.getMessageType() == Message.MessageType.ANNOUNCE) {
                    annoucement(message);
                } else if (message.getMessageType() == Message.MessageType.RESULT) {
                    resultat(message);
                }
            } catch (SocketTimeoutException e) {
                // Simple timeout de réception, rien à faire.
                // Ce timeout sert à éxécuter la clause "finally" périodiquement, même si rien n'est reçu.
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (waitingForCheckout && System.currentTimeMillis() - timeOfLastSentMessage > CHECKOUT_TIMEOUT) {
                    // retire le site en panne de la liste
                    lastSentMessage.getSites().removeIf((s) -> s.getNoSite() == neighbour);

                    // saute le site en panne
                    neighbour = (byte) ((neighbour + 1) % Main.getSiteCount());

                    // renvoie le message au site suivant
                    send(neighbour, lastSentMessage);

                } else if (waitingForResult && System.currentTimeMillis() - timeOfLastSentMessage > ELECTION_TIMEOUT) {
                    System.out.println("No response, restarting election ...");
                    start();

                    // Le cas d'une élection qui n'a pu aboutir est traité par le cas de base. Le site à la demande d'un
                    // elu refera sa demande, ce qui aura pour effet de relancer une élection.
                }
            }
        }
    }
}
