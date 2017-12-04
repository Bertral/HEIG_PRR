import java.rmi.*;


/**
 * Project : prr_labo1
 * Date : 08.11.17
 */
public class Main {

    // Le frontend tourne sur la VM qui servira d'interface au backend

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Argument invalide : [numéro du site]");
        }

        // connexion au serveur RMI
        try {
            Remote r = Naming.lookup("rmi://localhost/Data"+args[0]);
            Data data = (Data)r;

            System.out.println("Received data : " + data.getValue());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
