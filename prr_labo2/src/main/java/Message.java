import java.util.SortedSet;

/**
 * Project : prr_labo2
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
        ANNOUNCE((byte) 0),
        RESULT((byte) 1),
        PING((byte) 2),
        PONG((byte) 3),
        CHECKOUT((byte) 4);

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
    private SortedSet<Site> sites;      // sites contenus dans le message
    private byte resultElect;           // site élu (seulement utilisé dans un message RESULT)

    /**
     * Constructeur
     *
     * @param messageType
     * @param sites
     */
    public Message(MessageType messageType, SortedSet<Site> sites) {
        this.messageType = messageType;
        this.sites = sites;
    }

    /**
     * Récupères le type du message
     * @return type du message
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * Récupères l'ensemble des sites contenus dans les message
     * @return SortedSet<Site>
     */
    public SortedSet<Site> getSites() {
        return sites;
    }

    /**
     * Récupère le byte correspondant au numéro du site élu
     * Si le message est de type RESULT, il s'agit du byte correspondant au résultat de l'élection
     * Sinon, il s'agit du meilleur candidat de l'ensemble stocké dans le message
     * @return byte élu
     */
    public byte getResultByte() {
        return messageType == MessageType.RESULT ? resultElect : getBestSite();
    }

    /**
     * Définit le site élu d'un message de type RESULT
     * @param resultElect élu
     */
    public void setResultByte(byte resultElect) {
        this.resultElect = resultElect;
    }

    /**
     * Calcule le meilleur candidat de l'ensemble des sites contenus dans le message
     * @return byte numéro du candidat
     */
    private byte getBestSite() {
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

    /**
     * Convertit le message en String lisible pour l'affichage
     * @return String
     */
    @Override
    public String toString() {
        String string = "Type : " + messageType.toString();
        if (!sites.isEmpty()) {
            string += " Election < ";
            for (Site s : sites) {
                string += s.getNoSite() + " ";
            }
            string += ">";
        }
        if (getBestSite() > -1) {
            string += " best candidate : " + getBestSite();
        }
        return string;
    }
}
