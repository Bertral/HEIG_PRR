
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
    private byte site;
    private int aptitude;

    /**
     * Constructeur
     *
     * @param messageType
     * @param site
     * @param aptitude    facultatif pour des messages de type RESULT
     */
    public Message(MessageType messageType, byte site, int aptitude) {
        this.messageType = messageType;
        this.site = site;
        this.aptitude = aptitude;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public byte getSite() {
        return site;
    }

    public int getAptitude() {
        return aptitude;
    }
}
