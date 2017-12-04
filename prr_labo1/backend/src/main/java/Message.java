enum TYPE {REQUETE, QUITTANCE, LIBERE}

public class Message {
    TYPE type;
    long estampille;
    int originSite;

    public Message(TYPE type, long estampille, int originSite) {
        this.estampille = estampille;
        this.type = type;
        this.originSite = originSite;
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

}
