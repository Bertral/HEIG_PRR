import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/*
 * Algorithme d'élection en anneau avec panne des sites possibles.
 * Une seule élection peut avoir lieu
 * - En cas de reprise de panne un site ne peut pas interrompre une élection
 * - En cas de panne de l'élu une nouvelle élection est démarrer
 */
public class AlgoElection implements Runnable {

    enum Phase {ANNONCE, RESULTAT}

    private static final long CHECKOUT_TIMEOUT = 200;
    private static final long CYCLE_TIMEOUT = 2000;
    private Site me; // Site courant
    private byte neighbour; // Site voisin
    private byte coordinator; // Site élu
    private UDPController udpController;
    private Phase phase;
    private Message lastMessage;
    private long timeOfLastSentMessage;
    private boolean waitingCheckout = false;
    private boolean waitingCycle = false;

    /**
     * Constructeur
     *
     * @param num       numéro du site local
     * @param neighbour numéro du site voisin
     */
    public AlgoElection(byte num, byte neighbour, UDPController udpController) {
        this.neighbour = neighbour;
        phase = Phase.ANNONCE;
        this.me = new Site(num, udpController.getAptitude());
        this.udpController = udpController;
        start();
    }


    /**
     * Lancement de l'élection au démarrage
     */
    public void start() {
        ArrayList<Site> election = new ArrayList<>();
        election.add(me);
        // Sauvegarde du dernier message envoyé
        lastMessage = new Message(Message.MessageType.ANNOUNCE, election);
        send(neighbour, lastMessage);

        phase = Phase.ANNONCE;
    }

    /**
     * Annonce reçu d'un autre site, traitement de l'information.
     *
     * @param election site actuellement dans l'election
     */
    public void annoucement(ArrayList<Site> election) {
        if (election.contains(me)) {
            // définir l'elu
            if (!election.isEmpty()) {
                Site elu = election.get(0);
                for (Site s : election) {
                    if (s.getAptitude() > elu.getAptitude() ||
                            ((s.getAptitude() == elu.getAptitude())
                                    && (s.getNoSite() > elu.getNoSite()))) {
                        elu = s;
                    }
                }
                ArrayList<Site> resultat = new ArrayList<>();
                resultat.add(elu);
                // Dernier message envoyé
                lastMessage = new Message(Message.MessageType.RESULT, election);
                send(neighbour, lastMessage);
                phase = Phase.RESULTAT;
            } else {
                election.add(me);
                // Dernier message envoyé
                lastMessage = new Message(Message.MessageType.ANNOUNCE, election);
                send(neighbour, lastMessage);
                phase = Phase.ANNONCE;
            }
        }
    }

    /**
     * Réception du résultat de l'élection
     *
     * @param election
     */
    public void resultat(ArrayList<Site> election, byte elu) {
        if (!election.contains(me)) {
            // 2 elections en cours.. redémarre l'élection
            if (phase == Phase.RESULTAT && coordinator != elu) {
                election = new ArrayList<>();
                election.add(me);
                // Dernier message envoyé
                lastMessage = new Message(Message.MessageType.ANNOUNCE, election);
                send(neighbour, lastMessage);
                phase = Phase.ANNONCE;
            } else if (phase == Phase.ANNONCE) {
                coordinator = elu;
                election.add(me);
                // Dernier message envoyé
                lastMessage = new Message(Message.MessageType.RESULT, election);
                send(neighbour, lastMessage);
                phase = Phase.RESULTAT;
            }
        }
    }

    private void send(byte dest, Message message) {
        udpController.send(dest, message);
        waitingCheckout = true;
        timeOfLastSentMessage = System.currentTimeMillis();
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

                if (message.getMessageType() == Message.MessageType.CHECKOUT) {
                    lastMessage = null;
                    waitingCheckout = false;
                } else if (message.getMessageType() == Message.MessageType.ANNOUNCE) {
                    annoucement(message.getSites());
                } else if (message.getMessageType() == Message.MessageType.RESULT) {
                    resultat(message.getSites(), message.getElu());
                }
            } catch (SocketTimeoutException e) {
                // Simple timeout de réception, rien à faire.
                // Ce timeout sert à éxécuter la clause "finally" périodiquement, même si rien n'est reçu.
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (waitingCheckout && timeOfLastSentMessage - System.currentTimeMillis() > CHECKOUT_TIMEOUT) {
                    // TODO : traiter le manque de quittance ici
                } else if (waitingCycle && timeOfLastSentMessage - System.currentTimeMillis() > CYCLE_TIMEOUT) {
                    // TODO : traiter le timeout du cycle (on aurait dû recevoir le message ayant fait le tour)
                    // TODO : donner la bonne valeur à waitingCycle dans l'algorithme
                }
            }
        }
    }
}
