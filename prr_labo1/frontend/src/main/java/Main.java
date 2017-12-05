import java.rmi.*;
import java.util.Scanner;


/**
 * Project : prr_labo1
 * Date : 08.11.17
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Argument invalide : [numéro du site]");
        }

        // connexion au serveur RMI
        try {
            Remote r = Naming.lookup("rmi://localhost/Data" + args[0]);
            Data data = (Data) r;

            // boucle d'exécution
            Scanner sc = new Scanner(System.in);
            displayCommands();
            while (true) {
                String input = sc.nextLine();

                if (input.contains("set")) {
                    System.out.println("Locking mutex ...");
                    data.lockMutex();
                    System.out.println("Mutex locked");

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
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static void displayCommands() {
        System.out.println("===== Commands ====");
        System.out.println("set : set new value");
        System.out.println("get : display value");
        System.out.print("> ");
    }
}
