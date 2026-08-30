/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client;

import theknife.common.ServiziTK;
import theknife.common.model.Cliente;
import theknife.common.model.Ristoratore;
import theknife.common.model.Utente;

/**
 * Stato dell'applicazione client: la connessione ai servizi (Proxy verso
 * il server) e l'utente eventualmente autenticato sulla sessione
 * corrente. Un'unica finestra/istanza di clientTK ha un solo utente
 * connesso alla volta, per cui un holder statico e' una semplificazione
 * ragionevole rispetto a un contesto iniettato esplicitamente in ogni
 * controller.
 */
public final class SessioneClient {

    private static ServiziTK servizi;
    private static Utente utenteCorrente;

    private SessioneClient() {
    }

    public static void setServizi(ServiziTK s) {
        servizi = s;
    }

    public static ServiziTK getServizi() {
        return servizi;
    }

    public static void impostaUtente(Utente u) {
        utenteCorrente = u;
    }

    public static void logout() {
        utenteCorrente = null;
    }

    public static Utente getUtenteCorrente() {
        return utenteCorrente;
    }

    public static boolean isAutenticato() {
        return utenteCorrente != null;
    }

    public static boolean isCliente() {
        return utenteCorrente instanceof Cliente;
    }

    public static boolean isRistoratore() {
        return utenteCorrente instanceof Ristoratore;
    }
}
