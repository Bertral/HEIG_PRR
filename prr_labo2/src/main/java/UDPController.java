import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Project : prr_labo2
 * Date : 14.12.17
 * Authors : Antoine Friant, Michela Zucca
 * <p>
 * Classe contrôlant les échanges de messages UDP
 */
public class UDPController {
    private static final int SOCKET_TIMEOUT = 200;      // Timeout de réception d'un message en ms
    private DatagramSocket socket;
    private HashMap<Byte, InetSocketAddress> network;   // Adresses des sites du réseau

    /**
     * Construit UDPController à partir de son numéro de site (l'adresse est définie dans sites.properties)
     *
     * @param siteId  numéro du site
     * @param network adresses des sites
     */
    public UDPController(byte siteId, HashMap<Byte, InetSocketAddress> network) {
        this.network = network;

        // ouverture du socket
        try {
            socket = new DatagramSocket(network.get(siteId).getPort());
            socket.setSoTimeout(SOCKET_TIMEOUT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calcule l'aptitude du site local à partir de son numéro de port et du dernier byte de son adresse ip
     *
     * @return int aptitude
     */
    public int getAptitude() {
        try {
            return socket.getLocalPort() + InetAddress.getLocalHost().getAddress()[3];
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Envoie un message
     *
     * @param destination destinataire du message (numéro de site)
     * @param message     message à transmettre
     */
    public void send(byte destination, Message message) {
        byte[] array = new byte[1 + message.getSites().size() * 5 + 1];

        // Type du message
        array[0] = message.getMessageType().getByte();

        int i = 0;
        for (Site s : message.getSites()) {
            array[1 + i * 5] = s.getNoSite(); // Numéro de site
            array[1 + i * 5 + 1] = (byte) (s.getAptitude() >> 24); // byte 1 aptitude
            array[1 + i * 5 + 2] = (byte) (s.getAptitude() >> 16); // byte 2 aptitude
            array[1 + i * 5 + 3] = (byte) (s.getAptitude() >> 8); // byte 3 aptitude
            array[1 + i * 5 + 4] = (byte) (s.getAptitude()); // byte 4 aptitude
            i++;
        }

        if (message.getMessageType() == Message.MessageType.RESULT) {
            // Résultat de l'élection
            array[1 + i * 5] = message.getResultByte();
        }

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
        DatagramPacket packet = new DatagramPacket(new byte[1 + Main.getSiteCount() * 5 + 1], 1 + Main.getSiteCount() *
                5 + 1);

        // attend la réception d'un packet
        socket.receive(packet);

        byte[] data = packet.getData();
        Message.MessageType type = null;

        // Récupère le type du message
        if (data[0] == Message.MessageType.ANNOUNCE.getByte()) {
            type = Message.MessageType.ANNOUNCE;
        } else if (data[0] == Message.MessageType.RESULT.getByte()) {
            type = Message.MessageType.RESULT;
        } else if (data[0] == Message.MessageType.PONG.getByte()) {
            type = Message.MessageType.PONG;
        } else if (data[0] == Message.MessageType.PING.getByte()) {
            type = Message.MessageType.PING;

            // Répond immédiatement au PINGs
            byte[] array = {Message.MessageType.PONG.getByte()};
            try {
                socket.send(new DatagramPacket(array, array.length, packet.getSocketAddress()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (data[0] == Message.MessageType.CHECKOUT.getByte()) {
            type = Message.MessageType.CHECKOUT;
        } else {
            System.out.println("Unknown message type received !");
        }

        Message message = new Message(type, new TreeSet<>());

        // En cas d'annonce ou resultat
        if (message.getMessageType() == Message.MessageType.ANNOUNCE
                || message.getMessageType() == Message.MessageType.RESULT) {

            // Envoie une immédiatement une quittance
            byte[] array = {Message.MessageType.CHECKOUT.getByte()};
            try {
                socket.send(new DatagramPacket(array, array.length, packet.getSocketAddress()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Récupère le résultat de l'élection, le cas échéant
            if (Message.MessageType.RESULT == message.getMessageType()) {
                message.setResultByte(data[packet.getLength() - 1]);
            }

            // Récupère la liste des sites reçus
            for (int i = 0; i < (packet.getLength() - 2) / 5; i++) {
                message.getSites().add(new Site(data[1 + 5 * i],
                        data[1 + 5 * i + 1] << 24
                                | (data[1 + 5 * i + 2] & 0xFF) << 16
                                | (data[1 + 5 * i + 3] & 0xFF) << 8
                                | (data[1 + 5 * i + 4] & 0xFF)));
            }
        }

        return message;
    }
}
