/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client;

import java.time.format.DateTimeFormatter;

import theknife.common.model.Ristorante;

/**
 * Funzioni di formattazione condivise tra i vari controller della UI.
 */
public final class Formattatori {

    public static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formattatori() {
    }

    public static String stelle(double media) {
        int piene = (int) Math.round(media);
        return "★".repeat(Math.max(0, Math.min(5, piene))) + "☆".repeat(Math.max(0, 5 - piene));
    }

    public static String prezzo(java.math.BigDecimal fasciaPrezzo) {
        return fasciaPrezzo == null ? "-" : String.format("%.0f €", fasciaPrezzo);
    }

    public static String rigaRistorante(Ristorante r) {
        return String.format("%s - %s, %s - %s - %s (%d recensioni)",
                r.getNome(), r.getCitta(), r.getNazione(), prezzo(r.getFasciaPrezzo()),
                stelle(r.getMediaStelle()), r.getNumeroRecensioni());
    }
}
