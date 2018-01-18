import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

/**
 * Project : prr_labo4
 * Date : 14.12.17
 * Authors : Antoine Friant, Michela Zucca
 * <p>
 * Les sites doivent être adressés dans le fichier "sites.properties", numérotés de 0 à 127
 */
public class Main {

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
        final Terminaison terminaison = new Terminaison(udpController, num,siteCount);

        final Thread terminaisonThread = new Thread(terminaison);
        terminaisonThread.start();
        terminaison.newTask();

        System.out.println("Enter s to stop");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                if(scanner.nextLine().equals("s")) {
                    System.out.println("Stopping application ...");
                    try {
                        terminaison.requestStop();
                        terminaisonThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        try {
            terminaisonThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Application stopped");

    }
}