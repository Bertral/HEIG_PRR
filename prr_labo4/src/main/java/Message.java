import java.util.SortedSet;

/**
 * Project : prr_labo4
 * Date : 14.12.17
 * Authors : Antoine Friant, Michela Zucca
 *
 * Représente un message
 */
public class Message {
    /**
     * Représente un type de message (type énuméré sûr)
     */
    public enum MessageType {
        REQUETE((byte) 0),
        JETON((byte) 1),
        FIN((byte) 2);

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

    /**
     * Constructeur
     *
     * @param messageType
     */
    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Récupères le type du message
     * @return type du message
     */
    public MessageType getMessageType() {
        return messageType;
    }


    /**
     * Convertit le message en String lisible pour l'affichage
     * @return String
     */
    @Override
    public String toString() {
        String string = "Type : " + messageType.toString();
        return string;
    }
}
