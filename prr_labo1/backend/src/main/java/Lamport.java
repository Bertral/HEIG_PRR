import java.util.ArrayList;
import java.util.List;

/**
 * Project : prr_labo1
 * Author(s) : Antoine Friant
 * Date : 16.11.17
 */
enum TYPE{REQUETE,QUITTANCE, LIBERE}

class Request{
    TYPE type;
    long estampille;
    int originSite;

    public Request(TYPE type, long estampille, int originSite){
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

public class Lamport  implements Runnable {

    private int numSite;
    private int nbSite;
    private long clockLogical;
    private boolean scAccorde;
    private ArrayList<Request> tFile;
    private ArrayList<Integer> file;

    public Lamport(int numSite, int nbSite) {
        this.numSite = numSite;
        this.nbSite= nbSite;
        this.clockLogical = 0;
        this.scAccorde = false;
        this.tFile = new ArrayList(nbSite);
        this.file = new ArrayList<Integer>(nbSite);
        initTFile();
        initFile();
    }

    private void initTFile(){
        for(int i = 0; i < tFile.size(); i++){
            tFile.add(i, new Request(TYPE.LIBERE, 0, i));
        }
    }

    private void initFile(){
        for(int i = 0; i < file.size(); i++){
            file.add(i,i);
        }
    }

    public void run() {
        // Gestion des 4 rendez-vous
    }

    public boolean permission(int me){
        boolean accord = true;
        for(int i = 0; i < file.size(); i++){
            if(i != me){
                accord = (tFile.get(me).getEstampille() < tFile.get(i).getEstampille() ) ||
                        ( tFile.get(me).getEstampille() == tFile.get(i).getEstampille() && me < i) ;
            }
        }
        return accord;
    }

    public void envoi(Request req, int dest){
        // Envoyer au site dest, la requete et mon num de site
    }

    public void demande(){
        // Maj horloge interne
        this.clockLogical += 1;
        // Enregistre la requête dans sa liste
        Request req = new Request(TYPE.REQUETE, clockLogical, numSite);
        tFile.add(this.numSite, req);
        // Signaler à tous les autres sites la nouvelle requête
        for(int i = 0; i < file.size(); i++){
            if(i != numSite){
                envoi(req, i);
            }
        }
        scAccorde = permission(numSite);
    }

    public void fin(){
        // Enregistre la requête dans sa liste
        Request req = new Request(TYPE.LIBERE, clockLogical, numSite);
        tFile.add(this.numSite, req);
        // Signaler à tous les autres sites la nouvelle requête
        for(int i = 0; i < file.size(); i ++){
            if(i != numSite){
                envoi(req, i);
            }
        }
        scAccorde = false;
    }

    public void recoit(Request req){
        // Maj de l'horloge logique
        clockLogical = Math.max(clockLogical, req.getEstampille())+1;
        switch (req.getType()){
            case REQUETE:
                tFile.add(req.getOriginSite(), req);
                envoi(new Request(TYPE.QUITTANCE,clockLogical,numSite),req.getOriginSite());
                 break;
            case LIBERE:
                tFile.add(req.getOriginSite(), req);
                break;
            case QUITTANCE:
                if(tFile.get(req.originSite).getType() != TYPE.REQUETE){
                    tFile.add(req.getOriginSite(),req);
                }
                break;
        }
        scAccorde = (tFile.get(numSite).getType() == TYPE.REQUETE) && permission(numSite);
    }

}
