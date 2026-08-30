/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client;

import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import theknife.client.rete.ConnessioneServer;
import theknife.client.rete.ServiziTKProxy;

/**
 * Punto di ingresso grafico del modulo clientTK. All'avvio si connette al
 * serverTK (l'indirizzo puo' essere passato come argomento a riga di
 * comando "host porta", utile per un avvio rapido da script/collegamento,
 * altrimenti viene chiesto tramite una finestra di dialogo), poi mostra
 * il menu iniziale da cui e' possibile accedere, registrarsi o proseguire
 * come utente ospite indicando un luogo.
 */
public class ClientTK extends Application {

    private static final String HOST_DEFAULT = "localhost";
    private static final int PORTA_DEFAULT = 9090;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("TheKnife");

        if (!connessioneDaArgomenti() && !connettiAlServer(stage)) {
            Platform.exit();
            return;
        }

        BorderPane radice = new BorderPane();
        Navigatore.inizializza(stage, radice);

        FXMLLoader loaderBarra = new FXMLLoader(getClass().getResource("/fxml/barraNavigazione.fxml"));
        Parent barra = loaderBarra.load();
        Navigatore.impostaBarra(loaderBarra.getController());
        Navigatore.applicaHoverATuttiIBottoni(barra);
        radice.setTop(barra);

        Navigatore.vaiA("menuIniziale.fxml");

        Scene scena = new Scene(radice, 1050, 700);
        scena.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setScene(scena);
        stage.show();
    }

    /** Se l'app e' stata avviata con "host porta" come argomenti, prova a connettersi subito, senza dialogo. */
    private boolean connessioneDaArgomenti() {
        List<String> parametri = getParameters().getRaw();
        if (parametri.size() < 2) {
            return false;
        }
        try {
            ConnessioneServer connessione = new ConnessioneServer(parametri.get(0), Integer.parseInt(parametri.get(1)));
            SessioneClient.setServizi(new ServiziTKProxy(connessione));
            return true;
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }

    private boolean connettiAlServer(Stage owner) {
        while (true) {
            String[] hostPorta = chiediHostPorta();
            if (hostPorta == null) {
                return false;
            }
            try {
                int porta = Integer.parseInt(hostPorta[1]);
                ConnessioneServer connessione = new ConnessioneServer(hostPorta[0], porta);
                SessioneClient.setServizi(new ServiziTKProxy(connessione));
                return true;
            } catch (IOException | NumberFormatException e) {
                mostraErrore("Impossibile connettersi al server " + hostPorta[0] + ":" + hostPorta[1]
                        + ".\nVerifica che serverTK sia in esecuzione e riprova.\n(" + e.getMessage() + ")");
            }
        }
    }

    private String[] chiediHostPorta() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Connessione a serverTK");
        dialog.setHeaderText("Inserisci l'indirizzo del server TheKnife a cui connettersi");
        ButtonType bottoneConnetti = new ButtonType("Connetti", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bottoneConnetti, ButtonType.CANCEL);

        TextField campoHost = new TextField(HOST_DEFAULT);
        TextField campoPorta = new TextField(String.valueOf(PORTA_DEFAULT));
        GridPane griglia = new GridPane();
        griglia.setHgap(10);
        griglia.setVgap(10);
        griglia.addRow(0, new Label("Host:"), campoHost);
        griglia.addRow(1, new Label("Porta:"), campoPorta);
        dialog.getDialogPane().setContent(griglia);

        dialog.setResultConverter(bottone ->
                bottone == bottoneConnetti ? new String[]{campoHost.getText().trim(), campoPorta.getText().trim()} : null);
        return dialog.showAndWait().orElse(null);
    }

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR, messaggio);
        alert.setHeaderText("Errore di connessione");
        alert.showAndWait();
    }
}
