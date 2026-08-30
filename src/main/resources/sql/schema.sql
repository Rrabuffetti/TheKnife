-- =====================================================================
-- TheKnife - Schema del database dbTK (PostgreSQL)
-- Laboratorio Interdisciplinare B - a.a. 2025/2026
-- Autori: Riccardo Rabuffetti, Davide Gorla, Francesco Scarselli
--
-- Traduzione dello schema ER concettuale (vedi doc/ER_TheKnife.*) nello
-- schema logico/relazionale seguente. Motivazioni delle scelte principali
-- sono riportate nei commenti e nel Manuale Tecnico.
-- =====================================================================

-- Ricreazione pulita (utile in fase di sviluppo/test; per un ambiente di
-- produzione consolidato si userebbero migrazioni incrementali).
DROP TABLE IF EXISTS risposte_recensioni CASCADE;
DROP TABLE IF EXISTS recensioni CASCADE;
DROP TABLE IF EXISTS preferiti CASCADE;
DROP TABLE IF EXISTS ristoranti_cucine CASCADE;
DROP TABLE IF EXISTS ristoranti CASCADE;
DROP TABLE IF EXISTS cucine CASCADE;
DROP TABLE IF EXISTS utenti CASCADE;
DROP TYPE IF EXISTS ruolo_utente CASCADE;

-- ---------------------------------------------------------------------
-- Tipo enumerativo per il ruolo dell'utente. Si e' scelta una specializ-
-- zazione totale ed esclusiva Cliente/Ristoratore tradotta con un'unica
-- tabella "utenti" con attributo discriminante "ruolo" (le due sotto-
-- classi condividono lo stesso insieme di attributi: la differenza sta
-- solo nel comportamento applicativo, non nei dati), evitando quindi
-- join superflui che si avrebbero con tabelle separate.
-- ---------------------------------------------------------------------
CREATE TYPE ruolo_utente AS ENUM ('CLIENTE', 'RISTORATORE');

-- ---------------------------------------------------------------------
-- Tabella Utenti
-- ---------------------------------------------------------------------
CREATE TABLE utenti (
    id                  SERIAL PRIMARY KEY,
    nome                VARCHAR(100)  NOT NULL,
    cognome             VARCHAR(100)  NOT NULL,
    email               VARCHAR(150)  NOT NULL,          -- funge da username di login (unica per ruolo, vedi sotto)
    username            VARCHAR(30)   NOT NULL UNIQUE,   -- nome pubblico mostrato sulle recensioni (univoco)
    password_hash       VARCHAR(60)   NOT NULL,          -- hash BCrypt (mai in chiaro)
    data_nascita        DATE,                             -- facoltativo
    nazione_domicilio   VARCHAR(100),
    citta_domicilio     VARCHAR(100),
    ruolo               ruolo_utente  NOT NULL,
    data_registrazione  TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT chk_email_formato CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_username_formato CHECK (username ~ '^[A-Za-z0-9._]{3,30}$'),

    -- La stessa email puo' essere usata per al piu' un account CLIENTE e al
    -- piu' un account RISTORATORE (due "profili" distinti sotto la stessa
    -- identita' reale), ma non due volte per lo stesso ruolo: l'unicita' e'
    -- quindi sulla coppia (email, ruolo) e non sulla sola email. Lo username
    -- invece e' univoco in assoluto (indipendentemente dal ruolo), perche'
    -- e' l'identita' pubblica mostrata sulle recensioni.
    CONSTRAINT uq_utenti_email_ruolo UNIQUE (email, ruolo)
);

CREATE INDEX idx_utenti_ruolo ON utenti (ruolo);

-- ---------------------------------------------------------------------
-- Tabella Cucine (dizionario dei tipi di cucina) + associazione N:M con
-- Ristoranti, poiche' un ristorante puo' avere piu' tipologie di cucina
-- (es. "Creative, Contemporary" nel dataset Michelin) e una tipologia di
-- cucina e' condivisa da piu' ristoranti: normalizzazione in 3NF invece
-- di un campo testuale libero ripetuto, per evitare ridondanza e
-- anomalie di aggiornamento, e per rendere efficiente la ricerca per
-- tipologia di cucina.
-- ---------------------------------------------------------------------
CREATE TABLE cucine (
    id      SERIAL PRIMARY KEY,
    nome    VARCHAR(80) NOT NULL UNIQUE
);

-- ---------------------------------------------------------------------
-- Tabella Ristoranti
-- ---------------------------------------------------------------------
CREATE TABLE ristoranti (
    id                  SERIAL PRIMARY KEY,
    nome                VARCHAR(200)   NOT NULL,
    nazione             VARCHAR(100)   NOT NULL,
    citta               VARCHAR(100)   NOT NULL,
    indirizzo           VARCHAR(255)   NOT NULL,          -- via e numero civico
    fascia_prezzo       NUMERIC(8,2)   NOT NULL,          -- prezzo medio in euro
    delivery            BOOLEAN        NOT NULL DEFAULT FALSE,
    prenotazione_online BOOLEAN        NOT NULL DEFAULT FALSE,
    id_ristoratore      INTEGER        NOT NULL REFERENCES utenti(id) ON DELETE CASCADE,
    data_creazione      TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT chk_fascia_prezzo CHECK (fascia_prezzo >= 0)
);

CREATE INDEX idx_ristoranti_luogo   ON ristoranti (nazione, citta);
CREATE INDEX idx_ristoranti_prezzo  ON ristoranti (fascia_prezzo);
CREATE INDEX idx_ristoranti_gestore ON ristoranti (id_ristoratore);

