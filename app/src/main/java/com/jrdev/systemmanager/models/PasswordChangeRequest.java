package com.jrdev.systemmanager.models;

import com.google.gson.annotations.SerializedName;

public class PasswordChangeRequest {
    @SerializedName("passwordActual")
    public String passwordActual;
    @SerializedName("passwordNueva")
    public String passwordNueva;

    public PasswordChangeRequest(String actual, String nueva) {
        this.passwordActual = actual;
        this.passwordNueva = nueva;
    }
}
