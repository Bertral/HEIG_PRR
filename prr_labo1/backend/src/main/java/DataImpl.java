import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Project : prr_labo1
 * Date : 16.11.17
 */
public class DataImpl extends UnicastRemoteObject implements Data {
    //private static final long serialVersionUID = 2674880711467464646L;

    private int value = 0;
    private Lamport lamport;

    protected DataImpl() throws RemoteException {
        super();
    }

    public void setLamport(Lamport lamport) {
        this.lamport = lamport;
    }

    public int getValue() throws RemoteException {
        System.out.println("Sending value : " + value);
        return value;
    }

    public void lockMutex() throws RemoteException {
        System.out.println("Locking mutex");
        try {
            lamport.demande();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void releaseMutex() throws RemoteException {
        System.out.println("Releasing mutex");
        lamport.fin(value);
    }

    public void setValue(int value) throws RemoteException {
        System.out.println("Setting value : " + value);
        this.value = value;
    }

    public void sendMessage(Message msg) {
        lamport.recoit(msg);
    }
}
