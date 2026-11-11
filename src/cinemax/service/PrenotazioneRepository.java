package cinemax.service;

import cinemax.model.Prenotazione;
import cinemax.model.Proiezione;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes reservation records in the CSV file. */
public class PrenotazioneRepository {
    private static final String HEADER =
            "codice;username;titoloFilm;dataOra;numBiglietti;prezzoUnitario";
    private final Path file = Path.of("data", "prenotazioni.csv");

    /** Appends one reservation row to the CSV file. */
    public void salva(Prenotazione prenotazione) throws IOException {
        assicuraFile();
        Proiezione proiezione = prenotazione.getProiezione();
        String riga = String.join(";",
                prenotazione.getCodice(),
                prenotazione.getUsername(),
                proiezione.getFilm().getTitolo(),
                proiezione.getDataOra().toString(),
                Integer.toString(prenotazione.getNumBiglietti()),
                Double.toString(proiezione.getPrezzo())
        );
        Files.writeString(file, riga + System.lineSeparator(),
                StandardOpenOption.APPEND);
    }

    /** Loads reservations and links each one to its screening object. */
    public List<Prenotazione> caricaTutte(List<Proiezione> proiezioni)
            throws IOException {
        assicuraFile();
        List<String> righe = Files.readAllLines(file);
        List<Prenotazione> prenotazioni = new ArrayList<>();

        for (int i = 1; i < righe.size(); i++) {
            if (righe.get(i).isBlank()) {
                continue;
            }
            String[] colonne = righe.get(i).split(";", -1);
            if (colonne.length != 6) {
                throw new IOException("Prenotazione non valida alla riga "
                        + (i + 1));
            }

            Proiezione proiezione = trovaProiezione(
                    proiezioni, colonne[2], colonne[3]
            );
            if (proiezione == null) {
                throw new IOException("Proiezione non trovata per la prenotazione "
                        + colonne[0]);
            }

            prenotazioni.add(new Prenotazione(
                    colonne[0], colonne[1],
                    Integer.parseInt(colonne[4]), proiezione
            ));
        }
        return prenotazioni;
    }

    /** Rewrites the reservation file with the supplied records. */
    public void salvaTutte(List<Prenotazione> prenotazioni)
            throws IOException {
        List<String> righe = new ArrayList<>();
        righe.add(HEADER);
        for (Prenotazione prenotazione : prenotazioni) {
            Proiezione proiezione = prenotazione.getProiezione();
            righe.add(String.join(";",
                    prenotazione.getCodice(),
                    prenotazione.getUsername(),
                    proiezione.getFilm().getTitolo(),
                    proiezione.getDataOra().toString(),
                    Integer.toString(prenotazione.getNumBiglietti()),
                    Double.toString(proiezione.getPrezzo())
            ));
        }
        Files.write(file, righe);
    }

    /** Counts booked seats for the given screening. */
    public int contaBiglietti(Proiezione proiezione) throws IOException {
        assicuraFile();
        int totale = 0;
        List<String> righe = Files.readAllLines(file);

        for (int i = 1; i < righe.size(); i++) {
            if (righe.get(i).isBlank()) {
                continue;
            }
            String[] colonne = righe.get(i).split(";", -1);
            if (colonne.length == 6
                    && colonne[2].equals(proiezione.getFilm().getTitolo())
                    && colonne[3].equals(proiezione.getDataOra().toString())) {
                totale += Integer.parseInt(colonne[4]);
            }
        }
        return totale;
    }

    private Proiezione trovaProiezione(List<Proiezione> proiezioni,
                                       String titolo, String dataOra) {
        for (Proiezione proiezione : proiezioni) {
            if (proiezione.getFilm().getTitolo().equals(titolo)
                    && proiezione.getDataOra().toString().equals(dataOra)) {
                return proiezione;
            }
        }
        return null;
    }

    private void assicuraFile() throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            Files.writeString(file, HEADER + System.lineSeparator());
        }
    }
}
