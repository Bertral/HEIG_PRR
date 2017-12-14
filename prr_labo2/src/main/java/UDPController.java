import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * Project : prr_labo2
 * Date : 14.12.17
 */
public class UDPController {
    public enum MessageType {
        MESSAGE_ANNOUNCE((byte) 0),
        MESSAGE_RESULT((byte) 1);

        private byte value;

        MessageType(byte b) {
            value = b;
        }

        public byte getByte() {
            return value;
        }
    }

    public class Message {
        private MessageType messageType;
        private byte site;
        private int aptitude;

        public Message(MessageType messageType, byte site, int aptitude) {
            this.messageType = messageType;
            this.site = site;
            this.aptitude = aptitude;
        }
    }

    private DatagramSocket socket;
    private HashMap<Byte, InetSocketAddress> network;

    public UDPController(byte siteId) {
        // TODO : remplir network à partir d'un fichier (comme labo précédent)
        // récupération de la liste des serveurs
        Properties properties = new Properties();
        network = new HashMap<>();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("sites.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (String name : properties.stringPropertyNames()) {
            String[] address = properties.getProperty(name).split(":");
            network.put(Byte.parseByte(name), new InetSocketAddress(address[0], Integer.parseInt(address[1])));
        }

        try {
            socket = new DatagramSocket(network.get(siteId).getPort());
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
    }

    public int getAptitude() {
        return socket.getPort() + socket.getInetAddress().getAddress()[3];
    }

    public void sendResult(byte destination, byte electedSite) {
        byte[] array = {MessageType.MESSAGE_RESULT.getByte(), electedSite};

        try {
            socket.send(new DatagramPacket(array, array.length, network.get(destination)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAnnounce(byte destination, byte bestSite, int aptitude) {
        byte[] array = {MessageType.MESSAGE_ANNOUNCE.getByte(), bestSite, (byte) (aptitude >> 24), (byte) (aptitude
                >> 16), (byte) (aptitude >> 8), (byte) aptitude};

        try {
            socket.send(new DatagramPacket(array, array.length, network.get(destination)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message listen() {
        DatagramPacket packet = new DatagramPacket(new byte[6], 6);

        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] data = packet.getData();

        MessageType type = null;
        if (data[0] == MessageType.MESSAGE_ANNOUNCE.getByte()) {
            type = MessageType.MESSAGE_ANNOUNCE;
        } else if (data[1] == MessageType.MESSAGE_RESULT.getByte()) {
            type = MessageType.MESSAGE_RESULT;
        }

        return new Message(type, data[1],
                data[2] << 24 | (data[3] & 0xFF) << 16 | (data[4] & 0xFF) << 8 | (data[5] & 0xFF));
    }
}
