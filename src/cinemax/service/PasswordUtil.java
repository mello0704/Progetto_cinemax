package cinemax.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/** Utilities for storing passwords as salted PBKDF2 hashes. */
public final class PasswordUtil {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return "PBKDF2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derived);
    }

    public static boolean isHash(String value) {
        return value != null && value.startsWith("PBKDF2$");
    }

    public static boolean verify(String password, String stored) {
        if (!isHash(stored)) {
            return false;
        }
        try {
            String[] parts = stored.split("\\$", -1);
            if (parts.length != 4) {
                return false;
            }
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password, salt, iterations);
            if (actual.length != expected.length) {
                return false;
            }
            int difference = 0;
            for (int i = 0; i < expected.length; i++) {
                difference |= expected[i] ^ actual[i];
            }
            return difference == 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec specification = new PBEKeySpec(
                password.toCharArray(), salt, iterations, KEY_LENGTH
        );
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(specification)
                    .getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Impossibile cifrare la password.", e);
        } finally {
            specification.clearPassword();
        }
    }
}
