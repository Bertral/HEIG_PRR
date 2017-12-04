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
        if(args.length != 2){
            System.out.println("Arguments invalides : [numéro du site] [nombre de sites]");
            return;
        }

        try {
            // création du registre
            Registry registry = LocateRegistry.createRegistry(1099 + Integer.parseInt(args[0]));

            // instanciation de la donnée
            DataImpl dataImpl = new DataImpl();
            dataImpl.setLamport(new Lamport(Integer.parseInt(args[0]), Integer.parseInt(args[1])));

            // définition de l'adresse de la donnée
            String url = "rmi://" + InetAddress.getLocalHost().getHostAddress() + "/Data" + args[0];
            Naming.rebind(url, dataImpl);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
