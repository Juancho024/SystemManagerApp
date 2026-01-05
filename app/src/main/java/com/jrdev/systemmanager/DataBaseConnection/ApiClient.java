package com.jrdev.systemmanager.DataBaseConnection;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;
    private static String currentBaseUrl;

    public static Retrofit get(String baseUrl) {
        String sanitized = sanitizeBaseUrl(baseUrl);

        // Re-crear el cliente si cambia la baseUrl
        if (retrofit == null || currentBaseUrl == null || !currentBaseUrl.equals(sanitized)) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(sanitized)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            currentBaseUrl = sanitized;
        }
        return retrofit;
    }

    // Limpia la URL: quita comillas/espacios y asegura el slash final requerido por Retrofit
    private static String sanitizeBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            throw new IllegalArgumentException("La URL base no puede ser nula");
        }
        String clean = baseUrl.replace("\"", "").trim();
        if (!clean.endsWith("/")) {
            clean = clean + "/";
        }
        return clean;
    }
}
