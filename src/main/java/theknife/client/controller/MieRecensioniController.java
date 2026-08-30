/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import theknife.client.EseguiAsync;
import theknife.client.Formattatori;
import theknife.client.Navigatore;
import theknife.client.SessioneClient;
import theknife.common.model.Recensione;

/**
 * Schermata "Le mie recensioni": elenca i ristoranti per cui il cliente
 * ha inserito una recensione, con la recensione stessa, modificabile o
 * cancellabile direttamente da qui (funzionalita' {@code modificaRecensione()}
 * e {@code eliminaRecensione()}).
 */
public class MieRecensioniController {

    @FXML private Label etichettaErrore;
    @FXML private VBox contenitore;

    public void carica() {
        int idCliente = SessioneClient.getUtenteCorrente().getId();
        EseguiAsync.esegui(
                () -> SessioneClient.getServizi().visualizzaRecensioniProprie(idCliente),
                lista -> {
                    contenitore.getChildren().clear();
                    if (lista.isEmpty()) {
                        contenitore.getChildren().add(new Label("Non hai ancora inserito nessuna recensione."));
                    }
                    for (Recensione r : lista) {
                        contenitore.getChildren().add(costruisciCard(r));
                    }
                },
                errore -> etichettaErrore.setText(errore.getMessage())
        );
    }

    private Node costruisciCard(Recensione r) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card-recensione");

        Label nomeRistorante = new Label(r.getNomeRistorante());
        nomeRistorante.getStyleClass().add("sottotitolo");
        Button apriRistorante = new Button("Vai al ristorante");
        apriRistorante.setOnAction(e -> {
            DettaglioRistoranteController controller = Navigatore.vaiA("dettaglioRistorante.fxml");
            controller.carica(r.getIdRistorante());
        });

        ComboBox<Integer> stelle = new ComboBox<>();
        stelle.getItems().addAll(1, 2, 3, 4, 5);
        stelle.setValue(r.getStelle());
        TextArea testo = new TextArea(r.getTesto());
        testo.setPrefRowCount(3);

        if (r.isRisposta()) {
            Label risposta = new Label("Risposta del ristoratore: " + r.getRispostaTesto());
            risposta.setWrapText(true);
            risposta.getStyleClass().add("risposta");
            card.getChildren().add(risposta);
        }

        Button salva = new Button("Salva modifiche");
        Button elimina = new Button("Elimina");
        Label erroreCard = new Label();
        erroreCard.getStyleClass().add("errore");

        salva.setOnAction(e -> {
            int idCliente = SessioneClient.getUtenteCorrente().getId();
            int stelleValore = stelle.getValue();
            String testoValore = testo.getText();
            EseguiAsync.esegui(
                    () -> SessioneClient.getServizi().modificaRecensione(idCliente, r.getId(), stelleValore, testoValore),
                    this::carica,
                    err -> erroreCard.setText(err.getMessage())
            );
        });
        elimina.setOnAction(e -> {
            int idCliente = SessioneClient.getUtenteCorrente().getId();
            EseguiAsync.esegui(
                    () -> SessioneClient.getServizi().eliminaRecensione(idCliente, r.getId()),
                    this::carica,
                    err -> erroreCard.setText(err.getMessage())
            );
        });

        HBox intestazione = new HBox(10, nomeRistorante, apriRistorante);
        HBox pulsanti = new HBox(8, salva, elimina);
        card.getChildren().addAll(intestazione, new Label(Formattatori.stelle(r.getStelle()) + "  " + r.getDataRecensione().format(Formattatori.DATA)),
                stelle, testo, pulsanti, erroreCard);
        Navigatore.applicaHoverATuttiIBottoni(card);
        return card;
    }
}
