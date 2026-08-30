/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import theknife.client.controller.BarraNavigazioneController;

/**
 * Gestisce la navigazione tra le schermate dell'applicazione: carica il
 * contenuto FXML richiesto nell'area centrale di una {@link BorderPane}
 * radice condivisa, restituendo il controller cosi' che il chiamante
 * possa passargli i dati necessari (es. l'id del ristorante da mostrare).
 */
public final class Navigatore {

    private static Stage stage;
    private static BorderPane radice;
    private static BarraNavigazioneController barra;

    private Navigatore() {
    }

    public static void inizializza(Stage stagePrincipale, BorderPane radicePrincipale) {
        stage = stagePrincipale;
        radice = radicePrincipale;
    }

    public static Stage getStage() {
        return stage;
    }

    public static void impostaBarra(BarraNavigazioneController controllerBarra) {
        barra = controllerBarra;
    }

    /** Da richiamare dopo login/registrazione/logout per aggiornare i pulsanti mostrati. */
    public static void aggiornaBarra() {
        if (barra != null) {
            barra.aggiorna();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T vaiA(String nomeFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigatore.class.getResource("/fxml/" + nomeFxml));
            Parent contenuto = loader.load();
            radice.setCenter(contenuto);
            Animazioni.dissolvenzaIngresso(contenuto);
            applicaHoverATuttiIBottoni(contenuto);
            return (T) loader.getController();
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare la schermata " + nomeFxml, e);
        }
    }

    /** Applica l'animazione di hover a tutti i pulsanti di un contenuto gia' caricato (es. la barra di navigazione). */
    public static void applicaHoverATuttiIBottoni(Node nodo) {
        if (nodo instanceof ButtonBase bottone) {
            Animazioni.applicaHoverIngrandimento(bottone);
        }
        if (nodo instanceof Parent genitore) {
            for (Node figlio : genitore.getChildrenUnmodifiable()) {
                applicaHoverATuttiIBottoni(figlio);
            }
        }
    }
}
