/**
 * Project : prr_labo2
 * Date : 08.11.17
 * Auteurs : Antoine Friant et Michela Zucca
 *
 * Classe représentant un message de l'algorithme de Lamport pour communication par RMI
 */

import java.io.Serializable;

public class Message implements Serializable {
    // 3 types de messages
    public enum TYPE {REQUEST, RECEIPT, RELEASE}

    TYPE type;              // type du message
    long stamp;             // estampille
    int originSite;         // site originaire du message

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
     * Récupère le type du message du type REQUEST, RECEIPT ou RELEASE
     * @return type du message
     */
    public TYPE getType() {
        return type;
    }
}
