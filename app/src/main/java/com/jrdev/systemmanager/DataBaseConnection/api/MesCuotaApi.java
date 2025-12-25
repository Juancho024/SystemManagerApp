package com.jrdev.systemmanager.DataBaseConnection.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MesCuotaApi {
    @GET("api/mescuotas")
    Call<List<String>> listar();

    @DELETE("api/mescuotas/{mesAnio}")
    Call<Void> eliminar(@Path("mesAnio") String mesAnio);
}
