package org.arrampicata;

import com.dieselpoint.norm.*;
import de.vandermeer.asciitable.AsciiTable;

import java.sql.Time;
import java.sql.Date;
import java.util.*;


public class Main {
    public static void main(String[] args) {
        Database db = new Database();
        db.setJdbcUrl("jdbc:mysql://localhost:3306/palestra_arrampicata?serverTimezone=CET");
        db.setUser("root");
        db.setPassword("SuperPassword");
        DatiSessione dati = new DatiSessione();
        //istruttore, utente: mrossi password: password1 = gJYWkgX+DzDisyonclDAIDwU9HbXMTD6PDt9DVnaXvSsgjBM1dUDHRXynAKIDhB2
        //tracciatore utente: lbianchi password: password2 = Kycp6kqeAxPl+Z95aNVmMWonfu8+E2Yzcwz6AP2YVMQx8UVTe+jqiU225okvgcrd
        //studente utente: aneri password: password4 = YCeTlRM1uIEqq33Dd8gWJ3MW/ITBmu6KU3AsYKyIPb08R+dE7dDmQprFL3wYxYgp
        //utente utente: gverdi password: password3 = ywlXCERrP5hPUwCJUS9Rd5Mqy0eMhbP+ZAxJl9kvGhm93s8U5e2jTZBdOQLyxkZ6

        //System.out.println(PassManager.encryptPassword("password3"));
        //System.out.println(PassManager.checkPassword("password1",PassManager.encryptPassword("password1")));

        String ruolo = login(db,dati);
        dati.setRuolo(ruolo);
        //mostraOpzioni(ruolo);
        //aggiungiVia(db,dati);
        //mostraVia(db);
        //mostraCommenti(db);
        //scriviCommento(db,dati);
        //aggiungiVia(db,dati);
        //cancellaVia(db);
        //resettaParete(db);
        //aggiungiLezione(db,dati);
        //cancellaLezione(db);
        mostraLezioni(db);

    }

    private static String login(Database db, DatiSessione dati){
        boolean check = true;
        String username="";
        String dbUsername;
        String password;
        String dbPassword;

        while (check == true) {
            System.out.println("ciao inserisci il tuo username: ");
            Scanner scanner = new Scanner(System.in);
            username = scanner.next();
            dbUsername = db.sql("select username from utente where username = ?", username).first(String.class);
            System.out.println("inserisci la password: ");
            password = scanner.next();
            dbPassword = db.sql("select password from utente where username = ?", username).first(String.class);
            if (username.equals(dbUsername) && PassManager.checkPassword(password,dbPassword)) {check = false;}
            else System.out.println("questo utente non esiste o la password è sbagliata, riprova\n");
        }

        dati.setUsername(username);
        return db.sql("select ruolo from utente where username = ?",username).first(String.class);
    }

    private static void mostraOpzioni(String ruolo,DatiSessione dati){
        System.out.println("l'utente selezionato ha i permessi del ruolo: "+ruolo);
        System.out.println("cosa vuoi fare? scrivi la parte tra '' per scegliere");
        System.out.println("'mostra vie' per vedere le vie");
        System.out.println("'mostra commenti' per vedere i commenti su una via");
        System.out.println("'scrivi commento' per aggiungere un commendo ad una via");
        if(ruolo.equals("tracciatore")){
            System.out.println("'aggiungi via' per aggiungere una via");
            System.out.println("'cancella via' per cancellare una via");
            System.out.println("'resetta parete' per cancellare tutte le vie di una parete");
        }
        if(ruolo.equals("istruttore")){
            System.out.println("'aggiungi lezione' per aggiungere una lezione");
            System.out.println("'cancella lezione' per cancellare una lezione");
        }
        if(ruolo.equals("studente")){
            System.out.println("'mostra lezioni' per vedere le lezioni disponibili");
            System.out.println("'iscriviti' per iscriverti ad una lezione");
        }
    }

