/**
 * Project : prr_labo4
 * Date : 11.01.18
 * Authors : Antoine Friant, Michela Zucca
 */

public class Terminaison {
    enum T_Etat {actif, inactif}

    private Message T_message;
    private T_Etat etat ;
    public Terminaison(){
        this.etat  = T_Etat.actif;
    }
}
