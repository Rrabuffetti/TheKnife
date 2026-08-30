/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.server.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import theknife.common.ServiziTK;
import theknife.common.model.Recensione;
import theknife.common.model.Ristorante;
import theknife.common.model.Ruolo;
import theknife.common.model.Utente;
import theknife.common.protocol.CriteriRicerca;
import theknife.common.util.PasswordUtil;
import theknife.common.util.TheKnifeException;
import theknife.server.dao.ConfigurazioneDB;
import theknife.server.dao.PreferitoDAO;
import theknife.server.dao.RecensioneDAO;
import theknife.server.dao.RistoranteDAO;
import theknife.server.dao.UtenteCredenziali;
import theknife.server.dao.UtenteDAO;

/**
 * Implementazione server-side di {@link ServiziTK}: realizza la logica
 * applicativa e le regole di business, appoggiandosi ai DAO per la
 * persistenza (design pattern Facade, che presenta ai chiamanti
 * un'unica interfaccia coerente sopra i quattro DAO sottostanti).
 * <p>
 * Il controllo di autorizzazione "l'utente della richiesta e' davvero
 * chi dice di essere" e' demandato al chiamante ({@code GestoreClient}),
 * che conosce l'utente autenticato sulla connessione corrente; questa
 * classe assume percio' che gli id passati siano gia' stati validati e
 * si concentra sulle regole di dominio (unicita' email, range stelle,
 * proprieta' delle recensioni, ecc.).
 */
public class ServiziTKImpl implements ServiziTK {

    private static final Logger LOG = Logger.getLogger(ServiziTKImpl.class.getName());

    private final UtenteDAO utenteDAO;
    private final RistoranteDAO ristoranteDAO;
    private final RecensioneDAO recensioneDAO;
    private final PreferitoDAO preferitoDAO;

    public ServiziTKImpl(ConfigurazioneDB config) {
        this.utenteDAO = new UtenteDAO(config);
        this.ristoranteDAO = new RistoranteDAO(config);
        this.recensioneDAO = new RecensioneDAO(config);
        this.preferitoDAO = new PreferitoDAO(config);
    }

    @Override
    public List<Ristorante> cercaRistorante(CriteriRicerca criteri) throws TheKnifeException {
        if (criteri == null || criteri.getLuogo() == null || criteri.getLuogo().isBlank()) {
            throw new TheKnifeException("Il luogo e' un criterio di ricerca obbligatorio.");
        }
        try {
            return ristoranteDAO.cerca(criteri);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile completare la ricerca dei ristoranti.", e);
        }
    }

    @Override
    public Ristorante visualizzaRistorante(int idRistorante) throws TheKnifeException {
        try {
            Ristorante r = ristoranteDAO.trovaPerId(idRistorante);
            if (r == null) {
                throw new TheKnifeException("Ristorante non trovato.");
            }
            return r;
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile recuperare il ristorante.", e);
        }
    }

    @Override
    public List<Recensione> visualizzaRecensioni(int idRistorante) throws TheKnifeException {
        try {
            return recensioneDAO.trovaPerRistorante(idRistorante);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile recuperare le recensioni.", e);
        }
    }

    @Override
    public List<String> elencoTipiCucina() throws TheKnifeException {
        try {
            return ristoranteDAO.elencoTipiCucina();
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile recuperare i tipi di cucina.", e);
        }
    }

    @Override
    public List<String> elencoCittaPerNazione(String nazione) throws TheKnifeException {
        if (nazione == null || nazione.isBlank()) {
            return List.of();
        }
        try {
            return ristoranteDAO.elencoCittaPerNazione(nazione);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile recuperare le citta'.", e);
        }
    }

