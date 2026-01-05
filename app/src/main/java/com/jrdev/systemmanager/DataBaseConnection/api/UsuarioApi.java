package com.jrdev.systemmanager.DataBaseConnection.api;

import com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao;
import com.jrdev.systemmanager.DataBaseConnection.dao.UsuarioDao;
import com.jrdev.systemmanager.models.LoginRequest;
import com.jrdev.systemmanager.models.PasswordChangeRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UsuarioApi {
    // Listar y Buscar
    @GET("api/usuarios")
    Call<List<UsuarioDao>> listar();

    @GET("api/usuarios/buscar")
    Call<List<UsuarioDao>> listarConFiltros(@Query("q") String query);

    // Obtener por ID
    @GET("api/usuarios/{id}")
    Call<UsuarioDao> obtenerPorId(@Path("id") Long id);

    // Crear
    @POST("api/usuarios")
    Call<UsuarioDao> crear(@Body UsuarioDao usuario);

    // Actualizar
    @PUT("api/usuarios/{id}")
    Call<UsuarioDao> actualizar(@Path("id") Long id, @Body UsuarioDao usuario);

    // Eliminar
    @DELETE("api/usuarios/{id}")
    Call<Void> eliminar(@Path("id") Long id);

    // Login / Validar Credenciales
    // Probando múltiples endpoints
    @POST("api/usuarios/login")
    Call<UsuarioDao> login(@Body LoginRequest request);

    // Alternativa: /auth/login
    @POST("api/auth/login")
    Call<UsuarioDao> authLogin(@Body LoginRequest request);

    // Alternativa: /login
    @POST("api/login")
    Call<UsuarioDao> simpleLogin(@Body LoginRequest request);

    // Alternativa sin body: query params sin “encriptación”
    @GET("api/usuarios/login")
    Call<UsuarioDao> loginQuery(@Query("usuario") String usuario,
                                @Query("password") String password);

    // Validación estilo escritorio: /usuarios/validar?usuario=X&password=Y
    @GET("api/usuarios/validar")
    Call<UsuarioDao> loginValidar(@Query("usuario") String usuario,
                                  @Query("password") String password);

    // Cambiar Password
    @POST("api/usuarios/{id}/password")
    Call<Void> cambiarPassword(@Path("id") Long id, @Body PasswordChangeRequest request);
}
