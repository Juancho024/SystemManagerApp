package com.jrdev.systemmanager.controllers.propietarios;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao;
import com.jrdev.systemmanager.R;

import java.util.LinkedList;
import java.util.List;

public class PropietarioFragment extends Fragment {

    private List<PropietarioDao> propietarios = new LinkedList<>();
    private PropietarioViewModel propietarioViewModel;

    private EditText txtBuscarPropietario;
    private EditText txtNumApto;
    private EditText txtNombreP;
    private EditText txtTotalAbonado;
    private EditText txtTotalAdeudado;
    private EditText txtMontoAgregadoAbono;
    private LinearLayout llMontoAgregadoAbono;
    private LinearLayout llTotalAbonado;
    private TextView lbAlDia;
    private TextView lbPendiente;
    private MaterialButton btnModificar;
    private MaterialButton btnEliminar;
    private MaterialCardView cvDetalles;
    private MaterialCardView cvMensajeBienvenida;
    private List<PropietarioDao> propietariosFiltrados = new LinkedList<>();
    private boolean isUpdatingSearch = false;

    // Para guardar el propietario actual
    private PropietarioDao propietarioActual = null;
    private boolean modoEdicion = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_propietario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        propietarioViewModel = new ViewModelProvider(this).get(PropietarioViewModel.class);

        propietarioViewModel.getPropietarios().observe(getViewLifecycleOwner(), aux -> {
            if (aux != null) {
                propietarios = aux;
            }
        });

        // Inicializar vistas
        txtBuscarPropietario = view.findViewById(R.id.txtBuscarPropietario);
        txtNumApto = view.findViewById(R.id.txtNumApto);
        txtNombreP = view.findViewById(R.id.txtNombreP);
        txtTotalAbonado = view.findViewById(R.id.txtTotalAbonado);
        txtTotalAdeudado = view.findViewById(R.id.txtTotalAdeudado);
        txtMontoAgregadoAbono = view.findViewById(R.id.txtMontoAgregadoAbono);
        llMontoAgregadoAbono = view.findViewById(R.id.llMontoAgregadoAbono);
        llTotalAbonado = view.findViewById(R.id.llTotalAbonado);
        lbAlDia = view.findViewById(R.id.lbAlDia);
        lbPendiente = view.findViewById(R.id.lbPendiente);
        btnModificar = view.findViewById(R.id.btnModificar);
        btnEliminar = view.findViewById(R.id.btnEliminar);
        cvDetalles = view.findViewById(R.id.cvDetalles);
        cvMensajeBienvenida = view.findViewById(R.id.cvMensajeBienvenida);

        // Mostrar mensaje inicial, ocultar detalles
        cvMensajeBienvenida.setVisibility(View.VISIBLE);
        cvDetalles.setVisibility(View.GONE);

        desactivarEdicion();

