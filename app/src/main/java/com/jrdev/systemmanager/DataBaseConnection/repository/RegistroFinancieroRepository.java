package com.jrdev.systemmanager.DataBaseConnection.repository;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jrdev.systemmanager.DataBaseConnection.ApiClient;
import com.jrdev.systemmanager.DataBaseConnection.api.RegistroFinancieroApi;
import com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegistroFinancieroRepository {
    private final RegistroFinancieroApi api;

    public RegistroFinancieroRepository(String baseUrl) {
        Retrofit r = ApiClient.get(baseUrl);
        this.api = r.create(RegistroFinancieroApi.class);
    }

    public LiveData<List<RegistroFinancieroDao>> listar() {
        MutableLiveData<List<RegistroFinancieroDao>> data = new MutableLiveData<>();
        api.listar().enqueue(new Callback<List<RegistroFinancieroDao>>() {
            @Override public void onResponse(Call<List<RegistroFinancieroDao>> call, Response<List<RegistroFinancieroDao>> resp) {
                data.postValue(resp.body());
            }
            @Override public void onFailure(Call<List<RegistroFinancieroDao>> call, Throwable t) { data.postValue(null); }
        });
        return data;
    }

    public LiveData<RegistroFinancieroDao> obtener(long id) {
        MutableLiveData<RegistroFinancieroDao> data = new MutableLiveData<>();
        api.obtener(id).enqueue(new Callback<RegistroFinancieroDao>() {
            @Override public void onResponse(Call<RegistroFinancieroDao> call, Response<RegistroFinancieroDao> resp) {
                data.postValue(resp.body());
            }
            @Override public void onFailure(Call<RegistroFinancieroDao> call, Throwable t) { data.postValue(null); }
        });
        return data;
    }

    public LiveData<RegistroFinancieroDao> crear(RegistroFinancieroDao r) {
        MutableLiveData<RegistroFinancieroDao> data = new MutableLiveData<>();
        api.crear(r).enqueue(new Callback<RegistroFinancieroDao>() {
            @Override public void onResponse(Call<RegistroFinancieroDao> call, Response<RegistroFinancieroDao> resp) {
                data.postValue(resp.body());
            }
            @Override public void onFailure(Call<RegistroFinancieroDao> call, Throwable t) { data.postValue(null); }
        });
        return data;
    }

    public LiveData<RegistroFinancieroDao> actualizar(long id, RegistroFinancieroDao r) {
        MutableLiveData<RegistroFinancieroDao> data = new MutableLiveData<>();
        api.actualizar(id, r).enqueue(new Callback<RegistroFinancieroDao>() {
            @Override public void onResponse(Call<RegistroFinancieroDao> call, Response<RegistroFinancieroDao> resp) {
                data.postValue(resp.body());
            }
            @Override public void onFailure(Call<RegistroFinancieroDao> call, Throwable t) { data.postValue(null); }
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
}