    @Override
    public Utente registrazione(Utente nuovoUtente, String passwordChiaro) throws TheKnifeException {
        if (nuovoUtente == null || nuovoUtente.getNome() == null || nuovoUtente.getNome().isBlank()
                || nuovoUtente.getCognome() == null || nuovoUtente.getCognome().isBlank()
                || nuovoUtente.getEmail() == null || nuovoUtente.getEmail().isBlank()
                || nuovoUtente.getUsername() == null || nuovoUtente.getUsername().isBlank()) {
            throw new TheKnifeException("Nome, cognome, email e username sono campi obbligatori.");
        }
        if (nuovoUtente.getUsername().length() < 3 || nuovoUtente.getUsername().length() > 30) {
            throw new TheKnifeException("Lo username deve essere lungo tra 3 e 30 caratteri.");
        }
        if (passwordChiaro == null || passwordChiaro.length() < 8) {
            throw new TheKnifeException("La password deve contenere almeno 8 caratteri.");
        }
        try {
            if (utenteDAO.esisteEmailPerRuolo(nuovoUtente.getEmail(), nuovoUtente.getRuolo())) {
                String ruoloLeggibile = nuovoUtente.getRuolo() == Ruolo.CLIENTE ? "cliente" : "ristoratore";
                throw new TheKnifeException("Esiste gia' un account " + ruoloLeggibile + " registrato con questa email.");
            }
            if (utenteDAO.esisteUsername(nuovoUtente.getUsername())) {
                throw new TheKnifeException("Questo username e' gia' in uso: scegline un altro.");
            }
            String hash = PasswordUtil.hash(passwordChiaro);
            return utenteDAO.creaUtente(nuovoUtente, hash);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile completare la registrazione.", e);
        }
    }

    @Override
    public Utente login(String email, String passwordChiaro, Ruolo ruolo) throws TheKnifeException {
        try {
            UtenteCredenziali credenziali = utenteDAO.trovaCredenzialiPerEmailERuolo(email, ruolo);
            if (credenziali == null || !PasswordUtil.verifica(passwordChiaro, credenziali.getPasswordHash())) {
                throw new TheKnifeException("Email o password non corrette.");
            }
            return credenziali.getUtente();
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile completare il login.", e);
        }
    }

    @Override
    public void aggiungiPreferito(int idCliente, int idRistorante) throws TheKnifeException {
        try {
            if (ristoranteDAO.trovaPerId(idRistorante) == null) {
                throw new TheKnifeException("Ristorante non trovato.");
            }
            preferitoDAO.aggiungi(idCliente, idRistorante);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile aggiungere il ristorante ai preferiti.", e);
        }
    }

    @Override
    public void rimuoviPreferito(int idCliente, int idRistorante) throws TheKnifeException {
        try {
            preferitoDAO.rimuovi(idCliente, idRistorante);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile rimuovere il ristorante dai preferiti.", e);
        }
    }

    @Override
    public List<Ristorante> visualizzaPreferiti(int idCliente) throws TheKnifeException {
        try {
            return ristoranteDAO.trovaPreferitiDiCliente(idCliente);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile recuperare i ristoranti preferiti.", e);
        }
    }

    @Override
    public Recensione aggiungiRecensione(int idCliente, int idRistorante, int stelle, String testo) throws TheKnifeException {
        validaRecensione(stelle, testo);
        try {
            if (ristoranteDAO.trovaPerId(idRistorante) == null) {
                throw new TheKnifeException("Ristorante non trovato.");
            }
            return recensioneDAO.inserisci(idRistorante, idCliente, stelle, testo);
        } catch (SQLException e) {
            if (isViolazioneUnicita(e)) {
                throw new TheKnifeException("Hai gia' inserito una recensione per questo ristorante: usa 'modifica'.");
            }
            throw erroreTecnico("Impossibile salvare la recensione.", e);
        }
    }

    @Override
    public void modificaRecensione(int idCliente, int idRecensione, int stelle, String testo) throws TheKnifeException {
        validaRecensione(stelle, testo);
        try {
            boolean aggiornata = recensioneDAO.modifica(idRecensione, idCliente, stelle, testo);
            if (!aggiornata) {
                throw new TheKnifeException("Recensione non trovata o non appartenente all'utente corrente.");
            }
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile modificare la recensione.", e);
        }
    }

    @Override
    public void eliminaRecensione(int idCliente, int idRecensione) throws TheKnifeException {
        try {
            boolean eliminata = recensioneDAO.elimina(idRecensione, idCliente);
            if (!eliminata) {
                throw new TheKnifeException("Recensione non trovata o non appartenente all'utente corrente.");
            }
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile eliminare la recensione.", e);
        }
    }

