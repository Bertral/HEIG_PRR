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

        byte siteCount = Byte.parseByte(args[1]);
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
        final Terminaison terminaison = new Terminaison(udpController, num, siteCount);

        final Thread terminaisonThread = new Thread(terminaison);
        terminaisonThread.start();

        System.out.println("Press <n> to new task\nPress <s> to stop");

        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("");
        while (terminaison.isRunning.get()) {
            char response = scanner.next().charAt(0);
            if (response == 's') {
                System.out.println("Stopping application ...");
                terminaison.requestStop();
                break;
            } else if (response == 'n') {
                if (terminaison.isRunning.get()) {
                    System.out.println("New task create ...");
                    terminaison.newTask();
                } else {
                    System.out.println("Site stopping, it's not possible");
                }
            }
        }

        try {
            terminaisonThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println("Application stopped");

    }
}