import java.io.Serializable;

/**
 * Sous-type de message contenant un payload supplémentaires : la nouvelle valeur à affecter
 */
public class MessageLibere extends Message implements Serializable {
    private int newValue;

    /**
     * Constructeur
     * @param type
     * @param estampille
     * @param originSite
     * @param newValue
     */
    public MessageLibere(TYPE type, long estampille, int originSite, int newValue) {
        super(type, estampille, originSite);
        this.newValue = newValue;
    }

    public int getNewValue() {
        return newValue;
    }

}
