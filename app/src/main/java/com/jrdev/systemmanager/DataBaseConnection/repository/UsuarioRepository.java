package com.jrdev.systemmanager.DataBaseConnection.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jrdev.systemmanager.DataBaseConnection.ApiClient;
import com.jrdev.systemmanager.DataBaseConnection.api.UsuarioApi;
import com.jrdev.systemmanager.DataBaseConnection.dao.UsuarioDao;
import com.jrdev.systemmanager.models.LoginRequest;
import com.jrdev.systemmanager.models.PasswordChangeRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.List;
import java.util.Locale;

public class UsuarioRepository {

    private final UsuarioApi api;

    public UsuarioRepository(String baseUrl) {
        Retrofit r = ApiClient.get(baseUrl);
        this.api = r.create(UsuarioApi.class);
    }

    // 1. Obtener Todos / Buscar
    public LiveData<List<UsuarioDao>> listar(String buscar) {
        MutableLiveData<List<UsuarioDao>> data = new MutableLiveData<>();
        Call<List<UsuarioDao>> call;

        if (buscar == null || buscar.trim().isEmpty()) {
            call = api.listar();
        } else {
            call = api.listarConFiltros(buscar);
        }

        call.enqueue(new Callback<List<UsuarioDao>>() {
            @Override public void onResponse(Call<List<UsuarioDao>> call, Response<List<UsuarioDao>> resp) {
                if (resp.isSuccessful()) {
                    data.postValue(resp.body());
                } else {
                    data.postValue(null);
                }
            }
            @Override public void onFailure(Call<List<UsuarioDao>> call, Throwable t) {
                t.printStackTrace();
                data.postValue(null);
            }
        });
        return data;
    }

    // 2. Obtener por ID
    public LiveData<UsuarioDao> obtenerPorId(Long id) {
        MutableLiveData<UsuarioDao> data = new MutableLiveData<>();
        api.obtenerPorId(id).enqueue(new Callback<UsuarioDao>() {
            @Override public void onResponse(Call<UsuarioDao> call, Response<UsuarioDao> resp) {
                data.postValue(resp.body());
            }
            @Override public void onFailure(Call<UsuarioDao> call, Throwable t) {
                data.postValue(null);
            }
        });
        return data;
    }

    // 3. Crear Usuario
    public LiveData<UsuarioDao> crear(UsuarioDao usuario) {
        MutableLiveData<UsuarioDao> data = new MutableLiveData<>();
        api.crear(usuario).enqueue(new Callback<UsuarioDao>() {
            @Override public void onResponse(Call<UsuarioDao> call, Response<UsuarioDao> resp) {
                if(resp.isSuccessful()) {
                    data.postValue(resp.body());
                } else {
                    // Aquí podrías manejar errores como "Usuario ya existe" leyendo resp.errorBody()
                    data.postValue(null);
                }
            }
            @Override public void onFailure(Call<UsuarioDao> call, Throwable t) {
                data.postValue(null);
            }
        });
        return data;
    }

    // 4. Actualizar Usuario
    public LiveData<UsuarioDao> actualizar(Long id, UsuarioDao usuario) {
        MutableLiveData<UsuarioDao> data = new MutableLiveData<>();
        if (id == null) {
            data.postValue(null);
            return data;
        }
        api.actualizar(id, usuario).enqueue(new Callback<UsuarioDao>() {
            @Override public void onResponse(Call<UsuarioDao> call, Response<UsuarioDao> resp) {
                if (resp.isSuccessful()) {
                    // Algunos backends devuelven 200/204 sin body; en ese caso devolvemos el objeto enviado
                    UsuarioDao body = resp.body();
                    data.postValue(body != null ? body : usuario);
                } else {
                    data.postValue(null);
                }
            }
            @Override public void onFailure(Call<UsuarioDao> call, Throwable t) {
                data.postValue(null);
            }
        });
        return data;
    }

