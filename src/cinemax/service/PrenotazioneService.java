package cinemax.service;

import cinemax.model.Prenotazione;
import cinemax.model.Proiezione;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Implements client reservation rules. */
public class PrenotazioneService {
    private static final int POSTI_SALA = 200;
    private final PrenotazioneRepository repository =
            new PrenotazioneRepository();
    private final ProiezioneRepository proiezioneRepository =
            new ProiezioneRepository();

    /** Creates and stores a reservation after checking the seat limit. */
    public Prenotazione crea(String username, Proiezione proiezione,
                             int numBiglietti) throws IOException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Cliente non valido.");
        }
        if (numBiglietti <= 0) {
            throw new IllegalArgumentException(
                    "Il numero di biglietti deve essere positivo."
            );
        }
        if (!proiezione.getDataOra().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Non puoi prenotare una proiezione già iniziata."
            );
        }

        int disponibili = POSTI_SALA - repository.contaBiglietti(proiezione);
        if (numBiglietti > disponibili) {
            throw new IllegalArgumentException(
                    "Posti disponibili: " + Math.max(0, disponibili)
            );
        }

        Prenotazione prenotazione = new Prenotazione(
                UUID.randomUUID().toString(), username,
                numBiglietti, proiezione
        );
        repository.salva(prenotazione);
        return prenotazione;
    }

    /** Returns every stored reservation. */
    public List<Prenotazione> tutte() throws IOException {
        return repository.caricaTutte(proiezioneRepository.caricaTutte());
    }

    /** Returns reservations owned by a customer. */
    public List<Prenotazione> perCliente(String username) throws IOException {
        List<Prenotazione> risultato = new ArrayList<>();
        for (Prenotazione prenotazione : tutte()) {
            if (prenotazione.getUsername().equalsIgnoreCase(username)) {
                risultato.add(prenotazione);
            }
        }
        return risultato;
    }

    /** Finds one reservation by its code, or returns null. */
    public Prenotazione cercaPerCodice(String codice) throws IOException {
        for (Prenotazione prenotazione : tutte()) {
            if (prenotazione.getCodice().equalsIgnoreCase(codice)) {
                return prenotazione;
            }
        }
        return null;
    }

    /** Moves a customer's reservation to another future screening. */
    public void modificaData(String codice, String username,
                             Proiezione nuovaProiezione) throws IOException {
        List<Proiezione> proiezioni = proiezioneRepository.caricaTutte();
        List<Prenotazione> prenotazioni = repository.caricaTutte(proiezioni);
        Prenotazione prenotazione = trovaPosseduta(prenotazioni, codice, username);

        LocalDateTime adesso = LocalDateTime.now();
        if (!prenotazione.getProiezione().getDataOra().isAfter(adesso)
                || !nuovaProiezione.getDataOra().isAfter(adesso)) {
            throw new IllegalArgumentException(
                    "La vecchia e la nuova proiezione devono essere future."
            );
        }

        boolean stessa = stessaProiezione(
                prenotazione.getProiezione(), nuovaProiezione
        );
        int occupati = repository.contaBiglietti(nuovaProiezione);
        if (stessa) {
            occupati -= prenotazione.getNumBiglietti();
        }
        if (occupati + prenotazione.getNumBiglietti() > POSTI_SALA) {
            throw new IllegalArgumentException(
                    "Non ci sono abbastanza posti nella nuova proiezione."
            );
        }

        prenotazione.setProiezione(nuovaProiezione);
        repository.salvaTutte(prenotazioni);
    }

    /** Deletes an owned reservation when the stated specification permits it. */
    public void elimina(String codice, String username) throws IOException {
        List<Prenotazione> prenotazioni = tutte();
        Prenotazione prenotazione = trovaPosseduta(prenotazioni, codice, username);

        // The supplied specification says cancellation is allowed for past shows.
        if (!prenotazione.getProiezione().getDataOra().toLocalDate()
                .isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La specifica consente la cancellazione solo per proiezioni passate."
            );
        }

        prenotazioni.remove(prenotazione);
        repository.salvaTutte(prenotazioni);
    }

    private Prenotazione trovaPosseduta(List<Prenotazione> prenotazioni,
                                        String codice, String username) {
        for (Prenotazione prenotazione : prenotazioni) {
            if (prenotazione.getCodice().equalsIgnoreCase(codice)
                    && prenotazione.getUsername().equalsIgnoreCase(username)) {
                return prenotazione;
            }
        }
        throw new IllegalArgumentException(
                "Prenotazione non trovata o non appartenente al cliente."
        );
    }

    private boolean stessaProiezione(Proiezione prima, Proiezione seconda) {
        return prima.getFilm().getTitolo().equals(seconda.getFilm().getTitolo())
                && prima.getDataOra().equals(seconda.getDataOra());
    }
}
