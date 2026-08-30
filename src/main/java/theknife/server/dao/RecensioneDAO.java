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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import theknife.common.model.Recensione;

/**
 * Accesso alla tabella {@code recensioni}.
 */
public class RecensioneDAO {

    private static final String SELECT_BASE =
            "SELECT rec.id, rec.id_ristorante, ris.nome AS nome_ristorante, rec.id_cliente, u.username AS username_cliente, " +
            "       rec.stelle, rec.testo, rec.data_recensione, rec.data_modifica, rec.risposta_testo, rec.data_risposta " +
            "FROM recensioni rec " +
            "JOIN ristoranti ris ON ris.id = rec.id_ristorante " +
            "JOIN utenti u ON u.id = rec.id_cliente ";

    private final ConfigurazioneDB config;

    public RecensioneDAO(ConfigurazioneDB config) {
        this.config = config;
    }

    public Recensione inserisci(int idRistorante, int idCliente, int stelle, String testo) throws SQLException {
        String sql = "INSERT INTO recensioni (id_ristorante, id_cliente, stelle, testo) VALUES (?,?,?,?) " +
                "RETURNING id, data_recensione";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRistorante);
            ps.setInt(2, idCliente);
            ps.setInt(3, stelle);
            ps.setString(4, testo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Recensione r = new Recensione();
                r.setId(rs.getInt("id"));
                r.setIdRistorante(idRistorante);
                r.setIdCliente(idCliente);
                r.setStelle(stelle);
                r.setTesto(testo);
                r.setDataRecensione(rs.getTimestamp("data_recensione").toLocalDateTime());
                return r;
            }
        }
    }

    public Recensione trovaPerId(int id) throws SQLException {
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE rec.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mappaRiga(rs) : null;
            }
        }
    }

    public List<Recensione> trovaPerRistorante(int idRistorante) throws SQLException {
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_BASE + "WHERE rec.id_ristorante = ? ORDER BY rec.data_recensione DESC")) {
            ps.setInt(1, idRistorante);
            return mappaTutte(ps);
        }
    }

    public List<Recensione> trovaPerCliente(int idCliente) throws SQLException {
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_BASE + "WHERE rec.id_cliente = ? ORDER BY rec.data_recensione DESC")) {
            ps.setInt(1, idCliente);
            return mappaTutte(ps);
        }
    }

    /** Aggiorna testo e stelle di una recensione esistente del cliente indicato; {@code false} se non trovata/non sua. */
    public boolean modifica(int idRecensione, int idCliente, int stelle, String testo) throws SQLException {
        String sql = "UPDATE recensioni SET stelle = ?, testo = ? WHERE id = ? AND id_cliente = ?";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stelle);
            ps.setString(2, testo);
            ps.setInt(3, idRecensione);
            ps.setInt(4, idCliente);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean elimina(int idRecensione, int idCliente) throws SQLException {
        String sql = "DELETE FROM recensioni WHERE id = ? AND id_cliente = ?";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRecensione);
            ps.setInt(2, idCliente);
            return ps.executeUpdate() > 0;
        }
    }

    /** Imposta (o sostituisce) la risposta del ristoratore alla recensione indicata. */
    public boolean rispondi(int idRecensione, String risposta) throws SQLException {
        String sql = "UPDATE recensioni SET risposta_testo = ?, data_risposta = now() WHERE id = ?";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, risposta);
            ps.setInt(2, idRecensione);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Recensione> mappaTutte(PreparedStatement ps) throws SQLException {
        List<Recensione> risultato = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                risultato.add(mappaRiga(rs));
            }
        }
        return risultato;
    }

    private Recensione mappaRiga(ResultSet rs) throws SQLException {
        Recensione r = new Recensione();
        r.setId(rs.getInt("id"));
        r.setIdRistorante(rs.getInt("id_ristorante"));
        r.setNomeRistorante(rs.getString("nome_ristorante"));
        r.setIdCliente(rs.getInt("id_cliente"));
        r.setUsernameCliente(rs.getString("username_cliente"));
        r.setStelle(rs.getInt("stelle"));
        r.setTesto(rs.getString("testo"));
        r.setDataRecensione(rs.getTimestamp("data_recensione").toLocalDateTime());
        Timestamp modifica = rs.getTimestamp("data_modifica");
        if (modifica != null) r.setDataModifica(modifica.toLocalDateTime());
        r.setRispostaTesto(rs.getString("risposta_testo"));
        Timestamp risposta = rs.getTimestamp("data_risposta");
        if (risposta != null) r.setDataRisposta(risposta.toLocalDateTime());
        return r;
    }
}
