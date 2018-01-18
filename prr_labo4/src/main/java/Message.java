
/**
 * Project : prr_labo4
 * Date : 14.12.17
 * Authors : Antoine Friant, Michela Zucca
 * <p>
 * Représente un message
 */
public class Message {
    /**
     * Représente un type de message (type énuméré sûr)
     */
    public enum MessageType {
        REQUEST((byte) 0),
        TOKEN((byte) 1),
        END((byte) 2);

        private byte value; // byte du type de message

        MessageType(byte b) {
            value = b;
        }

        /**
         * @return Type de message sous forme de byte (pour communication UDP)
         */
        public byte getByte() {
            return value;
        }
    }

    private MessageType messageType;    // type du message
    private byte originSite;            // origine du message

    /**
     * Constructeur
     *
     * @param messageType type du messsage
     * @param originSite  site d'orrigine
     */
    public Message(MessageType messageType, byte originSite) {
        this.messageType = messageType;
        this.originSite = originSite;
    }

    /**
     * Récupères le type du message
     *
     * @return type du message
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * @return numéro du site d'origine du message
     */
    public byte getOriginSite() {
        return originSite;
    }

    /**
     * Convertit le message en String lisible pour l'affichage
     *
     * @return String
     */
    @Override
    public String toString() {
        String string = "Type : " + messageType.toString();
        return string;
    }
}
