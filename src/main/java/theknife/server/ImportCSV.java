/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.server;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import theknife.common.util.PasswordUtil;
import theknife.server.dao.ConfigurazioneDB;

/**
 * Strumento standalone per importare il file draft {@code michelin_my_maps.csv}
 * (fornito dal docente) nella tabella {@code ristoranti} del database dbTK,
 * come dati iniziali di popolamento. Non e' distribuito da TheKnife perche'
 * ogni ristorante deve appartenere a un ristoratore registrato: i record
 * importati vengono quindi assegnati a un account "seed" dedicato
 * ({@code import@theknife.local}), creato automaticamente al primo avvio.
 * <p>
 * Il formato Michelin non coincide con lo schema TheKnife, quindi alcuni dati
 * vengono adattati in fase di import (dettagli anche nel Manuale Tecnico).
 * Nazione e citta' si ricavano dalla colonna {@code Location} ("Citta',
 * Nazione"); per le poche citta'-stato senza virgola (Singapore, Hong Kong...)
 * coincidono. La colonna {@code Price} non e' un importo ma un simbolo di
 * valuta ripetuto 1-4 volte: senza un cambio storico affidabile lo si
 * traduce in una fascia di prezzo convenzionale in euro (25/45/80/150 EUR).
 * Delivery e prenotazione online non sono nel dataset Michelin e vengono
 * quindi importati a "no" (un ristoratore reale li corregge lui stesso con
 * {@code aggiungiRistorante()}). Infine {@code Cuisine} e' in inglese e
 * contiene anche qualche valore spurio (es. "604-0941", codici postali
 * finiti li' per un errore nel dataset originale): {@link #CUCINE_DA_SCARTARE}
 * li esclude, {@link #TRADUZIONE_CUCINA} traduce gli altri in italiano (i
 * valori non mappati vengono importati cosi' come sono).
 */
public final class ImportCSV {

    private static final String EMAIL_UTENTE_SEED = "import@theknife.local";

    private static final Set<String> CUCINE_DA_SCARTARE = Set.of(
            "406 Kameyacho", "604-0941", "Japan", "Nakagyo-ku", "Kyoto");

