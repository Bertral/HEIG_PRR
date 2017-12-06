import java.rmi.*;

/**
 * Project : prr_labo1
 * Date : 16.11.17
 *
 * Interface de l'objet Remote contenant la donnée protégée par l'algorithme de Lamport
 */
public interface Data extends Remote {
    /**
     * Récupère la valeur de la donnée globale. L'appel ne nécessite pas l'exclusion mutuelle
     * @return la valeur de la donnée
     * @throws RemoteException
     */
    int getValue() throws RemoteException;

    /**
     * Vérouille le mutex (appel bloquant)
     * @throws RemoteException
     */
    void lockMutex() throws RemoteException;

    /**
     * Relache le mutex
     * @throws RemoteException
     */
    void releaseMutex() throws RemoteException;

    /**
     * Donne une valeur à la donnée globale. Nécessite de vérouiller le mutex au préalable.
     * @param value
     * @throws RemoteException
     */
    void setValue(int value) throws RemoteException;

    /**
     * Méthode de communication entre les serveurs.
     * @param msg
     * @throws RemoteException
     */
    void recoit(Message msg) throws RemoteException;
}
