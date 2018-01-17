import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

/**
 * Project : prr_labo4
 * Date : 14.12.17
 * Authors : Antoine Friant, Michela Zucca
 * <p>
 * Application interrogeant le site élu. Lance une élection si le site élu est en panne.
 * Les sites doivent être adressés dans le fichier "sites.properties", numérotés de 0 à 127
 */
public class Main {
    private static final int PING_TIMEOUT = 1000; // lance une élection si l'élu ne répond pas après PING_TIMEOUT ms

    public static void main(String[] args) {

        // récupération de la liste des serveurs dans sites.properties
        Properties properties = new Properties();
        HashMap<Byte, InetSocketAddress> network = new HashMap<>();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("sites.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        byte siteCount = (byte) properties.stringPropertyNames().size(); // nombre de sites dans le système réparti, maxiumum 127
        for (String name : properties.stringPropertyNames()) {
            String[] address = properties.getProperty(name).split(":");
            network.put(Byte.parseByte(name), new InetSocketAddress(address[0], Integer.parseInt(address[1])));
        }

        // lecture des arguments du programme
        byte num;
        try {
            num = Byte.parseByte(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid argument, must be the site number in sites.properties (numbered " +
                    "from 0 to N-1)");
            return;
        }

        // initialise l'élection et le controlleur UDP
        UDPController udpController = new UDPController(num, network, siteCount);
        Terminaison terminaison = new Terminaison(udpController, num,siteCount);
        //AlgoElection election = new AlgoElection(num, udpController, siteCount);


        // lance l'écoute des élections
        // new Thread(election).start();
    }
}