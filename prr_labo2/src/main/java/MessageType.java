import java.util.ArrayList;
import java.util.HashMap;

/**
 * Project : prr_labo2
 * Date : 15.12.17
 * Représente un message reçu par UDP
 */
public class MessageType extends Message{

    private ArrayList<Site> sites;

    /**
     * Constructeur
     *
     * @param messageType
     * @param sitesAptitudes
     */
    public MessageType(MessageType messageType, ArrayList<Site> sitesAptitudes) {
        super(messageType);
        this.sites = sitesAptitudes;
    }

    public ArrayList<Site> getSites() {
        return sites;
    }
}
