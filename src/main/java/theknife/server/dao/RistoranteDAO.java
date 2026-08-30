/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import theknife.common.model.Ristorante;
import theknife.common.protocol.CriteriRicerca;

/**
 * Accesso alla tabella {@code ristoranti} (e alle tabelle correlate
 * {@code cucine}/{@code ristoranti_cucine}/{@code preferiti}).
 * <p>
 * Le letture si appoggiano a una vista SQL costruita al volo (clausola
 * {@code WITH}) che calcola per ogni ristorante la media delle stelle,
 * il numero di recensioni e l'elenco dei tipi di cucina in un'unica
 * interrogazione, evitando il problema delle N+1 query.
 */
public class RistoranteDAO {

    private static final String VISTA_SQL =
            "WITH vista AS (" +
            "  SELECT r.id, r.nome, r.nazione, r.citta, r.indirizzo, " +
            "         r.fascia_prezzo, r.delivery, r.prenotazione_online, r.id_ristoratore, r.data_creazione, " +
            "         COALESCE(agg.media, 0)::double precision AS media_stelle, " +
            "         COALESCE(agg.num, 0)::int AS numero_recensioni, " +
            "         COALESCE((SELECT string_agg(c.nome, ',' ORDER BY c.nome) FROM ristoranti_cucine rc " +
            "                   JOIN cucine c ON c.id = rc.id_cucina WHERE rc.id_ristorante = r.id), '') AS cucine " +
            "  FROM ristoranti r " +
            "  LEFT JOIN (SELECT id_ristorante, AVG(stelle) AS media, COUNT(*) AS num " +
            "             FROM recensioni GROUP BY id_ristorante) agg ON agg.id_ristorante = r.id" +
            ") ";

    private final ConfigurazioneDB config;

    public RistoranteDAO(ConfigurazioneDB config) {
        this.config = config;
    }

