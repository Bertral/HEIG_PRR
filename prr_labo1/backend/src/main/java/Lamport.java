import java.util.ArrayList;

/**
 * Project : prr_labo1
 * Date : 16.11.17
 */

public class Lamport implements Runnable {
    enum TYPE {REQUETE, QUITTANCE, LIBERE}

    class Request {
        TYPE type;
        long estampille;
        int originSite;

        public Request(TYPE type, long estampille, int originSite) {
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

    private int numSite;
    private int nbSite;
    private long clockLogical;
    private boolean scAccorde;
    private ArrayList<Request> requestFile;
    private ArrayList<Integer> siteAdressFile;

    public Lamport(int numSite, int nbSite) {
        this.numSite = numSite;
        this.nbSite = nbSite;
        this.clockLogical = 0;
        this.scAccorde = false;
        this.requestFile = new ArrayList(nbSite);
        this.siteAdressFile = new ArrayList<Integer>(nbSite);
        initRequestFile();
        initSiteAdressFile();
    }

    private void initRequestFile(){
        for(int i = 0; i < requestFile.size(); i++){
            requestFile.add(i, new Request(TYPE.LIBERE, 0, i));
        }
    }

    private void initSiteAdressFile() {
        for (int i = 0; i < siteAdressFile.size(); i++) {
            siteAdressFile.add(i, i);
        }
    }

    public void run() {
        // Gestion des 4 rendez-vous

    }

    public boolean permission(int me) {
        boolean accord = true;
        for(int i = 0; i < siteAdressFile.size(); i++){
            if(i != me){
                accord = (requestFile.get(me).getEstampille() < requestFile.get(i).getEstampille() ) ||
                        ( requestFile.get(me).getEstampille() == requestFile.get(i).getEstampille() && me < i) ;
            }
        }
        return accord;
    }

    public void envoi(Request req, int dest) {
        // Envoyer au site dest, la requete et mon num de site
    }

    public void demande(){
        // Maj horloge interne
        this.clockLogical += 1;
        // Enregistre la requête dans sa liste
        Request req = new Request(TYPE.REQUETE, clockLogical, numSite);
        requestFile.add(this.numSite, req);
        // Signaler à tous les autres sites la nouvelle requête
        for(int i = 0; i < siteAdressFile.size(); i++){
            if(i != numSite){
                envoi(req, i);
            }
        }
        scAccorde = permission(numSite);
    }

    public void fin() {
        // Enregistre la requête dans sa liste
        Request req = new Request(TYPE.LIBERE, clockLogical, numSite);
        requestFile.add(this.numSite, req);
        // Signaler à tous les autres sites la nouvelle requête
        for(int i = 0; i < siteAdressFile.size(); i ++){
            if(i != numSite){
                envoi(req, i);
            }
        }
        scAccorde = false;
    }

    public void recoit(Request req) {
        // Maj de l'horloge logique
        clockLogical = Math.max(clockLogical, req.getEstampille()) + 1;
        switch (req.getType()) {
            case REQUETE:
                requestFile.add(req.getOriginSite(), req);
                envoi(new Request(TYPE.QUITTANCE,clockLogical,numSite),req.getOriginSite());
                 break;
            case LIBERE:
                requestFile.add(req.getOriginSite(), req);
                break;
            case QUITTANCE:
                if(requestFile.get(req.originSite).getType() != TYPE.REQUETE){
                    requestFile.add(req.getOriginSite(),req);
                }
                break;
        }
        scAccorde = (requestFile.get(numSite).getType() == TYPE.REQUETE) && permission(numSite);
    }

}