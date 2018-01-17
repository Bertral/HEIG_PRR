import java.util.LinkedList;
import java.util.List;

/**
 * Project : prr_labo4
 * Date : 11.01.18
 * Authors : Antoine Friant, Michela Zucca
 */

public class Terminaison {
    enum T_Etat {actif, inactif}

    private Message T_message;
    private T_Etat etat ;
    private byte moi;
    private byte N;
    private UDPController application;
    private List<Worker> workers = new LinkedList<>();

    public Terminaison(UDPController application, byte proc, byte N){
        this.etat  = T_Etat.actif;
        this.moi = proc;
        this.application = application;
        this.N = N;
    }

    public void travail(Message msg){
        byte neightbours = (byte)( (moi %N) +1);

        switch(msg.getMessageType()){
            case REQUETE:
                // TODO faire le travail demandé
                workers.add(new Worker(application));
                break;
            case JETON:
                if(etat == T_Etat.inactif){
                    // Envoyer fin au voisin
                    application.send(  neightbours , new Message(Message.MessageType.FIN, moi));
                }else{
                    // TODO attendre que le travaille soit terminé
                    // Vérifier si les workers sont terminés
                    for(Worker w : workers){
                       w.requestStop();
                       if(w.isRunning()){
                           w.join(); // rejoint pour attendre qu'il finisse, sinon il ne relancera pas de travail
                       }
                    }
                    // Passe en inactif et on transmet le jeton
                    etat = T_Etat.inactif;
                    application.send( neightbours, new Message(Message.MessageType.JETON, moi));
                }
                break;
            case FIN:
                if(moi == msg.getOriginSite()){
                    // TODO terminer

                }
                else{
                    // Transmet le jeton au voisin
                    application.send(neightbours, new Message(Message.MessageType.FIN, moi));
                }
                break;
        }
    }
}
