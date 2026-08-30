/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Parametri di connessione al database dbTK e factory delle connessioni
 * JDBC. Ogni DAO apre una connessione dedicata per ciascuna operazione
 * (try-with-resources): con un thread per client (vedi
 * {@code theknife.server.GestoreClient}) questo evita la necessita' di
 * sincronizzare l'accesso a una connessione condivisa, delegando il
 * controllo della concorrenza al DBMS stesso.
 */
public final class ConfigurazioneDB {

    private final String url;
    private final String utente;
    private final String password;

    public ConfigurazioneDB(String host, int porta, String nomeDatabase, String utente, String password) {
        this.url = "jdbc:postgresql://" + host + ":" + porta + "/" + nomeDatabase;
        this.utente = utente;
        this.password = password;
    }

    public Connection nuovaConnessione() throws SQLException {
        return DriverManager.getConnection(url, utente, password);
    }

    public String getUrl() {
        return url;
    }
}