    private static final Map<String, String> TRADUZIONE_CUCINA = Map.ofEntries(
            Map.entry("Afghan", "Afghana"),
            Map.entry("African", "Africana"),
            Map.entry("Alpine", "Alpina"),
            Map.entry("Alsatian", "Alsaziana"),
            Map.entry("American", "Americana"),
            Map.entry("American Contemporary", "Americana contemporanea"),
            Map.entry("Andalusian", "Andalusa"),
            Map.entry("Apulian", "Pugliese"),
            Map.entry("Argentinian", "Argentina"),
            Map.entry("Armenian", "Armena"),
            Map.entry("Asian", "Asiatica"),
            Map.entry("Asian and Western", "Asiatica e occidentale"),
            Map.entry("Asian Contemporary", "Asiatica contemporanea"),
            Map.entry("Asian Influences", "Influenze asiatiche"),
            Map.entry("Asturian", "Asturiana"),
            Map.entry("Australian Contemporary", "Australiana contemporanea"),
            Map.entry("Austrian", "Austriaca"),
            Map.entry("Bakery", "Panetteria"),
            Map.entry("Balinese", "Balinese"),
            Map.entry("Balkan", "Balcanica"),
            Map.entry("Barbecue", "Barbecue"),
            Map.entry("Basque", "Basca"),
            Map.entry("Bavarian", "Bavarese"),
            Map.entry("Beef", "Carne di manzo"),
            Map.entry("Beijing Cuisine", "Cucina di Pechino"),
            Map.entry("Belgian", "Belga"),
            Map.entry("Brazilian", "Brasiliana"),
            Map.entry("Breton", "Bretone"),
            Map.entry("British Contemporary", "Britannica contemporanea"),
            Map.entry("Bulgogi", "Bulgogi (piatto coreano)"),
            Map.entry("Burgundian", "Borgognona"),
            Map.entry("Burmese", "Birmana"),
            Map.entry("Cajun", "Cajun"),
            Map.entry("Calabrian", "Calabrese"),
            Map.entry("Californian", "Californiana"),
            Map.entry("Cambodian", "Cambogiana"),
            Map.entry("Campanian", "Campana"),
            Map.entry("Cantonese", "Cantonese"),
            Map.entry("Cantonese Roast Meats", "Arrosti cantonesi"),
            Map.entry("Caribbean", "Caraibica"),
            Map.entry("Castilian", "Castigliana"),
            Map.entry("Catalan", "Catalana"),
            Map.entry("Central Asian", "Asia centrale"),
            Map.entry("Chao Zhou", "Cucina di Chaozhou"),
            Map.entry("Cheese", "Formaggi"),
            Map.entry("Chicken Specialities", "Specialita' di pollo"),
            Map.entry("Chinese", "Cinese"),
            Map.entry("Chinese Contemporary", "Cinese contemporanea"),
            Map.entry("Chiu Chow", "Cucina Chiu Chow"),
            Map.entry("Chueotang", "Chueotang (piatto coreano)"),
            Map.entry("Classic Cuisine", "Cucina classica"),
            Map.entry("Classic French", "Francese classica"),
            Map.entry("Colombian", "Colombiana"),
            Map.entry("Congee", "Congee (crema di riso)"),
            Map.entry("Contemporary", "Contemporanea"),
            Map.entry("Corsican", "Corsa"),
            Map.entry("Country cooking", "Cucina rustica"),
            Map.entry("Crab Specialities", "Specialita' di granchio"),
            Map.entry("Creative", "Creativa"),
            Map.entry("Creative British", "Britannica creativa"),
            Map.entry("Creative French", "Francese creativa"),
            Map.entry("Creole", "Creola"),
            Map.entry("Croatian", "Croata"),
            Map.entry("Cuban", "Cubana"),
            Map.entry("Cuisine from Abruzzo", "Abruzzese"),
            Map.entry("Cuisine from Basilicata", "Lucana (Basilicata)"),
            Map.entry("Cuisine from Franche-Comté", "Della Franca Contea"),
            Map.entry("Cuisine from Lazio", "Laziale"),
            Map.entry("Cuisine from Romagna", "Romagnola"),
            Map.entry("Cuisine from South West France", "Del sud-ovest della Francia"),
            Map.entry("Cuisine from the Aosta Valley", "Valdostana"),
            Map.entry("Cuisine from the Marches", "Marchigiana"),
            Map.entry("Cuisine from Valtellina", "Valtellinese"),
            Map.entry("Curry", "Curry"),
            Map.entry("Czech", "Ceca"),
            Map.entry("Danish", "Danese"),
            Map.entry("Deli", "Gastronomia"),
            Map.entry("Dim Sum", "Dim Sum"),
            Map.entry("Doganitang", "Doganitang (piatto coreano)"),
            Map.entry("Dongbei", "Cucina del Dongbei"),
            Map.entry("Dubu", "Dubu (piatto coreano)"),
            Map.entry("Duck Specialities", "Specialita' di anatra"),
            Map.entry("Dumplings", "Ravioli"),
            Map.entry("Dwaeji-gukbap", "Dwaeji-gukbap (piatto coreano)"),
            Map.entry("Eastern European", "Est Europa"),
            Map.entry("Egyptian", "Egiziana"),
            Map.entry("Emilian", "Emiliana"),
            Map.entry("Emirati Cuisine", "Emiratina"),
            Map.entry("English", "Inglese"),
            Map.entry("Ethiopian", "Etiope"),
            Map.entry("European", "Europea"),
            Map.entry("European Contemporary", "Europea contemporanea"),
            Map.entry("Farm to table", "Dalla fattoria alla tavola"),
            Map.entry("Filipino", "Filippina"),
            Map.entry("Finnish", "Finlandese"),
            Map.entry("Fish and Chips", "Fish and chips"),
            Map.entry("Flemish", "Fiamminga"),
            Map.entry("Fondue and Raclette", "Fonduta e raclette"),
            Map.entry("French", "Francese"),
            Map.entry("French Contemporary", "Francese contemporanea"),
            Map.entry("Friulian", "Friulana"),
            Map.entry("Fugu / Pufferfish", "Fugu (pesce palla)"),
            Map.entry("Fujian", "Cucina del Fujian"),
            Map.entry("Fusion", "Fusion"),
            Map.entry("Galician", "Galiziana"),
            Map.entry("Gastropub", "Gastropub"),
            Map.entry("Gejang", "Gejang (piatto coreano)"),
            Map.entry("German", "Tedesca"),
            Map.entry("Gomtang", "Gomtang (piatto coreano)"),
            Map.entry("Greek", "Greca"),
            Map.entry("Grills", "Grigliate"),
            Map.entry("Hainanese", "Cucina di Hainan"),
            Map.entry("Hakkanese", "Cucina Hakka"),
            Map.entry("Hang Zhou", "Cucina di Hangzhou"),
            Map.entry("Home Cooking", "Cucina casalinga"),
            Map.entry("Hotpot", "Hot pot (fonduta asiatica)"),
            Map.entry("Huaiyang", "Cucina Huaiyang"),
            Map.entry("Hubei", "Cucina dell'Hubei"),
            Map.entry("Hui Cuisine", "Cucina Hui"),
            Map.entry("Hunanese", "Cucina dello Hunan"),
            Map.entry("Hunanese and Sichuan", "Hunan e Sichuan"),
            Map.entry("Hungarian", "Ungherese"),
            Map.entry("Indian", "Indiana"),
            Map.entry("Indian Vegetarian", "Indiana vegetariana"),
            Map.entry("Indonesian", "Indonesiana"),
            Map.entry("Innovative", "Innovativa"),
            Map.entry("International", "Internazionale"),
            Map.entry("Irish", "Irlandese"),
            Map.entry("Isan", "Cucina Isan (Thailandia)"),
            Map.entry("Israeli", "Israeliana"),
            Map.entry("Italian", "Italiana"),
            Map.entry("Italian-American", "Italo-americana"),
            Map.entry("Italian and Japanese", "Italiana e giapponese"),
            Map.entry("Italian Contemporary", "Italiana contemporanea"),
            Map.entry("Izakaya", "Izakaya"),
            Map.entry("Jamaican", "Giamaicana"),
            Map.entry("Japanese", "Giapponese"),
            Map.entry("Japanese Contemporary", "Giapponese contemporanea"),
            Map.entry("Japanese Steakhouse", "Steakhouse giapponese"),
            Map.entry("Jiangzhe", "Cucina Jiangzhe"),
            Map.entry("Jokbal", "Jokbal (piatto coreano)"),
            Map.entry("Kalguksu", "Kalguksu (piatto coreano)"),
            Map.entry("Korean", "Coreana"),
            Map.entry("Korean Contemporary", "Coreana contemporanea"),
            Map.entry("Kushiage", "Kushiage (spiedini fritti)"),
            Map.entry("Lamb Specialities", "Specialita' di agnello"),
            Map.entry("Lao", "Laotiana"),
            Map.entry("Latin American", "Latino-americana"),
            Map.entry("Lebanese", "Libanese"),
            Map.entry("Ligurian", "Ligure"),
            Map.entry("Lombardian", "Lombarda"),
            Map.entry("Lyonnaise", "Lionese"),
            Map.entry("Macanese", "Macanese"),
            Map.entry("Malaysian", "Malese"),
            Map.entry("Mandu", "Mandu (piatto coreano)"),
            Map.entry("Mantuan", "Mantovana"),
            Map.entry("Meats and Grills", "Carni e grigliate"),
            Map.entry("Meats and Seafood", "Carne e pesce"),
            Map.entry("Mediterranean Cuisine", "Mediterranea"),
            Map.entry("Memil-guksu", "Memil-guksu (piatto coreano)"),
            Map.entry("Mexican", "Messicana"),
            Map.entry("Middle Eastern", "Mediorientale"),
            Map.entry("Milanese", "Milanese"),
            Map.entry("Modern British", "Britannica moderna"),
            Map.entry("Modern Cuisine", "Cucina moderna"),
            Map.entry("Modern French", "Francese moderna"),
            Map.entry("Moroccan", "Marocchina"),
            Map.entry("Naengmyeon", "Naengmyeon (piatto coreano)"),
            Map.entry("Nepali", "Nepalese"),
            Map.entry("Ningbo", "Cucina di Ningbo"),
            Map.entry("Noodles", "Tagliatelle"),
            Map.entry("Noodles and Congee", "Tagliatelle e congee"),
            Map.entry("North African", "Nord Africana"),
            Map.entry("North American", "Nord Americana"),
            Map.entry("Northern Thai", "Thailandese del nord"),
            Map.entry("Norwegian", "Norvegese"),
            Map.entry("Obanzai", "Obanzai (cucina casalinga di Kyoto)"),
            Map.entry("Oden", "Oden (piatto giapponese)"),
            Map.entry("Okonomiyaki", "Okonomiyaki"),
            Map.entry("Onigiri", "Onigiri"),
            Map.entry("Organic", "Biologica"),
            Map.entry("Oyster Specialities", "Specialita' di ostriche"),
            Map.entry("Pakistani", "Pakistana"),
            Map.entry("Peranakan", "Peranakan"),
            Map.entry("Persian", "Persiana"),
            Map.entry("Peruvian", "Peruviana"),
            Map.entry("Piedmontese", "Piemontese"),
            Map.entry("Pizza", "Pizza"),
            Map.entry("Polish", "Polacca"),
            Map.entry("Pork", "Carne di maiale"),
            Map.entry("Portuguese", "Portoghese"),
            Map.entry("Provençal", "Provenzale"),
            Map.entry("Puerto Rican", "Portoricana"),
            Map.entry("Ramen", "Ramen"),
            Map.entry("Regional Cuisine", "Cucina regionale"),
            Map.entry("Regional European", "Europea regionale"),
            Map.entry("Rice Dishes", "Piatti di riso"),
            Map.entry("Roman", "Romana"),
            Map.entry("Russian", "Russa"),
            Map.entry("Sardinian", "Sarda"),
            Map.entry("Savoyard", "Savoiarda"),
            Map.entry("Scandinavian", "Scandinava"),
            Map.entry("Scottish", "Scozzese"),
            Map.entry("Seafood", "Pesce"),
            Map.entry("Seasonal Cuisine", "Cucina di stagione"),
            Map.entry("Seolleongtang", "Seolleongtang (piatto coreano)"),
            Map.entry("Shaanxi", "Cucina dello Shaanxi"),
            Map.entry("Shabu-shabu", "Shabu-shabu"),
            Map.entry("Shandong", "Cucina dello Shandong"),
            Map.entry("Shanghainese", "Cucina di Shanghai"),
            Map.entry("Sharing", "Piatti da condividere"),
            Map.entry("Shellfish Specialities", "Specialita' di crostacei"),
            Map.entry("Shojin", "Shojin (cucina buddista)"),
            Map.entry("Shun Tak", "Cucina Shun Tak"),
            Map.entry("Sichuan", "Cucina dello Sichuan"),
            Map.entry("Sicilian", "Siciliana"),
            Map.entry("Singaporean", "Singaporiana"),
            Map.entry("Singaporean and Malaysian", "Singaporiana e malese"),
            Map.entry("Small eats", "Piccoli assaggi"),
            Map.entry("Smørrebrød", "Smørrebrød (tartine danesi)"),
            Map.entry("Soba", "Soba"),
            Map.entry("South African", "Sudafricana"),
            Map.entry("South American", "Sudamericana"),
            Map.entry("South East Asian", "Sud-est asiatica"),
            Map.entry("South Indian", "Indiana del sud"),
            Map.entry("South Tyrolean", "Sudtirolese"),
            Map.entry("Southern", "Cucina del Sud (USA)"),
            Map.entry("Southern Thai", "Thailandese del sud"),
            Map.entry("Spanish", "Spagnola"),
            Map.entry("Spanish Contemporary", "Spagnola contemporanea"),
            Map.entry("Sri Lankan", "Cingalese"),
            Map.entry("Steakhouse", "Steakhouse"),
            Map.entry("Street Food", "Street food"),
            Map.entry("Sujebi", "Sujebi (piatto coreano)"),
            Map.entry("Sukiyaki", "Sukiyaki"),
            Map.entry("Sushi", "Sushi"),
            Map.entry("Swabian", "Sveva"),
            Map.entry("Swedish", "Svedese"),
            Map.entry("Swiss", "Svizzera"),
            Map.entry("Taiwanese", "Taiwanese"),
            Map.entry("Taiwanese contemporary", "Taiwanese contemporanea"),
            Map.entry("Taizhou", "Cucina di Taizhou"),
            Map.entry("Tempura", "Tempura"),
            Map.entry("Teochew", "Cucina Teochew"),
            Map.entry("Teppanyaki", "Teppanyaki"),
            Map.entry("Tex-Mex", "Tex-Mex"),
            Map.entry("Thai", "Thailandese"),
            Map.entry("Thai-Chinese", "Thailandese-cinese"),
            Map.entry("Thai and Vietnamese", "Thailandese e vietnamita"),
            Map.entry("Thai contemporary", "Thailandese contemporanea"),
            Map.entry("Tibetan", "Tibetana"),
            Map.entry("Tonkatsu", "Tonkatsu"),
            Map.entry("Traditional British", "Britannica tradizionale"),
            Map.entry("Traditional Cuisine", "Cucina tradizionale"),
            Map.entry("Turkish", "Turca"),
            Map.entry("Tuscan", "Toscana"),
            Map.entry("Udon", "Udon"),
            Map.entry("Umbrian", "Umbra"),
            Map.entry("Unagi / Freshwater Eel", "Anguilla (Unagi)"),
            Map.entry("Vegan", "Vegana"),
            Map.entry("Vegetarian", "Vegetariana"),
            Map.entry("Venetian", "Veneta"),
            Map.entry("Venezuelan", "Venezuelana"),
            Map.entry("Vietnamese", "Vietnamita"),
            Map.entry("Vietnamese Contemporary", "Vietnamita contemporanea"),
            Map.entry("World Cuisine", "Cucina del mondo"),
            Map.entry("Xibei", "Cucina Xibei"),
            Map.entry("Xinjiang", "Cucina dello Xinjiang"),
            Map.entry("Yakitori", "Yakitori"),
            Map.entry("Yoshoku", "Yoshoku (cucina occidentale giapponese)"),
            Map.entry("Yukhoe", "Yukhoe (piatto coreano)"),
            Map.entry("Yunnanese", "Cucina dello Yunnan"),
            Map.entry("Zhejiang", "Cucina dello Zhejiang"));

