/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.server;

import java.io.Console;
import java.util.Scanner;

import theknife.server.dao.ConfigurazioneDB;

/**
 * Funzioni di utilita' per richiedere via console i parametri di
 * connessione al database, condivise da {@link ServerTK} e dal tool di
 * importazione dati {@code theknife.server.ImportCSV}.
 */
public final class ConsoleUtil {

    private ConsoleUtil() {
    }

    public static ConfigurazioneDB chiediConfigurazioneDB(Scanner scanner) {
        Console console = System.console();

        String host = leggiConDefault(scanner, "Host del database", "localhost");
        int porta = Integer.parseInt(leggiConDefault(scanner, "Porta del database", "5432"));
        String nomeDb = leggiConDefault(scanner, "Nome del database", "dbtk");
        String utente = leggiConDefault(scanner, "Utente del database", "postgres");

        String password;
        if (console != null) {
            char[] pwd = console.readPassword("Password del database: ");
            password = new String(pwd);
        } else {
            System.out.print("Password del database: ");
            password = scanner.nextLine();
        }

        return new ConfigurazioneDB(host, porta, nomeDb, utente, password);
    }

    public static String leggiConDefault(Scanner scanner, String etichetta, String valoreDefault) {
        System.out.print(etichetta + " [" + valoreDefault + "]: ");
        String valore = scanner.nextLine().trim();
        return valore.isEmpty() ? valoreDefault : valore;
    }
}
