package com.jrdev.systemmanager.models;

public class TablaRegistroItem {
    public String numApto;
    public Float cuotaMensual;
    public String descripcion;
    public Float montoPagar;
    public Float balance;

    public TablaRegistroItem(String numApto, Float cuotaMensual, String descripcion, Float montoPagar, Float balance) {
        this.numApto = numApto;
        this.cuotaMensual = cuotaMensual;
        this.descripcion = descripcion;
        this.montoPagar = montoPagar;
        this.balance = balance;
    }
}