/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client;

import javafx.application.Application;

/**
 * Classe di avvio effettiva del jar eseguibile del client: non estende
 * {@link javafx.application.Application} direttamente, cosi' che il jar
 * possa dichiararla come Main-Class senza incorrere nel controllo che il
 * JVM effettua sulla presenza del runtime JavaFX prima ancora di
 * caricare la classe applicativa vera e propria ({@link ClientTK}).
 */
public final class LauncherClient {

    private LauncherClient() {
    }

    public static void main(String[] args) {
        Application.launch(ClientTK.class, args);
    }
}
