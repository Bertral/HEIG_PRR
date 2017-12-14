/*
* Un site se caractérise par son numéro et son aptitude.
* L'aptitude est calculée selon le numéro de port du site et le dernier octet de son adresse IP
*   <aptitude = port + octet>
*
*/
public class Site {
    private int aptitude;
    private byte noSite;

    /**
     * Constructeur d'un site. L'aptitude d'un site est calculé selon son numéro de port et le dernier octet de
     * son adresse IP.
     * @param noSite no du site
     * @param port port du site
     * @param octet dernier octet de l'adresse IP du site
     */
    public Site(byte noSite, int port, int octet) {
       this.noSite = noSite;
       // Calcul de l'aptitude du site (immuable)
       this.aptitude = port+octet;
    }

    public int getAptitude() {
        return aptitude;
    }

    public void setAptitude(int aptitude) {
        this.aptitude = aptitude;
    }

    public byte getNoSite() {
        return noSite;
    }

    public void setNoSite(byte noSite) {
        this.noSite = noSite;
    }

    public boolean equals(Site otherSite){
        return noSite == otherSite.noSite;
    }
}
