import java.net.InetSocketAddress;
import java.util.HashMap;

/*
 * Algorithme d'élection en anneau avec panne des sites possibles.
 * Une seule élection peut avoir lieu
 * - En cas de reprise de panne un site ne peut pas interrompre une élection
 * - En cas de panne de l'élu une nouvelle élection est démarrer
 */
public class AlgoElection implements Runnable {
    private byte me; // Site courant
    private byte neighbour; // Site voisin
    private byte coordinator; // Site élu
    private boolean annoucementInProgess; // élection en cours
    private UDPController udpController;
    private HashMap<Byte, Integer> election; // paires num_site <---> aptitude

    /**
     * Constructeur
     *
     * @param num       numéro du site local
     * @param neighbour numéro du site voisin
     */
    public AlgoElection(byte num, byte neighbour, UDPController udpController) {
        this.annoucementInProgess = false;
        this.neighbour = neighbour;
        election = new HashMap<>();
        me = num;
        this.udpController = udpController;
    }


    /**
     * Lancement de l'élection au démarrage
     */
    public void start() {
        udpController.sendAnnounce(neighbour, me, udpController.getAptitude());
        annoucementInProgess = true;
    }

    /**
     * Annonce reçu d'un autre site, traitement de l'information.
     *
     * @param otherSite
     * @param otherAptitude
     */
    public void annoucement(byte otherSite, int otherAptitude) {
        if (udpController.getAptitude() > otherAptitude
                || (udpController.getAptitude() == otherAptitude && me > otherSite)) {
            if (!annoucementInProgess) {
                udpController.sendAnnounce(neighbour, me, udpController.getAptitude());
                annoucementInProgess = true;
            }
        } else if (me == otherSite) {
            udpController.sendResult(neighbour, me);
        } else {
            udpController.sendAnnounce(neighbour, otherSite, otherAptitude);
            annoucementInProgess = true;
        }
    }

    /**
     * Réception du résultat de l'élection
     *
     * @param elu Site élu
     */
    public void resultat(byte elu) {
        this.coordinator = elu;
        annoucementInProgess = false;
        if (this.coordinator != me) {
            udpController.sendResult(neighbour, coordinator);
        }
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
        while(true) {
            Message message = udpController.listen();

            if(message.getMessageType() == Message.MessageType.ANNOUNCE) {
                annoucement(message.getSite(), message.getAptitude());
            } else if (message.getMessageType() == Message.MessageType.RESULT) {
                resultat(message.getSite());
            }
        }
    }
}
