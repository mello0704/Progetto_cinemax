package cinemax.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** A scheduled screening of a film, including time and ticket price. */
public class Proiezione {

    private final Film film;
    private LocalDateTime dataOra;   // non final: il proiezionista può modificarla
    private double prezzo;           // non final: modificabile se non ci sono prenotazioni
    /** Creates a screening. */
    public Proiezione(Film film, LocalDateTime dataOra, double prezzo) {
        this.film = film;
        this.dataOra = dataOra;
        this.prezzo = prezzo;
    }

    /** Returns the screening start date and time. */
    public LocalDateTime getDataOra() {
        return dataOra;
    }

    /** Returns the ticket price. */
    public double getPrezzo() {
        return prezzo;
    }

    /** Returns the film shown in this screening. */
    public Film getFilm() {
        return film;
    }

    /** Calculates the end time from the film duration. */
    public LocalDateTime getFine() {
        return dataOra.plusMinutes(film.getDurataMinuti());
    }

    /** Changes the screening start date and time. */
    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    /** Changes the ticket price. */
    public void setPrezzo(double prezzo) {
        this.prezzo = prezzo;
    }

    // dentro la classe, tra i campi
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Returns a short screening description. */
    @Override
    public String toString() {
        return film.getTitolo() + " - " + dataOra.format(FORMATO)
                + " - " + String.format("%.2f", prezzo) + " €";
    }

}
