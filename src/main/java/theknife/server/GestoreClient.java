/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import theknife.common.model.Recensione;
import theknife.common.model.Ristorante;
import theknife.common.model.Ruolo;
import theknife.common.model.Utente;
import theknife.common.protocol.CriteriRicerca;
import theknife.common.protocol.Richiesta;
import theknife.common.protocol.Risposta;
import theknife.common.util.TheKnifeException;
import theknife.server.service.ServiziTKImpl;

/**
 * Gestisce il ciclo di vita di una singola connessione client: viene
 * eseguito su un thread dedicato (un thread per client, avviato da
 * {@link ServerTK}), legge richieste, le esegue e scrive le risposte
 * finche' il client non si disconnette.
 * <p>
 * Mantiene lo stato di sessione (l'utente autenticato sulla connessione,
 * se presente) e applica i controlli di autorizzazione prima di inoltrare
 * la chiamata a {@link ServiziTKImpl}: un client non puo' operare per
 * conto di un altro utente ne' invocare funzionalita' riservate al ruolo
 * opposto, anche se costruisse una richiesta "a mano".
 */
public class GestoreClient implements Runnable {

    private static final Logger LOG = Logger.getLogger(GestoreClient.class.getName());

    private final Socket socket;
    private final ServiziTKImpl servizi;
    private Utente utenteCorrente;

    public GestoreClient(Socket socket, ServiziTKImpl servizi) {
        this.socket = socket;
        this.servizi = servizi;
    }

