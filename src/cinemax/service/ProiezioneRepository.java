package cinemax.service;

import cinemax.model.Film;
import cinemax.model.Proiezione;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Reads, creates, updates, and deletes projection records. */
public class ProiezioneRepository {
    private static final String HEADER =
            "data_ora_proiezione,titolo_film,genere,regista,anno,durata_minuti,eta_minima,prezzo_biglietto";
    private static final DateTimeFormatter FORMATO_FILE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Path file = Path.of("data", "proiezioni.csv");

    /** Loads all projection records from the CSV file. */
    public List<Proiezione> caricaTutte() throws IOException {
        List<String> righe = Files.readAllLines(file);
        List<Proiezione> proiezioni = new ArrayList<>();

        for (int i = 1; i < righe.size(); i++) {
            if (righe.get(i).isBlank()) {
                continue;
            }
            String[] colonne = dividiCsv(righe.get(i));
            if (colonne.length != 8) {
                throw new IOException("Formato CSV non valido alla riga "
                        + (i + 1));
            }

            try {
                LocalDateTime dataOra = LocalDateTime.parse(
                        colonne[0], FORMATO_FILE
                );
                Film film = new Film(
                        colonne[1], colonne[2], colonne[3],
                        Integer.parseInt(colonne[4]),
                        Integer.parseInt(colonne[5]),
                        Integer.parseInt(colonne[6])
                );
                proiezioni.add(new Proiezione(
                        film, dataOra, Double.parseDouble(colonne[7])
                ));
            } catch (RuntimeException e) {
                throw new IOException("Dati non validi alla riga "
                        + (i + 1), e);
            }
        }
        return proiezioni;
    }

    /** Adds a projection if it does not overlap an existing screening. */
    public void aggiungi(Proiezione nuova) throws IOException {
        List<Proiezione> proiezioni = caricaTutte();
        verificaNessunaSovrapposizione(proiezioni, nuova, null);
        proiezioni.add(nuova);
        salvaTutte(proiezioni);
    }

    /** Changes the date and time of an existing projection. */
    public void modificaData(Proiezione originale, LocalDateTime nuovaDataOra)
            throws IOException {
        List<Proiezione> proiezioni = caricaTutte();
        Proiezione salvata = trova(proiezioni, originale);
        if (salvata == null) {
            throw new IllegalArgumentException("Proiezione non trovata.");
        }

        LocalDateTime dataPrecedente = salvata.getDataOra();
        salvata.setDataOra(nuovaDataOra);
        try {
            verificaNessunaSovrapposizione(proiezioni, salvata, salvata);
        } catch (IllegalArgumentException e) {
            salvata.setDataOra(dataPrecedente);
            throw e;
        }
        salvaTutte(proiezioni);
        originale.setDataOra(nuovaDataOra);
    }

    /** Removes an existing projection. */
    public void elimina(Proiezione daEliminare) throws IOException {
        List<Proiezione> proiezioni = caricaTutte();
        Proiezione salvata = trova(proiezioni, daEliminare);
        if (salvata == null) {
            throw new IllegalArgumentException("Proiezione non trovata.");
        }
        proiezioni.remove(salvata);
        salvaTutte(proiezioni);
    }

    private void verificaNessunaSovrapposizione(List<Proiezione> esistenti,
                                                Proiezione candidata,
                                                Proiezione daIgnorare) {
        for (Proiezione esistente : esistenti) {
            if (esistente == daIgnorare) {
                continue;
            }
            boolean siSovrappone = esistente.getDataOra()
                    .isBefore(candidata.getFine())
                    && candidata.getDataOra().isBefore(esistente.getFine());
            if (siSovrappone) {
                throw new IllegalArgumentException(
                        "La sala è già occupata in quell'orario."
                );
            }
        }
    }

    private Proiezione trova(List<Proiezione> proiezioni,
                             Proiezione riferimento) {
        for (Proiezione proiezione : proiezioni) {
            if (stessaProiezione(proiezione, riferimento)) {
                return proiezione;
            }
        }
        return null;
    }

    private boolean stessaProiezione(Proiezione prima, Proiezione seconda) {
        return prima.getFilm().getTitolo().equals(seconda.getFilm().getTitolo())
                && prima.getDataOra().equals(seconda.getDataOra());
    }

    private void salvaTutte(List<Proiezione> proiezioni) throws IOException {
        List<String> righe = new ArrayList<>();
        righe.add(HEADER);
        for (Proiezione proiezione : proiezioni) {
            Film film = proiezione.getFilm();
            righe.add(String.join(",",
                    quota(proiezione.getDataOra().format(FORMATO_FILE)),
                    quota(film.getTitolo()),
                    quota(film.getGenere()),
                    quota(film.getRegista()),
                    Integer.toString(film.getAnno()),
                    Integer.toString(film.getDurataMinuti()),
                    Integer.toString(film.getEtaMinima()),
                    Double.toString(proiezione.getPrezzo())
            ));
        }
        Files.write(file, righe);
    }

    private String quota(String valore) {
        return "\"" + valore.replace("\"", "\"\"") + "\"";
    }

    private String[] dividiCsv(String riga) {
        List<String> colonne = new ArrayList<>();
        StringBuilder valore = new StringBuilder();
        boolean dentroVirgolette = false;

        for (int i = 0; i < riga.length(); i++) {
            char carattere = riga.charAt(i);
            if (carattere == '"') {
                if (dentroVirgolette && i + 1 < riga.length()
                        && riga.charAt(i + 1) == '"') {
                    valore.append('"');
                    i++;
                } else {
                    dentroVirgolette = !dentroVirgolette;
                }
            } else if (carattere == ',' && !dentroVirgolette) {
                colonne.add(valore.toString());
                valore.setLength(0);
            } else {
                valore.append(carattere);
            }
        }
        colonne.add(valore.toString());
        return colonne.toArray(new String[0]);
    }
}
