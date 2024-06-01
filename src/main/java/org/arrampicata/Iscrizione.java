package org.arrampicata;

import javax.persistence.Column;
import javax.persistence.Table;
@Table(name = "iscrizione")
public class Iscrizione {
    private int lezione;
    private String utente;

    public Iscrizione() {
    }

    public Iscrizione(int lezione, String utente) {
        this.lezione = lezione;
        this.utente = utente;
    }

    @Column(name = "lezione")
    public int getLezione() {
        return lezione;
    }

    public void setLezione(int lezione) {
        this.lezione = lezione;
    }
    @Column(name = "utente")
    public String getUtente() {
        return utente;
    }

    public void setUtente(String utente) {
        this.utente = utente;
    }
}
