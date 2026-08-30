/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.server.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import theknife.common.model.Cliente;
import theknife.common.model.Ristoratore;
import theknife.common.model.Ruolo;
import theknife.common.model.Utente;

/**
 * Accesso alla tabella {@code utenti} (pattern DAO: isola il resto
 * dell'applicazione dai dettagli SQL/JDBC).
 */
public class UtenteDAO {

    private final ConfigurazioneDB config;

    public UtenteDAO(ConfigurazioneDB config) {
        this.config = config;
    }

    /** @return {@code true} se esiste gia' un account con quella email per QUEL ruolo (stessa email, ruolo diverso e' ammesso). */
    public boolean esisteEmailPerRuolo(String email, Ruolo ruolo) throws SQLException {
        String sql = "SELECT 1 FROM utenti WHERE email = ? AND ruolo = ?::ruolo_utente";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, ruolo.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** @return {@code true} se esiste gia' un account con quello username (univoco in assoluto, indipendente dal ruolo). */
    public boolean esisteUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM utenti WHERE username = ?";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Utente creaUtente(Utente u, String passwordHash) throws SQLException {
        String sql = "INSERT INTO utenti (nome, cognome, email, username, password_hash, data_nascita, "
                + "nazione_domicilio, citta_domicilio, ruolo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::ruolo_utente) "
                + "RETURNING id, data_registrazione";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getCognome());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getUsername());
            ps.setString(5, passwordHash);
            if (u.getDataNascita() != null) {
                ps.setDate(6, Date.valueOf(u.getDataNascita()));
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }
            ps.setString(7, u.getNazioneDomicilio());
            ps.setString(8, u.getCittaDomicilio());
            ps.setString(9, u.getRuolo().name());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                u.setId(rs.getInt("id"));
                u.setDataRegistrazione(rs.getTimestamp("data_registrazione").toLocalDateTime());
            }
            return u;
        }
    }

    /** Cerca le credenziali dell'account con quella email e quel ruolo (vedi {@link theknife.common.ServiziTK#login}). */
    public UtenteCredenziali trovaCredenzialiPerEmailERuolo(String email, Ruolo ruolo) throws SQLException {
        String sql = "SELECT id, nome, cognome, email, username, password_hash, data_nascita, "
                + "nazione_domicilio, citta_domicilio, ruolo, data_registrazione "
                + "FROM utenti WHERE email = ? AND ruolo = ?::ruolo_utente";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, ruolo.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Utente u = mappaRiga(rs);
                return new UtenteCredenziali(u, rs.getString("password_hash"));
            }
        }
    }

    public Utente trovaPerId(int id) throws SQLException {
        String sql = "SELECT id, nome, cognome, email, username, password_hash, data_nascita, "
                + "nazione_domicilio, citta_domicilio, ruolo, data_registrazione "
                + "FROM utenti WHERE id = ?";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mappaRiga(rs) : null;
            }
        }
    }

    private Utente mappaRiga(ResultSet rs) throws SQLException {
        Ruolo ruolo = Ruolo.valueOf(rs.getString("ruolo"));
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String cognome = rs.getString("cognome");
        String email = rs.getString("email");
        String username = rs.getString("username");
        Date dataNascitaSql = rs.getDate("data_nascita");
        LocalDate dataNascita = dataNascitaSql != null ? dataNascitaSql.toLocalDate() : null;
        String nazioneDomicilio = rs.getString("nazione_domicilio");
        String cittaDomicilio = rs.getString("citta_domicilio");
        Timestamp dataRegistrazioneSql = rs.getTimestamp("data_registrazione");
        LocalDateTime dataRegistrazione = dataRegistrazioneSql != null ? dataRegistrazioneSql.toLocalDateTime() : null;

        if (ruolo == Ruolo.CLIENTE) {
            return new Cliente(id, nome, cognome, email, username, dataNascita, nazioneDomicilio, cittaDomicilio, dataRegistrazione);
        }
        return new Ristoratore(id, nome, cognome, email, username, dataNascita, nazioneDomicilio, cittaDomicilio, dataRegistrazione);
    }
}
