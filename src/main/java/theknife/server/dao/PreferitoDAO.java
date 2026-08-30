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
import java.sql.SQLException;

/**
 * Accesso alla tabella {@code preferiti} (associazione N:M cliente-ristorante).
 */
public class PreferitoDAO {

    private final ConfigurazioneDB config;

    public PreferitoDAO(ConfigurazioneDB config) {
        this.config = config;
    }

    public void aggiungi(int idCliente, int idRistorante) throws SQLException {
        String sql = "INSERT INTO preferiti (id_cliente, id_ristorante) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setInt(2, idRistorante);
            ps.executeUpdate();
        }
    }

    public void rimuovi(int idCliente, int idRistorante) throws SQLException {
        String sql = "DELETE FROM preferiti WHERE id_cliente = ? AND id_ristorante = ?";
        try (Connection conn = config.nuovaConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setInt(2, idRistorante);
            ps.executeUpdate();
        }
    }
}
