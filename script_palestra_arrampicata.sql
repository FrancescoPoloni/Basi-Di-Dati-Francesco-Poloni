CREATE DATABASE IF NOT EXISTS palestra_arrampicata;
USE palestra_arrampicata;

DROP TABLE IF EXISTS commento;
DROP TABLE IF EXISTS iscrizione;
DROP TABLE IF EXISTS lezione;
DROP TABLE IF EXISTS tracciatura;
DROP TABLE IF EXISTS utente;
DROP TABLE IF EXISTS presa;
DROP TABLE IF EXISTS dettagli_via;
DROP TABLE IF EXISTS via;
DROP TABLE IF EXISTS parete;
DROP TABLE IF EXISTS stanza;

create table stanza (
    nome varchar (30) PRIMARY KEY,
    descrizione varchar(200)
);

create table parete (
    numero int,
    descrizione varchar (200),
    stanza varchar (30),
    numero_vie int,
    foreign key (stanza) references stanza(nome),
    primary key (stanza,numero)
);

create table via (
    grado varchar(10) NOT NULL,
    numero int,
    stanza varchar(30),
    parete int,
    tipo varchar(30) check (tipo IN ('boulder','arrampicata sportiva')) NOT NULL,
    foreign key (stanza, parete) references parete(stanza, numero),
    primary key (parete,stanza,numero)
);

create table dettagli_via (

    lunghezza int,
    data_tracciatura date,
    scadenza date,
    descrizione varchar(300),
    stanza varchar(30),
    parete int,
    via int,
    foreign key (stanza, parete, via) references via(stanza, parete, numero),
    primary key (parete,stanza,via)
);

create table presa(
    id int auto_increment primary key,
    set_prese varchar(20),
    colore varchar(20) NOT NULL,
    stanza varchar(30),
    parete int,
    via int,
    foreign key (stanza, parete, via) references via(stanza, parete, numero)
);

create table utente(
    nome varchar(30) NOT NULL,
    cognome varchar(30) NOT NULL,
    username varchar(30) primary key,
    password varchar(255) NOT NULL,
    ruolo varchar(15) check(ruolo in ('utente','studente','istruttore','tracciatore')) NOT NULL
);

create table tracciatura(
    tracciatore varchar(30),
    via int,
    parete int,
    stanza varchar(30),
    foreign key (tracciatore) references utente(username),
    foreign key (stanza,parete,via) references via(stanza,parete,numero),
    primary key (tracciatore,stanza,parete,via)
);

create table lezione(
    id int auto_increment primary key,
    luogo varchar(30) NOT NULL,
    giorno date NOT NULL,
    inizio time NOT NULL,
    fine time NOT NULL,
    istruttore varchar(30) NOT NULL,
    foreign key (luogo) references stanza(nome),
    foreign key (istruttore) references utente(username)
);

create table iscrizione(
    utente varchar(30),
    lezione int,
    foreign key(utente) references utente(username),
    foreign key(lezione) references lezione(id),
    primary key (utente,lezione)
);

create table commento(
    id int auto_increment primary key,
    testo varchar(500) not null,
    via int not null,
    parete int not null,
    stanza varchar(30) not null,
    utente varchar(30) not null,
    foreign key (stanza, parete, via) references via(stanza, parete, numero),
    foreign key (utente) references utente(username)
);

DROP TRIGGER IF EXISTS trigger_istruttore_lezione;
DELIMITER $$
CREATE TRIGGER trigger_istruttore_lezione
BEFORE INSERT ON lezione
FOR EACH ROW
BEGIN
    DECLARE ruolo_istruttore VARCHAR(15);
    SELECT ruolo INTO ruolo_istruttore FROM utente WHERE username = NEW.istruttore;
    IF ruolo_istruttore <> 'istruttore' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'L\'utente specificato non e\' un istruttore.';
    END IF;
END $$
DELIMITER ;

