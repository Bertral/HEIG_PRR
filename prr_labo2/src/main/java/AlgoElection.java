/*
 * Algorithme d'élection en anneau avec panne des sites possibles.
 * Une seule élection peut avoir lieu
 * - En cas de reprise de panne un site ne peut pas interrompre une élection
 * - En cas de panne de l'élu une nouvelle élection est démarrer
 */
public class AlgoElection {

    private Site me; // Site courant
    private Site neighbour; // Site voisin
    private Site coordinator; // Site élu
    private boolean annoucementInProgess; // élection en cours

    public AlgoElection() {
        this.annoucementInProgess = false;
    }

    // TODO n'est pas utile dans cette configuration
    public int evalAptitude(){
        return 0;
    }

    /**
     * Initialisation du site. Un site contient les informations :
     * - no site
     * - aptitude
     * @param me le site courant
     * @param neighbour le site voisin
     */
    public void initialise(Site me, Site neighbour){
        this.me = me;
        this.neighbour = neighbour;
    }

    /**
     * Lancement de l'élection au démarrage
     */
    public void start(){
        // TODO voir la méthode de communication
        communication.annonce(me, neighbour);
        annoucementInProgess = true;
    }

    /**
     * Annonce reçu d'un autre site, traitement de l'information.
     * @param otherSite
     */
    public void annoucement(Site otherSite){
        this.otheSite = otherSite;
        if(me.getAptitude() > otherSite.getAptitude()
                || (me.getAptitude() == otherSite.getAptitude()
                && me.getNoSite() > otherSite.getNoSite())){
            if(annoucementInProgess == false){
                // TODO voir la méthode de communication
                communication.annonce(me ,neighbour);
                annoucementInProgess = true;
            }
        }else if(me == otherSite){
            // TODO voir la méthode de communication
            communication.resultat(me, neighbour);
        }else{
            // TODO voir la méthode de communication
            communication.annonce(otherSite,neighbour);
        }
    }

    /**
     * Réception du résultat de l'élection
     * @param elu Site élu
     */
    public void resultat(Site elu){
        this.coordinator = elu;
        annoucementInProgess = false;
    }

    /**
     * Retourne l'élu actuelle connu
     * @return l'élu
     */
    public Site getCoordinator(){
        return coordinator;
    }

}
