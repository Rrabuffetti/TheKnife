/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.common.protocol;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Messaggio inviato dal client al server sulla connessione socket:
 * incapsula l'{@link Operazione} richiesta e i parametri necessari a
 * eseguirla, serializzati tramite {@link java.io.ObjectOutputStream}.
 */
public class Richiesta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Operazione operazione;
    private final Object[] parametri;

    public Richiesta(Operazione operazione, Object... parametri) {
        this.operazione = operazione;
        this.parametri = parametri;
    }

    public Operazione getOperazione() {
        return operazione;
    }

    public Object[] getParametri() {
        return parametri;
    }

    @SuppressWarnings("unchecked")
    public <T> T getParametro(int indice) {
        return (T) parametri[indice];
    }

    @Override
    public String toString() {
        return "Richiesta{" + operazione + ", parametri=" + Arrays.toString(parametri) + "}";
    }
}
