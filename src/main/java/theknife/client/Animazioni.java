/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Piccole animazioni riutilizzabili per rendere l'interfaccia piu'
 * vivace: dissolvenza in ingresso tra una schermata e l'altra,
 * ingrandimento leggero al passaggio del mouse sui pulsanti, e una
 * scossa per richiamare l'attenzione su un messaggio d'errore.
 * Applicate centralmente da {@link Navigatore} per non duplicare
 * codice in ogni controller.
 */
public final class Animazioni {

    private Animazioni() {
    }

    /** Dissolvenza in ingresso, usata quando si carica una nuova schermata. */
    public static void dissolvenzaIngresso(Node nodo) {
        FadeTransition dissolvenza = new FadeTransition(Duration.millis(260), nodo);
        dissolvenza.setFromValue(0);
        dissolvenza.setToValue(1);
        dissolvenza.play();
    }

    /** Leggero ingrandimento al passaggio del mouse, con ritorno alla dimensione normale. */
    public static void applicaHoverIngrandimento(Node nodo) {
        ScaleTransition ingrandisci = new ScaleTransition(Duration.millis(130), nodo);
        ingrandisci.setToX(1.06);
        ingrandisci.setToY(1.06);
        ScaleTransition rimpicciolisci = new ScaleTransition(Duration.millis(130), nodo);
        rimpicciolisci.setToX(1.0);
        rimpicciolisci.setToY(1.0);

        nodo.setOnMouseEntered(e -> {
            rimpicciolisci.stop();
            ingrandisci.playFromStart();
        });
        nodo.setOnMouseExited(e -> {
            ingrandisci.stop();
            rimpicciolisci.playFromStart();
        });
    }

    /** Piccola scossa orizzontale, per richiamare l'attenzione su un messaggio d'errore appena comparso. */
    public static void scuoti(Node nodo) {
        TranslateTransition scossa = new TranslateTransition(Duration.millis(55), nodo);
        scossa.setByX(6);
        scossa.setCycleCount(6);
        scossa.setAutoReverse(true);
        scossa.setOnFinished(e -> nodo.setTranslateX(0));
        scossa.play();
    }
}
