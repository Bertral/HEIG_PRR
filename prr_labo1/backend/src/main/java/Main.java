import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Project : prr_labo1
 * Date : 08.11.17
 */
public class Main {

    // Le backend implémente Lamport

    public static void main(String[] args) {
        try {
            // création du registre
            Registry registry = LocateRegistry.createRegistry(1099);

            // instanciation de la donnée
            DataImpl dataImpl = new DataImpl();

            // définition de l'adresse de la donnée
            String url = "rmi://" + InetAddress.getLocalHost().getHostAddress() + "/Data";
            Naming.rebind(url, dataImpl);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