    public Ristorante inserisci(Ristorante r, List<String> tipiCucina) throws SQLException {
        String sql = "INSERT INTO ristoranti (nome, nazione, citta, indirizzo, " +
                "fascia_prezzo, delivery, prenotazione_online, id_ristoratore) VALUES (?,?,?,?,?,?,?,?) " +
                "RETURNING id, data_creazione";
        try (Connection conn = config.nuovaConnessione()) {
            conn.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, r.getNome());
                    ps.setString(2, r.getNazione());
                    ps.setString(3, r.getCitta());
                    ps.setString(4, r.getIndirizzo());
                    ps.setBigDecimal(5, r.getFasciaPrezzo());
                    ps.setBoolean(6, r.isDelivery());
                    ps.setBoolean(7, r.isPrenotazioneOnline());
                    ps.setInt(8, r.getIdRistoratore());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        id = rs.getInt("id");
                        r.setId(id);
                        r.setDataCreazione(rs.getTimestamp("data_creazione").toLocalDateTime());
                    }
                }
                if (tipiCucina != null) {
                    for (String nomeCucina : tipiCucina) {
                        if (nomeCucina == null || nomeCucina.isBlank()) continue;
                        int idCucina = trovaOCreaCucina(conn, nomeCucina.trim());
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO ristoranti_cucine (id_ristorante, id_cucina) VALUES (?, ?) " +
                                "ON CONFLICT DO NOTHING")) {
                            ps.setInt(1, id);
                            ps.setInt(2, idCucina);
                            ps.executeUpdate();
                        }
                    }
                }
                conn.commit();
                r.setTipiCucina(tipiCucina != null ? tipiCucina : List.of());
                return r;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private int trovaOCreaCucina(Connection conn, String nome) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM cucine WHERE nome ILIKE ?")) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO cucine (nome) VALUES (?) RETURNING id")) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }

    public Ristorante trovaPerId(int id) throws SQLException {
        String sql = VISTA_SQL + "SELECT * FROM vista WHERE id = ?";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mappaRiga(rs) : null;
            }
        }
    }

    public List<Ristorante> trovaDiGestore(int idRistoratore) throws SQLException {
        String sql = VISTA_SQL + "SELECT * FROM vista WHERE id_ristoratore = ? ORDER BY nome";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRistoratore);
            return mappaTutte(ps);
        }
    }

    public List<Ristorante> trovaPreferitiDiCliente(int idCliente) throws SQLException {
        String sql = VISTA_SQL + "SELECT vista.* FROM vista JOIN preferiti p ON p.id_ristorante = vista.id " +
                "WHERE p.id_cliente = ? ORDER BY p.data_aggiunta DESC";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            return mappaTutte(ps);
        }
    }

    /**
     * Ricerca combinando i criteri opzionali; il luogo (citta'/nazione) e' l'unico
     * obbligatorio. Il confronto sul luogo prova anche gli eventuali alias noti
     * (es. "Milano"/"Milan", vedi {@link AliasLuogo}), cosi' da trovare i
     * ristoranti importati dal dataset Michelin (in inglese) anche cercando
     * col nome italiano della citta'/nazione, e viceversa.
     */
    public List<Ristorante> cerca(CriteriRicerca criteri) throws SQLException {
        List<String> variantiLuogo = AliasLuogo.varianti(criteri.getLuogo());

        StringBuilder sql = new StringBuilder(VISTA_SQL).append("SELECT * FROM vista WHERE (");
        List<Object> parametri = new ArrayList<>();
        for (int i = 0; i < variantiLuogo.size(); i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("nazione ILIKE ? OR citta ILIKE ?");
            String pattern = "%" + variantiLuogo.get(i) + "%";
            parametri.add(pattern);
            parametri.add(pattern);
        }
        sql.append(")");

        if (criteri.getTipoCucina() != null && !criteri.getTipoCucina().isBlank()) {
            sql.append(" AND cucine ILIKE ?");
            parametri.add("%" + criteri.getTipoCucina() + "%");
        }
        if (criteri.getPrezzoMinimo() != null) {
            sql.append(" AND fascia_prezzo >= ?");
            parametri.add(criteri.getPrezzoMinimo());
        }
        if (criteri.getPrezzoMassimo() != null) {
            sql.append(" AND fascia_prezzo <= ?");
            parametri.add(criteri.getPrezzoMassimo());
        }
        if (criteri.getDelivery() != null) {
            sql.append(" AND delivery = ?");
            parametri.add(criteri.getDelivery());
        }
        if (criteri.getPrenotazioneOnline() != null) {
            sql.append(" AND prenotazione_online = ?");
            parametri.add(criteri.getPrenotazioneOnline());
        }
        if (criteri.getStelleMinime() != null) {
            sql.append(" AND media_stelle >= ?");
            parametri.add(criteri.getStelleMinime());
        }
        sql.append(" ORDER BY nome");

        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametri.size(); i++) {
                Object valore = parametri.get(i);
                if (valore instanceof Boolean b) {
                    ps.setBoolean(i + 1, b);
                } else if (valore instanceof java.math.BigDecimal bd) {
                    ps.setBigDecimal(i + 1, bd);
                } else if (valore instanceof Double d) {
                    ps.setDouble(i + 1, d);
                } else if (valore == null) {
                    ps.setNull(i + 1, Types.NULL);
                } else {
                    ps.setString(i + 1, valore.toString());
                }
            }
            return mappaTutte(ps);
        }
    }

    private List<Ristorante> mappaTutte(PreparedStatement ps) throws SQLException {
        List<Ristorante> risultato = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                risultato.add(mappaRiga(rs));
            }
        }
        return risultato;
    }

    /** Elenco dei tipi di cucina gia' censiti, in ordine alfabetico (per i filtri di ricerca). */
    public List<String> elencoTipiCucina() throws SQLException {
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement("SELECT nome FROM cucine ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            List<String> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(rs.getString("nome"));
            }
            return risultato;
        }
    }

    /** Citta' gia' presenti per una data nazione (dai ristoranti esistenti), in ordine alfabetico. */
    public List<String> elencoCittaPerNazione(String nazione) throws SQLException {
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT citta FROM ristoranti WHERE nazione = ? ORDER BY citta")) {
            ps.setString(1, nazione);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> risultato = new ArrayList<>();
                while (rs.next()) {
                    risultato.add(rs.getString("citta"));
                }
                return risultato;
            }
        }
    }

    private Ristorante mappaRiga(ResultSet rs) throws SQLException {
        Ristorante r = new Ristorante();
        r.setId(rs.getInt("id"));
        r.setNome(rs.getString("nome"));
        r.setNazione(rs.getString("nazione"));
        r.setCitta(rs.getString("citta"));
        r.setIndirizzo(rs.getString("indirizzo"));
        r.setFasciaPrezzo(rs.getBigDecimal("fascia_prezzo"));
        r.setDelivery(rs.getBoolean("delivery"));
        r.setPrenotazioneOnline(rs.getBoolean("prenotazione_online"));
        r.setIdRistoratore(rs.getInt("id_ristoratore"));
        r.setDataCreazione(rs.getTimestamp("data_creazione").toLocalDateTime());
        r.setMediaStelle(rs.getDouble("media_stelle"));
        r.setNumeroRecensioni(rs.getInt("numero_recensioni"));
        String cucineCsv = rs.getString("cucine");
        if (cucineCsv != null && !cucineCsv.isBlank()) {
            r.setTipiCucina(new ArrayList<>(Arrays.asList(cucineCsv.split(","))));
        }
        return r;
    }
}
