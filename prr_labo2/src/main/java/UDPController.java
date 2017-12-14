import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Properties;

/**
 * Project : prr_labo2
 * Date : 14.12.17
 */
public class UDPController {
    public static final byte MESSAGE_ANNOUNCE = 0;
    public static final byte MESSAGE_RESULT = 1;

    private DatagramSocket socket;
    private HashMap<Byte, InetSocketAddress> network;

    public UDPController() {
        // TODO : remplir network à partir d'un fichier (comme labo précédent)
        // récupération de la liste des serveurs
        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("sites.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (String name : properties.stringPropertyNames()) {

        }

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
    }

    public int getAptitude() {
        return socket.getPort() + socket.getInetAddress().getAddress()[3];
    }

    public void sendResult(byte destination, byte electedSite) {
        byte[] array = {MESSAGE_RESULT, electedSite};

        try {
            socket.send(new DatagramPacket(array, array.length, network.get(destination)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAnnounce(byte destination, byte bestSite, int aptitude) {
        byte[] array = {MESSAGE_ANNOUNCE, bestSite, (byte) ((aptitude >> 8) & 0xFF), (byte) (aptitude & 0xFF)};

        try {
            socket.send(new DatagramPacket(array, array.length, network.get(destination)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
