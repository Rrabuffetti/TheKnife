/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.common.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Criteri di ricerca dei ristoranti, cosi' come richiesti dalla
 * funzionalita' {@code cercaRistorante()}: la localizzazione geografica
 * e' l'unico criterio obbligatorio, tutti gli altri sono opzionali e
 * combinabili liberamente tra loro (i campi opzionali non impostati
 * restano a {@code null} e vengono ignorati dalla query).
 */
public class CriteriRicerca implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Localizzazione geografica: citta' o nazione (obbligatorio). */
    private String luogo;

    /** Tipo di cucina desiderato (opzionale). */
    private String tipoCucina;

    /** Fascia di prezzo minima in euro (opzionale). */
    private BigDecimal prezzoMinimo;

    /** Fascia di prezzo massima in euro (opzionale). */
    private BigDecimal prezzoMassimo;

    /** {@code true}/{@code false} per filtrare, {@code null} = indifferente. */
    private Boolean delivery;

    /** {@code true}/{@code false} per filtrare, {@code null} = indifferente. */
    private Boolean prenotazioneOnline;

    /** Media minima di stelle richiesta (opzionale). */
    private Double stelleMinime;

    public CriteriRicerca() {
    }

    public CriteriRicerca(String luogo) {
        this.luogo = luogo;
    }

    public String getLuogo() {
        return luogo;
    }

    public void setLuogo(String luogo) {
        this.luogo = luogo;
    }

    public String getTipoCucina() {
        return tipoCucina;
    }

    public void setTipoCucina(String tipoCucina) {
        this.tipoCucina = tipoCucina;
    }

    public BigDecimal getPrezzoMinimo() {
        return prezzoMinimo;
    }

    public void setPrezzoMinimo(BigDecimal prezzoMinimo) {
        this.prezzoMinimo = prezzoMinimo;
    }

    public BigDecimal getPrezzoMassimo() {
        return prezzoMassimo;
    }

    public void setPrezzoMassimo(BigDecimal prezzoMassimo) {
        this.prezzoMassimo = prezzoMassimo;
    }

    public Boolean getDelivery() {
        return delivery;
    }

    public void setDelivery(Boolean delivery) {
        this.delivery = delivery;
    }

    public Boolean getPrenotazioneOnline() {
        return prenotazioneOnline;
    }

    public void setPrenotazioneOnline(Boolean prenotazioneOnline) {
        this.prenotazioneOnline = prenotazioneOnline;
    }

    public Double getStelleMinime() {
        return stelleMinime;
    }

    public void setStelleMinime(Double stelleMinime) {
        this.stelleMinime = stelleMinime;
    }
}
