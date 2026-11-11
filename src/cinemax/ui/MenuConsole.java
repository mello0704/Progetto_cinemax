package cinemax.ui;

import cinemax.model.Film;
import cinemax.model.Prenotazione;
import cinemax.model.Proiezione;
import cinemax.model.Ruolo;
import cinemax.model.Utente;
import cinemax.service.AuthenticationService;
import cinemax.service.PrenotazioneService;
import cinemax.service.ProiezioniService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Text-based menus for guests and the three registered user roles. */
public class MenuConsole {
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_ORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Scanner scanner = new Scanner(System.in);
    private final AuthenticationService auth;
    private final ProiezioniService proiezioniService =
            new ProiezioniService();
    private final PrenotazioneService prenotazioneService =
            new PrenotazioneService();
    private List<Proiezione> ultimeProiezioniTrovate = new ArrayList<>();

    public MenuConsole(AuthenticationService auth) {
        this.auth = auth;
    }

    public void avvia() {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n=== CINEMAX ===");
            System.out.println("1) Login");
            System.out.println("2) Registrati come cliente");
            System.out.println("3) Continua come guest");
            System.out.println("4) Esci");
            int scelta = leggiIntero("Scelta: ");

            switch (scelta) {
                case 1:
                    login();
                    break;
                case 2:
                    registrati();
                    break;
                case 3:
                    menuGuest();
                    break;
                case 4:
                    esci = true;
                    System.out.println("Arrivederci!");
                    break;
                default:
                    System.out.println("Scelta non valida.");
            }
        }
    }

    private void login() {
        String username = leggiTesto("Username: ");
        String password = leggiTesto("Password: ");
        Utente utente = auth.login(username, password);
        if (utente == null) {
            System.out.println("Credenziali errate.");
            return;
        }

        System.out.println("Benvenuto " + utente.getNome() + "!");
        switch (utente.getRuolo()) {
            case CLIENTE:
                menuCliente(utente);
                break;
            case PROIEZIONISTA:
                menuProiezionista();
                break;
            case BIGLIETTAIO:
                menuBigliettaio();
                break;
            default:
                System.out.println("Ruolo non riconosciuto.");
        }
    }

    private void registrati() {
        String nome = leggiTesto("Nome: ");
        String cognome = leggiTesto("Cognome: ");
        LocalDate nascita = leggiDataFacoltativa(
                "Data di nascita (gg/mm/aaaa, invio per saltare): "
        );
        String domicilio = leggiTesto("Domicilio: ");
        String username = leggiTesto("Username: ");
        String password = leggiTesto("Password: ");

        try {
            if (auth.registraCliente(nome, cognome, nascita, domicilio,
                    username, password)) {
                System.out.println("Registrazione completata.");
            } else {
                System.out.println("Username già in uso.");
            }
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Registrazione non riuscita: " + e.getMessage());
        }
    }

    private void menuGuest() {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n=== MENU GUEST ===");
            System.out.println("1) Cerca proiezioni");
            System.out.println("2) Visualizza dettagli di una proiezione trovata");
            System.out.println("3) Registrati");
            System.out.println("4) Torna al menu principale");
            switch (leggiIntero("Scelta: ")) {
                case 1:
                    ultimeProiezioniTrovate = ricercaProiezioni();
                    break;
                case 2:
                    visualizzaDettaglioSelezionato();
                    break;
                case 3:
                    registrati();
                    break;
                case 4:
                    esci = true;
                    break;
                default:
                    System.out.println("Scelta non valida.");
            }
        }
    }

    private void menuCliente(Utente cliente) {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n=== MENU CLIENTE ===");
            System.out.println("1) Cerca una proiezione e prenota");
            System.out.println("2) Visualizza le mie prenotazioni");
            System.out.println("3) Modifica la data di una prenotazione");
            System.out.println("4) Cancella una prenotazione");
            System.out.println("5) Logout");
            switch (leggiIntero("Scelta: ")) {
                case 1:
                    creaPrenotazione(cliente);
                    break;
                case 2:
                    visualizzaPrenotazioniCliente(cliente);
                    break;
                case 3:
                    modificaPrenotazione(cliente);
                    break;
                case 4:
                    eliminaPrenotazione(cliente);
                    break;
                case 5:
                    esci = true;
                    break;
                default:
                    System.out.println("Scelta non valida.");
            }
        }
    }

    private void menuProiezionista() {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n=== MENU PROIEZIONISTA ===");
            System.out.println("1) Inserisci film e proiezione");
            System.out.println("2) Modifica data e ora di una proiezione");
            System.out.println("3) Elimina una proiezione");
            System.out.println("4) Logout");
            switch (leggiIntero("Scelta: ")) {
                case 1:
                    aggiungiProiezione();
                    break;
                case 2:
                    modificaProiezione();
                    break;
                case 3:
                    eliminaProiezione();
                    break;
                case 4:
                    esci = true;
                    break;
                default:
                    System.out.println("Scelta non valida.");
            }
        }
    }

    private void menuBigliettaio() {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n=== MENU BIGLIETTAIO ===");
            System.out.println("1) Prenotazioni della giornata odierna");
            System.out.println("2) Cerca prenotazioni");
            System.out.println("3) Visualizza dettagli dell'ultimo risultato");
            System.out.println("4) Logout");
            switch (leggiIntero("Scelta: ")) {
                case 1:
                    cercaPrenotazioniOggi();
                    break;
                case 2:
                    cercaPrenotazioniBigliettaio();
                    break;
                case 3:
                    visualizzaDettaglioPrenotazione();
                    break;
                case 4:
                    esci = true;
                    break;
                default:
                    System.out.println("Scelta non valida.");
            }
        }
    }

    private List<Proiezione> ricercaProiezioni() {
        System.out.println("Inserisci uno o più filtri; invio per saltarli.");
        String titolo = leggiTesto("Titolo (anche parziale): ");
        String genere = leggiTesto("Genere: ");
        LocalDate dal = leggiDataFacoltativa("Data dal (gg/mm/aaaa): ");
        LocalDate al = leggiDataFacoltativa("Data al (gg/mm/aaaa): ");
        Double prezzoMin = leggiPrezzoFacoltativo("Prezzo minimo: ");
        Double prezzoMax = leggiPrezzoFacoltativo("Prezzo massimo: ");

        if (titolo.isBlank() && genere.isBlank() && dal == null && al == null
                && prezzoMin == null && prezzoMax == null) {
            System.out.println("Inserisci almeno un criterio di ricerca.");
            return new ArrayList<>();
        }
        if (dal != null && al != null && al.isBefore(dal)) {
            System.out.println("La data finale precede quella iniziale.");
            return new ArrayList<>();
        }
        if (prezzoMin != null && prezzoMax != null
                && prezzoMax < prezzoMin) {
            System.out.println("Il prezzo massimo è inferiore al minimo.");
            return new ArrayList<>();
        }

        try {
            return proiezioniService.cercaProiezioni(
                    titolo, genere, dal, al, prezzoMin, prezzoMax
            );
        } catch (IOException e) {
            System.out.println("Errore nella lettura delle proiezioni: "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void visualizzaDettaglioSelezionato() {
        Proiezione scelta = selezionaProiezione(ultimeProiezioniTrovate);
        if (scelta == null) {
            return;
        }
        try {
            proiezioniService.visualizzaDettagli(scelta);
        } catch (IOException e) {
            System.out.println("Impossibile leggere le prenotazioni: "
                    + e.getMessage());
        }
    }

    private void creaPrenotazione(Utente cliente) {
        List<Proiezione> risultati = ricercaProiezioni();
        Proiezione proiezione = selezionaProiezione(risultati);
        if (proiezione == null) {
            return;
        }
        int biglietti = leggiIntero("Numero di biglietti: ");
        try {
            Prenotazione prenotazione = prenotazioneService.crea(
                    cliente.getUsername(), proiezione, biglietti
            );
            System.out.println("Prenotazione creata. Codice: "
                    + prenotazione.getCodice());
            System.out.println("Totale: "
                    + String.format(Locale.ITALY, "%.2f €",
                    prenotazione.getTotale()));
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Prenotazione non riuscita: " + e.getMessage());
        }
    }

    private void visualizzaPrenotazioniCliente(Utente cliente) {
        try {
            List<Prenotazione> prenotazioni =
                    prenotazioneService.perCliente(cliente.getUsername());
            stampaPrenotazioni(prenotazioni);
        } catch (IOException e) {
            System.out.println("Impossibile leggere le prenotazioni: "
                    + e.getMessage());
        }
    }

    private void modificaPrenotazione(Utente cliente) {
        Prenotazione prenotazione = selezionaPrenotazioneCliente(cliente);
        if (prenotazione == null) {
            return;
        }
        System.out.println("Cerca la nuova proiezione:");
        List<Proiezione> risultati = ricercaProiezioni();
        Proiezione nuova = selezionaProiezione(risultati);
        if (nuova == null) {
            return;
        }
        try {
            prenotazioneService.modificaData(
                    prenotazione.getCodice(), cliente.getUsername(), nuova
            );
            System.out.println("Prenotazione modificata.");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Modifica non riuscita: " + e.getMessage());
        }
    }

    private void eliminaPrenotazione(Utente cliente) {
        Prenotazione prenotazione = selezionaPrenotazioneCliente(cliente);
        if (prenotazione == null) {
            return;
        }
        try {
            prenotazioneService.elimina(
                    prenotazione.getCodice(), cliente.getUsername()
            );
            System.out.println("Prenotazione cancellata.");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Cancellazione non riuscita: " + e.getMessage());
        }
    }

    private Prenotazione selezionaPrenotazioneCliente(Utente cliente) {
        try {
            List<Prenotazione> prenotazioni =
                    prenotazioneService.perCliente(cliente.getUsername());
            stampaPrenotazioni(prenotazioni);
            if (prenotazioni.isEmpty()) {
                return null;
            }
            int numero = leggiIntero("Numero della prenotazione: ");
            if (numero < 1 || numero > prenotazioni.size()) {
                System.out.println("Numero non valido.");
                return null;
            }
            return prenotazioni.get(numero - 1);
        } catch (IOException e) {
            System.out.println("Impossibile leggere le prenotazioni: "
                    + e.getMessage());
            return null;
        }
    }

    private void aggiungiProiezione() {
        String titolo = leggiTesto("Titolo del film: ");
        String genere = leggiTesto("Genere: ");
        String regista = leggiTesto("Regista: ");
        int anno = leggiIntero("Anno: ");
        int durata = leggiIntero("Durata in minuti: ");
        int eta = leggiIntero("Età minima: ");
        LocalDateTime dataOra = leggiDataOra("Data e ora (gg/mm/aaaa HH:mm): ");
        double prezzo = leggiPrezzo("Prezzo del biglietto: ");

        if (durata <= 0 || eta < 0 || anno < 1888 || anno > 3000) {
            System.out.println("I dati del film non sono validi.");
            return;
        }
        try {
            proiezioniService.aggiungiProiezione(
                    new Film(titolo, genere, regista, anno, durata, eta),
                    dataOra, prezzo
            );
            System.out.println("Proiezione aggiunta.");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Inserimento non riuscito: " + e.getMessage());
        }
    }

    private void modificaProiezione() {
        List<Proiezione> risultati = ricercaProiezioni();
        Proiezione scelta = selezionaProiezione(risultati);
        if (scelta == null) {
            return;
        }
        LocalDateTime nuovaData = leggiDataOra(
                "Nuova data e ora (gg/mm/aaaa HH:mm): "
        );
        try {
            proiezioniService.modificaData(scelta, nuovaData);
            System.out.println("Proiezione modificata.");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Modifica non riuscita: " + e.getMessage());
        }
    }

    private void eliminaProiezione() {
        List<Proiezione> risultati = ricercaProiezioni();
        Proiezione scelta = selezionaProiezione(risultati);
        if (scelta == null) {
            return;
        }
        try {
            proiezioniService.eliminaProiezione(scelta);
            System.out.println("Proiezione eliminata.");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Eliminazione non riuscita: " + e.getMessage());
        }
    }

    private void cercaPrenotazioniOggi() {
        try {
            List<Prenotazione> oggi = new ArrayList<>();
            for (Prenotazione prenotazione : prenotazioneService.tutte()) {
                if (prenotazione.getProiezione().getDataOra()
                        .toLocalDate().equals(LocalDate.now())) {
                    oggi.add(prenotazione);
                }
            }
            stampaPrenotazioni(oggi);
            ultimePrenotazioniTrovate = oggi;
        } catch (IOException e) {
            System.out.println("Impossibile leggere le prenotazioni: "
                    + e.getMessage());
        }
    }

    private List<Prenotazione> ultimePrenotazioniTrovate = new ArrayList<>();

    private void cercaPrenotazioniBigliettaio() {
        String codice = leggiTesto("Codice prenotazione (facoltativo): ");
        String nome = leggiTesto("Nome cliente (facoltativo): ");
        String cognome = leggiTesto("Cognome cliente (facoltativo): ");
        String titolo = leggiTesto("Titolo film (facoltativo): ");
        LocalDate dal = leggiDataFacoltativa("Data dal (facoltativa): ");
        LocalDate al = leggiDataFacoltativa("Data al (facoltativa): ");

        if (codice.isBlank() && nome.isBlank() && cognome.isBlank()
                && titolo.isBlank() && dal == null && al == null) {
            System.out.println("Inserisci almeno un criterio di ricerca.");
            return;
        }

        try {
            List<Prenotazione> risultati = new ArrayList<>();
            for (Prenotazione prenotazione : prenotazioneService.tutte()) {
                Utente cliente = auth.trovaUtente(prenotazione.getUsername());
                String titoloFilm = prenotazione.getProiezione().getFilm()
                        .getTitolo().toLowerCase(Locale.ROOT);
                LocalDate data = prenotazione.getProiezione().getDataOra()
                        .toLocalDate();

                boolean corrisponde =
                        (codice.isBlank() || prenotazione.getCodice()
                                .toLowerCase(Locale.ROOT)
                                .contains(codice.toLowerCase(Locale.ROOT)))
                        && (nome.isBlank() || (cliente != null
                                && cliente.getNome().toLowerCase(Locale.ROOT)
                                .contains(nome.toLowerCase(Locale.ROOT))))
                        && (cognome.isBlank() || (cliente != null
                                && cliente.getCognome().toLowerCase(Locale.ROOT)
                                .contains(cognome.toLowerCase(Locale.ROOT))))
                        && (titolo.isBlank() || titoloFilm.contains(
                                titolo.toLowerCase(Locale.ROOT)))
                        && (dal == null || !data.isBefore(dal))
                        && (al == null || !data.isAfter(al));
                if (corrisponde) {
                    risultati.add(prenotazione);
                }
            }
            ultimePrenotazioniTrovate = risultati;
            stampaPrenotazioni(risultati);
        } catch (IOException e) {
            System.out.println("Impossibile leggere le prenotazioni: "
                    + e.getMessage());
        }
    }

    private void visualizzaDettaglioPrenotazione() {
        if (ultimePrenotazioniTrovate.isEmpty()) {
            System.out.println("Prima cerca o visualizza delle prenotazioni.");
            return;
        }
        int numero = leggiIntero("Numero della prenotazione: ");
        if (numero < 1 || numero > ultimePrenotazioniTrovate.size()) {
            System.out.println("Numero non valido.");
            return;
        }
        Prenotazione prenotazione = ultimePrenotazioniTrovate.get(numero - 1);
        Utente cliente = auth.trovaUtente(prenotazione.getUsername());
        System.out.println("\n=== DETTAGLI PRENOTAZIONE ===");
        System.out.println("Codice: " + prenotazione.getCodice());
        System.out.println("Cliente: " + (cliente == null
                ? prenotazione.getUsername()
                : cliente.getNome() + " " + cliente.getCognome()));
        System.out.println("Proiezione: " + prenotazione.getProiezione());
        System.out.println("Biglietti: " + prenotazione.getNumBiglietti());
        System.out.printf("Prezzo unitario: %.2f €%n",
                prenotazione.getProiezione().getPrezzo());
        System.out.printf("Totale: %.2f €%n", prenotazione.getTotale());
    }

    private void stampaPrenotazioni(List<Prenotazione> prenotazioni) {
        if (prenotazioni.isEmpty()) {
            System.out.println("Nessuna prenotazione trovata.");
            return;
        }
        for (int i = 0; i < prenotazioni.size(); i++) {
            System.out.println((i + 1) + ") " + prenotazioni.get(i));
        }
    }

    private Proiezione selezionaProiezione(List<Proiezione> proiezioni) {
        if (proiezioni == null || proiezioni.isEmpty()) {
            System.out.println("Non ci sono proiezioni da selezionare.");
            return null;
        }
        int numero = leggiIntero("Numero della proiezione (0 per annullare): ");
        if (numero == 0) {
            return null;
        }
        if (numero < 1 || numero > proiezioni.size()) {
            System.out.println("Numero non valido.");
            return null;
        }
        return proiezioni.get(numero - 1);
    }

    private String leggiTesto(String richiesta) {
        System.out.print(richiesta);
        return scanner.nextLine().trim();
    }

    private int leggiIntero(String richiesta) {
        while (true) {
            String testo = leggiTesto(richiesta);
            try {
                return Integer.parseInt(testo);
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero intero.");
            }
        }
    }

    private double leggiPrezzo(String richiesta) {
        while (true) {
            String testo = leggiTesto(richiesta).replace(',', '.');
            try {
                double prezzo = Double.parseDouble(testo);
                if (prezzo < 0) {
                    System.out.println("Il prezzo non può essere negativo.");
                    continue;
                }
                return prezzo;
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un prezzo valido.");
            }
        }
    }

    private Double leggiPrezzoFacoltativo(String richiesta) {
        while (true) {
            String testo = leggiTesto(richiesta).replace(',', '.');
            if (testo.isBlank()) {
                return null;
            }
            try {
                double prezzo = Double.parseDouble(testo);
                if (prezzo < 0) {
                    System.out.println("Il prezzo non può essere negativo.");
                    continue;
                }
                return prezzo;
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un prezzo valido o invio.");
            }
        }
    }

    private LocalDate leggiDataFacoltativa(String richiesta) {
        while (true) {
            String testo = leggiTesto(richiesta);
            if (testo.isBlank()) {
                return null;
            }
            try {
                return LocalDate.parse(testo, FORMATO_DATA);
            } catch (DateTimeParseException e) {
                System.out.println("Usa il formato gg/mm/aaaa.");
            }
        }
    }

    private LocalDateTime leggiDataOra(String richiesta) {
        while (true) {
            String testo = leggiTesto(richiesta);
            try {
                return LocalDateTime.parse(testo, FORMATO_DATA_ORA);
            } catch (DateTimeParseException e) {
                System.out.println("Usa il formato gg/mm/aaaa HH:mm.");
            }
        }
    }
}
