/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.common.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utente registrato con ruolo di ristoratore (gestore): puo' creare e
 * gestire i propri ristoranti, visualizzarne le recensioni e rispondere
 * ad esse.
 */
public class Ristoratore extends Utente {

    private static final long serialVersionUID = 1L;

    public Ristoratore() {
        super();
    }

    public Ristoratore(int id, String nome, String cognome, String email, String username, LocalDate dataNascita,
                        String nazioneDomicilio, String cittaDomicilio, LocalDateTime dataRegistrazione) {
        super(id, nome, cognome, email, username, dataNascita, nazioneDomicilio, cittaDomicilio, dataRegistrazione);
    }

    @Override
    public Ruolo getRuolo() {
        return Ruolo.RISTORATORE;
    }
}
