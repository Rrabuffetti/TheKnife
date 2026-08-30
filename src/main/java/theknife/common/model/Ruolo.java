/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.common.model;

import java.io.Serializable;

/**
 * Ruolo di un {@link Utente} registrato sulla piattaforma TheKnife.
 * Specializzazione totale ed esclusiva: ogni utente e' o un cliente
 * o un ristoratore, mai entrambi ne' nessuno dei due.
 */
public enum Ruolo implements Serializable {
    CLIENTE,
    RISTORATORE
}
