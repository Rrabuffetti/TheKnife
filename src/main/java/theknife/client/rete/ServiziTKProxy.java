/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client.rete;

import java.util.List;

import theknife.common.ServiziTK;
import theknife.common.model.Recensione;
import theknife.common.model.Ristorante;
import theknife.common.model.Ruolo;
import theknife.common.model.Utente;
import theknife.common.protocol.CriteriRicerca;
import theknife.common.protocol.Operazione;
import theknife.common.protocol.Richiesta;
import theknife.common.protocol.Risposta;
import theknife.common.util.TheKnifeException;

/**
 * Implementazione client-side di {@link ServiziTK} (design pattern Proxy
 * remoto): ogni metodo costruisce una {@link Richiesta}, la invia al
 * server tramite {@link ConnessioneServer} e ne interpreta la
 * {@link Risposta}, cosi' che i controller JavaFX possano invocare i
 * servizi come se fossero locali, ignari del fatto che dietro le quinte
 * c'e' una comunicazione di rete.
 */
public class ServiziTKProxy implements ServiziTK {

    private final ConnessioneServer connessione;

    public ServiziTKProxy(ConnessioneServer connessione) {
        this.connessione = connessione;
    }

    private Risposta chiama(Operazione operazione, Object... parametri) throws TheKnifeException {
        try {
            Risposta risposta = connessione.invia(new Richiesta(operazione, parametri));
            if (!risposta.isSuccesso()) {
                throw new TheKnifeException(risposta.getMessaggioErrore());
            }
            return risposta;
        } catch (java.io.IOException | ClassNotFoundException e) {
            throw new TheKnifeException("Errore di comunicazione con il server.", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Ristorante> cercaRistorante(CriteriRicerca criteri) throws TheKnifeException {
        return (List<Ristorante>) chiama(Operazione.CERCA_RISTORANTI, criteri).getDato();
    }

    @Override
    public Ristorante visualizzaRistorante(int idRistorante) throws TheKnifeException {
        return (Ristorante) chiama(Operazione.VISUALIZZA_RISTORANTE, idRistorante).getDato();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Recensione> visualizzaRecensioni(int idRistorante) throws TheKnifeException {
        return (List<Recensione>) chiama(Operazione.VISUALIZZA_RECENSIONI, idRistorante).getDato();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> elencoTipiCucina() throws TheKnifeException {
        return (List<String>) chiama(Operazione.ELENCO_TIPI_CUCINA).getDato();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> elencoCittaPerNazione(String nazione) throws TheKnifeException {
        return (List<String>) chiama(Operazione.ELENCO_CITTA_PER_NAZIONE, nazione).getDato();
    }

    @Override
    public Utente registrazione(Utente nuovoUtente, String passwordChiaro) throws TheKnifeException {
        return (Utente) chiama(Operazione.REGISTRAZIONE, nuovoUtente, passwordChiaro).getDato();
    }

    @Override
    public Utente login(String email, String passwordChiaro, Ruolo ruolo) throws TheKnifeException {
        return (Utente) chiama(Operazione.LOGIN, email, passwordChiaro, ruolo).getDato();
    }

    @Override
    public void aggiungiPreferito(int idCliente, int idRistorante) throws TheKnifeException {
        chiama(Operazione.AGGIUNGI_PREFERITO, idCliente, idRistorante);
    }

    @Override
    public void rimuoviPreferito(int idCliente, int idRistorante) throws TheKnifeException {
        chiama(Operazione.RIMUOVI_PREFERITO, idCliente, idRistorante);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Ristorante> visualizzaPreferiti(int idCliente) throws TheKnifeException {
        return (List<Ristorante>) chiama(Operazione.VISUALIZZA_PREFERITI, idCliente).getDato();
    }

    @Override
    public Recensione aggiungiRecensione(int idCliente, int idRistorante, int stelle, String testo) throws TheKnifeException {
        return (Recensione) chiama(Operazione.AGGIUNGI_RECENSIONE, idCliente, idRistorante, stelle, testo).getDato();
    }

    @Override
    public void modificaRecensione(int idCliente, int idRecensione, int stelle, String testo) throws TheKnifeException {
        chiama(Operazione.MODIFICA_RECENSIONE, idCliente, idRecensione, stelle, testo);
    }

    @Override
    public void eliminaRecensione(int idCliente, int idRecensione) throws TheKnifeException {
        chiama(Operazione.ELIMINA_RECENSIONE, idCliente, idRecensione);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Recensione> visualizzaRecensioniProprie(int idCliente) throws TheKnifeException {
        return (List<Recensione>) chiama(Operazione.VISUALIZZA_RECENSIONI_PROPRIE, idCliente).getDato();
    }

    @Override
    public Ristorante aggiungiRistorante(int idRistoratore, Ristorante nuovoRistorante, List<String> tipiCucina) throws TheKnifeException {
        return (Ristorante) chiama(Operazione.AGGIUNGI_RISTORANTE, idRistoratore, nuovoRistorante, tipiCucina).getDato();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Ristorante> visualizzaRiepilogo(int idRistoratore) throws TheKnifeException {
        return (List<Ristorante>) chiama(Operazione.VISUALIZZA_RIEPILOGO, idRistoratore).getDato();
    }

    @Override
    public void rispostaRecensione(int idRistoratore, int idRecensione, String risposta) throws TheKnifeException {
        chiama(Operazione.RISPOSTA_RECENSIONE, idRistoratore, idRecensione, risposta);
    }
}
