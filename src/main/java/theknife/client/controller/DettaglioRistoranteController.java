/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import theknife.common.model.Ristorante;

/**
 * Schermata di dettaglio di un ristorante: caratteristiche, recensioni
 * (con lo username di chi le ha scritte) e azioni specifiche per ruolo:
 * <ul>
 *   <li>cliente: aggiungi/rimuovi dai preferiti, inserisci/modifica/
 *       elimina la propria recensione ({@code aggiungiPreferito()},
 *       {@code rimuoviPreferito()}, {@code aggiungiRecensione()},
 *       {@code modificaRecensione()}, {@code eliminaRecensione()});</li>
 *   <li>ristoratore proprietario del ristorante: rispondi a ciascuna
 *       recensione ({@code rispostaRecensione()}).</li>
 * </ul>
 */
public class DettaglioRistoranteController {

    @FXML private Label etichettaNome;
    @FXML private Label etichettaErrore;
    @FXML private Label etichettaIndirizzo;
    @FXML private Label etichettaLuogo;
    @FXML private Label etichettaCucina;
    @FXML private Label etichettaPrezzo;
    @FXML private Label etichettaServizi;
    @FXML private Label etichettaValutazione;
    @FXML private VBox sezioneCliente;
    @FXML private Button bottonePreferito;
    @FXML private VBox contenitoreRecensionePropria;
    @FXML private VBox contenitoreRecensioni;

    private Ristorante ristorante;
    private List<Recensione> recensioni = new ArrayList<>();
    private boolean isPreferito;

    public void carica(int idRistorante) {
        etichettaErrore.setText("");
        EseguiAsync.esegui(
                () -> SessioneClient.getServizi().visualizzaRistorante(idRistorante),
                r -> {
                    this.ristorante = r;
                    mostraInfoRistorante();
                    caricaRecensioni(idRistorante);
                },
                errore -> etichettaErrore.setText(errore.getMessage())
        );
    }

    private void mostraInfoRistorante() {
        etichettaNome.setText(ristorante.getNome());
        etichettaIndirizzo.setText(ristorante.getIndirizzo());
        etichettaLuogo.setText(ristorante.getCitta() + ", " + ristorante.getNazione());
        etichettaCucina.setText("Cucina: " + String.join(", ", ristorante.getTipiCucina()));
        etichettaPrezzo.setText("Fascia di prezzo media: " + Formattatori.prezzo(ristorante.getFasciaPrezzo()));
        etichettaServizi.setText("Delivery: " + (ristorante.isDelivery() ? "si" : "no")
                + "  -  Prenotazione online: " + (ristorante.isPrenotazioneOnline() ? "si" : "no"));
        etichettaValutazione.setText(String.format("Valutazione media: %s (%.1f su %d recensioni)",
                Formattatori.stelle(ristorante.getMediaStelle()), ristorante.getMediaStelle(), ristorante.getNumeroRecensioni()));

        boolean cliente = SessioneClient.isCliente();
        sezioneCliente.setVisible(cliente);
        sezioneCliente.setManaged(cliente);
        if (cliente) {
            aggiornaStatoPreferito();
        }
    }

    private void aggiornaStatoPreferito() {
        int idCliente = SessioneClient.getUtenteCorrente().getId();
        EseguiAsync.esegui(
                () -> SessioneClient.getServizi().visualizzaPreferiti(idCliente),
                preferiti -> {
                    isPreferito = preferiti.stream().anyMatch(r -> r.getId() == ristorante.getId());
                    bottonePreferito.setText(isPreferito ? "Rimuovi dai preferiti" : "Aggiungi ai preferiti");
                },
                errore -> etichettaErrore.setText(errore.getMessage())
        );
    }

    @FXML
    private void alternaPreferito() {
        int idCliente = SessioneClient.getUtenteCorrente().getId();
        int idRistorante = ristorante.getId();
        if (isPreferito) {
            EseguiAsync.esegui(
                    () -> SessioneClient.getServizi().rimuoviPreferito(idCliente, idRistorante),
                    () -> {
                        isPreferito = false;
                        bottonePreferito.setText("Aggiungi ai preferiti");
                    },
                    errore -> etichettaErrore.setText(errore.getMessage())
            );
        } else {
            EseguiAsync.esegui(
                    () -> SessioneClient.getServizi().aggiungiPreferito(idCliente, idRistorante),
                    () -> {
                        isPreferito = true;
                        bottonePreferito.setText("Rimuovi dai preferiti");
                    },
                    errore -> etichettaErrore.setText(errore.getMessage())
            );
        }
    }

    private void caricaRecensioni(int idRistorante) {
        EseguiAsync.esegui(
                () -> SessioneClient.getServizi().visualizzaRecensioni(idRistorante),
                lista -> {
                    this.recensioni = lista;
                    mostraRecensioni();
                    mostraSezioneRecensionePropria();
                },
                errore -> etichettaErrore.setText(errore.getMessage())
        );
    }

    private void mostraRecensioni() {
        contenitoreRecensioni.getChildren().clear();
        boolean ristoratoreProprietario = SessioneClient.isRistoratore()
                && SessioneClient.getUtenteCorrente().getId() == ristorante.getIdRistoratore();
        if (recensioni.isEmpty()) {
            contenitoreRecensioni.getChildren().add(new Label("Nessuna recensione per questo ristorante."));
            return;
        }
        for (Recensione r : recensioni) {
            contenitoreRecensioni.getChildren().add(costruisciCardRecensione(r, ristoratoreProprietario));
        }
    }

