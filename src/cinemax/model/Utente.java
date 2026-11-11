package cinemax.model;

import java.time.LocalDate;

/** A registered CineMax user and their role. */
public class Utente {

    private final String nome;
    private final String cognome;
    private final String username;
    private final String password;
    private final String domicilio;
    private final Ruolo ruolo;
    private final LocalDate dataNascita;

    /** Creates a user record. The password field stores a PBKDF2 hash. */
    public Utente(String nome, String cognome, String username, String password, LocalDate dataNascita, String domicilio, Ruolo ruolo) {

        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.password = password;
        this.dataNascita = dataNascita;
        this.domicilio = domicilio;
        this.ruolo = ruolo;
    }


    /** Returns the user's first name. */
    public String getNome() {

        return nome;
    }

    /** Returns the user's last name. */
    public String getCognome() {
        return cognome;
    }

    /** Returns the unique username. */
    public String getUsername() {
        return username;
    }

    /** Returns the stored password hash. */
    public String getPassword() {
        return password;
    }


    /** Returns the user's domicile. */
    public String getDomicilio() {
        return domicilio;
    }

    /** Returns the role assigned to the user. */
    public Ruolo getRuolo() {
        return ruolo;
    }

    /** Returns the optional date of birth. */
    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public boolean passwordCorretta(String tentativo) {
        return password.equals(tentativo);
    }

    /** Returns a human-readable user summary without exposing the password. */
    @Override
    public String toString() {
        return nome + " " + cognome + " (" + username + ", " + ruolo + ")";
    }
}
