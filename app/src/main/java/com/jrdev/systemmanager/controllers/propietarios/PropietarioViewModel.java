package com.jrdev.systemmanager.controllers.propietarios;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao;
import com.jrdev.systemmanager.DataBaseConnection.repository.PropietarioRepository;
import java.util.List;

public class PropietarioViewModel extends ViewModel {

    private final PropietarioRepository repository;
    private final LiveData<List<PropietarioDao>> propietarios;

    private static final String BASE_URL = "https://api-systemmanager.onrender.com";

    public PropietarioViewModel() {
        repository = new PropietarioRepository(BASE_URL);
        propietarios = repository.listar("", "");
    }

    public LiveData<List<PropietarioDao>> getPropietarios() {
        return propietarios;
    }

    public LiveData<PropietarioDao> actualizar(long id, PropietarioDao p) {
        return repository.actualizar(id, p);
    }

    public LiveData<Boolean> eliminar(long id) {
        return repository.eliminar(id);
    }
}
