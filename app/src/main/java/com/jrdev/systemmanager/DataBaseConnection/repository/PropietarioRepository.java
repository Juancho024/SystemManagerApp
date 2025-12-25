package com.jrdev.systemmanager.DataBaseConnection.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jrdev.systemmanager.DataBaseConnection.ApiClient;
import com.jrdev.systemmanager.DataBaseConnection.api.PropietarioApi;
import com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PropietarioRepository {
    private final PropietarioApi api;

    public PropietarioRepository(String baseUrl) {
        Retrofit r = ApiClient.get(baseUrl);
        this.api = r.create(PropietarioApi.class);
    }

    public LiveData<List<PropietarioDao>> listar(String estado, String buscar) {
        MutableLiveData<List<PropietarioDao>> data = new MutableLiveData<>();
        System.out.println("DEBUG Repository: Llamando a listar() con estado='" + estado + "' buscar='" + buscar + "'");
        
        Call<List<PropietarioDao>> call;
        
        // Si ambos parámetros están vacíos, llamar sin filtros
        if ((estado == null || estado.trim().isEmpty()) && (buscar == null || buscar.trim().isEmpty())) {
            call = api.listar();
        } else {
            // Si hay filtros, llamar con parámetros
            call = api.listarConFiltros(estado, buscar);
        }
        
        call.enqueue(new Callback<List<PropietarioDao>>() {
            @Override public void onResponse(Call<List<PropietarioDao>> call, Response<List<PropietarioDao>> resp) {
                data.postValue(resp.body());
            }
            @Override public void onFailure(Call<List<PropietarioDao>> call, Throwable t) {
                t.printStackTrace();
                data.postValue(null);
            }
        });
        return data;
    }

    public LiveData<PropietarioDao> obtener(long id) {
        MutableLiveData<PropietarioDao> data = new MutableLiveData<>();
        api.obtener(id).enqueue(new Callback<PropietarioDao>() {
            @Override public void onResponse(Call<PropietarioDao> call, Response<PropietarioDao> resp) {
                data.postValue(resp.body());
            }
            @Override public void onFailure(Call<PropietarioDao> call, Throwable t) { data.postValue(null); }
        });
        return data;
    }

    public LiveData<PropietarioDao> crear(PropietarioDao p) {
        MutableLiveData<PropietarioDao> data = new MutableLiveData<>();
        api.crear(p).enqueue(new Callback<PropietarioDao>() {
            @Override public void onResponse(Call<PropietarioDao> call, Response<PropietarioDao> resp) {
                data.postValue(resp.body());
            }
            @Override public void onFailure(Call<PropietarioDao> call, Throwable t) { data.postValue(null); }
        });
        return data;
    }

    public LiveData<PropietarioDao> actualizar(long id, PropietarioDao p) {
        MutableLiveData<PropietarioDao> data = new MutableLiveData<>();
        api.actualizar(id, p).enqueue(new Callback<PropietarioDao>() {
            @Override public void onResponse(Call<PropietarioDao> call, Response<PropietarioDao> resp) {
                data.postValue(resp.body());
            }
            @Override public void onFailure(Call<PropietarioDao> call, Throwable t) { data.postValue(null); }
        });
        return data;
    }

    public LiveData<Boolean> eliminar(long id) {
        MutableLiveData<Boolean> data = new MutableLiveData<>();
        api.eliminar(id).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) { data.postValue(resp.isSuccessful()); }
            @Override public void onFailure(Call<Void> call, Throwable t) { data.postValue(false); }
        });
        return data;
    }

    public LiveData<Boolean> actualizarEstado(long id) {
        MutableLiveData<Boolean> data = new MutableLiveData<>();
        api.actualizarEstado(id).enqueue(new Callback<PropietarioDao>() {
            @Override public void onResponse(Call<PropietarioDao> call, Response<PropietarioDao> resp) {
                data.postValue(resp.isSuccessful());
            }
            @Override public void onFailure(Call<PropietarioDao> call, Throwable t) {
                t.printStackTrace();
                data.postValue(false);
            }
        });
        return data;
    }

}