/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.client.controller;

import java.util.function.Consumer;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import theknife.client.Formattatori;
import theknife.common.model.Ristorante;

/**
 * Funzioni di utilita' condivise tra i controller delle schermate che
 * mostrano un elenco di ristoranti (Home, Ricerca, Preferiti, I miei
 * ristoranti), per evitare di duplicare la configurazione della
 * {@link ListView}.
 */
final class ControllerUtil {

    private ControllerUtil() {
    }

    static void configuraListaRistoranti(ListView<Ristorante> lista, Consumer<Ristorante> alDoppioClic) {
        lista.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Ristorante r, boolean vuoto) {
                super.updateItem(r, vuoto);
                setText(vuoto || r == null ? null : Formattatori.rigaRistorante(r));
            }
        });
        lista.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2 && lista.getSelectionModel().getSelectedItem() != null) {
                alDoppioClic.accept(lista.getSelectionModel().getSelectedItem());
            }
        });
    }

    static void mostraMessaggio(Label etichetta, String messaggio) {
        etichetta.setText(messaggio);
    }
}
