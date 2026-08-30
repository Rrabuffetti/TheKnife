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
 * Elenco delle operazioni che il {@code clientTK} puo' richiedere al
 * {@code serverTK}. Ogni valore corrisponde a un metodo dell'interfaccia
 * {@link theknife.common.ServiziTK} ed e' usato come discriminante nel
 * dispatching lato server (design pattern Command "leggero": la
 * {@link Richiesta} incapsula l'operazione da eseguire e i suoi
 * parametri, il server la esegue senza conoscere i dettagli del
 * chiamante).
 */
public enum Operazione implements Serializable {
    LOGIN,
    REGISTRAZIONE,
    CERCA_RISTORANTI,
    VISUALIZZA_RISTORANTE,
    VISUALIZZA_RECENSIONI,
    AGGIUNGI_PREFERITO,
    RIMUOVI_PREFERITO,
    VISUALIZZA_PREFERITI,
    AGGIUNGI_RECENSIONE,
    MODIFICA_RECENSIONE,
    ELIMINA_RECENSIONE,
    VISUALIZZA_RECENSIONI_PROPRIE,
    AGGIUNGI_RISTORANTE,
    VISUALIZZA_RIEPILOGO,
    RISPOSTA_RECENSIONE,
    ELENCO_TIPI_CUCINA,
    ELENCO_CITTA_PER_NAZIONE
}
