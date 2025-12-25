package com.jrdev.systemmanager.DataBaseConnection.repository;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jrdev.systemmanager.DataBaseConnection.ApiClient;
import com.jrdev.systemmanager.DataBaseConnection.api.MesCuotaApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MesCuotaRepository {
    private final MesCuotaApi api;

    public MesCuotaRepository(String baseUrl) {
        Retrofit r = ApiClient.get(baseUrl);
        this.api = r.create(MesCuotaApi.class);
    }

    public LiveData<List<String>> listar() {
        MutableLiveData<List<String>> data = new MutableLiveData<>();
        api.listar().enqueue(new Callback<List<String>>() {
            @Override public void onResponse(Call<List<String>> call, Response<List<String>> resp) { data.postValue(resp.body()); }
            @Override public void onFailure(Call<List<String>> call, Throwable t) { data.postValue(null); }
        });
        return data;
    }

    public LiveData<Boolean> eliminar(String mesAnio) {
        MutableLiveData<Boolean> data = new MutableLiveData<>();
        android.util.Log.d("MesCuotaRepository", "Eliminando mesAnio: '" + mesAnio + "'");
        api.eliminar(mesAnio).enqueue(new Callback<Void>() {
            @Override 
            public void onResponse(Call<Void> call, Response<Void> resp) { 
                android.util.Log.d("MesCuotaRepository", "Response code: " + resp.code() + ", isSuccessful: " + resp.isSuccessful());
                android.util.Log.d("MesCuotaRepository", "URL llamada: " + call.request().url());
                if (!resp.isSuccessful() && resp.errorBody() != null) {
                    try {
                        android.util.Log.e("MesCuotaRepository", "Error body: " + resp.errorBody().string());
                    } catch (Exception e) {}
                }
                data.postValue(resp.isSuccessful()); 
            }
            @Override 
            public void onFailure(Call<Void> call, Throwable t) { 
                android.util.Log.e("MesCuotaRepository", "Error en eliminación: " + t.getMessage(), t);
                data.postValue(false); 
            }
        });
        return data;
    }
}
