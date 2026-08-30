/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Alias italiano/nome originale per le principali citta' e nazioni del mondo.
 * <p>
 * Il dataset Michelin importato da {@link theknife.server.ImportCSV} riporta
 * citta' e nazioni nella lingua originale della guida (perlopiu' inglese, es.
 * "Milan", "Italy"), mentre un utente italiano cerca naturalmente col nome
 * italiano (es. "Milano", "Italia"). Senza una traduzione, la ricerca
 * (basata su corrispondenza parziale, {@code ILIKE '%luogo%'}) non trova
 * questi ristoranti a meno di usare esattamente il nome del dataset.
 * <p>
 * Questa classe non modifica i dati salvati: {@link #varianti(String)}
 * restituisce, dato un luogo digitato dall'utente, l'elenco dei nomi
 * equivalenti noti (incluso l'originale) da provare in ricerca, cosi' che
 * "Milano" trovi anche i ristoranti salvati come "Milan" e viceversa.
 */
final class AliasLuogo {

    private static final Map<String, String> ITALIANO_A_ORIGINALE = Map.ofEntries(
            // Citta' italiane piu' comuni nella guida Michelin
            Map.entry("milano", "milan"),
            Map.entry("roma", "rome"),
            Map.entry("napoli", "naples"),
            Map.entry("torino", "turin"),
            Map.entry("firenze", "florence"),
            Map.entry("venezia", "venice"),
            Map.entry("genova", "genoa"),
            Map.entry("padova", "padua"),
            Map.entry("siracusa", "syracuse"),
            // Altre citta' internazionali comuni
            Map.entry("londra", "london"),
            Map.entry("parigi", "paris"),
            Map.entry("bruxelles", "brussels"),
            Map.entry("praga", "prague"),
            Map.entry("varsavia", "warsaw"),
            Map.entry("mosca", "moscow"),
            Map.entry("atene", "athens"),
            Map.entry("lisbona", "lisbon"),
            Map.entry("barcellona", "barcelona"),
            Map.entry("copenaghen", "copenhagen"),
            Map.entry("stoccolma", "stockholm"),
            Map.entry("zurigo", "zurich"),
            Map.entry("ginevra", "geneva"),
            Map.entry("monaco di baviera", "munich"),
            // Nazioni
            Map.entry("italia", "italy"),
            Map.entry("francia", "france"),
            Map.entry("germania", "germany"),
            Map.entry("spagna", "spain"),
            Map.entry("regno unito", "united kingdom"),
            Map.entry("stati uniti", "united states"),
            Map.entry("giappone", "japan"),
            Map.entry("cina", "china"),
            Map.entry("svizzera", "switzerland"),
            Map.entry("belgio", "belgium"),
            Map.entry("paesi bassi", "netherlands"),
            Map.entry("olanda", "netherlands"),
            Map.entry("portogallo", "portugal"),
            Map.entry("grecia", "greece"),
            Map.entry("polonia", "poland"),
            Map.entry("svezia", "sweden"),
            Map.entry("danimarca", "denmark"),
            Map.entry("norvegia", "norway"),
            Map.entry("irlanda", "ireland"),
            Map.entry("brasile", "brazil"),
            Map.entry("messico", "mexico"),
            Map.entry("corea del sud", "south korea"),
            Map.entry("thailandia", "thailand"));

    private static final Map<String, String> ORIGINALE_A_ITALIANO = invert(ITALIANO_A_ORIGINALE);

    private AliasLuogo() {
    }

    private static Map<String, String> invert(Map<String, String> mappa) {
        Map<String, String> invertita = new java.util.HashMap<>();
        mappa.forEach((chiave, valore) -> invertita.put(valore, chiave));
        return invertita;
    }

    /**
     * @return il luogo digitato dall'utente, seguito dagli eventuali nomi
     *         equivalenti noti (mai vuoto, mai con duplicati).
     */
    static List<String> varianti(String luogo) {
        List<String> risultato = new ArrayList<>();
        if (luogo == null || luogo.isBlank()) {
            return risultato;
        }
        String pulito = luogo.trim();
        risultato.add(pulito);

        String chiave = pulito.toLowerCase();
        String alias = ITALIANO_A_ORIGINALE.get(chiave);
        if (alias == null) {
            alias = ORIGINALE_A_ITALIANO.get(chiave);
        }
        if (alias != null && !alias.equalsIgnoreCase(pulito)) {
            risultato.add(alias);
        }
        return risultato;
    }
}
