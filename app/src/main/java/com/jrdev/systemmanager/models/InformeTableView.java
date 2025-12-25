package com.jrdev.systemmanager.models;

public class InformeTableView {
    private final String mesCuota;
    private final String apto;
    private final String propietario;
    private final String estado;
    private final float totalRecibido;
    private final float cuotaMensual;
    private final String descripcion;
    private final float montoPagar;
    private final float balance;

    public InformeTableView(String mesCuota,
                            String apto,
                            String propietario,
                            String estado,
                            float totalRecibido,
                            float cuotaMensual,
                            String descripcion,
                            float montoPagar,
                            float balance) {
        this.mesCuota = mesCuota;
        this.apto = apto;
        this.propietario = propietario;
        this.estado = estado;
        this.totalRecibido = totalRecibido;
        this.cuotaMensual = cuotaMensual;
        this.descripcion = descripcion;
        this.montoPagar = montoPagar;
        this.balance = balance;
    }

    public String getMesCuota() { return mesCuota; }
    public String getApto() { return apto; }
    public String getPropietario() { return propietario; }
    public String getEstado() { return estado; }
    public float getTotalRecibido() { return totalRecibido; }
    public float getCuotaMensual() { return cuotaMensual; }
    public String getDescripcion() { return descripcion; }
    public float getMontoPagar() { return montoPagar; }
    public float getBalance() { return balance; }
}
