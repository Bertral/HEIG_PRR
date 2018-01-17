import java.io.IOException;
import java.net.*;
import java.util.HashMap;

/**
 * Project : prr_labo4
 * Date : 14.12.17
 * Authors : Antoine Friant, Michela Zucca
 * <p>
 * Classe contrôlant les échanges de messages UDP
 */
public class UDPController {
    private final byte SITE_COUNT;                      // Nombre total de sites
    private byte SITE_ID;
    private DatagramSocket socket;
    private HashMap<Byte, InetSocketAddress> network;   // Adresses des sites du réseau

    /**
     * Construit UDPController à partir de son numéro de site (l'adresse est définie dans sites.properties)
     *
     * @param siteId  numéro du site
     * @param network adresses des sites
     */
    public UDPController(byte siteId, HashMap<Byte, InetSocketAddress> network, byte numberOfSites) {
        this.SITE_COUNT = numberOfSites;
        this.SITE_ID = siteId;
        this.network = network;

        // ouverture du socket
        try {
            socket = new DatagramSocket(network.get(siteId).getPort());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public byte getSiteCount() {
        return SITE_COUNT;
    }

    public byte getSiteId() {
        return SITE_ID;
    }

    /**
     * Envoie un message de 1 byte
     *
     * @param destination destinataire du message (numéro de site)
     * @param message     message à transmettre
     */
    public void send(byte destination, Message message) {
        // todo : ajouter 1 case au tableau
        byte[] array = new byte[2];

        // Type du message
        array[0] = message.getMessageType().getByte();
        // todo : ajouter l'origine du message
        array[1] = message.getOriginSite();

        try {
            // Envoi
            socket.send(new DatagramPacket(array, array.length, network.get(destination)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Appel bloquant : renvoie le prochain message reçu.
     *
     * @return Message
     * @throws SocketTimeoutException si aucune message n'est reçu au bout de SOCKET_TIMEOUT ms
     */
    public Message listen() throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[1], 1);

        // attend la réception d'un packet
        socket.receive(packet);

        byte[] data = packet.getData();
        Message.MessageType type = null;

        // Récupère le type du message
        if (data[0] == Message.MessageType.REQUETE.getByte()) {
            type = Message.MessageType.REQUETE;
        } else if (data[0] == Message.MessageType.JETON.getByte()) {
            type = Message.MessageType.JETON;
        } else if (data[0] == Message.MessageType.FIN.getByte()) {
            type = Message.MessageType.FIN;
        } else {
            System.out.println("Unknown message type received !");
        }

        //todo : ajouter le numéro du site originaire du message
        Message message = new Message(type, data[1]);

        return message;
    }

}
