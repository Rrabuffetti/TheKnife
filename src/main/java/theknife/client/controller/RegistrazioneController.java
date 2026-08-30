/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client.controller;

import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import theknife.client.EseguiAsync;
import theknife.client.Nazioni;
import theknife.client.Navigatore;
import theknife.client.SessioneClient;
import theknife.common.model.Cliente;
import theknife.common.model.Ristoratore;
import theknife.common.model.Utente;

/**
 * Schermata di registrazione di un nuovo utente: permette di inserire
 * tutti i dati dell'utente e il tipo di account (cliente o ristoratore),
 * corrispondente a {@code registrazione()} nelle specifiche.
 */
public class RegistrazioneController {

    @FXML private TextField campoNome;
    @FXML private TextField campoCognome;
    @FXML private TextField campoEmail;
    @FXML private TextField campoUsername;
    @FXML private PasswordField campoPassword;
    @FXML private PasswordField campoConfermaPassword;
    @FXML private DatePicker campoDataNascita;
    @FXML private ComboBox<String> campoNazioneDomicilio;
    @FXML private ComboBox<String> campoCittaDomicilio;
    @FXML private RadioButton radioCliente;
    @FXML private RadioButton radioRistoratore;
    @FXML private Label etichettaErrore;

    @FXML
    public void initialize() {
        ToggleGroup gruppo = new ToggleGroup();
        radioCliente.setToggleGroup(gruppo);
        radioRistoratore.setToggleGroup(gruppo);

        campoNazioneDomicilio.getItems().addAll(Nazioni.ELENCO);
        campoNazioneDomicilio.valueProperty().addListener((oss, vecchia, nazioneScelta) -> {
            campoCittaDomicilio.getItems().clear();
            if (nazioneScelta == null) {
                return;
            }
            EseguiAsync.esegui(
                    () -> SessioneClient.getServizi().elencoCittaPerNazione(nazioneScelta),
                    citta -> campoCittaDomicilio.getItems().setAll(citta),
                    errore -> { /* suggerimenti non essenziali: si puo' comunque scrivere la citta' a mano */ }
            );
        });
    }

    @FXML
    private void registrati() {
        String nome = testo(campoNome);
        String cognome = testo(campoCognome);
        String email = testo(campoEmail);
        String username = testo(campoUsername);
        String password = campoPassword.getText();
        String conferma = campoConfermaPassword.getText();
        LocalDate dataNascita = campoDataNascita.getValue();
        String nazione = campoNazioneDomicilio.getValue();
        String citta = campoCittaDomicilio.getEditor().getText() == null ? "" : campoCittaDomicilio.getEditor().getText().trim();

        if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || username.isEmpty()) {
            etichettaErrore.setText("Nome, cognome, email e username sono obbligatori.");
            return;
        }
        if (password == null || !password.equals(conferma)) {
            etichettaErrore.setText("Le due password inserite non coincidono.");
            return;
        }
        etichettaErrore.setText("");

        Utente nuovoUtente = radioRistoratore.isSelected()
                ? new Ristoratore(0, nome, cognome, email, username, dataNascita, nazione, citta, null)
                : new Cliente(0, nome, cognome, email, username, dataNascita, nazione, citta, null);

        EseguiAsync.esegui(
                () -> SessioneClient.getServizi().registrazione(nuovoUtente, password),
                (Utente registrato) -> {
                    SessioneClient.impostaUtente(registrato);
                    Navigatore.aggiornaBarra();
                    HomeController controller = Navigatore.vaiA("home.fxml");
                    controller.carica();
                },
                errore -> etichettaErrore.setText(errore.getMessage())
        );
    }

    private String testo(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    @FXML
    private void tornaAlMenu() {
        Navigatore.vaiA("menuIniziale.fxml");
    }
}