    private ImportCSV() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Uso: ImportCSV <percorso-file-csv> [host dbPorta dbNome dbUtente dbPassword]");
            System.exit(1);
        }
        Path percorsoCsv = Path.of(args[0]);
        if (!Files.exists(percorsoCsv)) {
            System.err.println("File CSV non trovato: " + percorsoCsv.toAbsolutePath());
            System.exit(1);
        }

        ConfigurazioneDB config;
        if (args.length >= 6) {
            config = new ConfigurazioneDB(args[1], Integer.parseInt(args[2]), args[3], args[4], args[5]);
        } else {
            System.out.println("=== ImportCSV: configurazione connessione al database dbTK ===");
            config = ConsoleUtil.chiediConfigurazioneDB(new Scanner(System.in));
        }

        try (Connection conn = config.nuovaConnessione()) {
            conn.setAutoCommit(false);
            int idRistoratoreSeed = trovaOCreaRistoratoreSeed(conn);
            Map<String, Integer> cacheCucine = caricaCucine(conn);

            int importati = 0;
            int scartati = 0;

            CSVFormat formato = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
            try (var reader = Files.newBufferedReader(percorsoCsv, StandardCharsets.UTF_8);
                 CSVParser parser = new CSVParser(reader, formato)) {

                try (PreparedStatement psRistorante = conn.prepareStatement(
                        "INSERT INTO ristoranti (nome, nazione, citta, indirizzo, " +
                        "fascia_prezzo, delivery, prenotazione_online, id_ristoratore) VALUES (?,?,?,?,?,?,?,?) " +
                        "RETURNING id");
                     PreparedStatement psCucina = conn.prepareStatement(
                        "INSERT INTO ristoranti_cucine (id_ristorante, id_cucina) VALUES (?, ?) ON CONFLICT DO NOTHING")) {

                    for (CSVRecord record : parser) {
                        try {
                            int idRistorante = inserisciRistorante(psRistorante, record, idRistoratoreSeed);
                            for (String cucina : record.get("Cuisine").split(",")) {
                                String nome = cucina.trim();
                                if (nome.isEmpty() || CUCINE_DA_SCARTARE.contains(nome)) continue;
                                nome = TRADUZIONE_CUCINA.getOrDefault(nome, nome);
                                int idCucina = trovaOCreaCucina(conn, cacheCucine, nome);
                                psCucina.setInt(1, idRistorante);
                                psCucina.setInt(2, idCucina);
                                psCucina.executeUpdate();
                            }
                            importati++;
                            if (importati % 1000 == 0) {
                                conn.commit();
                                System.out.println("Importati " + importati + " ristoranti...");
                            }
                        } catch (Exception e) {
                            scartati++;
                        }
                    }
                }
            }
            conn.commit();
            System.out.println("Importazione completata: " + importati + " ristoranti importati, " + scartati + " record scartati.");
        }
    }

    private static int inserisciRistorante(PreparedStatement ps, CSVRecord record, int idRistoratoreSeed) throws SQLException {
        String nome = record.get("Name");
        String indirizzo = record.get("Address");
        String[] luogo = parseLocation(record.get("Location"));
        BigDecimal fasciaPrezzo = prezzoDaSimboli(valoreOpzionale(record, "Price"));

        ps.setString(1, nome);
        ps.setString(2, luogo[1]);
        ps.setString(3, luogo[0]);
        ps.setString(4, indirizzo);
        ps.setBigDecimal(5, fasciaPrezzo);
        ps.setBoolean(6, false);
        ps.setBoolean(7, false);
        ps.setInt(8, idRistoratoreSeed);
        try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt("id");
        }
    }

    /** Legge un campo opzionale del CSV senza sollevare eccezioni se assente nella riga corrente. */
    private static String valoreOpzionale(CSVRecord record, String colonna) {
        try {
            return record.get(colonna);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** @return {citta, nazione} a partire dalla colonna Location ("Citta', Nazione"). */
    private static String[] parseLocation(String location) {
        if (location == null || location.isBlank()) {
            return new String[]{"Sconosciuta", "Sconosciuta"};
        }
        int ultimaVirgola = location.lastIndexOf(',');
        if (ultimaVirgola < 0) {
            String valore = location.trim();
            return new String[]{valore, valore}; // citta'-stato: es. "Singapore", "Hong Kong"
        }
        String citta = location.substring(0, ultimaVirgola).trim();
        String nazione = location.substring(ultimaVirgola + 1).trim();
        return new String[]{citta, nazione};
    }

    /** Converte il simbolo di valuta ripetuto (1-4 volte) in una fascia di prezzo medio convenzionale in euro. */
    private static BigDecimal prezzoDaSimboli(String price) {
        if (price == null || price.isBlank()) {
            return BigDecimal.valueOf(45); // fallback: fascia media, per l'unico record senza prezzo nel dataset
        }
        int simboli = price.trim().codePointCount(0, price.trim().length());
        return switch (Math.max(1, Math.min(simboli, 4))) {
            case 1 -> BigDecimal.valueOf(25);
            case 2 -> BigDecimal.valueOf(45);
            case 3 -> BigDecimal.valueOf(80);
            default -> BigDecimal.valueOf(150);
        };
    }

    private static int trovaOCreaRistoratoreSeed(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM utenti WHERE email = ?")) {
            ps.setString(1, EMAIL_UTENTE_SEED);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        String passwordCasuale = UUID.randomUUID().toString();
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO utenti (nome, cognome, email, username, password_hash, ruolo) VALUES (?,?,?,?,?,'RISTORATORE'::ruolo_utente) " +
                "RETURNING id")) {
            ps.setString(1, "TheKnife");
            ps.setString(2, "Import");
            ps.setString(3, EMAIL_UTENTE_SEED);
            ps.setString(4, "TheKnifeImport");
            ps.setString(5, PasswordUtil.hash(passwordCasuale));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                conn.commit();
                return rs.getInt("id");
            }
        }
    }

    private static Map<String, Integer> caricaCucine(Connection conn) throws SQLException {
        Map<String, Integer> cache = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, nome FROM cucine");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cache.put(rs.getString("nome").toLowerCase(), rs.getInt("id"));
            }
        }
        return cache;
    }

    private static int trovaOCreaCucina(Connection conn, Map<String, Integer> cache, String nome) throws SQLException {
        Integer id = cache.get(nome.toLowerCase());
        if (id != null) {
            return id;
        }
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO cucine (nome) VALUES (?) RETURNING id")) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int nuovoId = rs.getInt("id");
                cache.put(nome.toLowerCase(), nuovoId);
                return nuovoId;
            }
        }
    }
}