        // Configurar buscador
        txtBuscarPropietario.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingSearch) return;

                propietariosFiltrados.clear();
                buscarPropietario(s.toString());
                if (!propietariosFiltrados.isEmpty()) {
                    cargarPropietario(propietariosFiltrados.get(0));
                } else {
                    limpiarFormulario(false);
                }
            }
        });

        // Boton Modificar
        btnModificar.setOnClickListener(v -> {
            if (!modoEdicion) {
                activarEdicion();
            } else {
                actualizarPropietario();
            }
        });

        // Boton Eliminar/Cancelar
        btnEliminar.setOnClickListener(v -> {
            if (!modoEdicion) {
                eliminarPropietario();
            } else {
                cancelarEdicion();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Al volver al fragment, limpiar el buscador y el formulario para evitar estados inconsistentes
        isUpdatingSearch = true;
        txtBuscarPropietario.setText("");
        isUpdatingSearch = false;
        propietariosFiltrados.clear();
        limpiarFormulario(false);
    }

    private void activarEdicion() {
        modoEdicion = true;
        txtMontoAgregadoAbono.setEnabled(true);
        llMontoAgregadoAbono.setVisibility(View.VISIBLE);
        
        // Ajustar weights para que ambos ocupen 50/50
        LinearLayout.LayoutParams paramsAbono = (LinearLayout.LayoutParams) llTotalAbonado.getLayoutParams();
        paramsAbono.weight = 1;
        llTotalAbonado.setLayoutParams(paramsAbono);

        btnModificar.setText("Actualizar");
        btnEliminar.setText("Cancelar");
    }

    private void desactivarEdicion() {
        modoEdicion = false;
        txtNumApto.setEnabled(false);
        txtNombreP.setEnabled(false);
        txtMontoAgregadoAbono.setEnabled(false);
        llMontoAgregadoAbono.setVisibility(View.GONE);
        
        // Ajustar weights para que los totales ocupen todos el anchos
        LinearLayout.LayoutParams paramsAbono = (LinearLayout.LayoutParams) llTotalAbonado.getLayoutParams();
        paramsAbono.weight = 2;
        llTotalAbonado.setLayoutParams(paramsAbono);

        btnModificar.setText("Modificar");
        btnEliminar.setText("Eliminar");
    }

    private void cancelarEdicion() {
        desactivarEdicion();
        // Recargar datos anteriores
        if (propietarioActual != null) {
            cargarPropietario(propietarioActual);
        }
    }

    private void actualizarPropietario() {
        if (propietarioActual == null) return;

        // Actualizar datos del propietario
        propietarioActual.numApto = txtNumApto.getText().toString().trim();
        propietarioActual.nombrePropietario = txtNombreP.getText().toString().trim();

        try {
            float totalAbonadoActual = parseNumber(txtTotalAbonado.getText().toString());
            float agregadoAbono = parseNumber(txtMontoAgregadoAbono.getText().toString());
            float newAbonado = totalAbonadoActual + agregadoAbono;
            float balance = parseNumber(txtTotalAdeudado.getText().toString());
            propietarioActual.totalabonado = newAbonado;
            propietarioActual.balance =  agregadoAbono + balance;
            if(propietarioActual.balance < 0){
                propietarioActual.estado = "Rojo";
            } else {
                propietarioActual.estado = "Verde";
            }
        } catch (NumberFormatException e) {
            showMessage("Formato de números inválido");
            return;
        }

        // Llamar al repositorio para actualizar
        propietarioViewModel.actualizar(propietarioActual.idpropietario, propietarioActual).observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                showMessage("Propietario actualizado correctamente");
                cargarPropietario(propietarioActual);
                desactivarEdicion();
            } else {
                showMessage("Error al actualizar propietario");
            }
        });
    }

    private void eliminarPropietario() {
        if (propietarioActual == null) return;

        propietarioViewModel.eliminar(propietarioActual.idpropietario).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                showMessage("Propietario eliminado correctamente");
                limpiarFormulario(true);
            } else {
                showMessage("Error al eliminar propietario");
            }
        });
    }

    private void limpiarFormulario(boolean clearSearchText) {
        if (clearSearchText) {
            isUpdatingSearch = true;
            txtBuscarPropietario.setText("");
            isUpdatingSearch = false;
        }
        txtNumApto.setText("");
        txtNombreP.setText("");
        txtTotalAbonado.setText("");
        txtTotalAdeudado.setText("");
        txtMontoAgregadoAbono.setText("0");
        lbAlDia.setVisibility(View.GONE);
        lbPendiente.setVisibility(View.GONE);
        propietarioActual = null;

//         Mostrar mensaje inicial
        cvMensajeBienvenida.setVisibility(View.VISIBLE);
        cvDetalles.setVisibility(View.GONE);
    }

    private void buscarPropietario(String buscar) {
        String busqueda = buscar.toLowerCase().trim();

        if (busqueda.isEmpty()) {
            limpiarFormulario(false);
            return;
        }
        for (PropietarioDao p : propietarios) {
            if(p.nombrePropietario.toLowerCase().contains(busqueda) || p.numApto.toLowerCase().contains(busqueda) ||
            String.format("%.2f", p.totalabonado).contains(busqueda) || String.format("%.2f", p.balance).contains(busqueda)){
                propietariosFiltrados.add(p);
            }
        }
    }

    private void cargarPropietario(PropietarioDao propietarioDao) {
        propietarioActual = propietarioDao;

        // Ocultar mensaje inicial, mostrar detalles
        cvMensajeBienvenida.setVisibility(View.GONE);
        cvDetalles.setVisibility(View.VISIBLE);

        if (propietarioDao.estado.equalsIgnoreCase("Verde")) {
            lbAlDia.setVisibility(View.VISIBLE);
            lbPendiente.setVisibility(View.GONE);
        } else if (propietarioDao.estado.equalsIgnoreCase("Rojo")) {
            lbAlDia.setVisibility(View.GONE);
            lbPendiente.setVisibility(View.VISIBLE);
        }

        txtNumApto.setText(propietarioDao.numApto);
        txtNombreP.setText(propietarioDao.nombrePropietario);
        txtTotalAbonado.setText(String.format("DOP$ %.2f", propietarioDao.totalabonado));
        txtTotalAdeudado.setText(String.format("DOP$ %.2f", propietarioDao.balance));
        txtMontoAgregadoAbono.setText("0");
    }

    private float parseNumber(String value) throws NumberFormatException {
        String clean = value.replace("DOP$ ", "").replace("$", "").trim();
        if (clean.isEmpty()) return 0f;
        return Float.parseFloat(clean);
    }

    private void showMessage(String message) {
        View root = getView();
        if (root != null) {
            Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
        } else if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
