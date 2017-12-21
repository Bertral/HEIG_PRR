public class Message {
    /**
     * Représente un type de message
     */
    public enum MessageType {
        ANNOUNCE((byte) 0),
        RESULT((byte) 1),
        PING((byte) 2),
        PONG((byte) 3),
        QUITTANCE((byte) 4);

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

    public Message(MessageType messageType){
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
