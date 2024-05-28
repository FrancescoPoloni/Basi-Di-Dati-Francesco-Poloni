package org.arrampicata;

import javax.persistence.Table;
import javax.persistence.Column;
@Table(name = "presa")
public class Presa {
    private int id;
    private String set_prese;
    private String colore;
    private String stanza;
    private int parete;
    private int via;
    @Column(name = "id")
    public int getId() {
        return id;
    }
    @Column(name = "set_prese")
    public String getPrese() {
        return set_prese;
    }
    @Column(name = "colore")
    public String getColore() {
        return colore;
    }
    @Column(name = "stanza")
    public String getStanza() {
        return stanza;
    }
    @Column(name = "parete")
    public int getParete() {
        return parete;
    }
    @Column(name = "via")
    public int getVia() {
        return via;
    }
    public void setId(int id) {
        this.id = id;
    }


    public void setSet_prese(String set_prese) {
        this.set_prese = set_prese;
    }

    public void setColore(String colore) {
        this.colore = colore;
    }

    public void setStanza(String stanza) {
        this.stanza = stanza;
    }

    public void setParete(int parete) {
        this.parete = parete;
    }

    public void setVia(int via) {
        this.via = via;
    }

    @Override
    public String toString(){
        return  this.id+" "+this.set_prese+" "+this.colore+" "+this.stanza+" "+this.parete+" "+this.via;
    }
}
