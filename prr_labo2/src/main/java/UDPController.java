import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Project : prr_labo2
 * Date : 14.12.17
 */
public class UDPController {
    private static final int SOCKET_TIMEOUT = 200;
    private DatagramSocket socket;
    private HashMap<Byte, InetSocketAddress> network; // carnet d'adresses

    /**
     * Construit UDPController à partir de son numéro de site (l'adresse est définie dans sites.properties)
     *
     * @param siteId
     * @param network
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
     * Calcule l'aptitude du site local
     *
     * @return int
     */
    public int getAptitude() {
        try {
            int sock = socket.getPort();
            int by = InetAddress.getLocalHost().getAddress()[3];
            return socket.getPort() + InetAddress.getLocalHost().getAddress()[3];
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
        byte[] array = new byte[1 + message.getSites().size() * 5];
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

        try {
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
        DatagramPacket packet = new DatagramPacket(new byte[1 + Main.getSiteCount() * 5], 1 + Main.getSiteCount() *
                5);

        socket.receive(packet);

        byte[] data = packet.getData();

        Message.MessageType type = null;
        if (data[0] == Message.MessageType.ANNOUNCE.getByte()) {
            type = Message.MessageType.ANNOUNCE;
        } else if (data[0] == Message.MessageType.RESULT.getByte()) {
            type = Message.MessageType.RESULT;
        } else if (data[0] == Message.MessageType.PONG.getByte()) {
            // UDPController ne devrait pas recevoir de PONG, car il n'emmet pas de PING
            type = Message.MessageType.PONG;
        } else if (data[0] == Message.MessageType.PING.getByte()) {
            type = Message.MessageType.PING;

            // Répond immédiatement au ping
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

        if (message.getMessageType() == Message.MessageType.ANNOUNCE
                || message.getMessageType() == Message.MessageType.RESULT) {
            // Envoie une quittance immédiatement
            byte[] array = {Message.MessageType.CHECKOUT.getByte()};
            try {
                socket.send(new DatagramPacket(array, array.length, packet.getSocketAddress()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < (packet.getLength() - 1) / 5; i++) {
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
