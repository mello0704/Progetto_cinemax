package cinemax;
import cinemax.service.AuthenticationService;
import cinemax.ui.MenuConsole;

import java.io.IOException;


/** Application entry point. */
public class Cinemax {
    /** Starts the console application. */
    public static void main(String[] args) throws IOException {

        AuthenticationService auth = new AuthenticationService();

        MenuConsole menu = new MenuConsole(auth);
        menu.avvia();






    }
}

