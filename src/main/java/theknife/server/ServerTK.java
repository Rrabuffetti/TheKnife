/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Logger;

import theknife.server.dao.ConfigurazioneDB;
import theknife.server.service.ServiziTKImpl;

/**
 * Punto di ingresso del modulo serverTK: chiede all'avvio le credenziali
 * di accesso al database dbTK e l'host su cui si trova, poi resta in
 * attesa di connessioni da parte dei client clientTK su una
 * {@link ServerSocket}, avviando un thread dedicato ({@link GestoreClient})
 * per ciascuna connessione accettata, cosi' da supportare piu' utenti
 * connessi in parallelo da postazioni diverse.
 */
public final class ServerTK {

    private static final Logger LOG = Logger.getLogger(ServerTK.class.getName());
    private static final int PORTA_ASCOLTO_DEFAULT = 9090;

    private ServerTK() {
    }

    public static void main(String[] args) {
        try {
            ConfigurazioneDB configDB;
            int portaAscolto;

            if (args.length >= 5) {
                // Uso non interattivo: host dbPorta dbNome dbUtente dbPassword [portaAscolto]
                String host = args[0];
                int dbPorta = Integer.parseInt(args[1]);
                String dbNome = args[2];
                String dbUtente = args[3];
                String dbPassword = args[4];
                portaAscolto = args.length >= 6 ? Integer.parseInt(args[5]) : PORTA_ASCOLTO_DEFAULT;
                configDB = new ConfigurazioneDB(host, dbPorta, dbNome, dbUtente, dbPassword);
            } else {
                configDB = chiediConfigurazioneDaConsole();
                portaAscolto = PORTA_ASCOLTO_DEFAULT;
            }

            verificaConnessione(configDB);

            ServiziTKImpl servizi = new ServiziTKImpl(configDB);
            avviaServer(servizi, portaAscolto);

        } catch (Exception e) {
            System.err.println("Impossibile avviare serverTK: " + e.getMessage());
            System.exit(1);
        }
    }

    private static ConfigurazioneDB chiediConfigurazioneDaConsole() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Avvio serverTK: configurazione connessione al database dbTK ===");
        return ConsoleUtil.chiediConfigurazioneDB(scanner);
    }

    private static void verificaConnessione(ConfigurazioneDB configDB) throws SQLException {
        try (Connection ignored = configDB.nuovaConnessione()) {
            LOG.info(() -> "Connessione al database riuscita: " + configDB.getUrl());
        }
    }

    private static void avviaServer(ServiziTKImpl servizi, int portaAscolto) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(portaAscolto)) {
            LOG.info(() -> "serverTK in ascolto sulla porta " + portaAscolto + ". In attesa di connessioni...");
            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new GestoreClient(socket, servizi));
                thread.setDaemon(true);
                thread.start();
            }
        }
    }
}
