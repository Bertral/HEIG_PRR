import java.io.Serializable;

/**
 * Project : prr_labo2
 * Date : 08.11.17
 * Auteurs : Antoine Friant et Michela Zucca
 *
 * Classe représentant un message de l'algorithme de Lamport pour communication par RMI
 */
public class Message implements Serializable {
    public enum TYPE {REQUEST, RECEIPT, RELEASE}
    TYPE type;
    long stamp;
    int originSite;

    /**
     * Constructeur
     * @param type
     * @param estampille
     * @param originSite
     */
    public Message(TYPE type, long estampille, int originSite) {
        this.stamp = estampille;
        this.type = type;
        this.originSite = originSite;
    }

    /**
     * Récupère l'stamp du message
     * @return stamp du message
     */
    public long getStamp() {
        return stamp;
    }

    /**
     * Récupère le site originaire du message
     * @return site d'origine du message
     */
    public int getOriginSite() {
        return originSite;
    }

    /**
     * Récupère le type du message, soit REQUEST, RECEIPT, RELEASE
     * @return
     */
    public TYPE getType() {
        return type;
    }
}
