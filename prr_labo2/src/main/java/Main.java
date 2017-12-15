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
    private static final int PING_TIMEOUT = 2000;

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
        for (String name : properties.stringPropertyNames()) {
            String[] address = properties.getProperty(name).split(":");
            network.put(Byte.parseByte(name), new InetSocketAddress(address[0], Integer.parseInt(address[1])));
        }

        // lecture des arguments
        byte num;
        byte neighbour;
        try {
            num = Byte.parseByte(args[0]);
            neighbour = Byte.parseByte(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid arguments, must be : [local_site_id] [neighbour_id]");
            return;
        }

        // initialise l'élection et le controlleur UDP
        UDPController udpController = udpController = new UDPController(num, network);
        AlgoElection election = election = new AlgoElection(num, neighbour, udpController);


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
                        do {
                            System.out.println("Waiting for ping response ...");
                            pingSocket.receive(pong);
                        } while (pong.getData()[0] != Message.MessageType.PONG.getByte());
                        System.out.println("Coordinator "+ coordinator+" is alive");
                    } catch (SocketTimeoutException e) {
                        // Si le pong n'est pas reçu en réponse, lance l'élection
                        System.out.println("Ping timed out, coordinator "+coordinator+" is dead, starting election");
                        election.start();
                    }
                }

                Thread.sleep(5000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}