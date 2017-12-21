import java.util.ArrayList;
import java.util.HashMap;

/**
 * Project : prr_labo2
 * Date : 15.12.17
 * Représente un message reçu par UDP
 */
public class Message {

    /**
     * Représente un type de message
     */
    public enum MessageType {
        ANNOUNCE((byte) 0),
        RESULT((byte) 1),
        PING((byte) 2),
        PONG((byte) 3);

        private byte value;

        MessageType(byte b) {
            value = b;
        }

        /**
         * @return Type de message sous forme de byte (pour communication)
         */
        public byte getByte() {
            return value;
        }
    }

    private MessageType messageType;
    private ArrayList<Site> sites;

    /**
     * Constructeur
     *
     * @param messageType
     * @param sitesAptitudes
     */
    public Message(MessageType messageType, ArrayList<Site> sitesAptitudes) {
        this.messageType = messageType;
        this.sites = sitesAptitudes;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public ArrayList<Site> getSites() {
        return sites;
    }
}
