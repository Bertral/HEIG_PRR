import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * Project : prr_labo2
 * Date : 14.12.17
 * Authors : Antoine Friant, Michela Zucca
 * <p>
 * Application interrogeant le site élu. Lance une élection si le site élu est en panne.
 * Les sites doivent être indexés et adressés dans le fichier "sites.properties"
 */
public class Main {
    private static final int PING_TIMEOUT = 1000;
    private static byte siteCount;

    public static void main(String[] args) {

        // récupération de la liste des serveurs
        Properties properties = new Properties();
        HashMap<Byte, InetSocketAddress> network = new HashMap<>();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("sites.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        siteCount = (byte) properties.stringPropertyNames().size();
        for (String name : properties.stringPropertyNames()) {
            String[] address = properties.getProperty(name).split(":");
            network.put(Byte.parseByte(name), new InetSocketAddress(address[0], Integer.parseInt(address[1])));
        }

        // lecture des arguments
        byte num;
        try {
            num = Byte.parseByte(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid argument, must be the site number in sites.properties (starting " +
                    "from 0 to N-1)");
            return;
        }

        // initialise l'élection et le controlleur UDP
        UDPController udpController = new UDPController(num, network);
        AlgoElection election = new AlgoElection(num, udpController);


        // lance l'écoute du réseau
        new Thread(election).start();

        try {
            DatagramSocket pingSocket = new DatagramSocket();
            pingSocket.setSoTimeout(PING_TIMEOUT);
            byte[] ping = {Message.MessageType.PING.getByte()};

            // lance périodiquement des ping
            while (true) {
                byte coordinator = election.getCoordinator();
                System.out.println("Coordinator is site " + coordinator);

                if (coordinator != num) {
                    // send ping
                    pingSocket.send(new DatagramPacket(ping, ping.length, network.get(coordinator)));
                    System.out.println("Sent ping");

                    // wait for pong
                    DatagramPacket pong = new DatagramPacket(new byte[1], 1);

                    try {
                        System.out.println("Waiting for ping response ...");
                        do {
                            pingSocket.receive(pong);
                        } while (pong.getData()[0] != Message.MessageType.PONG.getByte());
                        System.out.println("Coordinator " + coordinator + " is alive");
                    } catch (SocketTimeoutException e) {
                        // Si le pong n'est pas reçu en réponse, lance l'élection
                        System.out.println("Ping timed out, coordinator " + coordinator + " is dead, starting " +
                                "election");
                        election.start();
                    }
                }

                Thread.sleep(500);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static byte getSiteCount() {
        return siteCount;
    }
}