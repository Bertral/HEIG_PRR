import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Project : prr_labo2
 * Date : 14.12.17
 */
public class UDPController {
    private static final int SOCKET_TIMEOUT = 1000;
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
            return socket.getPort() + InetAddress.getLocalHost().getAddress()[3];
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Envoie un message de type RESULT
     *
     * @param destination destinataire du message (numéro de site)
     * @param electedSite site elu à transmettre
     */
    public void sendResult(byte destination, byte electedSite) {
        byte[] array = {Message.MessageType.RESULT.getByte(), electedSite};

        try {
            socket.send(new DatagramPacket(array, array.length, network.get(destination)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Envoie un message de type ANNOUNCE
     *
     * @param destination destinataire du message (numéro de site)
     * @param bestSite    meilleur candidat à transmettre
     * @param aptitude    aptitude du candidat
     */
    public void sendAnnounce(byte destination, byte bestSite, int aptitude) {
        byte[] array = {Message.MessageType.ANNOUNCE.getByte(), bestSite, (byte) (aptitude >> 24), (byte) (aptitude
                >> 16), (byte) (aptitude >> 8), (byte) aptitude};

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
    public Message listen() throws SocketTimeoutException {
        DatagramPacket packet = new DatagramPacket(new byte[6], 6);

        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] data = packet.getData();

        // num of bytes = 1 type + 1 num + 4 apt + 1 num + 4 apt + 1 num + 4 apt + 1 num + 4 apt

        Message.MessageType type = null;
        if (data[0] == Message.MessageType.ANNOUNCE.getByte()) {
            type = Message.MessageType.ANNOUNCE;
        } else if (data[0] == Message.MessageType.RESULT.getByte()) {
            type = Message.MessageType.RESULT;
        } else if(data[0] == Message.MessageType.PONG.getByte()) {
            // UDPController ne devrait pas recevoir de PONG, car il n'emmet pas de PING
            type = Message.MessageType.PONG;
        } else if(data[0] == Message.MessageType.PING.getByte()) {
            type = Message.MessageType.PING;

            // Répond immédiatement au ping
            byte[] array = {Message.MessageType.PONG.getByte()};
            try {
                socket.send(new DatagramPacket(array, array.length, packet.getSocketAddress()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Unknown message type received !");
        }

        return new Message(type, data[1],
                data[2] << 24 | (data[3] & 0xFF) << 16 | (data[4] & 0xFF) << 8 | (data[5] & 0xFF));
    }
}