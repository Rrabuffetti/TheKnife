/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import theknife.client.EseguiAsync;
import theknife.client.Navigatore;
import theknife.client.SessioneClient;
import theknife.common.model.Ristorante;
import theknife.common.model.Utente;
import theknife.common.protocol.CriteriRicerca;

/**
 * Schermata iniziale post-accesso: elenco dei ristoranti vicini al luogo
 * indicato dall'utente ospite, oppure al domicilio dell'utente
 * autenticato (cliente o ristoratore), come richiesto dalle specifiche.
 */
public class HomeController {

    @FXML private Label titolo;
    @FXML private Label etichettaErrore;
    @FXML private ListView<Ristorante> lista;

    @FXML
    public void initialize() {
        ControllerUtil.configuraListaRistoranti(lista, this::apriDettaglio);
    }

    /** Usato dopo login/registrazione: cerca vicino al domicilio dell'utente autenticato. */
    public void carica() {
        Utente u = SessioneClient.getUtenteCorrente();
        String luogo = null;
        if (u != null) {
            luogo = (u.getCittaDomicilio() != null && !u.getCittaDomicilio().isBlank())
                    ? u.getCittaDomicilio() : u.getNazioneDomicilio();
        }
        if (luogo == null || luogo.isBlank()) {
            titolo.setText("Ristoranti");
            etichettaErrore.setText("Nessun luogo di domicilio impostato: usa la ricerca per trovare ristoranti.");
            lista.getItems().clear();
            return;
        }
        eseguiRicerca(luogo);
    }

    /** Usato per l'utente ospite: cerca vicino al luogo indicato manualmente. */
    public void caricaConLuogo(String luogo) {
        eseguiRicerca(luogo);
    }

    private void eseguiRicerca(String luogo) {
        titolo.setText("Ristoranti vicino a: " + luogo);
        etichettaErrore.setText("");
        CriteriRicerca criteri = new CriteriRicerca(luogo);
        EseguiAsync.esegui(
                () -> SessioneClient.getServizi().cercaRistorante(criteri),
                risultati -> lista.getItems().setAll(risultati),
                errore -> etichettaErrore.setText(errore.getMessage())
        );
    }

    @FXML
    private void vediDettagli() {
        Ristorante selezionato = lista.getSelectionModel().getSelectedItem();
        if (selezionato != null) {
            apriDettaglio(selezionato);
        }
    }

    private void apriDettaglio(Ristorante r) {
        DettaglioRistoranteController controller = Navigatore.vaiA("dettaglioRistorante.fxml");
        controller.carica(r.getId());
    }
}
