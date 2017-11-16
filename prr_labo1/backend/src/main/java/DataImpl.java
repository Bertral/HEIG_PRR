import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Project : prr_labo1
 * Date : 16.11.17
 */
public class DataImpl extends UnicastRemoteObject implements Data {
    //private static final long serialVersionUID = 2674880711467464646L;

    private int value = 0;

    protected DataImpl() throws RemoteException {
        super();
    }

    public int getValue() throws RemoteException {
        System.out.println("Sending value : " + value);
        return value++;
    }
}
