package com.jrdev.systemmanager.DataBaseConnection.api;
import com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface RegistroFinancieroApi {
    @GET("api/registro-financiero")
    Call<List<RegistroFinancieroDao>> listar();

    @GET("api/registro-financiero/{id}")
    Call<RegistroFinancieroDao> obtener(@Path("id") long id);

    @POST("api/registro-financiero")
    Call<RegistroFinancieroDao> crear(@Body RegistroFinancieroDao registro);

    @PUT("api/registro-financiero/{id}")
    Call<RegistroFinancieroDao> actualizar(@Path("id") long id, @Body RegistroFinancieroDao registro);

    @DELETE("api/registro-financiero/{id}")
    Call<Void> eliminar(@Path("id") long id);
}