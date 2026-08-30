/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import theknife.client.EseguiAsync;
import theknife.client.Nazioni;
import theknife.client.Navigatore;
import theknife.client.SessioneClient;
import theknife.common.model.Ristorante;

/**
 * Schermata di creazione di un nuovo ristorante da parte di un
 * ristoratore autenticato (funzionalita' {@code aggiungiRistorante()}).
 */
public class AggiungiRistoranteController {

    @FXML private TextField campoNome;
    @FXML private ComboBox<String> campoNazione;
    @FXML private ComboBox<String> campoCitta;
    @FXML private TextField campoIndirizzo;
    @FXML private TextField campoFasciaPrezzo;
    @FXML private TextField campoTipiCucina;
    @FXML private CheckBox checkDelivery;
    @FXML private CheckBox checkPrenotazione;
    @FXML private Label etichettaErrore;

    @FXML
    public void initialize() {
        campoNazione.getItems().addAll(Nazioni.ELENCO);
        campoNazione.valueProperty().addListener((oss, vecchia, nazioneScelta) -> {
            campoCitta.getItems().clear();
            if (nazioneScelta == null) {
                return;
            }
            EseguiAsync.esegui(
                    () -> SessioneClient.getServizi().elencoCittaPerNazione(nazioneScelta),
                    citta -> campoCitta.getItems().setAll(citta),
                    errore -> { /* suggerimenti non essenziali: si puo' comunque scrivere la citta' a mano */ }
            );
        });
    }

    @FXML
    private void creaRistorante() {
        BigDecimal fasciaPrezzo;
        try {
            fasciaPrezzo = new BigDecimal(campoFasciaPrezzo.getText().trim());
        } catch (NumberFormatException | NullPointerException e) {
            etichettaErrore.setText("La fascia di prezzo deve essere un numero valido.");
            return;
        }

        Ristorante r = new Ristorante();
        r.setNome(testo(campoNome));
        r.setNazione(campoNazione.getValue());
        r.setCitta(campoCitta.getEditor().getText() == null ? "" : campoCitta.getEditor().getText().trim());
        r.setIndirizzo(testo(campoIndirizzo));
        r.setFasciaPrezzo(fasciaPrezzo);
        r.setDelivery(checkDelivery.isSelected());
        r.setPrenotazioneOnline(checkPrenotazione.isSelected());

        List<String> tipiCucina = Arrays.stream(testo(campoTipiCucina).split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        etichettaErrore.setText("");
        int idRistoratore = SessioneClient.getUtenteCorrente().getId();
        EseguiAsync.esegui(
                () -> SessioneClient.getServizi().aggiungiRistorante(idRistoratore, r, tipiCucina),
                creato -> {
                    DettaglioRistoranteController controller = Navigatore.vaiA("dettaglioRistorante.fxml");
                    controller.carica(creato.getId());
                },
                errore -> etichettaErrore.setText(errore.getMessage())
        );
    }

    private String testo(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }
}
