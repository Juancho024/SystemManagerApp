package com.jrdev.systemmanager.DataBaseConnection.api;
import com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface PropietarioApi {
    @GET("api/propietarios")
    Call<List<PropietarioDao>> listar();

    @GET("api/propietarios")
    Call<List<PropietarioDao>> listarConFiltros(
            @Query("estado") String estado,
            @Query("buscar") String buscar
    );

    @GET("api/propietarios/{id}")
    Call<PropietarioDao> obtener(@Path("id") long id);

    @POST("api/propietarios")
    Call<PropietarioDao> crear(@Body PropietarioDao propietario);

    @PUT("api/propietarios/{id}")
    Call<PropietarioDao> actualizar(@Path("id") long id, @Body PropietarioDao propietario);

    @DELETE("api/propietarios/{id}")
    Call<Void> eliminar(@Path("id") long id);

    @PATCH("api/propietarios/{id}/estado")
    Call<PropietarioDao> actualizarEstado(@Path("id") long id);

//    @GET("api/propietarios/stats")
//    Call<StatsDao> stats();
}