    @Override
    public List<Recensione> visualizzaRecensioniProprie(int idCliente) throws TheKnifeException {
        try {
            return recensioneDAO.trovaPerCliente(idCliente);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile recuperare le recensioni dell'utente.", e);
        }
    }

    @Override
    public Ristorante aggiungiRistorante(int idRistoratore, Ristorante nuovoRistorante, List<String> tipiCucina) throws TheKnifeException {
        validaRistorante(nuovoRistorante);
        nuovoRistorante.setIdRistoratore(idRistoratore);
        try {
            return ristoranteDAO.inserisci(nuovoRistorante, tipiCucina);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile creare il ristorante.", e);
        }
    }

    @Override
    public List<Ristorante> visualizzaRiepilogo(int idRistoratore) throws TheKnifeException {
        try {
            return ristoranteDAO.trovaDiGestore(idRistoratore);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile recuperare il riepilogo dei ristoranti.", e);
        }
    }

    @Override
    public void rispostaRecensione(int idRistoratore, int idRecensione, String risposta) throws TheKnifeException {
        if (risposta == null || risposta.isBlank()) {
            throw new TheKnifeException("Il testo della risposta non puo' essere vuoto.");
        }
        try {
            Recensione recensione = recensioneDAO.trovaPerId(idRecensione);
            if (recensione == null) {
                throw new TheKnifeException("Recensione non trovata.");
            }
            Ristorante ristorante = ristoranteDAO.trovaPerId(recensione.getIdRistorante());
            if (ristorante == null || ristorante.getIdRistoratore() != idRistoratore) {
                throw new TheKnifeException("La recensione non riguarda un tuo ristorante.");
            }
            recensioneDAO.rispondi(idRecensione, risposta);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile salvare la risposta alla recensione.", e);
        }
    }

    // ------------------------------------------------------------
    // Validazioni comuni e gestione errori tecnici
    // ------------------------------------------------------------

    private void validaRecensione(int stelle, String testo) throws TheKnifeException {
        if (stelle < 1 || stelle > 5) {
            throw new TheKnifeException("Il numero di stelle deve essere compreso tra 1 e 5.");
        }
        if (testo == null || testo.isBlank()) {
            throw new TheKnifeException("Il testo della recensione non puo' essere vuoto.");
        }
    }

    private void validaRistorante(Ristorante r) throws TheKnifeException {
        if (r == null || r.getNome() == null || r.getNome().isBlank()
                || r.getNazione() == null || r.getNazione().isBlank()
                || r.getCitta() == null || r.getCitta().isBlank()
                || r.getIndirizzo() == null || r.getIndirizzo().isBlank()) {
            throw new TheKnifeException("Nome, nazione, citta' e indirizzo del ristorante sono obbligatori.");
        }
        if (r.getFasciaPrezzo() == null || r.getFasciaPrezzo().compareTo(BigDecimal.ZERO) < 0) {
            throw new TheKnifeException("La fascia di prezzo deve essere un valore non negativo.");
        }
    }

    private boolean isViolazioneUnicita(SQLException e) {
        return "23505".equals(e.getSQLState());
    }

    private TheKnifeException erroreTecnico(String messaggioUtente, SQLException causa) {
        LOG.log(Level.SEVERE, messaggioUtente, causa);
        return new TheKnifeException(messaggioUtente, causa);
    }

    /** Espone il ruolo atteso per un id utente, usato da GestoreClient per i controlli di autorizzazione. */
    public Utente trovaUtentePerId(int id) throws TheKnifeException {
        try {
            return utenteDAO.trovaPerId(id);
        } catch (SQLException e) {
            throw erroreTecnico("Impossibile recuperare l'utente.", e);
        }
    }

    public static boolean isRistoratore(Utente u) {
        return u != null && u.getRuolo() == Ruolo.RISTORATORE;
    }

    public static boolean isCliente(Utente u) {
        return u != null && u.getRuolo() == Ruolo.CLIENTE;
    }
}
