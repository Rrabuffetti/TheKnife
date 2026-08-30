/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.common;

import java.util.List;

import theknife.common.model.Recensione;
import theknife.common.model.Ristorante;
import theknife.common.model.Ruolo;
import theknife.common.model.Utente;
import theknife.common.protocol.CriteriRicerca;
import theknife.common.util.TheKnifeException;

/**
 * Contratto dei servizi offerti dalla piattaforma TheKnife, con la
 * stessa nomenclatura delle funzionalita' elencate nel documento di
 * specifica di progetto. E' implementato:
 * <ul>
 *   <li>lato server da {@code theknife.server.service.ServiziTKImpl},
 *       che realizza la logica applicativa appoggiandosi ai DAO
 *       (pattern Facade);</li>
 *   <li>lato client da {@code theknife.client.rete.ServiziTKProxy}, che
 *       inoltra ogni chiamata al server tramite socket e ne restituisce
 *       il risultato (pattern Proxy remoto), cosi' che i controller
 *       JavaFX possano usare i servizi come se fossero locali.</li>
 * </ul>
 */
public interface ServiziTK {

    // ------------------------------------------------------------
    // Consultazione (login non necessario)
    // ------------------------------------------------------------

    /** Ricerca i ristoranti che soddisfano i criteri indicati (luogo obbligatorio). */
    List<Ristorante> cercaRistorante(CriteriRicerca criteri) throws TheKnifeException;

    /** Restituisce i dettagli di un ristorante (luogo, prezzo, cucina, servizi, media stelle). */
    Ristorante visualizzaRistorante(int idRistorante) throws TheKnifeException;

    /** Restituisce le recensioni di un ristorante (forma anonima), con stelle e numero recensioni. */
    List<Recensione> visualizzaRecensioni(int idRistorante) throws TheKnifeException;

    /** Elenco dei tipi di cucina gia' censiti (tabella cucine), in ordine alfabetico, per i filtri di ricerca. */
    List<String> elencoTipiCucina() throws TheKnifeException;

    /** Citta' gia' censite per una data nazione (dai ristoranti esistenti), usate come suggerimento in fase di inserimento. */
    List<String> elencoCittaPerNazione(String nazione) throws TheKnifeException;

    // ------------------------------------------------------------
    // Registrazione e login
    // ------------------------------------------------------------

    /** Registra un nuovo utente (cliente o ristoratore, in base al tipo concreto passato). */
    Utente registrazione(Utente nuovoUtente, String passwordChiaro) throws TheKnifeException;

    /**
     * Autentica un utente gia' registrato tramite email, password e ruolo scelto.
     * Il ruolo fa parte delle credenziali: la stessa email puo' infatti essere
     * condivisa da un account cliente e da un account ristoratore distinti, e
     * l'utente sceglie con quale dei due accedere.
     */
    Utente login(String email, String passwordChiaro, Ruolo ruolo) throws TheKnifeException;

    // ------------------------------------------------------------
    // Funzionalita' clienti (login necessario)
    // ------------------------------------------------------------

    void aggiungiPreferito(int idCliente, int idRistorante) throws TheKnifeException;

    void rimuoviPreferito(int idCliente, int idRistorante) throws TheKnifeException;

    List<Ristorante> visualizzaPreferiti(int idCliente) throws TheKnifeException;

    Recensione aggiungiRecensione(int idCliente, int idRistorante, int stelle, String testo) throws TheKnifeException;

    void modificaRecensione(int idCliente, int idRecensione, int stelle, String testo) throws TheKnifeException;

    void eliminaRecensione(int idCliente, int idRecensione) throws TheKnifeException;

    /** Recensioni inserite dal cliente, con il ristorante a cui si riferiscono (schermata "le mie recensioni"). */
    List<Recensione> visualizzaRecensioniProprie(int idCliente) throws TheKnifeException;

    // ------------------------------------------------------------
    // Funzionalita' ristoratori (login necessario)
    // ------------------------------------------------------------

    Ristorante aggiungiRistorante(int idRistoratore, Ristorante nuovoRistorante, List<String> tipiCucina) throws TheKnifeException;

    /** Ristoranti del gestore con, per ciascuno, media delle stelle e numero di recensioni. */
    List<Ristorante> visualizzaRiepilogo(int idRistoratore) throws TheKnifeException;

    void rispostaRecensione(int idRistoratore, int idRecensione, String risposta) throws TheKnifeException;
}
