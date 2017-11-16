import java.rmi.*;

/**
 * Project : prr_labo1
 * Date : 16.11.17
 */
public interface Data extends Remote {
    public int getValue() throws RemoteException;
}
