package org.arrampicata;

import javax.persistence.Column;
import javax.persistence.Table;
import java.sql.Date;
import java.sql.Time;

@Table(name = "lezione")
public class Lezione {

    private int id;
    private String luogo;
    @Column(name = "giorno")
    public Date giorno;
    @Column(name = "inizio")
    public Time inizio;
    @Column(name = "fine")
    public Time fine;
    private String istruttore;

    public Lezione() {
    }

    public Lezione(String luogo, Date giorno, Time inizio, Time fine, String istruttore) {
        this.luogo = luogo;
        this.giorno = giorno;
        this.inizio = inizio;
        this.fine = fine;
        this.istruttore = istruttore;
    }

    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "luogo")
    public String getLuogo() {
        return luogo;
    }

    public void setLuogo(String luogo) {
        this.luogo = luogo;
    }


    @Column(name = "istruttore")
    public String getIstruttore() {
        return istruttore;
    }

    public void setIstruttore(String istruttore) {
        this.istruttore = istruttore;
    }
}
