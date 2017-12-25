/**
 * Project : prr_labo2
 * Date : 14.12.17
 * Authors : Antoine Friant, Michela Zucca
 *
 * Un site se caractérise par son numéro et son aptitude.
 * L'aptitude est calculée selon le numéro de port du site et le dernier octet de son adresse IP
 *   <aptitude = port + dernierOctet>
 */
public class Site implements Comparable {
    private int aptitude;   // Aptitude du site
    private byte noSite;    // Numéro du site

    /**
     * Constructeur d'un site.
     *
     * @param noSite no du site
     */
    public Site(byte noSite, int aptitude) {
        this.noSite = noSite;
        this.aptitude = aptitude;
    }

    public byte getNoSite() {
        return noSite;
    }

    public int getAptitude() {
        return aptitude;
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
}
