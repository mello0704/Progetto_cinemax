package cinemax.service;

import cinemax.model.Film;
import cinemax.model.Proiezione;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Implements projection search and projectionist operations. */
public class ProiezioniService {
    private static final int POSTI_SALA = 200;
    private final ProiezioneRepository proiezioneRepository =
            new ProiezioneRepository();
    private final PrenotazioneRepository prenotazioneRepository =
            new PrenotazioneRepository();
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Searches projections by a partial title. */
    public List<Proiezione> cercaProiezioni(String testo) throws IOException {
        return cercaProiezioni(testo, "", null, null, null, null);
    }

    /** Searches by any combination of title, genre, date range, and price. */
    public List<Proiezione> cercaProiezioni(String titolo, String genere,
                                             LocalDate dal, LocalDate al,
                                             Double prezzoMin,
                                             Double prezzoMax)
            throws IOException {
        List<Proiezione> risultati = new ArrayList<>();
        String titoloCercato = titolo == null
                ? "" : titolo.trim().toLowerCase(Locale.ROOT);
        String genereCercato = genere == null
                ? "" : genere.trim().toLowerCase(Locale.ROOT);

        for (Proiezione proiezione : proiezioneRepository.caricaTutte()) {
            Film film = proiezione.getFilm();
            boolean titoloValido = titoloCercato.isEmpty()
                    || film.getTitolo().toLowerCase(Locale.ROOT)
                    .contains(titoloCercato);
            boolean genereValido = genereCercato.isEmpty()
                    || film.getGenere().toLowerCase(Locale.ROOT)
                    .contains(genereCercato);
            boolean dataValida = (dal == null
                    || !proiezione.getDataOra().toLocalDate().isBefore(dal))
                    && (al == null
                    || !proiezione.getDataOra().toLocalDate().isAfter(al));
            boolean prezzoValido = (prezzoMin == null
                    || proiezione.getPrezzo() >= prezzoMin)
                    && (prezzoMax == null
                    || proiezione.getPrezzo() <= prezzoMax);

            if (titoloValido && genereValido && dataValida && prezzoValido) {
                risultati.add(proiezione);
            }
        }

        if (risultati.isEmpty()) {
            System.out.println("Nessuna proiezione trovata.");
        } else {
            System.out.println("Proiezioni trovate:");
            for (int i = 0; i < risultati.size(); i++) {
                System.out.println((i + 1) + ") " + risultati.get(i));
            }
        }
        return risultati;
    }

    /** Prints film and screening details, including remaining seats. */
    public void visualizzaDettagli(Proiezione proiezione) throws IOException {
        Film film = proiezione.getFilm();
        int occupati = prenotazioneRepository.contaBiglietti(proiezione);
        int postiLiberi = Math.max(0, POSTI_SALA - occupati);

        System.out.println("\n=== DETTAGLI PROIEZIONE ===");
        System.out.println("Titolo: " + film.getTitolo());
        System.out.println("Genere: " + film.getGenere());
        System.out.println("Regista: " + film.getRegista());
        System.out.println("Anno: " + film.getAnno());
        System.out.println("Durata: " + film.getDurataMinuti() + " minuti");
        System.out.println("Età minima: " + film.getEtaMinima());
        System.out.println("Data e ora: "
                + proiezione.getDataOra().format(FORMATO_DATA));
        System.out.printf("Prezzo: %.2f €%n", proiezione.getPrezzo());
        System.out.println("Posti liberi: " + postiLiberi);
    }

    /** Adds a screening for a film. */
    public void aggiungiProiezione(Film film, LocalDateTime dataOra,
                                   double prezzo) throws IOException {
        if (prezzo < 0) {
            throw new IllegalArgumentException("Il prezzo non può essere negativo.");
        }
        proiezioneRepository.aggiungi(new Proiezione(film, dataOra, prezzo));
    }

    /** Changes a screening date only when it has no reservations. */
    public void modificaData(Proiezione proiezione, LocalDateTime nuovaDataOra)
            throws IOException {
        if (prenotazioneRepository.contaBiglietti(proiezione) > 0) {
            throw new IllegalArgumentException(
                    "Non puoi modificare una proiezione con prenotazioni."
            );
        }
        proiezioneRepository.modificaData(proiezione, nuovaDataOra);
    }

    /** Deletes a screening only when it has no reservations. */
    public void eliminaProiezione(Proiezione proiezione) throws IOException {
        if (prenotazioneRepository.contaBiglietti(proiezione) > 0) {
            throw new IllegalArgumentException(
                    "Non puoi eliminare una proiezione con prenotazioni."
            );
        }
        proiezioneRepository.elimina(proiezione);
    }

    /** Returns all screenings. */
    public List<Proiezione> tutteLeProiezioni() throws IOException {
        return proiezioneRepository.caricaTutte();
    }
}
