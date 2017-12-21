import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

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
     * Envoie un message
     *
     * @param destination destinataire du message (numéro de site)
     * @param message     message à transmettre
     */
    public void send(byte destination, Message message) {
        byte[] array = new byte[1 + message.getSites().size() * 5];
        array[0] = message.getMessageType().getByte();

        for (int i = 0; i < message.getSites().size(); i++) {
            array[1 + i * 5] = message.getSites().get(i).getNoSite(); // Numéro de site
            array[1 + i * 5 + 1] = (byte) (message.getSites().get(i).getAptitude() >> 24); // byte 1 aptitude
            array[1 + i * 5 + 2] = (byte) (message.getSites().get(i).getAptitude() >> 16); // byte 2 aptitude
            array[1 + i * 5 + 3] = (byte) (message.getSites().get(i).getAptitude() >> 8); // byte 3 aptitude
            array[1 + i * 5 + 4] = (byte) (message.getSites().get(i).getAptitude()); // byte 4 aptitude
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
    public Message listen() throws SocketTimeoutException {
        DatagramPacket packet = new DatagramPacket(new byte[1 + Main.NUMBER_OF_SITES * 5], 1 + Main.NUMBER_OF_SITES *
                5);

        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        } else {
            System.out.println("Unknown message type received !");
        }

        Message message = new Message(type, new ArrayList<>());
        int numberOfSites = (packet.getLength() - 1) / 5;

        for(int i = 0; i < numberOfSites; i++) {
            message.getSites().add(new Site(data[1 + 5*i], data[2] << 24 | (data[3] & 0xFF) << 16 | (data[4] & 0xFF) << 8 | (data[5] & 0xFF)));
        }

        return new Message(type, data[1],
                data[2] << 24 | (data[3] & 0xFF) << 16 | (data[4] & 0xFF) << 8 | (data[5] & 0xFF));
    }
}
