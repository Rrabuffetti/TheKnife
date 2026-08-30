/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Rappresenta un ristorante censito su TheKnife, con le informazioni sul
 * luogo, la fascia di prezzo, i servizi offerti e i tipi di cucina.
 * <p>
 * I campi {@link #mediaStelle} e {@link #numeroRecensioni} sono valori
 * calcolati (aggregati dalle recensioni) e popolati dal DAO in lettura:
 * non sono colonne dirette della tabella {@code ristoranti}, ma sono
 * inclusi qui per evitare una classe DTO separata, dato che ogni
 * schermata che mostra un ristorante ne ha bisogno.
 */
public class Ristorante implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private String nazione;
    private String citta;
    private String indirizzo;
    private BigDecimal fasciaPrezzo;
    private boolean delivery;
    private boolean prenotazioneOnline;
    private int idRistoratore;
    private LocalDateTime dataCreazione;
    private List<String> tipiCucina = new ArrayList<>();

    // Campi aggregati calcolati (non persistiti direttamente)
    private double mediaStelle;
    private int numeroRecensioni;

    public Ristorante() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNazione() {
        return nazione;
    }

    public void setNazione(String nazione) {
        this.nazione = nazione;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public BigDecimal getFasciaPrezzo() {
        return fasciaPrezzo;
    }

    public void setFasciaPrezzo(BigDecimal fasciaPrezzo) {
        this.fasciaPrezzo = fasciaPrezzo;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    public boolean isPrenotazioneOnline() {
        return prenotazioneOnline;
    }

    public void setPrenotazioneOnline(boolean prenotazioneOnline) {
        this.prenotazioneOnline = prenotazioneOnline;
    }

    public int getIdRistoratore() {
        return idRistoratore;
    }

    public void setIdRistoratore(int idRistoratore) {
        this.idRistoratore = idRistoratore;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public List<String> getTipiCucina() {
        return tipiCucina;
    }

    public void setTipiCucina(List<String> tipiCucina) {
        this.tipiCucina = tipiCucina;
    }

    public double getMediaStelle() {
        return mediaStelle;
    }

    public void setMediaStelle(double mediaStelle) {
        this.mediaStelle = mediaStelle;
    }

    public int getNumeroRecensioni() {
        return numeroRecensioni;
    }

    public void setNumeroRecensioni(int numeroRecensioni) {
        this.numeroRecensioni = numeroRecensioni;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ristorante)) return false;
        Ristorante that = (Ristorante) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ristorante{id=" + id + ", nome=" + nome + ", citta=" + citta + ", nazione=" + nazione + "}";
    }
}
