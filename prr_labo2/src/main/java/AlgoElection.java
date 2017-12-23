import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * Algorithme d'élection en anneau avec panne des sites possibles.
 * Une seule élection peut avoir lieu
 * - En cas de reprise de panne un site ne peut pas interrompre une élection
 * - En cas de panne de l'élu une nouvelle élection est démarrer
 */
public class AlgoElection implements Runnable {

    enum Phase {ANNOUNCE, RESULT}

    private static final long CHECKOUT_TIMEOUT = 200;
    private static final long CYCLE_TIMEOUT = 2000;
    private Site me; // Site courant
    private byte neighbour; // Site voisin
    private byte coordinator; // Site élu
    private UDPController udpController;
    private Phase phase;
    private Message lastSentMessage;
    private long timeOfLastSentMessage;
    private boolean waitingCheckout = false;
    private boolean waitingCycle = false;

    /**
     * Constructeur
     *
     * @param num numéro du site local
     */
    public AlgoElection(byte num, UDPController udpController) {
        this.me = new Site(num, udpController.getAptitude());
        this.udpController = udpController;
        System.out.println("***** Site : "+ me.getNoSite() + " aptitude : " + me.getAptitude()+ " started *****");
        start();
    }


    /**
     * Lancement d'une élection
     */
    public void start() {
        // réinitialise le voisin
        neighbour = (byte) ((me.getNoSite() + 1) % Main.getSiteCount());

        // envoie le premier message
        SortedSet<Site> election = new TreeSet<>();
        election.add(me);
        Message message = new Message(Message.MessageType.ANNOUNCE, election);
        send(neighbour, message);

        phase = Phase.ANNOUNCE;
    }

    /**
     * Annonce reçu d'un autre site, traitement de l'information.
     *
     * @param election site actuellement dans l'election
     */
    public void annoucement(SortedSet<Site> election) {
        if (election.contains(me)) {
            // trouve l'élu
            Site elu = me;
            for (Site s : election) {
                if (s.getAptitude() > elu.getAptitude() || (s.getAptitude() == elu.getAptitude()
                        && s.getNoSite() > elu.getNoSite())) {
                    elu = s;
                }
            }
            SortedSet<Site> resultat = new TreeSet<>();
            resultat.add(elu);
            send(neighbour, new Message(Message.MessageType.RESULT, resultat));
            phase = Phase.RESULT;

        } else {
            election.add(me);
            send(neighbour, new Message(Message.MessageType.ANNOUNCE, election));
            phase = Phase.ANNOUNCE;
        }
    }

    /**
     * Réception du résultat de l'élection
     *
     * @param election
     */
    public void resultat(SortedSet<Site> election, byte elu) {
        if (!election.contains(me)) {
            // 2 elections en cours.. redémarre l'élection
            if (phase == Phase.RESULT && coordinator != elu) {
                election = new TreeSet<>();
                election.add(me);
                send(neighbour, new Message(Message.MessageType.ANNOUNCE, election));
                phase = Phase.ANNOUNCE;
            } else if (phase == Phase.ANNOUNCE) {
                coordinator = elu;
                election.add(me);
                send(neighbour, new Message(Message.MessageType.RESULT, election));
                phase = Phase.RESULT;
            }
        } else {
            coordinator = elu;
            phase = Phase.ANNOUNCE;
        }
    }

    private void send(byte dest, Message message) {
        System.out.println("Site : " + me.getNoSite() + " SEND => " + message.toString());
        lastSentMessage = message;
        waitingCheckout = true;
        timeOfLastSentMessage = System.currentTimeMillis();
        udpController.send(dest, message);
    }

    /**
     * Retourne l'élu actuelle connu
     *
     * @return l'élu
     */
    public byte getCoordinator() {
        return coordinator;
    }

    @Override
    public void run() {
        // boucle d'exécution de l'élection
        while (true) {
            Message message = null;
            try {
                message = udpController.listen();
                System.out.println("Site : " + message.getMessageType().toString() + " RECEIVED => " + message.toString());
                if (message.getMessageType() == Message.MessageType.CHECKOUT) {
                    waitingCheckout = false;
                } else if (message.getMessageType() == Message.MessageType.ANNOUNCE) {
                    annoucement(message.getSites());
                } else if (message.getMessageType() == Message.MessageType.RESULT) {
                    resultat(message.getSites(), message.getElu());
                    timeOfLastSentMessage = System.currentTimeMillis();
                }
            } catch (SocketTimeoutException e) {
                // Simple timeout de réception, rien à faire.
                // Ce timeout sert à éxécuter la clause "finally" périodiquement, même si rien n'est reçu.
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (waitingCheckout && timeOfLastSentMessage - System.currentTimeMillis() > CHECKOUT_TIMEOUT) {
                    // retire le site en panne de la liste
                    lastSentMessage.getSites().removeIf((s) -> s.getNoSite() == neighbour);

                    // saute le site en panne
                    neighbour = (byte) ((neighbour + 1) % Main.getSiteCount());

                    // renvoie le message au site suivant
                    send(neighbour, lastSentMessage);

                } else if (waitingCycle && timeOfLastSentMessage - System.currentTimeMillis() > CYCLE_TIMEOUT) {
                    // TODO : traiter le timeout du cycle (on aurait dû recevoir le message ayant fait le tour)
                    // TODO : donner la bonne valeur à waitingCycle dans l'algorithme
                    waitingCycle = false;
                    lastSentMessage = null;
                    // Le cas d'une élection qui n'a pu aboutir est traité par le cas de base. Le site à la demande d'un
                    // elu refera sa demande, ce qui aura pour effet de relancer une élection.
                }
            }
        }
    }
}
