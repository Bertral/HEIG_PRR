import java.util.ArrayList;

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
    private ArrayList<Site> sites;

    public Message(MessageType messageType, ArrayList<Site> sitesAptitudes) {
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public ArrayList<Site> getSites() {
        return sites;
    }

    public byte getElu() {
        if (sites.isEmpty()) {
            return -1;
        }

        Site maxSite = sites.get(0);
        for (Site s : sites) {
            if (s.getAptitude() > maxSite.getAptitude() || ((s.getAptitude() == maxSite.getAptitude()) && (s
                    .getNoSite() > maxSite.getNoSite()))) {
                maxSite = s;
            }
        }
        return maxSite.getNoSite();
    }
}
