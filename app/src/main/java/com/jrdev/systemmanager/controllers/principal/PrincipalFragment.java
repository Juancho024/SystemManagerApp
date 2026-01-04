package com.jrdev.systemmanager.controllers.principal;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jrdev.systemmanager.BuildConfig;
import com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao;
import com.jrdev.systemmanager.DataBaseConnection.repository.PropietarioRepository;
import com.jrdev.systemmanager.R;
import com.jrdev.systemmanager.adapters.PropietariosAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PrincipalFragment extends Fragment {
    private RecyclerView tablePrincipal;
    private PropietariosAdapter adapter;
    private PropietarioRepository repository;
    private EditText etBuscarPrincipal;
    private TextView txtTotalAbonado, txtTotalAdeudado;
    
    // Lista completa de propietarios (sin filtrar)
    private List<PropietarioDao> propietariosCompletos = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_principal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializar repository
        repository = new PropietarioRepository(BuildConfig.API_URL);

        // Inicializar vistas
        tablePrincipal = view.findViewById(R.id.tablePrincipal);
        etBuscarPrincipal = view.findViewById(R.id.etBuscarPrincipal);
        txtTotalAbonado = view.findViewById(R.id.txtTotalAbonado);
        txtTotalAdeudado = view.findViewById(R.id.txtTotalAdeudado);

        // Configurar RecyclerView
        adapter = new PropietariosAdapter();
        tablePrincipal.setLayoutManager(new LinearLayoutManager(getContext()));
        tablePrincipal.setAdapter(adapter);

        // Cargar datos iniciales
        cargarPropietarios();

        // Configurar buscador
        etBuscarPrincipal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                buscarPropietario(s.toString());
            }
        });
    }

    private void cargarPropietarios() {
        // Cargar todos los propietarios una sola vez
        repository.listar("", "").observe(getViewLifecycleOwner(), propietarios -> {
            if (propietarios != null) {
                propietariosCompletos = new ArrayList<>(propietarios);
                adapter.actualizarLista(propietariosCompletos);
                calcularTotales(propietariosCompletos);
            } else {
                // Manejar error o lista vacía
                txtTotalAbonado.setText("DOP $0.00");
                txtTotalAdeudado.setText("DOP $0.00");
            }
        });
    }
    
    private void buscarPropietario(String buscar) {
        String busqueda = buscar.toLowerCase().trim();
        
        // Si la búsqueda está vacía, mostrar todos
        if (busqueda.isEmpty()) {
            adapter.actualizarLista(propietariosCompletos);
            return;
        }
        
        // Filtrar localmente
        List<PropietarioDao> propietariosFiltrados = new ArrayList<>();
        for (PropietarioDao p : propietariosCompletos) {
            if(p.nombrePropietario.toLowerCase().contains(busqueda) || p.numApto.toLowerCase().contains(busqueda) ||
                    String.format("%.2f", p.totalabonado).contains(busqueda) || String.format("%.2f", p.balance).contains(busqueda)){
                propietariosFiltrados.add(p);
            }
        }
        adapter.actualizarLista(propietariosFiltrados);
    }

    // Ordena aptos tipo "A-1", "A-10", "B-2": primero prefijo, luego número
    private int compareApto(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";

        String pa = a.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
        String pb = b.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
        int prefix = pa.compareTo(pb);
        if (prefix != 0) return prefix;

        // Si los prefijos son iguales, comparar el número
        try {
            int na = Integer.parseInt(a.replaceAll("[^0-9]", ""));
            int nb = Integer.parseInt(b.replaceAll("[^0-9]", ""));
            int numeric = Integer.compare(na, nb);
            if (numeric != 0) return numeric;
        } catch (Exception ignored) {
            // Si no hay números, cae a comparación completa
        }

        return a.compareToIgnoreCase(b);
    }

    private void calcularTotales(List<PropietarioDao> propietarios) {
        float totalAbonado = 0.0f;
        float totalAdeudado = 0.0f;

        for (PropietarioDao p : propietarios) {
            if (p.totalabonado != null && p.totalabonado >= 0) {
                totalAbonado += p.totalabonado;
            }
            if (p.balance != null && p.balance < 0) {
                totalAdeudado += p.balance;
            }
        }

        txtTotalAbonado.setText(String.format("DOP $ %.2f", totalAbonado));
        txtTotalAdeudado.setText(String.format("DOP $ %.2f", totalAdeudado));
    }

}