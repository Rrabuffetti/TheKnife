/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.common.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Rappresenta un utente registrato sulla piattaforma TheKnife.
 * <p>
 * Classe astratta: ogni utente reale e' o un {@link Cliente} o un
 * {@link Ristoratore} (specializzazione totale ed esclusiva). Il ruolo
 * e' quindi espresso dal tipo concreto dell'oggetto, non da un campo
 * modificabile, cosi' da sfruttare il polimorfismo invece di un
 * discriminante debole controllato manualmente.
 * <p>
 * La password non e' mai un campo di questa classe: l'hash e' gestito
 * esclusivamente lato server (tabella {@code utenti.password_hash}) e
 * non deve mai essere trasmesso al client.
 */
public abstract class Utente implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private String cognome;
    private String email;
    private String username;
    private LocalDate dataNascita;
    private String nazioneDomicilio;
    private String cittaDomicilio;
    private LocalDateTime dataRegistrazione;

    protected Utente() {
    }

    protected Utente(int id, String nome, String cognome, String email, String username, LocalDate dataNascita,
                      String nazioneDomicilio, String cittaDomicilio, LocalDateTime dataRegistrazione) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.username = username;
        this.dataNascita = dataNascita;
        this.nazioneDomicilio = nazioneDomicilio;
        this.cittaDomicilio = cittaDomicilio;
        this.dataRegistrazione = dataRegistrazione;
    }

    /** @return il ruolo dell'utente, determinato dal tipo concreto della sottoclasse. */
    public abstract Ruolo getRuolo();

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

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /** @return il nome pubblico dell'utente, mostrato ad esempio come autore di una recensione. */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /** @return la data di nascita, oppure {@code null} se non fornita (campo facoltativo). */
    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(LocalDate dataNascita) {
        this.dataNascita = dataNascita;
    }

    public String getNazioneDomicilio() {
        return nazioneDomicilio;
    }

    public void setNazioneDomicilio(String nazioneDomicilio) {
        this.nazioneDomicilio = nazioneDomicilio;
    }

    public String getCittaDomicilio() {
        return cittaDomicilio;
    }

    public void setCittaDomicilio(String cittaDomicilio) {
        this.cittaDomicilio = cittaDomicilio;
    }

    public LocalDateTime getDataRegistrazione() {
        return dataRegistrazione;
    }

    public void setDataRegistrazione(LocalDateTime dataRegistrazione) {
        this.dataRegistrazione = dataRegistrazione;
    }

    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utente)) return false;
        Utente utente = (Utente) o;
        return id == utente.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getRuolo() + "{id=" + id + ", nome=" + nome + " " + cognome + ", username=" + username + ", email=" + email + "}";
    }
}