CREATE TABLE ristoranti_cucine (
    id_ristorante INTEGER NOT NULL REFERENCES ristoranti(id) ON DELETE CASCADE,
    id_cucina     INTEGER NOT NULL REFERENCES cucine(id)     ON DELETE CASCADE,
    PRIMARY KEY (id_ristorante, id_cucina)
);

CREATE INDEX idx_ristoranti_cucine_cucina ON ristoranti_cucine (id_cucina);

-- ---------------------------------------------------------------------
-- Tabella Recensioni. Vincolo di identificazione esterna: una recensione
-- esiste solo in quanto associata a un ristorante e a un cliente. Si e'
-- scelto un vincolo UNIQUE(id_ristorante, id_cliente) per riflettere la
-- regola "un cliente inserisce/modifica/cancella LE PROPRIE recensioni"
-- (al singolare per ristorante), evitando recensioni duplicate dello
-- stesso cliente sullo stesso ristorante. La risposta del ristoratore
-- (al massimo una per recensione) e' modellata come attributo opzionale
-- della recensione stessa piuttosto che come tabella a parte, poiche' e'
-- una relazione 1:1 opzionale strettamente dipendente dalla recensione.
-- ---------------------------------------------------------------------
CREATE TABLE recensioni (
    id                  SERIAL PRIMARY KEY,
    id_ristorante       INTEGER      NOT NULL REFERENCES ristoranti(id) ON DELETE CASCADE,
    id_cliente          INTEGER      NOT NULL REFERENCES utenti(id)     ON DELETE CASCADE,
    stelle              SMALLINT     NOT NULL,
    testo               TEXT         NOT NULL,
    data_recensione     TIMESTAMP    NOT NULL DEFAULT now(),
    data_modifica       TIMESTAMP,
    risposta_testo      TEXT,
    data_risposta       TIMESTAMP,

    CONSTRAINT chk_stelle CHECK (stelle BETWEEN 1 AND 5),
    CONSTRAINT uq_recensione_cliente_ristorante UNIQUE (id_ristorante, id_cliente)
);

CREATE INDEX idx_recensioni_ristorante ON recensioni (id_ristorante);
CREATE INDEX idx_recensioni_cliente    ON recensioni (id_cliente);

-- ---------------------------------------------------------------------
-- Tabella Preferiti: relazione N:M cliente-ristorante con attributo
-- "data_aggiunta". Chiave primaria composta (nessun bisogno di un id
-- surrogato: la coppia cliente/ristorante identifica univocamente la
-- riga e ne impedisce la duplicazione).
-- ---------------------------------------------------------------------
CREATE TABLE preferiti (
    id_cliente     INTEGER   NOT NULL REFERENCES utenti(id)     ON DELETE CASCADE,
    id_ristorante  INTEGER   NOT NULL REFERENCES ristoranti(id) ON DELETE CASCADE,
    data_aggiunta  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (id_cliente, id_ristorante)
);

CREATE INDEX idx_preferiti_ristorante ON preferiti (id_ristorante);

-- =====================================================================
-- Vincoli non esprimibili direttamente con FK/CHECK dichiarativi:
-- implementati tramite trigger applicati lato database, cosi' che
-- l'integrita' sia garantita indipendentemente dal client che scrive.
-- =====================================================================

-- Un ristorante deve appartenere a un utente con ruolo RISTORATORE.
CREATE OR REPLACE FUNCTION fn_check_ruolo_ristoratore() RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM utenti WHERE id = NEW.id_ristoratore AND ruolo = 'RISTORATORE'
    ) THEN
        RAISE EXCEPTION 'id_ristoratore (%) deve riferire un utente con ruolo RISTORATORE', NEW.id_ristoratore;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_ruolo_ristoratore
    BEFORE INSERT OR UPDATE OF id_ristoratore ON ristoranti
    FOR EACH ROW EXECUTE FUNCTION fn_check_ruolo_ristoratore();

-- Una recensione deve essere inserita da un utente con ruolo CLIENTE.
CREATE OR REPLACE FUNCTION fn_check_ruolo_cliente() RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM utenti WHERE id = NEW.id_cliente AND ruolo = 'CLIENTE'
    ) THEN
        RAISE EXCEPTION 'id_cliente (%) deve riferire un utente con ruolo CLIENTE', NEW.id_cliente;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_ruolo_cliente
    BEFORE INSERT OR UPDATE OF id_cliente ON recensioni
    FOR EACH ROW EXECUTE FUNCTION fn_check_ruolo_cliente();

-- Solo un cliente puo' aggiungere un ristorante ai preferiti.
CREATE OR REPLACE FUNCTION fn_check_ruolo_cliente_preferiti() RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM utenti WHERE id = NEW.id_cliente AND ruolo = 'CLIENTE'
    ) THEN
        RAISE EXCEPTION 'id_cliente (%) deve riferire un utente con ruolo CLIENTE', NEW.id_cliente;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_ruolo_cliente_preferiti
    BEFORE INSERT ON preferiti
    FOR EACH ROW EXECUTE FUNCTION fn_check_ruolo_cliente_preferiti();

-- Aggiorna automaticamente data_modifica quando il testo/stelle di una
-- recensione vengono modificati da modificaRecensione().
CREATE OR REPLACE FUNCTION fn_set_data_modifica() RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.testo IS DISTINCT FROM OLD.testo) OR (NEW.stelle IS DISTINCT FROM OLD.stelle) THEN
        NEW.data_modifica := now();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_set_data_modifica
    BEFORE UPDATE ON recensioni
    FOR EACH ROW EXECUTE FUNCTION fn_set_data_modifica();