DROP TRIGGER IF EXISTS trigger_tracciatore_via;
DELIMITER $$
CREATE TRIGGER trigger_tracciatore_via
BEFORE INSERT ON tracciatura
FOR EACH ROW
BEGIN
    DECLARE ruolo_tracciatore VARCHAR(15);
    SELECT ruolo INTO ruolo_tracciatore FROM utente WHERE username = NEW.tracciatore;
    IF ruolo_tracciatore <> 'tracciatore' THEN
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'L\'utente specificato non e\' un tracciatore.';
    END IF;
END $$
DELIMITER ;

DROP TRIGGER IF EXISTS prevenzione_inserimento_diretto_via;
DELIMITER $$
CREATE TRIGGER prevenzione_inserimento_diretto_via
BEFORE INSERT ON via
FOR EACH ROW
BEGIN
    IF @bypass_trigger IS NULL OR @bypass_trigger = FALSE THEN
        SIGNAL SQLSTATE '45002'
        SET MESSAGE_TEXT = 'L\'inserimento diretto nella tabella via non e\' consentito. Usa invece la procedura aggiuntaVia.';
    END IF;
END $$
DELIMITER ;

DROP TRIGGER IF EXISTS aggiornamento_numero_vie_alla_cancellazione;
DELIMITER $$
CREATE TRIGGER aggiornamento_numero_vie_alla_cancellazione
AFTER DELETE ON via
FOR EACH ROW
BEGIN
    UPDATE parete
        SET numero_vie = (SELECT COUNT(*) FROM via WHERE via.parete = OLD.parete AND via.stanza = OLD.stanza)
        WHERE parete.numero = OLD.parete AND parete.stanza = OLD.stanza;
END $$
DELIMITER ;

DROP TRIGGER IF EXISTS trigger_studente_iscrizione;
DELIMITER $$
CREATE TRIGGER trigger_studente_iscrizione
BEFORE INSERT ON iscrizione
FOR EACH ROW
BEGIN
    DECLARE ruolo_studente VARCHAR(15);
    SELECT ruolo INTO ruolo_studente FROM utente WHERE username = NEW.utente;
    IF ruolo_studente <> 'studente' THEN
        SIGNAL SQLSTATE '45003' SET MESSAGE_TEXT = 'L\'utente specificato non è uno studente.';
    END IF;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS aggiuntaVia;
DELIMITER $$
CREATE PROCEDURE aggiuntaVia(
    IN stanza varchar(30), IN parete int,
    IN numero_via int, IN tracciatore varchar(30),
    IN tipo varchar(30), IN grado varchar(10), IN prima_presa int)
BEGIN
    SET @bypass_trigger = TRUE;

    START TRANSACTION;
    INSERT INTO via(grado, numero, stanza, parete, tipo)
        VALUES (grado,numero_via,stanza,parete,tipo);

    INSERT INTO tracciatura(tracciatore, via, parete, stanza)
        VALUES (tracciatore,numero_via,parete,stanza);

    UPDATE presa
        SET presa.stanza=stanza, presa.parete = parete, presa.via = numero_via
        WHERE id = prima_presa;

    UPDATE parete
        SET numero_vie = (SELECT COUNT(*) FROM via WHERE via.parete = parete AND via.stanza = stanza)
        WHERE parete.numero = parete AND parete.stanza = stanza;
    COMMIT;

    SET @bypass_trigger = FALSE;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS aggiunta_dettagli;
DELIMITER $$
CREATE PROCEDURE aggiunta_dettagli(
    IN stanza varchar(30), IN parete int ,IN via int,
    IN lunghezza int, IN descrizione varchar(300),
    IN data_tracciatura date, IN scadenza date)
BEGIN
    START TRANSACTION;
    INSERT INTO dettagli_via(stanza, parete, via, lunghezza, data_tracciatura, scadenza, descrizione)
        VALUES (stanza, parete, via, lunghezza, data_tracciatura, scadenza, descrizione);
    COMMIT;
end $$
DELIMITER ;