    private Node costruisciCardRecensione(Recensione r, boolean permettiRisposta) {
        VBox card = new VBox(4);
        card.getStyleClass().add("card-recensione");

        Label autore = new Label(r.getUsernameCliente());
        autore.getStyleClass().add("sottotitolo");
        Label stelle = new Label(Formattatori.stelle(r.getStelle()) + "   " + r.getDataRecensione().format(Formattatori.DATA));
        Label testo = new Label(r.getTesto());
        testo.setWrapText(true);
        card.getChildren().addAll(autore, stelle, testo);

        if (r.isRisposta()) {
            Label risposta = new Label("Risposta del ristoratore: " + r.getRispostaTesto());
            risposta.setWrapText(true);
            risposta.getStyleClass().add("risposta");
            card.getChildren().add(risposta);
        }

        if (permettiRisposta) {
            TextArea campoRisposta = new TextArea(r.getRispostaTesto() != null ? r.getRispostaTesto() : "");
            campoRisposta.setPrefRowCount(2);
            Button bottoneRispondi = new Button(r.isRisposta() ? "Aggiorna risposta" : "Rispondi");
            Label erroreCard = new Label();
            erroreCard.getStyleClass().add("errore");
            bottoneRispondi.setOnAction(e -> {
                String testoRisposta = campoRisposta.getText();
                if (testoRisposta == null || testoRisposta.isBlank()) {
                    erroreCard.setText("Il testo della risposta non puo' essere vuoto.");
                    return;
                }
                int idRistoratore = SessioneClient.getUtenteCorrente().getId();
                EseguiAsync.esegui(
                        () -> SessioneClient.getServizi().rispostaRecensione(idRistoratore, r.getId(), testoRisposta),
                        () -> {
                            r.setRispostaTesto(testoRisposta);
                            bottoneRispondi.setText("Aggiorna risposta");
                            erroreCard.setText("Risposta salvata.");
                        },
                        errore -> erroreCard.setText(errore.getMessage())
                );
            });
            card.getChildren().addAll(campoRisposta, bottoneRispondi, erroreCard);
        }
        Navigatore.applicaHoverATuttiIBottoni(card);
        return card;
    }

    private void mostraSezioneRecensionePropria() {
        contenitoreRecensionePropria.getChildren().clear();
        if (!SessioneClient.isCliente()) {
            return;
        }
        int idCliente = SessioneClient.getUtenteCorrente().getId();
        Optional<Recensione> propria = recensioni.stream().filter(r -> r.getIdCliente() == idCliente).findFirst();
        contenitoreRecensionePropria.getChildren().add(
                propria.isPresent() ? costruisciFormModificaRecensione(propria.get()) : costruisciFormNuovaRecensione());
    }

    private Node costruisciFormNuovaRecensione() {
        VBox box = new VBox(6);
        Label titolo = new Label("Lascia una recensione");
        ComboBox<Integer> stelle = new ComboBox<>();
        stelle.getItems().addAll(1, 2, 3, 4, 5);
        stelle.setValue(5);
        TextArea testo = new TextArea();
        testo.setPromptText("Scrivi la tua recensione...");
        testo.setPrefRowCount(3);
        Button invia = new Button("Invia recensione");
        Label errore = new Label();
        errore.getStyleClass().add("errore");

        invia.setOnAction(e -> {
            int idCliente = SessioneClient.getUtenteCorrente().getId();
            int stelleValore = stelle.getValue();
            String testoValore = testo.getText();
            EseguiAsync.esegui(
                    () -> SessioneClient.getServizi().aggiungiRecensione(idCliente, ristorante.getId(), stelleValore, testoValore),
                    nuova -> caricaRecensioni(ristorante.getId()),
                    err -> errore.setText(err.getMessage())
            );
        });
        box.getChildren().addAll(titolo, stelle, testo, invia, errore);
        Navigatore.applicaHoverATuttiIBottoni(box);
        return box;
    }

    private Node costruisciFormModificaRecensione(Recensione propria) {
        VBox box = new VBox(6);
        Label titolo = new Label("La tua recensione");
        ComboBox<Integer> stelle = new ComboBox<>();
        stelle.getItems().addAll(1, 2, 3, 4, 5);
        stelle.setValue(propria.getStelle());
        TextArea testo = new TextArea(propria.getTesto());
        testo.setPrefRowCount(3);
        Button salva = new Button("Salva modifiche");
        Button elimina = new Button("Elimina recensione");
        Label errore = new Label();
        errore.getStyleClass().add("errore");

        salva.setOnAction(e -> {
            int idCliente = SessioneClient.getUtenteCorrente().getId();
            int stelleValore = stelle.getValue();
            String testoValore = testo.getText();
            EseguiAsync.esegui(
                    () -> SessioneClient.getServizi().modificaRecensione(idCliente, propria.getId(), stelleValore, testoValore),
                    () -> caricaRecensioni(ristorante.getId()),
                    err -> errore.setText(err.getMessage())
            );
        });
        elimina.setOnAction(e -> {
            int idCliente = SessioneClient.getUtenteCorrente().getId();
            EseguiAsync.esegui(
                    () -> SessioneClient.getServizi().eliminaRecensione(idCliente, propria.getId()),
                    () -> caricaRecensioni(ristorante.getId()),
                    err -> errore.setText(err.getMessage())
            );
        });

        HBox pulsanti = new HBox(8, salva, elimina);
        box.getChildren().addAll(titolo, stelle, testo, pulsanti, errore);
        Navigatore.applicaHoverATuttiIBottoni(box);
        return box;
    }
}
