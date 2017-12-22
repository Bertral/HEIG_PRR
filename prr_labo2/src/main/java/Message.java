import java.util.SortedSet;

public class Message {
    /**
     * Représente un type de message
     */
    public enum MessageType {
        ANNOUNCE((byte) 0),
        RESULT((byte) 1),
        PING((byte) 2),
        PONG((byte) 3),
        CHECKOUT((byte) 4);

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
    private SortedSet<Site> sites;

    public Message(MessageType messageType, SortedSet<Site> sites) {
        this.messageType = messageType;
        this.sites = sites;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public SortedSet<Site> getSites() {
        return sites;
    }

    public byte getElu() {
        if (sites.isEmpty()) {
            return -1;
        }

        Site maxSite = sites.first();
        for (Site s : sites) {
            if (s.getAptitude() > maxSite.getAptitude() || (s.getAptitude() == maxSite.getAptitude() && s
                    .getNoSite() > maxSite.getNoSite())) {
                maxSite = s;
            }
        }
        return maxSite.getNoSite();
    }
}