DROP PROCEDURE IF EXISTS cancellazione_via;
DELIMITER $$
CREATE PROCEDURE cancellazione_via(
    IN stanza varchar(30), IN parete int ,IN via int)
BEGIN
    START TRANSACTION;

    DELETE FROM commento
        WHERE commento.stanza=stanza and commento.parete=parete and commento.via=via;

    UPDATE presa
        SET presa.stanza=NULL, presa.parete=NULL, presa.via=NULL
        WHERE presa.stanza=stanza and presa.parete=parete and presa.via=via;

    DELETE FROM dettagli_via
        WHERE dettagli_via.stanza=stanza and dettagli_via.parete=parete and dettagli_via.via=via;

    DELETE FROM tracciatura
        WHERE tracciatura.stanza=stanza and tracciatura.parete=parete and tracciatura.via=via;

    DELETE FROM via
        WHERE via.stanza=stanza and via.parete=parete and via.numero=via;

    COMMIT;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS mostra_vie;
DELIMITER $$
CREATE PROCEDURE mostra_vie(
    IN stanza varchar(30), IN parametro varchar(30))
BEGIN
    SELECT via.stanza, parete, numero, grado, tipo FROM via
        WHERE stanza IS NULL or via.stanza = stanza
        ORDER BY CASE
            WHEN parametro = 'grado' THEN grado
            WHEN parametro = 'tipo' THEN tipo
            ELSE stanza
        END;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS cancella_tutte_le_vie_della_parete;
DELIMITER $$
CREATE PROCEDURE cancella_tutte_le_vie_della_parete(
IN stanza VARCHAR(30), IN parete INT)
BEGIN
    DECLARE controllo_esistenza INT DEFAULT 0;
    DECLARE finito INT DEFAULT 0;
    DECLARE via_da_eliminare INT DEFAULT 0;
    DECLARE cursore_via CURSOR FOR SELECT numero FROM via WHERE via.stanza = stanza AND via.parete = parete;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET finito = 1;

    SELECT COUNT(*) INTO controllo_esistenza FROM via WHERE via.stanza = stanza AND via.parete = parete;
    IF controllo_esistenza = 0 THEN
        SIGNAL SQLSTATE '45004'
        SET MESSAGE_TEXT = 'La parete selezionata non esiste';
    ELSE
        OPEN cursore_via;
        WHILE (finito = 0) DO
            FETCH cursore_via INTO via_da_eliminare;
            IF finito = 0 THEN
                CALL cancellazione_via(stanza,parete,via_da_eliminare);
            END IF;
        END WHILE;
        CLOSE cursore_via;
    END IF;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS leggi_commenti;
DELIMITER $$
CREATE PROCEDURE leggi_commenti(
    IN stanza VARCHAR(30), IN parete INT, IN via INT)
BEGIN
    SELECT testo FROM commento
        WHERE commento.stanza=stanza AND commento.parete=parete AND commento.via=via;
END $$
DELIMITER ;



INSERT INTO stanza (nome, descrizione) VALUES
('Palestra Principale', 'La palestra principale per arrampicata indoor'),
('Sala Boulder', 'Una sala dedicata al bouldering'),
('Palestra Esterna', 'Spazio esterno per arrampicata sportiva');

INSERT INTO parete (numero, descrizione, stanza, numero_vie) VALUES
(1, 'Parete Nord', 'Palestra Principale', 0),
(2, 'Parete Sud', 'Palestra Principale', 0),
(1, 'Parete Ovest', 'Sala Boulder', 0),
(1, 'Parete Est', 'Palestra Esterna', 0),
(2, 'Parete Ovest', 'Palestra Esterna', 0);

