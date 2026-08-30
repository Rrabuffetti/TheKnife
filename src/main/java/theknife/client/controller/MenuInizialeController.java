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
import javafx.scene.control.TextField;

import theknife.client.Navigatore;

/**
 * Schermata iniziale: login, registrazione o accesso come ospite
 * indicando solo un luogo, come richiesto dalle specifiche di progetto.
 */
public class MenuInizialeController {

    @FXML private TextField campoLuogo;
    @FXML private Label etichettaErrore;

    @FXML
    private void vaiLogin() {
        Navigatore.vaiA("login.fxml");
    }

    @FXML
    private void vaiRegistrazione() {
        Navigatore.vaiA("registrazione.fxml");
    }

    @FXML
    private void continuaComeGuest() {
        String luogo = campoLuogo.getText() == null ? "" : campoLuogo.getText().trim();
        if (luogo.isEmpty()) {
            etichettaErrore.setText("Indica un luogo per continuare come ospite.");
            return;
        }
        HomeController controller = Navigatore.vaiA("home.fxml");
        controller.caricaConLuogo(luogo);
    }
}
