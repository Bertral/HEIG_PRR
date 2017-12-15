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
    private UDPController udpController;

    /**
     * Constructeur
     *
     * @param num numéro du site local
     */
    public AlgoElection(byte num) {
        this.annoucementInProgess = false;
        this.udpController = new UDPController(num);
    }

    /**
     * Initialisation du site. Un site contient les informations :
     * - no site
     * - aptitude
     *
     * @param me        le site courant
     * @param neighbour le site voisin
     */
    public void initialise(Site me, Site neighbour) {
        this.me = me;
        this.neighbour = neighbour;
    }

    /**
     * Lancement de l'élection au démarrage
     */
    public void start() {
        udpController.sendAnnounce(neighbour.getNoSite(), me.getNoSite(), udpController.getAptitude());
        annoucementInProgess = true;
    }

    /**
     * Annonce reçu d'un autre site, traitement de l'information.
     *
     * @param otherSite
     */
    public void annoucement(Site otherSite) {
        if (me.getAptitude() > otherSite.getAptitude()
                || (me.getAptitude() == otherSite.getAptitude()
                && me.getNoSite() > otherSite.getNoSite())) {
            if (!annoucementInProgess) {
                udpController.sendAnnounce(neighbour.getNoSite(), me.getNoSite(), udpController.getAptitude());
                annoucementInProgess = true;
            }
        } else if (me == otherSite) {
            udpController.sendResult(neighbour.getNoSite(), me.getNoSite());
        } else {
            udpController.sendAnnounce(neighbour.getNoSite(), otherSite.getNoSite(), otherSite.getAptitude());
            annoucementInProgess = true;
        }
    }

    /**
     * Réception du résultat de l'élection
     *
     * @param elu Site élu
     */
    public void resultat(Site elu) {
        this.coordinator = elu;
        annoucementInProgess = false;
        if (this.coordinator.getNoSite() != me.getNoSite()) {
            udpController.sendResult(neighbour.getNoSite(), coordinator.getNoSite());
        }
    }

    /**
     * Retourne l'élu actuelle connu
     *
     * @return l'élu
     */
    public Site getCoordinator() {
        return coordinator;
    }

}