    // 5. Eliminar Usuario
    public LiveData<Boolean> eliminar(Long id) {
        MutableLiveData<Boolean> data = new MutableLiveData<>();
        api.eliminar(id).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                data.postValue(resp.isSuccessful());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                data.postValue(false);
            }
        });
        return data;
    }

    // 6. Validar Credenciales (Login)
    public LiveData<UsuarioDao> login(String usuario, String password) {
        MutableLiveData<UsuarioDao> data = new MutableLiveData<>();
        String userOrig = usuario != null ? usuario.trim() : "";
        String userLower = userOrig.toLowerCase(Locale.ROOT);

        LoginRequest reqOrig = new LoginRequest(userOrig, password);
        LoginRequest reqLower = new LoginRequest(userLower, password);

        // Probamos secuencialmente variantes de login con usuario original y luego en minúsculas
        attemptLoginVariant(0, reqOrig, reqLower, userOrig, userLower, password, data);
        return data;
    }

    private void attemptLoginVariant(int variantIndex,
                                     LoginRequest reqOrig,
                                     LoginRequest reqLower,
                                     String userOrig,
                                     String userLower,
                                     String password,
                                     MutableLiveData<UsuarioDao> data) {
        Call<UsuarioDao> call;
        switch (variantIndex) {
            case 0: // validar con usuario original
                call = api.loginValidar(userOrig, password);
                break;
            case 1: // validar con usuario lower
                call = api.loginValidar(userLower, password);
                break;
            case 2: // body /usuarios/login con original
                call = api.login(reqOrig);
                break;
            case 3: // body /usuarios/login con lower
                call = api.login(reqLower);
                break;
            case 4: // /auth/login con original
                call = api.authLogin(reqOrig);
                break;
            case 5: // /auth/login con lower
                call = api.authLogin(reqLower);
                break;
            case 6: // /login con original
                call = api.simpleLogin(reqOrig);
                break;
            case 7: // /login con lower
                call = api.simpleLogin(reqLower);
                break;
            case 8: // query /usuarios/login con original
                call = api.loginQuery(userOrig, password);
                break;
            case 9: // query /usuarios/login con lower
                call = api.loginQuery(userLower, password);
                break;
            default:
                data.postValue(null);
                return;
        }

        final int next = variantIndex + 1;
        call.enqueue(new Callback<UsuarioDao>() {
            @Override public void onResponse(Call<UsuarioDao> call, Response<UsuarioDao> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    data.postValue(resp.body());
                } else {
                    android.util.Log.e("UsuarioRepository", "Login variant " + variantIndex + " error: " + resp.code() + " - " + resp.message());
                    try {
                        String errorBody = resp.errorBody() != null ? resp.errorBody().string() : "No error body";
                        android.util.Log.e("UsuarioRepository", "Error details: " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    attemptLoginVariant(next, reqOrig, reqLower, userOrig, userLower, password, data);
                }
            }
            @Override public void onFailure(Call<UsuarioDao> call, Throwable t) {
                android.util.Log.e("UsuarioRepository", "Login variant " + variantIndex + " failure: " + t.getMessage(), t);
                attemptLoginVariant(next, reqOrig, reqLower, userOrig, userLower, password, data);
            }
        });
    }

    // 7. Cambiar Password
//    public LiveData<Boolean> cambiarPassword(Long id, String passActual, String passNueva) {
//        MutableLiveData<Boolean> data = new MutableLiveData<>();
//        PasswordChangeRequest request = new PasswordChangeRequest(passActual, passNueva);
//
//        api.cambiarPassword(id, request).enqueue(new Callback<Void>() {
//            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
//                android.util.Log.d("UsuarioRepository", "cambiarPassword resp code=" + resp.code());
//                try {
//                    String body = resp.errorBody() != null ? resp.errorBody().string() : "";
//                    if (!body.isEmpty()) {
//                        android.util.Log.d("UsuarioRepository", "cambiarPassword errorBody=" + body);
//                    }
//                } catch (Exception ignored) {}
//                data.postValue(resp.isSuccessful());
//            }
//            @Override public void onFailure(Call<Void> call, Throwable t) {
//                android.util.Log.e("UsuarioRepository", "cambiarPassword failure: " + t.getMessage(), t);
//                data.postValue(false);
//            }
//        });
//        return data;
//    }

    public LiveData<Boolean> cambiarPassword(Long id, String passActual, String passNueva) {
        MutableLiveData<Boolean> data = new MutableLiveData<>();

        // Usamos la clase auxiliar para crear el JSON correcto
        PasswordChangeRequest request = new PasswordChangeRequest(passActual, passNueva);

        api.cambiarPassword(id, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> resp) {
                if (resp.isSuccessful()) {
                    // Todo salió bien (Código 200)
                    data.postValue(true);
                } else {
                    // EL SERVIDOR DIO ERROR. Vamos a leer qué pasó.
                    try {
                        String errorBody = resp.errorBody() != null ? resp.errorBody().string() : "Sin mensaje de error";
                        int codigo = resp.code();

                        // ESTO saldrá en tu Logcat (pestaña inferior de Android Studio)
                        android.util.Log.e("API_ERROR", "Código: " + codigo + " | Error: " + errorBody);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    data.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                android.util.Log.e("API_FAIL", "Fallo de conexión: " + t.getMessage());
                data.postValue(false);
            }
        });
        return data;
    }
}