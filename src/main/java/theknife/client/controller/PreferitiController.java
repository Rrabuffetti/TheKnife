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

/** Schermata "I miei ristoranti preferiti" (funzionalita' {@code visualizzaPreferiti()}). */
public class PreferitiController {

    @FXML private Label etichettaErrore;
    @FXML private ListView<Ristorante> lista;

    @FXML
    public void initialize() {
        ControllerUtil.configuraListaRistoranti(lista, this::apriDettaglio);
    }

    public void carica() {
        int idCliente = SessioneClient.getUtenteCorrente().getId();
        EseguiAsync.esegui(
                () -> SessioneClient.getServizi().visualizzaPreferiti(idCliente),
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
