package com.jrdev.systemmanager.DataBaseConnection.dao;

public class RegistroFinancieroDao {

    public Long id;
    public String mesCuota;
    public Float cuotaMensual;
    public String descripcion;
    public Float montoPagar;
    // Usa Base64 strings para compatibilidad con Jackson/Gson
    public String img1;
    public String img2;
    public String img3;
    public String img4;
}
