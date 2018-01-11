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

    /**
     * Récupère le numéro du site sous forme de byte
     * @return byte (numéro du site)
     */
    public byte getNoSite() {
        return noSite;
    }

    /**
     * Récupère l'aptitude sauvegardée du site
     * @return int aptitude
     */
    public int getAptitude() {
        return aptitude;
    }

    /**
     * Redéfinit equals() pour ne comparer que les numéros du site (utile pour ajouter un Site à un SortedSet
     * sans avoir à vérifier s'il existe déjà dans la liste)
     * @param otherSite autre Site
     * @return numéroSite == numéroAutreSite
     */
    @Override
    public boolean equals(Object otherSite) {
        if (otherSite != null && otherSite instanceof Site) {
            return noSite == ((Site) otherSite).noSite;
        } else {
            return false;
        }
    }

    /**
     * Compare un site à un autre (pour les trier dans l'ordre de leur numéros)
     * @param otherSite autre site
     * @return différence (int signé) entre les sites
     */
    @Override
    public int compareTo(Object otherSite) {
        return noSite - ((Site) otherSite).noSite;
    }
}