    private static void mostraVia(Database db){
        Scanner scanner = new Scanner(System.in);
        String filtro;
        String dove;
        System.out.println("La tabella sotto mostra le stanze disponibili, quale stanza vuoi selezionare?\nse il nome non corrisponde con quello di una stanza verranno mostrate tutte le vie");
        //cerco nel database le stanze disponibili e costruisco la tabella
        List<String> stanze = db.sql("select distinct stanza from via").results(String.class);
        AsciiTable at1 = new AsciiTable();
        at1.addRule();
        at1.addRow("Stanza");
        at1.addRule();
        for (String stanza:stanze) {
            at1.addRow(stanza);
        }
        at1.addRule();
        System.out.println(at1.render());
        System.out.println("che stanza vuoi?");
        dove=scanner.nextLine();
        Long checkStanza = db.sql("select count(*) from via where via.stanza = ?",dove).first(Long.class);
        if (checkStanza==0) {dove=null;}
        System.out.println(checkStanza);
        System.out.println("le vuoi ordinate per 'grado' o per 'tipo'? di default è per stanza");
        filtro = scanner.nextLine();
        if (!(filtro.equals("grado") || filtro.equals("tipo"))){
            filtro="Null";
        }
        List<Via> vie = db.sql("CALL mostra_vie(?,?)",dove,filtro).results(Via.class);

        AsciiTable at2 = new AsciiTable();
        at2.addRule();
        at2.addRow("Stanza","Parete","Numero","Grado","Tipo");
        at2.addRule();
        for (Via via:vie) {
            at2.addRow(via.getStanza(),via.getParete(),via.getNumero(),via.getGrado(),via.getTipo());
        }
        at2.addRule();
        System.out.println(at2.render());
    }

