/*
 * Un site se caractérise par son numéro et son aptitude.
 * L'aptitude est calculée selon le numéro de port du site et le dernier octet de son adresse IP
 *   <aptitude = port + octet>
 *
 */
public class Site implements Comparable {
    private int aptitude;
    private byte noSite;

    /**
     * Constructeur d'un site. L'aptitude d'un site est calculé selon son numéro de port et le dernier octet de
     * son adresse IP.
     *
     * @param noSite no du site
     */
    public Site(byte noSite, int aptitude) {
        this.noSite = noSite;
        this.aptitude = aptitude;
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

    @Override
    public boolean equals(Object otherSite) {
        if (otherSite != null && otherSite instanceof Site) {
            return noSite == ((Site) otherSite).noSite;
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Object otherSite) {
        return noSite - ((Site) otherSite).noSite;
    }

    public int getAptitude() {
        return aptitude;
    }
}
