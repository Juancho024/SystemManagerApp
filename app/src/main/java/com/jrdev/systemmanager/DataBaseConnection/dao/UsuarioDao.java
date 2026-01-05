package com.jrdev.systemmanager.DataBaseConnection.dao;

import com.google.gson.annotations.SerializedName;

public class UsuarioDao {

    // IMPORTANTE: El texto dentro de @SerializedName debe ser EXACTAMENTE
    // igual a como aparece en el JSON de tu backend (Spring Boot).

    @SerializedName("idUsuario") // <--- ESTE ES EL QUE ARREGLA EL NULL
    private Long idusuario;

    @SerializedName("numApto")
    private String numApto;

    @SerializedName("nombreUsuario")
    private String nombreUsuario;

    @SerializedName("tipoUsuario")
    private String tipoUsuario;

    @SerializedName("usuario")
    private String usuario;

    @SerializedName("passwordUsuario")
    private String passwordUsuario;

    // Constructor vacío (requerido por Retrofit)
    public UsuarioDao() {}

    // Constructor con parámetros
    public UsuarioDao(Long idusuario, String numApto, String nombreUsuario, String tipoUsuario, String usuario, String passwordUsuario) {
        this.idusuario = idusuario;
        this.numApto = numApto;
        this.nombreUsuario = nombreUsuario;
        this.tipoUsuario = tipoUsuario;
        this.usuario = usuario;
        this.passwordUsuario = passwordUsuario;
    }

    // Getters
    public Long getIdusuario() {
        return idusuario;
    }

    public String getNumApto() {
        return numApto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getPasswordUsuario() {
        return passwordUsuario;
    }

    // Setters
    public void setIdusuario(Long idusuario) {
        this.idusuario = idusuario;
    }

    public void setNumApto(String numApto) {
        this.numApto = numApto;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setPasswordUsuario(String passwordUsuario) {
        this.passwordUsuario = passwordUsuario;
    }
}