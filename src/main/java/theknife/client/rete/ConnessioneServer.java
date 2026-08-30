/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client.rete;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import theknife.common.protocol.Richiesta;
import theknife.common.protocol.Risposta;

/**
 * Incapsula la connessione socket verso serverTK e lo scambio di
 * messaggi {@link Richiesta}/{@link Risposta} serializzati.
 * <p>
 * {@link #invia(Richiesta)} e' sincronizzato perche' il protocollo e'
 * strettamente request/response su un'unica connessione: se due
 * operazioni venissero inviate in parallelo dallo stesso client le
 * risposte potrebbero incrociarsi. Le chiamate vanno comunque effettuate
 * da un thread diverso da quello grafico (vedi {@code EseguiAsync}) per
 * non bloccare l'interfaccia in attesa della risposta del server.
 */
public class ConnessioneServer implements AutoCloseable {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public ConnessioneServer(String host, int porta) throws IOException {
        this.socket = new Socket(host, porta);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public synchronized Risposta invia(Richiesta richiesta) throws IOException, ClassNotFoundException {
        out.writeObject(richiesta);
        out.flush();
        out.reset();
        return (Risposta) in.readObject();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
