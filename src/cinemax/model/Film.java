package cinemax.model;

/** Describes a film shown by the cinema. */
public class Film {

    private final String titolo;
    private final String genere;
    private final String regista;
    private final int anno;
    private final int durataMinuti;
    private final int etaMinima;

    /** Creates a film with its catalogue information. */
    public Film(String titolo, String genere, String regista, int anno, int durataMinuti, int etaMinima) {
        this.titolo = titolo;
        this.genere = genere;
        this.regista = regista;
        this.anno = anno;
        this.durataMinuti = durataMinuti;
        this.etaMinima = etaMinima;

    }

    /** Returns the film title. */
    public String getTitolo() {
        return titolo;
    }

    /** Returns the film genre. */
    public String getGenere() {
        return genere;
    }

    /** Returns the director's name. */
    public String getRegista() {
        return regista;
    }

    /** Returns the release year. */
    public int getAnno() {
        return anno;
    }

    /** Returns the running time in minutes. */
    public int getDurataMinuti() {
        return durataMinuti;

    }
    /** Returns the minimum recommended audience age. */
    public int getEtaMinima() {
        return etaMinima;
    }



    /** Returns a human-readable summary of the film. */
    @Override
    public String toString() {
        return titolo + " (" + anno + ") - " + genere + ", regia di " + regista
                + ", " + durataMinuti + " min, età minima " + etaMinima;
    }
    }

