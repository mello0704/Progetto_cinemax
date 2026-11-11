package cinemax.model;

/** A customer's reservation for a screening. */
public class Prenotazione {

    private final String codice;
    private final String username;     // chi ha prenotato
    private Proiezione proiezione;
    private int numBiglietti;
    /** Creates a reservation with its unique code and ticket count. */
    public Prenotazione(String codice, String username, int numBiglietti, Proiezione proiezione) {
        this.codice = codice;
        this.username = username;
        this.numBiglietti = numBiglietti;
        this.proiezione = proiezione;

    }

    /** Returns the unique reservation code. */
    public String getCodice() {
        return codice;
    }

    /** Returns the username that owns the reservation. */
    public String getUsername() {
        return username;
    }

    /** Returns the number of tickets. */
    public int getNumBiglietti() {
        return numBiglietti;
    }

    /** Returns the reserved screening. */
    public Proiezione getProiezione() {
        return proiezione;
    }

    /** Calculates the total from ticket count and unit price. */
    public double getTotale() {
        return this.numBiglietti * proiezione.getPrezzo();
    }

    /** Changes the number of reserved tickets. */
    public void setNumBiglietti(int numBiglietti) {
        this.numBiglietti = numBiglietti;
    }


    /** Changes the screening linked to this reservation. */
    public void setProiezione(Proiezione proiezione) {
        this.proiezione = proiezione;
    }

    /** Returns a human-readable reservation summary. */
    @Override
    public String toString() {
        return "Prenotazione " + codice + " - cliente: " + username
                + "\n  " + proiezione
                + "\n  " + numBiglietti + " biglietti, totale "
                + String.format("%.2f", getTotale()) + " €";
    }

}


