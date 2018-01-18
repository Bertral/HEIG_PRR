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
    private byte SITE_ID;                               // Numéro du site local
    private DatagramSocket socket;                      // Socket UDP local
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
     * Envoie un message de 2 bytes
     *
     * @param destination destinataire du message (numéro de site)
     * @param message     message à transmettre
     */
    public void send(byte destination, Message message) {
        byte[] array = new byte[2];

        // Type du message
        array[0] = message.getMessageType().getByte();
        // Origine du message
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
     */
    public Message listen() throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[2], 2);

        // attend la réception d'un packet
        socket.receive(packet);

        byte[] data = packet.getData();
        Message.MessageType type = null;

        // Récupère le type du message
        if (data[0] == Message.MessageType.REQUEST.getByte()) {
            type = Message.MessageType.REQUEST;
        } else if (data[0] == Message.MessageType.TOKEN.getByte()) {
            type = Message.MessageType.TOKEN;
        } else if (data[0] == Message.MessageType.END.getByte()) {
            type = Message.MessageType.END;
        } else {
            System.out.println("Unknown message type received !");
        }

        return new Message(type, data[1]);
    }

}
