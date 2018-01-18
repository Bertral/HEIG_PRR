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
        // lecture des arguments du programme
        byte num;
        byte siteCount;
        try {
            num = Byte.parseByte(args[0]);
            siteCount = Byte.parseByte(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid arguments ! Arguments must be [site_id] [total_number_of_sites]." +
                    "site_id is the site's number in sites.properties (starts from 0).");
            return;
        }

        // récupération de la liste des serveurs dans sites.properties
        Properties properties = new Properties();
        HashMap<Byte, InetSocketAddress> network = new HashMap<>();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("sites.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // remplissage du réseau
        for (String name : properties.stringPropertyNames()) {
            String[] address = properties.getProperty(name).split(":");
            network.put(Byte.parseByte(name), new InetSocketAddress(address[0], Integer.parseInt(address[1])));
        }

        // initialise le controlleur UDP
        UDPController udpController = new UDPController(num, network, siteCount);

        // lance l'algorithme de terminaison
        Terminaison terminaison = new Terminaison(udpController);
        Thread terminaisonThread = new Thread(terminaison);
        terminaisonThread.start();

        // lecture et traitement des entrées utilisateur, tant que l'algo de terminaison est actif
        System.out.println("Enter <n> to new task\nEnter <s> to stop");
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("");
        while (terminaison.isRunning()) {
            char response = scanner.next().charAt(0);
            if (response == 's') {
                // fait une demande d'arrêt
                System.out.println("Stopping application ...");
                terminaison.requestStop();
                break;
            } else if (response == 'n') {
                // demande le lancement d'une nouvelle tâche
                if (terminaison.isRunning()) {
                    System.out.println("New task create ...");
                    terminaison.newTask();
                } else {
                    System.out.println("Site stopping, it's not possible");
                }
            }
        }

        // attend la fin du thread de l'algo de terminaison
        try {
            terminaisonThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println("Application stopped");

    }
}