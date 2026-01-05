package com.jrdev.systemmanager.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {

    private static final String PREF_NAME = "secure_session";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_USER_ID = "user_id";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // El constructor necesita el Contexto (Activity)
    public SessionManager(Context context) {
        try {
            // Creamos o recuperamos la llave maestra del sistema Android
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Iniciamos las preferencias encriptadas
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Encripta las llaves
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Encripta los valores
            );

            editor = sharedPreferences.edit();

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    // Guarda el username y password en el cell
    public void guardarSesion(String username, String password) {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password); // Se guarda encriptado automáticamente
        editor.putLong(KEY_TIMESTAMP, System.currentTimeMillis());
        editor.apply(); // Guardar cambios
    }

    // Guarda el ID del usuario autenticado
    public void guardarIdUsuario(Long id) {
        if (id != null) {
            editor.putLong(KEY_USER_ID, id);
            editor.apply();
        }
    }

    // Me da el ID del usuario autenticado
    public Long obtenerIdUsuario() {
        return sharedPreferences.getLong(KEY_USER_ID, -1L);
    }

    // Sacar datos de almacenamiento del cell
    public SesionData obtenerSesion() {
        String username = sharedPreferences.getString(KEY_USERNAME, null);
        String password = sharedPreferences.getString(KEY_PASSWORD, null);
        long timestamp = sharedPreferences.getLong(KEY_TIMESTAMP, 0);

        if (username != null && password != null) {
            // Al hacer .getString, Android lo desencripta automáticamente para ti
            return new SesionData(username, password, timestamp);
        }
        return null;
    }

    // cuando cierra sesion y quieres eliminar el usuario y password pero no id
    public void eliminarSesion() {
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PASSWORD);
        editor.remove(KEY_TIMESTAMP);
        // Dejamos KEY_USER_ID intacto para que siga disponible si se requiere
        editor.apply();
    }

    //borrar todos, eso es para cerrar sesion bien
    public void cerrarSesionCompleta() {
        editor.clear();
        editor.apply();
    }

    public static class SesionData {
        public String username;
        public String password;
        public long timestamp;

        public SesionData(String username, String password, long timestamp) {
            this.username = username;
            this.password = password;
            this.timestamp = timestamp;
        }
    }
}