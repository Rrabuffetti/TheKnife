/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import theknife.client.Navigatore;
import theknife.client.SessioneClient;
import theknife.common.model.Utente;

/**
 * Controller della barra di navigazione superiore, sempre visibile: i
 * pulsanti mostrati variano in base allo stato della sessione (ospite,
 * cliente autenticato, ristoratore autenticato).
 */
public class BarraNavigazioneController {

    @FXML private Button bottoneHome;
    @FXML private Button bottoneCerca;
    @FXML private Button bottonePreferiti;
    @FXML private Button bottoneMieRecensioni;
    @FXML private Button bottoneMieiRistoranti;
    @FXML private Button bottoneAggiungiRistorante;
    @FXML private Button bottoneLogin;
    @FXML private Button bottoneRegistrati;
    @FXML private Button bottoneLogout;
    @FXML private Label etichettaUtente;

    @FXML
    public void initialize() {
        aggiorna();
    }

    /** Da richiamare dopo ogni login/registrazione/logout per riflettere il nuovo stato della sessione. */
    public void aggiorna() {
        boolean autenticato = SessioneClient.isAutenticato();
        boolean cliente = SessioneClient.isCliente();
        boolean ristoratore = SessioneClient.isRistoratore();

        mostra(bottonePreferiti, cliente);
        mostra(bottoneMieRecensioni, cliente);
        mostra(bottoneMieiRistoranti, ristoratore);
        mostra(bottoneAggiungiRistorante, ristoratore);
        mostra(bottoneLogin, !autenticato);
        mostra(bottoneRegistrati, !autenticato);
        mostra(bottoneLogout, autenticato);

        Utente u = SessioneClient.getUtenteCorrente();
        etichettaUtente.setText(u == null ? "Ospite" : u.getNomeCompleto() + " (" + u.getRuolo() + ")");
    }

    private void mostra(Button bottone, boolean visibile) {
        bottone.setVisible(visibile);
        bottone.setManaged(visibile);
    }

    @FXML
    private void vaiHome() {
        HomeController c = Navigatore.vaiA("home.fxml");
        c.carica();
    }

    @FXML
    private void vaiCerca() {
        Navigatore.vaiA("ricerca.fxml");
    }

    @FXML
    private void vaiPreferiti() {
        PreferitiController c = Navigatore.vaiA("preferiti.fxml");
        c.carica();
    }

    @FXML
    private void vaiMieRecensioni() {
        MieRecensioniController c = Navigatore.vaiA("mieRecensioni.fxml");
        c.carica();
    }

    @FXML
    private void vaiMieiRistoranti() {
        MieiRistorantiController c = Navigatore.vaiA("mieiRistoranti.fxml");
        c.carica();
    }

    @FXML
    private void vaiAggiungiRistorante() {
        Navigatore.vaiA("aggiungiRistorante.fxml");
    }

    @FXML
    private void vaiLogin() {
        Navigatore.vaiA("login.fxml");
    }

    @FXML
    private void vaiRegistrazione() {
        Navigatore.vaiA("registrazione.fxml");
    }

    @FXML
    private void logout() {
        SessioneClient.logout();
        aggiorna();
        Navigatore.vaiA("menuIniziale.fxml");
    }
}
