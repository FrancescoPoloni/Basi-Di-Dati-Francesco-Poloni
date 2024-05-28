package org.arrampicata;

import javax.persistence.Table;
import javax.persistence.Column;

@Table(name="via")
public class Via {

    private String grado;
    private int numero;
    private String stanza;
    private int parete;
    private String tipo;
    @Column(name = "grado")
    public String getGrado() {
        return grado;
    }
    @Column(name = "numero")
    public int getNumero() {
        return numero;
    }
    @Column(name = "stanza")
    public String getStanza() {
        return stanza;
    }
    @Column(name = "parete")
    public int getParete() {
        return parete;
    }
    @Column(name = "tipo")
    public String getTipo() {
        return tipo;
    }
    public void setGrado(String grado) {
        this.grado = grado;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setStanza(String stanza) {
        this.stanza = stanza;
    }

    public void setParete(int parete) {
        this.parete = parete;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString(){
        return  this.stanza+" "+this.parete+" "+this.numero+" "+this.grado+" "+this.tipo;
    }
}
