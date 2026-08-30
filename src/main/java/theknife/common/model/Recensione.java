/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Rappresenta una recensione lasciata da un cliente per un ristorante:
 * un numero di stelle da 1 a 5 e un testo, con eventuale risposta del
 * ristoratore (al massimo una per recensione).
 * <p>
 * L'{@code idCliente} permette al client di determinare se la recensione
 * appartiene all'utente correntemente autenticato (per abilitare modifica/
 * cancellazione); {@code usernameCliente} e' invece il nome pubblico
 * dell'autore, mostrato nell'interfaccia accanto alla recensione.
 */
public class Recensione implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int idRistorante;
    private String nomeRistorante;
    private int idCliente;
    private String usernameCliente;
    private int stelle;
    private String testo;
    private LocalDateTime dataRecensione;
    private LocalDateTime dataModifica;
    private String rispostaTesto;
    private LocalDateTime dataRisposta;

    public Recensione() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdRistorante() {
        return idRistorante;
    }

    public void setIdRistorante(int idRistorante) {
        this.idRistorante = idRistorante;
    }

    /** Usato nella schermata "le mie recensioni", per non dover ricaricare il ristorante a parte. */
    public String getNomeRistorante() {
        return nomeRistorante;
    }

    public void setNomeRistorante(String nomeRistorante) {
        this.nomeRistorante = nomeRistorante;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    /** Nome pubblico dell'autore, mostrato accanto alla recensione. */
    public String getUsernameCliente() {
        return usernameCliente;
    }

    public void setUsernameCliente(String usernameCliente) {
        this.usernameCliente = usernameCliente;
    }

    public int getStelle() {
        return stelle;
    }

    public void setStelle(int stelle) {
        this.stelle = stelle;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public LocalDateTime getDataRecensione() {
        return dataRecensione;
    }

    public void setDataRecensione(LocalDateTime dataRecensione) {
        this.dataRecensione = dataRecensione;
    }

    public LocalDateTime getDataModifica() {
        return dataModifica;
    }

    public void setDataModifica(LocalDateTime dataModifica) {
        this.dataModifica = dataModifica;
    }

    public String getRispostaTesto() {
        return rispostaTesto;
    }

    public void setRispostaTesto(String rispostaTesto) {
        this.rispostaTesto = rispostaTesto;
    }

    public LocalDateTime getDataRisposta() {
        return dataRisposta;
    }

    public void setDataRisposta(LocalDateTime dataRisposta) {
        this.dataRisposta = dataRisposta;
    }

    public boolean isRisposta() {
        return rispostaTesto != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recensione)) return false;
        Recensione that = (Recensione) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Recensione{id=" + id + ", idRistorante=" + idRistorante + ", stelle=" + stelle + "}";
    }
}