    private static void mostraCommenti(Database db){
        Scanner scanner = new Scanner(System.in);
        String stanza="";
        String parete="";
        String via="";
        boolean check=true;
        while (check==true) {
            System.out.println("in che stanza è la via? scegli una delle seguenti");
            List<String> stanze = db.sql("select distinct stanza from via").results(String.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Stanza");
            at1.addRule();
            for (String stanzaDaLista : stanze) {
                at1.addRow(stanzaDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che stanza vuoi?");
            stanza = scanner.nextLine();
            Long checkStanza = db.sql("select count(*) from via where via.stanza =?", stanza).first(Long.class);
            if (checkStanza == 0) {
                System.out.println("La stanza selezionata non è disponibile! riprova");
            } else check = false;
        }
        check = true;
        while (check==true){
            System.out.println("su che parete è la via?");
            List<Integer> pareti = db.sql("select distinct parete from via where via.stanza = ?",stanza).results(Integer.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Parete");
            at1.addRule();
            for (Integer pareteDaLista : pareti) {
                at1.addRow(pareteDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che parete vuoi?");
            parete = scanner.nextLine();
            Long checkParete = db.sql("select count(*) from via where via.stanza =? and via.parete=?",stanza,parete).first(Long.class);
            if (checkParete == 0) {
                System.out.println("La parete selezionata non è disponibile! riprova");
            } else check = false;
        }
        check = true;
        while (check==true){
            System.out.println("di che via vuoi leggere i commenti?");
            List<Integer> vie = db.sql("select distinct numero from via where via.stanza = ? and via.parete= ?",stanza,parete).results(Integer.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Vie");
            at1.addRule();
            for (Integer viaDaLista : vie) {
                at1.addRow(viaDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che via vuoi?");
            via = scanner.nextLine();
            Long checkVia = db.sql("select count(*) from via where via.stanza =? and via.parete=? and via.numero=?",stanza,parete,via).first(Long.class);
            if (checkVia == 0) {
                System.out.println("La via selezionata non è disponibile! riprova");
            } else check = false;
        }
        List<Commento> commenti = db.sql("select * from commento where commento.stanza =? and commento.parete=? and commento.via=?",stanza,parete,via).results(Commento.class);
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Id","Utente","Commento","Via","Parete","Stanza");
        at.addRule();
        for (Commento commento : commenti) {
            at.addRow(commento.getId(),commento.getUtente(),commento.getTesto(),commento.getVia(),commento.getParete(),commento.getStanza());
        }
        at.addRule();
        System.out.println(at.render());
    }

    private static void scriviCommento(Database db,DatiSessione dati){
        Scanner scanner = new Scanner(System.in);
        String stanza="";
        String parete="";
        String via="";
        String commento="";
        boolean check=true;
        while (check==true) {
            System.out.println("in che stanza è la via alla quale vuoi aggiungere il commento? scegli una delle seguenti");
            List<String> stanze = db.sql("select distinct stanza from via").results(String.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Stanza");
            at1.addRule();
            for (String stanzaDaLista : stanze) {
                at1.addRow(stanzaDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che stanza vuoi?");
            stanza = scanner.nextLine();
            Long checkStanza = db.sql("select count(*) from via where via.stanza =?", stanza).first(Long.class);
            if (checkStanza == 0) {
                System.out.println("La stanza selezionata non è disponibile! riprova");
            } else check = false;
        }
        check = true;
        while (check==true){
            System.out.println("su che parete è la via?");
            List<Integer> pareti = db.sql("select distinct parete from via where via.stanza = ?",stanza).results(Integer.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Parete");
            at1.addRule();
            for (Integer pareteDaLista : pareti) {
                at1.addRow(pareteDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che parete vuoi?");
            parete = scanner.nextLine();
            Long checkParete = db.sql("select count(*) from via where via.stanza =? and via.parete=?",stanza,parete).first(Long.class);
            if (checkParete == 0) {
                System.out.println("La parete selezionata non è disponibile! riprova");
            } else check = false;
        }
        check = true;
        while (check==true){
            System.out.println("a che via vuoi aggiungere il commento?");
            List<Integer> vie = db.sql("select distinct numero from via where via.stanza = ? and via.parete= ?",stanza,parete).results(Integer.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Vie");
            at1.addRule();
            for (Integer viaDaLista : vie) {
                at1.addRow(viaDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che via vuoi?");
            via = scanner.nextLine();
            Long checkVia = db.sql("select count(*) from via where via.stanza =? and via.parete=? and via.numero=?",stanza,parete,via).first(Long.class);
            if (checkVia == 0) {
                System.out.println("La via selezionata non è disponibile! riprova");
            } else check = false;
        }
        System.out.println("Scrivi il commento: ");
        commento= scanner.nextLine();
        db.sql("INSERT INTO commento (testo, via, parete, stanza, utente) VALUES (?, ?, ?, ?, ?)",commento,via,parete,stanza,dati.getUsername()).execute();
    }

    private static void aggiungiVia(Database db, DatiSessione dati){
        Scanner scanner = new Scanner(System.in);
        String stanza="";
        String parete="";
        String presa="";
        String via="";
        String grado="";
        String tipo="";
        boolean check=true;
        while (check==true) {
            System.out.println("in che stanza è la via che vuoi aggiungere? scegli una delle seguenti");
            List<String> stanze = db.sql("select nome from stanza").results(String.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Stanza");
            at1.addRule();
            for (String stanzaDaLista : stanze) {
                at1.addRow(stanzaDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che stanza vuoi?");
            stanza = scanner.nextLine();
            Long checkStanza = db.sql("select count(*) from stanza where stanza.nome =?", stanza).first(Long.class);
            if (checkStanza == 0) {
                System.out.println("La stanza selezionata non è disponibile! riprova");
            } else check = false;
        }
        check = true;
        while (check==true){
            System.out.println("su che parete vuoi mettere la via?");
            List<Integer> pareti = db.sql("select numero from parete where parete.stanza = ?",stanza).results(Integer.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Parete");
            at1.addRule();
            for (Integer pareteDaLista : pareti) {
                at1.addRow(pareteDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che parete vuoi?");
            parete = scanner.nextLine();
            Long checkParete = db.sql("select count(*) from parete where parete.stanza =? and parete.numero=?",stanza,parete).first(Long.class);
            if (checkParete == 0) {
                System.out.println("La parete selezionata non è disponibile! riprova");
            } else check = false;
        }
        check = true;
        while (check==true) {
            System.out.println("che numero vuoi dare alla via? questi sono gia' occupati:");
            List<Integer> vie = db.sql("select distinct numero from via where via.stanza = ? and via.parete= ?", stanza, parete).results(Integer.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Vie");
            at1.addRule();
            for (Integer viaDaLista : vie) {
                at1.addRow(viaDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("scegli il numero?");
            via = scanner.nextLine();
            Long checkVia = db.sql("select count(*) from via where via.stanza =? and via.parete=? and via.numero=?", stanza, parete, via).first(Long.class);
            if (checkVia != 0L) {
                System.out.println("Il numero selezionato non è disponibile! riprova");
            } else check = false;
        }
        check = true;
        while (check==true){
            System.out.println("che presa vuoi mettere come presa di partenza? scegli l'id di una di queste prese");
            List<Integer> prese = db.sql("select Id from presa").results(Integer.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Id Prese");
            at1.addRule();
            for (Integer presaDaLista : prese) {
                at1.addRow(presaDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("scrivi l'id");
            presa = scanner.nextLine();
            Long checkPresa = db.sql("select count(*) from presa where presa.id=?",presa).first(Long.class);
            if (checkPresa == 0) {
                System.out.println("La presa selezionata non è disponibile! riprova");
            } else check = false;
        }
        check=true;

        System.out.println("che grado ha la via?");
        grado=scanner.nextLine();
        while (check==true) {
            System.out.println("Scegli il tipo di via: (arrampicata sportiva o boulder)");
            tipo=scanner.nextLine();
            if(!(tipo.equals("arrampicata sportiva") || tipo.equals("boulder"))){
                System.out.println("Devi scegliere tra arrampicata sportiva e boulder");
            } else check=false;
        }
        db.sql("CALL aggiuntaVia(?, ?, ?, ?, ?, ?, ?)", stanza, parete, via, dati.getUsername(), tipo, grado, presa).execute();
    }

    private static void cancellaVia(Database db) {
        Scanner scanner = new Scanner(System.in);
        String stanza = "";
        String parete = "";
        String via = "";
        boolean check = true;
        while (check == true) {
            System.out.println("in che stanza è la via cancellare? scegli una delle seguenti");
            List<String> stanze = db.sql("select distinct stanza from via").results(String.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Stanza");
            at1.addRule();
            for (String stanzaDaLista : stanze) {
                at1.addRow(stanzaDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che stanza vuoi?");
            stanza = scanner.nextLine();
            Long checkStanza = db.sql("select count(*) from via where via.stanza =?", stanza).first(Long.class);
            if (checkStanza == 0) {
                System.out.println("La stanza selezionata non è disponibile! riprova");
            } else check = false;
        }
        check = true;
        while (check == true) {
            System.out.println("su che parete è la via che vuoi cancellare?");
            List<Integer> pareti = db.sql("select distinct parete from via where via.stanza = ?", stanza).results(Integer.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Parete");
            at1.addRule();
            for (Integer pareteDaLista : pareti) {
                at1.addRow(pareteDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che parete vuoi?");
            parete = scanner.nextLine();
            Long checkParete = db.sql("select count(*) from via where via.stanza =? and via.parete=?", stanza, parete).first(Long.class);
            if (checkParete == 0) {
                System.out.println("La parete selezionata non è disponibile! riprova");
            } else check = false;
        }
        check = true;
        while (check == true) {
            System.out.println("Che via vuoi cancellare?");
            List<Integer> vie = db.sql("select distinct numero from via where via.stanza = ? and via.parete= ?", stanza, parete).results(Integer.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Vie");
            at1.addRule();
            for (Integer viaDaLista : vie) {
                at1.addRow(viaDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che via vuoi?");
            via = scanner.nextLine();
            Long checkVia = db.sql("select count(*) from via where via.stanza =? and via.parete=? and via.numero=?", stanza, parete, via).first(Long.class);
            if (checkVia == 0) {
                System.out.println("La via selezionata non è disponibile! riprova");
            } else check = false;
        }
        db.sql("call cancellazione_via(?,?,?)",stanza,parete,via).execute();
    }

    private static void resettaParete(Database db) {
        Scanner scanner = new Scanner(System.in);
        String stanza = "";
        String parete = "";
        boolean check = true;
        while (check == true) {
            System.out.println("in che stanza è parete da resettare? scegli una delle seguenti");
            List<String> stanze = db.sql("select distinct stanza from via").results(String.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Stanza");
            at1.addRule();
            for (String stanzaDaLista : stanze) {
                at1.addRow(stanzaDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che stanza vuoi?");
            stanza = scanner.nextLine();
            Long checkStanza = db.sql("select count(*) from via where via.stanza =?", stanza).first(Long.class);
            if (checkStanza == 0) {
                System.out.println("La stanza selezionata non è disponibile! riprova");
            } else check = false;
        }
        check = true;
        while (check == true) {
            System.out.println("che parete vuoi resettare?");
            List<Integer> pareti = db.sql("select distinct parete from via where via.stanza = ?", stanza).results(Integer.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Parete");
            at1.addRule();
            for (Integer pareteDaLista : pareti) {
                at1.addRow(pareteDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che parete vuoi?");
            parete = scanner.nextLine();
            Long checkParete = db.sql("select count(*) from via where via.stanza =? and via.parete=?", stanza, parete).first(Long.class);
            if (checkParete == 0) {
                System.out.println("La parete selezionata non è disponibile! riprova");
            } else check = false;
        }
        db.sql("call cancella_tutte_le_vie_della_parete(?,?)",stanza,parete).execute();
    }

    private static void aggiungiLezione(Database db,DatiSessione dati) {
        Scanner scanner = new Scanner(System.in);
        String stanza = "";
        Date giorno;
        Time inizio;
        Time fine;
        boolean check = true;
        while (check == true) {
            System.out.println("in che stanza vuoi fare la lezione? scegli una delle seguenti");
            List<String> stanze = db.sql("select nome from stanza").results(String.class);
            AsciiTable at1 = new AsciiTable();
            at1.addRule();
            at1.addRow("Stanza");
            at1.addRule();
            for (String stanzaDaLista : stanze) {
                at1.addRow(stanzaDaLista);
            }
            at1.addRule();
            System.out.println(at1.render());
            System.out.println("che stanza vuoi?");
            stanza = scanner.nextLine();
            Long checkStanza = db.sql("select count(*) from stanza where stanza.nome =?", stanza).first(Long.class);
            if (checkStanza == 0) {
                System.out.println("La stanza selezionata non è disponibile! riprova");
            } else check = false;
        }
        check = true;
        System.out.println("in che giorno si svolgerà la lezione?\n scrivere il giorno nel seguente formato anno-mese-giorno esempio: 2050-10-20");
        giorno = Date.valueOf(scanner.nextLine());

        System.out.println("a che ora inizia la lezione?\n scrivere l'ora nel seguente formato ore:minuti:secondi esempio: 10:00:00");
        inizio = Time.valueOf(scanner.nextLine());

        System.out.println("a che ora finisce la lezione?\n scrivere l'ora nel seguente formato ore:minuti:secondi esempio: 10:00:00");
        fine = Time.valueOf(scanner.nextLine());
        Transaction trans = db.startTransaction();
        try {
            db.transaction(trans).insert(new Lezione(stanza,giorno,inizio,fine,dati.getUsername()));
            trans.commit();
        } catch (Throwable t) {
            trans.rollback();
        }
    }

    private static void cancellaLezione(Database db){
        Scanner scanner = new Scanner(System.in);
        String id;
        System.out.println("Che lezione vuoi cancellare? digitare l'id");
        mostraLezioni(db);
        id = scanner.nextLine();
        db.table("lezione").where("id=?",id).delete();
    }

    private static void mostraLezioni(Database db) {
        System.out.println("Queste sono le lezioni programmate");
        List<Lezione> lezioni = db.sql("select * from lezione").results(Lezione.class);
        AsciiTable at1 = new AsciiTable();
        at1.addRule();
        at1.addRow("id", "luogo", "giorno", "inizio", "fine", "istruttore");
        at1.addRule();
        for (Lezione lezione : lezioni) {
            at1.addRow(lezione.getId(), lezione.getLuogo(), lezione.giorno, lezione.inizio, lezione.fine, lezione.getIstruttore());
        }
        at1.addRule();
        System.out.println(at1.render());
    }

    private static void iscrizioneLezione(Database db, DatiSessione dati) {
        Scanner scanner = new Scanner(System.in);
        String lezione;
        boolean check = true;
        while (check == true){
            System.out.println("A che lezione ti vuoi iscrivere?\n digita l'id");
            mostraLezioni(db);
            lezione = scanner.nextLine();
            Long checkLezione db.sql("select count(*) from lezione").where("id=?", lezione).first(Long.class);
            if (checkLezione==0) {
                System.out.println("La lezione selezionata non e' disponibile! riprova");
            } else check=false;
        }
        db.sql("insert " ).execute();
    }
}