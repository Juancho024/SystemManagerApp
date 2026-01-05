package com.jrdev.systemmanager.models;

public class LoginRequest {
    public String usuario;
    public String password;

    public LoginRequest(String u, String p) {
        this.usuario = u;
        this.password = p;
    }
}