INSERT INTO utente (nome, cognome, username, password, ruolo) VALUES
('Mario', 'Rossi', 'mrossi', 'gJYWkgX+DzDisyonclDAIDwU9HbXMTD6PDt9DVnaXvSsgjBM1dUDHRXynAKIDhB2', 'istruttore'),
('Luca', 'Bianchi', 'lbianchi', 'Kycp6kqeAxPl+Z95aNVmMWonfu8+E2Yzcwz6AP2YVMQx8UVTe+jqiU225okvgcrd', 'tracciatore'),
('Giulia', 'Verdi', 'gverdi', 'ywlXCERrP5hPUwCJUS9Rd5Mqy0eMhbP+ZAxJl9kvGhm93s8U5e2jTZBdOQLyxkZ6', 'utente'),
('Anna', 'Neri', 'aneri', 'YCeTlRM1uIEqq33Dd8gWJ3MW/ITBmu6KU3AsYKyIPb08R+dE7dDmQprFL3wYxYgp', 'studente');

INSERT INTO presa (set_prese, colore) VALUES
('Set 1', 'Rosso'),
('Set 2', 'Blu'),
('Set 3', 'Verde'),
('Set 4', 'Giallo');

INSERT INTO presa (set_prese, colore) VALUES
('Set 4', 'Giallo'),
('Set 2', 'Blu'),
('Set 5', 'Nero'),
('Set 5', 'Bianco');

DELIMITER $$

CALL aggiuntaVia('Palestra Principale', 1, 1, 'lbianchi', 'arrampicata sportiva', '6a', 1);
CALL aggiuntaVia('Palestra Principale', 1, 2, 'lbianchi', 'arrampicata sportiva', '6b', 2);
CALL aggiuntaVia('Sala Boulder', 1, 1, 'lbianchi', 'boulder', 'V3', 3);
CALL aggiuntaVia('Palestra Esterna', 1, 1, 'lbianchi', 'arrampicata sportiva', '6c', 4);
CALL aggiuntaVia('Palestra Principale', 1, 3, 'lbianchi', 'arrampicata sportiva', '7a', 5);
CALL aggiuntaVia('Palestra Principale', 2, 1, 'lbianchi', 'arrampicata sportiva', '6a+', 6);
CALL aggiuntaVia('Sala Boulder', 1, 2, 'lbianchi', 'boulder', 'V4', 7);
CALL aggiuntaVia('Palestra Esterna', 1, 2, 'lbianchi', 'arrampicata sportiva', '7b', 8);
CALL aggiuntaVia('Palestra Esterna', 2, 1, 'lbianchi', 'arrampicata sportiva', '5b', 11);


CALL aggiunta_dettagli('Palestra Principale', 1, 1, 20, 'Via lunga e impegnativa', '2024-01-01', '2024-12-31');
CALL aggiunta_dettagli('Palestra Principale', 1, 2, 15, 'Via tecnica con prese piccole', '2024-02-01', '2024-12-31');
CALL aggiunta_dettagli('Sala Boulder', 1, 1, 5, 'Boulder corto e intenso', '2024-03-01', '2024-12-31');
CALL aggiunta_dettagli('Palestra Esterna', 1, 1, 30, 'Via esterna con vista', '2024-04-01', '2024-12-31');

DELIMITER ;


INSERT INTO lezione (luogo, giorno, inizio, fine, istruttore) VALUES
('Palestra Principale', '2024-06-01', '10:00:00', '12:00:00', 'mrossi'),
('Sala Boulder', '2024-06-02', '14:00:00', '16:00:00', 'mrossi');

INSERT INTO iscrizione (utente, lezione) VALUES
('aneri', 1),
('aneri', 2);

INSERT INTO commento (testo, via, parete, stanza, utente) VALUES
('Ottima via, molto divertente!', 1, 1, 'Palestra Principale', 'gverdi'),
('Prese scivolose ma molto divertente!', 1, 2, 'Palestra Principale', 'mrossi'),
('molto bello il passaggio dopo la presa bidito', 1, 1, 'Palestra Esterna', 'lbianchi'),
('Ottima via, molto divertente!', 1, 1, 'Sala Boulder', 'gverdi'),
('Secondo me il grado è troppo basso', 1, 2, 'Palestra Esterna', 'mrossi'),
('Molto difficile ma gratificante', 2, 1, 'Palestra Principale', 'aneri');

