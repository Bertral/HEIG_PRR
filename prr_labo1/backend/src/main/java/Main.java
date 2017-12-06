import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;

/**
 * Project : prr_labo1
 * Date : 08.11.17
 *
 * Point d'entrée du programme serveur
 */
public class Main {
    public static void main(String[] args) {
        if(args.length != 2){
            System.out.println("Arguments invalides : [numéro du site (0 .. n-1)] [nombre de sites (n)]");
            return;
        }

        try {
            // création du registre
            LocateRegistry.createRegistry(1099 + Integer.parseInt(args[0]));

            // instanciation de la donnée
            DataImpl dataImpl = new DataImpl();
            dataImpl.init(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

            // définition de l'adresse de la donnée
            String url = "rmi://" + InetAddress.getLocalHost().getHostAddress() + "/Data" + args[0];
            Naming.rebind(url, dataImpl);

            System.out.println("Server started");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
