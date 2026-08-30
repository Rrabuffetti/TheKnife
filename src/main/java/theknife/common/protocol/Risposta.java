/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.common.protocol;

import java.io.Serializable;

/**
 * Messaggio di risposta inviato dal server al client in seguito a una
 * {@link Richiesta}. In caso di successo {@link #dato} contiene il
 * risultato dell'operazione (puo' essere {@code null} per operazioni
 * senza valore di ritorno); in caso di fallimento {@link #successo} e'
 * {@code false} e {@link #messaggioErrore} descrive il motivo (regola di
 * business violata o errore tecnico generico, mai lo stack trace).
 */
public class Risposta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean successo;
    private final String messaggioErrore;
    private final Object dato;

    private Risposta(boolean successo, String messaggioErrore, Object dato) {
        this.successo = successo;
        this.messaggioErrore = messaggioErrore;
        this.dato = dato;
    }

    public static Risposta ok(Object dato) {
        return new Risposta(true, null, dato);
    }

    public static Risposta ok() {
        return new Risposta(true, null, null);
    }

    public static Risposta errore(String messaggio) {
        return new Risposta(false, messaggio, null);
    }

    public boolean isSuccesso() {
        return successo;
    }

    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    public Object getDato() {
        return dato;
    }
}
