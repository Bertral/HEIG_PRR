import java.io.*;
import java.rmi.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;


/**
 * Project : prr_labo2
 * Date : 08.11.17
 * Auteurs : Antoine Friant et Michela Zucca
 *
 * Point d'entrée du client
 *
 * Nous avons choisi de donner la responsabilité pour vérouiller le mutex au client.
 * En situation réelle, lockMutex() et unlockMutex() devraient être appelées par le serveur afin
 * de rendre l'applicaition plus robuste.
 * Cependant, demander au client de gérer ces appels nous permet de mieux illustrer le comportement
 * de l'algorithme de Lamport dans le cadre de ce laboratoire.
 *
 * Marche à suivre (dans l'ordre !) :
 * 0. Entrer les adresses des serveurs dans le fichier sites.properties du module common
 * 1. On lance un backend avec les arguments 0 et 2
 * 2. On lance un backend avec les arguments 1 et 2
 * 3. On lance un frontend avec l'argument 0
 * 4. On lance un frontend avec l'argument 1
 * 5. On tape "set" dans le premier client : le mutex et locké et la console demande d'entrer une valeur
 * 6. On tape "set" dans le deuxième client : blocage en attente de la section critique
 * 7. On donne une valeur au 1er client (6, par exemple), on remarque que le 2ème client entre en section critique
 * 8. On donne une valeur au 2ème client à présent débloqué
 * 9. On tape get dans les deux clients pour vérifier la modification de la valeur globale.
 *
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Argument invalide : [numéro du site (0 .. n-1)]");
        }

        // récupération de la liste des serveurs
        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("sites.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // connexion au serveur RMI
        try {
            System.out.println("Connecting to " + properties.getProperty(args[0]));
            Remote r = Naming.lookup(properties.getProperty(args[0]));
            Data data = (Data) r;

            testAuto(data);
         //   testManuel(data);


        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }
    /**
     * Afficher la liste de commandes et le prompt
     */
    private static void displayCommands() {
        System.out.println("===== Commands ====");
        System.out.println("set : set new value");
        System.out.println("get : display value");
        System.out.print("> ");
    }

    private static void testManuel(Data data ) throws RemoteException {
       Scanner sc = new Scanner(System.in);
            displayCommands();
            while (true) {
                String input = sc.nextLine();

                if (input.contains("set")) {
                    System.out.println("Locking mutex ...");
                    data.lockMutex();
                    System.out.println("Mutex locked");

                    System.out.println("Valeur actuelle : " + data.getValue()+ "\n");

                    System.out.print("Enter new global value (integer only) : ");
                    data.setValue(sc.nextInt());
                    System.out.println("New value set");

                    System.out.println("Releasing mutex ...");
                    data.releaseMutex();
                    System.out.println("Mutex released");
                } else if (input.contains("get")) {
                    System.out.println("Current global value : " + data.getValue());
                } else {
                    continue;
                }
                displayCommands();
            }
    }

    private static void testAuto(Data data ) throws RemoteException, InterruptedException {
        for(int i = 0; i < 1000; i++) {
            System.out.println("Lire la valeur : " + data.getValue());

            System.out.println("\nLocking mutex ...");
            data.lockMutex();
            System.out.println("Mutex locked");
            System.out.println("Valeur avant modification : " + data.getValue());
            data.setValue(data.getValue()+ 1);
            System.out.println("Valeur après modification : " + data.getValue());
            System.out.println("Releasing mutex ...");
            data.releaseMutex();
            System.out.println("Mutex released\n");

            sleep(10);
        }
    }
}
