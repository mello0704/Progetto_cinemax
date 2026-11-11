package cinemax.service;

import cinemax.model.Ruolo;
import cinemax.model.Utente;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Handles sign-in, registration, and persistent user records. */
public class AuthenticationService {
    private static final String HEADER =
            "nome;cognome;username;password;dataNascita;domicilio;ruolo";
    private final Path file = Path.of("data", "Utenti.csv");
    private final List<Utente> utenti = new ArrayList<>();

    /** Loads users and upgrades legacy clear-text passwords to salted hashes. */
    public AuthenticationService() throws IOException {
        caricaUtenti();
    }

    private void caricaUtenti() throws IOException {
        List<String> righe = Files.readAllLines(file);
        boolean passwordDaMigrare = false;

        for (int i = 1; i < righe.size(); i++) {
            if (righe.get(i).isBlank()) {
                continue;
            }
            String[] colonne = righe.get(i).split(";", -1);
            if (colonne.length != 7) {
                throw new IOException("Riga utenti non valida: " + (i + 1));
            }

            String password = colonne[3];
            if (!PasswordUtil.isHash(password)) {
                password = PasswordUtil.hash(password);
                passwordDaMigrare = true;
            }

            LocalDate nascita = colonne[4].isBlank()
                    ? null : LocalDate.parse(colonne[4]);
            utenti.add(new Utente(
                    colonne[0], colonne[1], colonne[2], password,
                    nascita, colonne[5], Ruolo.valueOf(colonne[6])
            ));
        }

        if (passwordDaMigrare) {
            salvaUtenti();
        }
    }

    /** Authenticates a user, returning {@code null} when credentials do not match. */
    public Utente login(String username, String password) {
        for (Utente utente : utenti) {
            if (utente.getUsername().equalsIgnoreCase(username)
                    && PasswordUtil.verify(password, utente.getPassword())) {
                return utente;
            }
        }
        return null;
    }

    /** Registers a customer; returns false if the username is already in use. */
    public boolean registraCliente(String nome, String cognome, LocalDate data,
                                   String domicilio, String username,
                                   String password) throws IOException {
        if (nome == null || nome.isBlank() || cognome == null || cognome.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Nome, cognome, username e password sono obbligatori."
            );
        }
        if (trovaUtente(username) != null) {
            return false;
        }

        Utente nuovo = new Utente(
                nome.trim(), cognome.trim(), username.trim(),
                PasswordUtil.hash(password), data,
                domicilio == null ? "" : domicilio.trim(), Ruolo.CLIENTE
        );
        utenti.add(nuovo);
        salvaUtenti();
        return true;
    }

    /** Finds a user by username, ignoring letter case. */
    public Utente trovaUtente(String username) {
        for (Utente utente : utenti) {
            if (utente.getUsername().equalsIgnoreCase(username)) {
                return utente;
            }
        }
        return null;
    }

    private void salvaUtenti() throws IOException {
        List<String> righe = new ArrayList<>();
        righe.add(HEADER);
        for (Utente utente : utenti) {
            righe.add(String.join(";",
                    utente.getNome(),
                    utente.getCognome(),
                    utente.getUsername(),
                    utente.getPassword(),
                    utente.getDataNascita() == null
                            ? "" : utente.getDataNascita().toString(),
                    utente.getDomicilio(),
                    utente.getRuolo().name()
            ));
        }
        Files.write(file, righe);
    }
}