INSERT INTO presa (set_prese, colore) VALUES
('Set 1', 'Rosso'),
('Set 2', 'Blu'),
('Set 3', 'Verde'),
('Set 4', 'Giallo'),
('Set 5', 'Arancione'),
('Set 1', 'Viola'),
('Set 4', 'Nero'),
('Set 5', 'Bianco'),
('Set 7', 'Marrone'),
('Set 1', 'Grigio'),
('Set 3', 'Rosa'),
('Set 2', 'Azzurro'),
('Set 3', 'Lime'),
('Set 4', 'Turchese'),
('Set 5', 'Oro'),
('Set 6', 'Argento'),
('Set 7', 'Fucsia'),
('Set 7', 'Lilla'),
('Set 6', 'Bronzo'),
('Set 5', 'Bordeaux');

UPDATE presa SET stanza='Palestra Principale', parete=1, via=1 WHERE id=9;
UPDATE presa SET stanza='Palestra Principale', parete=1, via=2 WHERE id=10;
UPDATE presa SET stanza='Palestra Esterna', parete=1, via=1 WHERE id=12;
UPDATE presa SET stanza='Palestra Principale', parete=1, via=3 WHERE id=13;
UPDATE presa SET stanza='Palestra Principale', parete=2, via=1 WHERE id=14;
UPDATE presa SET stanza='Sala Boulder', parete=1, via=2 WHERE id=15;
UPDATE presa SET stanza='Palestra Esterna', parete=1, via=2 WHERE id=17;
UPDATE presa SET stanza='Palestra Principale', parete=1, via=1 WHERE id=19;
UPDATE presa SET stanza='Palestra Principale', parete=1, via=2 WHERE id=20;
UPDATE presa SET stanza='Sala Boulder', parete=1, via=1 WHERE id=21;
UPDATE presa SET stanza='Palestra Esterna', parete=1, via=1 WHERE id=22;
UPDATE presa SET stanza='Palestra Principale', parete=1, via=3 WHERE id=23;
UPDATE presa SET stanza='Palestra Principale', parete=2, via=1 WHERE id=24;
UPDATE presa SET stanza='Sala Boulder', parete=1, via=2 WHERE id=25;
UPDATE presa SET stanza='Palestra Esterna', parete=1, via=2 WHERE id=26;
UPDATE presa SET stanza='Palestra Principale', parete=1, via=1 WHERE id=27;
UPDATE presa SET stanza='Palestra Principale', parete=1, via=2 WHERE id=28;


INSERT INTO commento (testo, via, parete, stanza, utente)
VALUES
    ('Bella via, ottima per migliorare la tecnica.', 1, 1, 'Palestra Principale', 'mrossi'),
    ('Divertente e stimolante.', 1, 1, 'Palestra Principale', 'lbianchi'),
    ('Buona sfida, ma alcune prese sono difficili.', 1, 1, 'Palestra Principale', 'gverdi'),
    ('Perfetta per chi cerca qualcosa di impegnativo.', 1, 1, 'Palestra Principale', 'aneri');

-- Commenti per la via 2 nella "Palestra Principale", parete 1
INSERT INTO commento (testo, via, parete, stanza, utente)
VALUES
    ('Molto impegnativa, ma gratificante.', 2, 1, 'Palestra Principale', 'mrossi'),
    ('Ottima per migliorare le abilità di arrampicata.', 2, 1, 'Palestra Principale', 'lbianchi'),
    ('Via ben progettata.', 2, 1, 'Palestra Principale', 'gverdi'),
    ('Molto tecnica, richiede precisione.', 2, 1, 'Palestra Principale', 'aneri');

