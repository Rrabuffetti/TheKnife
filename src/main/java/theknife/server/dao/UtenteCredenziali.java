/*
 * TheKnife - Laboratorio Interdisciplinare B (a.a. 2025/2026)
 * Autori:
 *   Rabuffetti Riccardo - matricola 756625 - sede VA
 *   Gorla Davide        - matricola 756140 - sede VA
 *   Scarselli Francesco - matricola 756661 - sede VA
 */
package theknife.server.dao;

import theknife.common.model.Utente;

/**
 * Coppia (utente, hash password) usata internamente da {@link UtenteDAO}
 * per la verifica delle credenziali in fase di login. Non attraversa mai
 * il confine client/server: l'hash resta confinato al livello server.
 */
public final class UtenteCredenziali {

    private final Utente utente;
    private final String passwordHash;

    public UtenteCredenziali(Utente utente, String passwordHash) {
        this.utente = utente;
        this.passwordHash = passwordHash;
    }

    public Utente getUtente() {
        return utente;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
