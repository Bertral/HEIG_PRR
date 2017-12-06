/**
 * Project : prr_labo2
 * Date : 08.11.17
 * Auteurs : Antoine Friant et Michela Zucca
 *
 * Nous posons les hypothèses/règles suivantes :
 *  - Les gestionnaires de site doivent être lancés avant les clients
 *  - Démarrer un serveur RMI :
 *      - les sites sont connues, compris entre 0 et N-1
 *      - Les accès aux différents sites sont contenus dans le fichier sites.properties
 *      - Au lancement d'un serveur les arguments suivants sont indispensables
 *          - Numéro du site
 *          - Nombres de sites
 *  - Démarrer un client :
 *      - Les clients sont identifiés par un numéro compris entre 0 et N-1, correspondant à son gestionnaire de site
 *      - Dans un but démonstratif, un client à la responsabilité d'acquérir le mutex avant toute modification de la
 *        valeur globale.
 *      - Au lancement d'un client l'argument suivant est indispensable
 *          - Numéro du site
 *
 * Choix d'implémentation :
 * Nous avons choisi de donner la responsabilité pour vérouiller le mutex au client.
 * En situation réelle, lockMutex() et unlockMutex() devraient être appelées de manière invisible par le serveur afin
 * de rendre l'applicaition plus robuste.
 * Cependant, demander au client de gérer ces appels nous permet de mieux illustrer le comportement
 * de l'algorithme de Lamport dans le cadre de ce laboratoire.
 *
 * Marche à suivre pour 2 sites (dans l'ordre !) :
 * - Mode manuel :
 *      0. Entrer les adresses des serveurs dans le fichier sites.properties du module common
 *      1. Démarrer un backend avec les arguments 0 et 2
 *      2. Démarrer un backend avec les arguments 1 et 2
 *      3. Démarrer un frontend avec l'argument 0
 *      4. Démarrer un frontend avec l'argument 1
 *      6. Sélectionner le mode manuel (choix 2)
 *      7. Taper "set" dans le premier client :
 *          - le mutex et locké, accès à la section critique
 *          - La console invite à enter une nouvelle valeur
 *      8. Taper "set" dans le deuxième client :
 *          - Blocage, en attente de la section critique
 *      9. Doner une valeur au 1er client (6, par exemple)
 *          - La valeur est mise à jour
 *          - Relachement de la section critique automatique
 *          - 2ème client est maintenant débloquer et en section critique
 *     10. Donner une valeur au 2ème client
 *          - La valeur est mise à jour
 *          - Relachement de la section critique automatique
 *     11. Taper get dans les deux clients pour vérifier l'état de la valeur globale
 *          - La valeur correspond à la dernière modification opérée par client 2
 * - Mode automatique
 *    1-4. Etapes 1 à 4 identiques au mode manuel
 *      5. Sélectionner le mode automatique (choix 1)
 *      6. Le client effectue une lecture suivit d'une incrémentaiton de la valeur à 1000 reprises.
 *          - La valeur avant modificiation est affichée
 *          - La valeur après modificiation est affichée
 */

import java.io.*;
import java.rmi.*;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Argument invalide : [numéro du site (0 .. n-1)]");
            System.exit(1);
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

            Scanner sc = new Scanner(System.in);

            do {
                System.out.println("===== Select Mode ====");
                System.out.println("- Automatique [1]");
                System.out.println("- Manuel [2]");
                System.out.println("Choice: ");
                String input = sc.nextLine();
                if (input.equals("1")) {
                    testAuto(data);
                } else if (input.equals("2")) {
                    testManuel(data);
                }
            }while(true);
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

    /**
     * Test manuel de l'accès à la section critique
     * @param data Acces au serveur
     * @throws RemoteException
     */
    private static void testManuel(Data data) throws RemoteException {
        Scanner sc = new Scanner(System.in);
        displayCommands();
        while (true) {
            String input = sc.nextLine();

            if (input.contains("set")) {
                System.out.println("Locking mutex ...");
                data.lockMutex();
                System.out.println("Mutex locked");

                System.out.println("Valeur actuelle : " + data.getValue() + "\n");

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

    /**
     * Test automatique de l'accès à la section critique
     * @param data accès au serveur
     * @throws RemoteException
     * @throws InterruptedException
     */
    private static void testAuto(Data data) throws RemoteException, InterruptedException {
        for (int i = 0; i < 1000; i++) {
            System.out.println("Lire la valeur : " + data.getValue());

            System.out.println("\nLocking mutex ...");
            data.lockMutex();
            System.out.println("Mutex locked");
            System.out.println("Valeur avant modification : " + data.getValue());
            data.setValue(data.getValue() + 1);
            System.out.println("Valeur après modification : " + data.getValue());
            System.out.println("Releasing mutex ...");
            data.releaseMutex();
            System.out.println("Mutex released\n");
        }
    }
}