-- Commenti per la via 1 nella "Sala Boulder", parete 1
INSERT INTO commento (testo, via, parete, stanza, utente)
VALUES
    ('Ottimo boulder per allenarsi.', 1, 1, 'Sala Boulder', 'mrossi'),
    ('Divertente e impegnativo.', 1, 1, 'Sala Boulder', 'lbianchi'),
    ('Buona via per migliorare la forza.', 1, 1, 'Sala Boulder', 'gverdi'),
    ('Sfida interessante, ottime prese.', 1, 1, 'Sala Boulder', 'aneri');

-- Commenti per la via 1 nella "Palestra Esterna", parete 1
INSERT INTO commento (testo, via, parete, stanza, utente)
VALUES
    ('Ottima via esterna, buona per pratica.', 1, 1, 'Palestra Esterna', 'mrossi'),
    ('Le prese sono un po\' scivolose.', 1, 1, 'Palestra Esterna', 'lbianchi'),
    ('Perfetta per chi cerca un po\' di avventura.', 1, 1, 'Palestra Esterna', 'gverdi'),
    ('Via interessante, ben progettata.', 1, 1, 'Palestra Esterna', 'aneri');

-- Commenti per la via 3 nella "Palestra Principale", parete 1
INSERT INTO commento (testo, via, parete, stanza, utente)
VALUES
    ('Molto tecnica, ottima sfida.', 3, 1, 'Palestra Principale', 'mrossi'),
    ('Divertente, ma impegnativa.', 3, 1, 'Palestra Principale', 'lbianchi'),
    ('Richiede molta forza e tecnica.', 3, 1, 'Palestra Principale', 'gverdi'),
    ('Perfetta per chi cerca una vera sfida.', 3, 1, 'Palestra Principale', 'aneri');

-- Commenti per la via 1 nella "Palestra Principale", parete 2
INSERT INTO commento (testo, via, parete, stanza, utente)
VALUES
    ('Ottima via, ben bilanciata.', 1, 2, 'Palestra Principale', 'mrossi'),
    ('Le prese sono ben posizionate.', 1, 2, 'Palestra Principale', 'lbianchi'),
    ('Buona per migliorare la tecnica.', 1, 2, 'Palestra Principale', 'gverdi'),
    ('Via interessante e impegnativa.', 1, 2, 'Palestra Principale', 'aneri');

-- Commenti per la via 2 nella "Sala Boulder", parete 1
INSERT INTO commento (testo, via, parete, stanza, utente)
VALUES
    ('Sfida interessante, ottimo per allenamento.', 2, 1, 'Sala Boulder', 'mrossi'),
    ('Molto divertente e stimolante.', 2, 1, 'Sala Boulder', 'lbianchi'),
    ('Perfetto per migliorare la tecnica.', 2, 1, 'Sala Boulder', 'gverdi'),
    ('Buona via per principianti e intermedi.', 2, 1, 'Sala Boulder', 'aneri');

-- Commenti per la via 2 nella "Palestra Esterna", parete 1
INSERT INTO commento (testo, via, parete, stanza, utente)
VALUES
    ('Via molto impegnativa, ma gratificante.', 2, 1, 'Palestra Esterna', 'mrossi'),
    ('Perfetta per arrampicatori esperti.', 2, 1, 'Palestra Esterna', 'lbianchi'),
    ('Buona sfida, ottime prese.', 2, 1, 'Palestra Esterna', 'gverdi'),
    ('Divertente e stimolante.', 2, 1, 'Palestra Esterna', 'aneri');

-- Commenti per la via 1 nella "Palestra Esterna", parete 2
INSERT INTO commento (testo, via, parete, stanza, utente)
VALUES
    ('Ottima via per principianti.', 1, 2, 'Palestra Esterna', 'mrossi'),
    ('Buona per fare pratica.', 1, 2, 'Palestra Esterna', 'lbianchi'),
    ('Perfetta per migliorare le basi.', 1, 2, 'Palestra Esterna', 'gverdi'),
    ('Molto semplice, ma utile per principianti.', 1, 2, 'Palestra Esterna', 'aneri');
