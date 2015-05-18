package com.game.guillermo.truc;

public class Carta {

    private String numero;
    private String palo;
    private String valor;

    public Carta(String numero, String palo, String valor) {
        this.numero = numero;
        this.palo = palo;
        this.valor = valor;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getPalo() {
        return palo;
    }

    public void setPalo(String palo) {
        this.palo = palo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Carta carta = (Carta) o;

        if (numero != null ? !numero.equals(carta.numero) : carta.numero != null) return false;
        if (palo != null ? !palo.equals(carta.palo) : carta.palo != null) return false;
        if (valor != null ? !valor.equals(carta.valor) : carta.valor != null) return false;

        return true;
    }

}
