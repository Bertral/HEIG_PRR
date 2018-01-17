/**
 * Project : prr_labo4
 * Date : 11.01.18
 * Authors : Antoine Friant, Michela Zucca
 */

public class Terminaison {
    enum T_Etat {actif, inactif}

    private Message T_message;
    private T_Etat etat ;
    private int moi;
    private int N;
    private UDPController application;

    public Terminaison(UDPController application, int proc, int N){
        this.etat  = T_Etat.actif;
        this.moi = proc;
        this.application = application;
        this.N = N;
    }

    public void travail(Message msg){
        switch(msg.getMessageType()){
            case REQUETE:
                // TODO faire le travail demandé
                break;
            case JETON:
                if(moi == 1 && etat == T_Etat.inactif){
                    application.send(2, new Message(Message.MessageType.FIN));
                }else{
                    // TODO attendre que le travaille soit terminé
                    etat= T_Etat.inactif;
                    application.send( ((moi % N) + 1), new Message(Message.MessageType.JETON));
                }
                break;
            case FIN:
                if(moi == 1){
                    // TODO terminer
                }
                else{
                    application.send(((moi % N) + 1), new Message(Message.MessageType.FIN));
                }
                break;
        }
    }
}