    @Override
    public void run() {
        String indirizzo = socket.getRemoteSocketAddress().toString();
        LOG.info(() -> "Connessione stabilita con " + indirizzo);
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.flush();
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                while (true) {
                    Richiesta richiesta;
                    try {
                        richiesta = (Richiesta) in.readObject();
                    } catch (EOFException | SocketException fineConnessione) {
                        break;
                    } catch (ClassNotFoundException e) {
                        LOG.log(Level.WARNING, "Richiesta non riconosciuta da " + indirizzo, e);
                        continue;
                    }
                    Risposta risposta = elabora(richiesta);
                    out.writeObject(risposta);
                    out.flush();
                    out.reset();
                }
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Connessione interrotta con " + indirizzo, e);
        } finally {
            chiudiSocket();
            LOG.info(() -> "Connessione chiusa con " + indirizzo
                    + (utenteCorrente != null ? " (utente: " + utenteCorrente.getEmail() + ")" : ""));
        }
    }

    private void chiudiSocket() {
        try {
            socket.close();
        } catch (IOException ignored) {
            // connessione gia' chiusa: nulla da fare
        }
    }

    private Risposta elabora(Richiesta richiesta) {
        try {
            Object[] p = richiesta.getParametri();
            switch (richiesta.getOperazione()) {
                case LOGIN -> {
                    Utente u = servizi.login((String) p[0], (String) p[1], (Ruolo) p[2]);
                    utenteCorrente = u;
                    return Risposta.ok(u);
                }
                case REGISTRAZIONE -> {
                    Utente u = servizi.registrazione((Utente) p[0], (String) p[1]);
                    utenteCorrente = u;
                    return Risposta.ok(u);
                }
                case CERCA_RISTORANTI -> {
                    return Risposta.ok(servizi.cercaRistorante((CriteriRicerca) p[0]));
                }
                case VISUALIZZA_RISTORANTE -> {
                    return Risposta.ok(servizi.visualizzaRistorante((Integer) p[0]));
                }
                case VISUALIZZA_RECENSIONI -> {
                    return Risposta.ok(servizi.visualizzaRecensioni((Integer) p[0]));
                }
                case ELENCO_TIPI_CUCINA -> {
                    return Risposta.ok(servizi.elencoTipiCucina());
                }
                case ELENCO_CITTA_PER_NAZIONE -> {
                    return Risposta.ok(servizi.elencoCittaPerNazione((String) p[0]));
                }
                case AGGIUNGI_PREFERITO -> {
                    int idCliente = (Integer) p[0];
                    assicuraProprietarioCliente(idCliente);
                    servizi.aggiungiPreferito(idCliente, (Integer) p[1]);
                    return Risposta.ok();
                }
                case RIMUOVI_PREFERITO -> {
                    int idCliente = (Integer) p[0];
                    assicuraProprietarioCliente(idCliente);
                    servizi.rimuoviPreferito(idCliente, (Integer) p[1]);
                    return Risposta.ok();
                }
                case VISUALIZZA_PREFERITI -> {
                    int idCliente = (Integer) p[0];
                    assicuraProprietarioCliente(idCliente);
                    List<Ristorante> ristoranti = servizi.visualizzaPreferiti(idCliente);
                    return Risposta.ok(ristoranti);
                }
                case AGGIUNGI_RECENSIONE -> {
                    int idCliente = (Integer) p[0];
                    assicuraProprietarioCliente(idCliente);
                    Recensione r = servizi.aggiungiRecensione(idCliente, (Integer) p[1], (Integer) p[2], (String) p[3]);
                    return Risposta.ok(r);
                }
                case MODIFICA_RECENSIONE -> {
                    int idCliente = (Integer) p[0];
                    assicuraProprietarioCliente(idCliente);
                    servizi.modificaRecensione(idCliente, (Integer) p[1], (Integer) p[2], (String) p[3]);
                    return Risposta.ok();
                }
                case ELIMINA_RECENSIONE -> {
                    int idCliente = (Integer) p[0];
                    assicuraProprietarioCliente(idCliente);
                    servizi.eliminaRecensione(idCliente, (Integer) p[1]);
                    return Risposta.ok();
                }
                case VISUALIZZA_RECENSIONI_PROPRIE -> {
                    int idCliente = (Integer) p[0];
                    assicuraProprietarioCliente(idCliente);
                    List<Recensione> recensioni = servizi.visualizzaRecensioniProprie(idCliente);
                    return Risposta.ok(recensioni);
                }
                case AGGIUNGI_RISTORANTE -> {
                    int idRistoratore = (Integer) p[0];
                    assicuraProprietarioRistoratore(idRistoratore);
                    @SuppressWarnings("unchecked")
                    List<String> cucine = (List<String>) p[2];
                    Ristorante r = servizi.aggiungiRistorante(idRistoratore, (Ristorante) p[1], cucine);
                    return Risposta.ok(r);
                }
                case VISUALIZZA_RIEPILOGO -> {
                    int idRistoratore = (Integer) p[0];
                    assicuraProprietarioRistoratore(idRistoratore);
                    List<Ristorante> ristoranti = servizi.visualizzaRiepilogo(idRistoratore);
                    return Risposta.ok(ristoranti);
                }
                case RISPOSTA_RECENSIONE -> {
                    int idRistoratore = (Integer) p[0];
                    assicuraProprietarioRistoratore(idRistoratore);
                    servizi.rispostaRecensione(idRistoratore, (Integer) p[1], (String) p[2]);
                    return Risposta.ok();
                }
                default -> {
                    return Risposta.errore("Operazione non riconosciuta.");
                }
            }
        } catch (TheKnifeException e) {
            return Risposta.errore(e.getMessage());
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Errore inatteso elaborando " + richiesta.getOperazione(), e);
            return Risposta.errore("Errore interno del server.");
        }
    }

    private void assicuraProprietarioCliente(int idDichiarato) throws TheKnifeException {
        if (utenteCorrente == null) {
            throw new TheKnifeException("Devi effettuare il login per questa operazione.");
        }
        if (utenteCorrente.getRuolo() != Ruolo.CLIENTE) {
            throw new TheKnifeException("Operazione riservata ai clienti.");
        }
        if (utenteCorrente.getId() != idDichiarato) {
            throw new TheKnifeException("Operazione non autorizzata.");
        }
    }

    private void assicuraProprietarioRistoratore(int idDichiarato) throws TheKnifeException {
        if (utenteCorrente == null) {
            throw new TheKnifeException("Devi effettuare il login per questa operazione.");
        }
        if (utenteCorrente.getRuolo() != Ruolo.RISTORATORE) {
            throw new TheKnifeException("Operazione riservata ai ristoratori.");
        }
        if (utenteCorrente.getId() != idDichiarato) {
            throw new TheKnifeException("Operazione non autorizzata.");
        }
    }
}
