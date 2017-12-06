import java.io.Serializable;

/**
 * Project : prr_labo2
 * Date : 08.11.17
 * Auteurs : Antoine Friant et Michela Zucca
 *
 * Sous-type de message contenant un payload supplémentaires : la nouvelle valeur à affecter
 */
public class MessageRelease extends Message implements Serializable {
    private int newValue;

    /**
     * Constructeur
     * @param estampille
     * @param originSite
     * @param newValue
     */
    public MessageRelease(long estampille, int originSite, int newValue) {
        super(TYPE.RELEASE, estampille, originSite);
        this.newValue = newValue;
    }

    /**
     * Récupère la valeur associée au message de type RELEASE
     * @return valeur associée au message
     */
    public int getNewValue() {
        return newValue;
    }

}
