/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.common.util;

/**
 * Eccezione applicativa (checked) sollevata dai servizi di TheKnife per
 * segnalare violazioni delle regole di business (es. credenziali errate,
 * email gia' registrata, operazione non autorizzata, dati non validi).
 * <p>
 * E' distinta dalle eccezioni tecniche (es. {@link java.sql.SQLException},
 * problemi di rete): queste ultime vengono intercettate lato server e
 * tradotte in un messaggio generico per non esporre dettagli implementativi
 * al client.
 */
public class TheKnifeException extends Exception {

    private static final long serialVersionUID = 1L;

    public TheKnifeException(String messaggio) {
        super(messaggio);
    }

    public TheKnifeException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
