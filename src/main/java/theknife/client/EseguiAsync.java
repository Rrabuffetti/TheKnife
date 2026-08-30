/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javafx.application.Platform;

/**
 * Esegue una chiamata di rete verso serverTK su un thread separato dal
 * thread grafico di JavaFX, cosi' che l'interfaccia non si blocchi in
 * attesa della risposta; il risultato (o l'errore) viene poi riportato
 * sul thread JavaFX tramite {@link Platform#runLater}, come richiesto
 * per qualunque aggiornamento della UI.
 */
public final class EseguiAsync {

    private EseguiAsync() {
    }

    public static <T> void esegui(Callable<T> operazione, Consumer<T> alSuccesso, Consumer<Throwable> alFallimento) {
        Thread thread = new Thread(() -> {
            try {
                T risultato = operazione.call();
                Platform.runLater(() -> alSuccesso.accept(risultato));
            } catch (Throwable e) {
                Platform.runLater(() -> alFallimento.accept(e));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /** Variante per operazioni senza valore di ritorno (es. modifica/elimina/rispondi). */
    public static void esegui(OperazioneSenzaRisultato operazione, Runnable alSuccesso, Consumer<Throwable> alFallimento) {
        esegui(() -> {
            operazione.esegui();
            return null;
        }, ignorato -> alSuccesso.run(), alFallimento);
    }

    @FunctionalInterface
    public interface OperazioneSenzaRisultato {
        void esegui() throws Exception;
    }
}
