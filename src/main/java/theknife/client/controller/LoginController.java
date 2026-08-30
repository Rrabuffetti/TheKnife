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
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import theknife.client.EseguiAsync;
import theknife.client.Navigatore;
import theknife.client.SessioneClient;
import theknife.common.model.Ruolo;
import theknife.common.model.Utente;

public class LoginController {

    @FXML private TextField campoEmail;
    @FXML private PasswordField campoPassword;
    @FXML private RadioButton radioCliente;
    @FXML private RadioButton radioRistoratore;
    @FXML private Label etichettaErrore;

    @FXML
    public void initialize() {
        ToggleGroup gruppo = new ToggleGroup();
        radioCliente.setToggleGroup(gruppo);
        radioRistoratore.setToggleGroup(gruppo);
    }

    @FXML
    private void accedi() {
        String email = campoEmail.getText() == null ? "" : campoEmail.getText().trim();
        String password = campoPassword.getText();
        if (email.isEmpty() || password == null || password.isEmpty()) {
            etichettaErrore.setText("Inserisci email e password.");
            return;
        }
        etichettaErrore.setText("");
        Ruolo ruoloScelto = radioRistoratore.isSelected() ? Ruolo.RISTORATORE : Ruolo.CLIENTE;

        EseguiAsync.esegui(
                () -> SessioneClient.getServizi().login(email, password, ruoloScelto),
                (Utente utente) -> {
                    SessioneClient.impostaUtente(utente);
                    Navigatore.aggiornaBarra();
                    HomeController controller = Navigatore.vaiA("home.fxml");
                    controller.carica();
                },
                errore -> etichettaErrore.setText(errore.getMessage())
        );
    }

    @FXML
    private void tornaAlMenu() {
        Navigatore.vaiA("menuIniziale.fxml");
    }
}
