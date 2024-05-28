package org.arrampicata;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name = "commento")
public class Commento {
    private int id;
    private String utente;
    private String testo;
    private String Stanza;
    private int parete;
    private int via;
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    @Column(name = "utente")
    public String getUtente() {
        return utente;
    }

    public void setUtente(String utente) {
        this.utente = utente;
    }
    @Column(name = "testo")
    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }
    @Column(name = "stanza")
    public String getStanza() {
        return Stanza;
    }

    public void setStanza(String stanza) {
        Stanza = stanza;
    }
    @Column(name = "parete")
    public int getParete() {
        return parete;
    }

    public void setParete(int parete) {
        this.parete = parete;
    }
    @Column(name = "via")
    public int getVia() {
        return via;
    }

    public void setVia(int via) {
        this.via = via;
    }

    @Override
    public String toString() {
        return "Commento{" +
                "id=" + id +
                ", utente='" + utente + '\'' +
                ", testo='" + testo + '\'' +
                ", Stanza='" + Stanza + '\'' +
                ", parete=" + parete +
                ", via=" + via +
                '}';
    }
}
