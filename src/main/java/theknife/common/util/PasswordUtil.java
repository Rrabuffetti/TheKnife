/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.common.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility per la cifratura e la verifica delle password degli utenti,
 * basata su BCrypt invece di un semplice hash SHA-256: il salt integrato
 * e il costo configurabile rendono impraticabili gli attacchi a forza
 * bruta. La password in chiaro non viene mai salvata su DB.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /** Calcola l'hash cifrato della password in chiaro, da salvare su DB. */
    public static String hash(String passwordChiaro) {
        return BCrypt.hashpw(passwordChiaro, BCrypt.gensalt());
    }

    /** Verifica che la password in chiaro corrisponda all'hash memorizzato. */
    public static boolean verifica(String passwordChiaro, String hash) {
        return BCrypt.checkpw(passwordChiaro, hash);
    }
}
