/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client.controller;

import java.math.BigDecimal;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import theknife.client.EseguiAsync;
import theknife.client.Navigatore;
import theknife.client.SessioneClient;
import theknife.common.model.Ristorante;
import theknife.common.protocol.CriteriRicerca;

/**
 * Schermata di ricerca ristoranti: il luogo e' l'unico criterio
 * obbligatorio, tutti gli altri sono opzionali e combinabili tra loro
 * (funzionalita' {@code cercaRistorante()}).
 */
public class RicercaController {

    private static final String INDIFFERENTE = "Indifferente";

    @FXML private TextField campoLuogo;
    @FXML private ComboBox<String> comboTipoCucina;
    @FXML private TextField campoPrezzoMin;
    @FXML private TextField campoPrezzoMax;
    @FXML private ComboBox<Integer> comboStelleMinime;
    @FXML private CheckBox checkDelivery;
    @FXML private CheckBox checkPrenotazione;
    @FXML private Label etichettaErrore;
    @FXML private Label etichettaRisultati;
    @FXML private ListView<Ristorante> lista;

    @FXML
    public void initialize() {
        comboStelleMinime.getItems().addAll(0, 1, 2, 3, 4, 5);
        comboStelleMinime.setValue(0);
        ControllerUtil.configuraListaRistoranti(lista, this::apriDettaglio);

        comboTipoCucina.getItems().add(INDIFFERENTE);
        comboTipoCucina.setValue(INDIFFERENTE);
        EseguiAsync.esegui(
                () -> SessioneClient.getServizi().elencoTipiCucina(),
                tipi -> comboTipoCucina.getItems().addAll(tipi),
                errore -> etichettaErrore.setText(errore.getMessage())
        );
    }

    @FXML
    private void cerca() {
        String luogo = campoLuogo.getText() == null ? "" : campoLuogo.getText().trim();
        if (luogo.isEmpty()) {
            etichettaErrore.setText("Il luogo e' un criterio di ricerca obbligatorio.");
            return;
        }

        CriteriRicerca criteri = new CriteriRicerca(luogo);
        String cucinaScelta = comboTipoCucina.getValue();
        if (cucinaScelta != null && !cucinaScelta.equals(INDIFFERENTE)) {
            criteri.setTipoCucina(cucinaScelta);
        }
        BigDecimal prezzoMin = numeroOppureNull(campoPrezzoMin.getText());
        BigDecimal prezzoMax = numeroOppureNull(campoPrezzoMax.getText());
        if (prezzoMin != null) criteri.setPrezzoMinimo(prezzoMin);
        if (prezzoMax != null) criteri.setPrezzoMassimo(prezzoMax);

        if (!checkDelivery.isIndeterminate()) {
            criteri.setDelivery(checkDelivery.isSelected());
        }
        if (!checkPrenotazione.isIndeterminate()) {
            criteri.setPrenotazioneOnline(checkPrenotazione.isSelected());
        }
        Integer stelleMinime = comboStelleMinime.getValue();
        if (stelleMinime != null && stelleMinime > 0) {
            criteri.setStelleMinime(stelleMinime.doubleValue());
        }

        etichettaErrore.setText("");
        EseguiAsync.esegui(
                () -> SessioneClient.getServizi().cercaRistorante(criteri),
                risultati -> {
                    lista.getItems().setAll(risultati);
                    etichettaRisultati.setText(risultati.size() + " ristoranti trovati.");
                },
                errore -> etichettaErrore.setText(errore.getMessage())
        );
    }

    private BigDecimal numeroOppureNull(String testo) {
        if (testo == null || testo.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(testo.trim());
        } catch (NumberFormatException e) {
            return null;
        }
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
