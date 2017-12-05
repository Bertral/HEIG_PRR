import java.awt.*;

public class Message {
    public enum TYPE {REQUETE, QUITTANCE, LIBERE}
    TYPE type;
    long estampille;
    int originSite;
    int newValue;

    public Message(TYPE type, long estampille, int originSite) {
        this.estampille = estampille;
        this.type = type;
        this.originSite = originSite;
    }

    public Message(TYPE type, long estampille, int originSite, int newValue){
        this.estampille = estampille;
        this.type = type;
        this.originSite = originSite;
        this.newValue = newValue;
    }

    public long getEstampille() {
        return estampille;
    }

    public int getOriginSite() {
        return originSite;
    }

    public TYPE getType() {
        return type;
    }

    /**
     * Retourne la nouvelle valeur pour un message de TYPE libère sinon renvoi -1
     * @return
     */
    public int getNewValue() {
        if(type == TYPE.LIBERE) {
            return newValue;
        }
        return -1;
    }
}
