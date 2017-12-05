import java.rmi.*;

/**
 * Project : prr_labo1
 * Date : 16.11.17
 */
public interface Data extends Remote {
    public int getValue() throws RemoteException;
    public void lockMutex() throws RemoteException; // appel bloquant
    public void releaseMutex() throws RemoteException;
    public void setValue(int value) throws RemoteException;
    public void recoit(Message msg) throws RemoteException;
}
