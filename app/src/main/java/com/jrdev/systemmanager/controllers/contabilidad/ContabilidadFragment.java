package com.jrdev.systemmanager.controllers.contabilidad;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao;
import com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao;
import com.jrdev.systemmanager.DataBaseConnection.repository.PropietarioRepository;
import com.jrdev.systemmanager.DataBaseConnection.repository.RegistroFinancieroRepository;
import com.jrdev.systemmanager.models.TablaRegistroItem;
import com.jrdev.systemmanager.R;
import com.jrdev.systemmanager.adapters.RegistroFinancieroAdapter;
import com.jrdev.systemmanager.models.InformeTableView;
import com.google.android.material.card.MaterialCardView;
import com.jrdev.systemmanager.utilities.GeneradorIMG;
import com.jrdev.systemmanager.utilities.GeneradorPDF;

import java.time.YearMonth;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import com.jrdev.systemmanager.BuildConfig;

public class ContabilidadFragment extends Fragment {
    private RecyclerView tableContabilidad;
    private RegistroFinancieroAdapter adapter;
    private RegistroFinancieroRepository registroRepo;
    private PropietarioRepository propietarioRepo;
    private List<RegistroFinancieroDao> registros = new LinkedList<>();
    private List<PropietarioDao> propietarios = new LinkedList<>();
    private MaterialButton btnActualizarRegistro;
    private MaterialButton btnGenerarPdf;
    private EditText txtBuscarContabilidad;
    private TextView txtMesCuota;
    private MaterialCardView cvMensajeBienvenidaContabilidad;
    private MaterialCardView cardTabla;
    private boolean isUpdatingSearch = false;
    private ActivityResultLauncher<Intent> createPdfLauncher;

    private boolean modoPDF = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contabilidad, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializar repository
        registroRepo = new RegistroFinancieroRepository(BuildConfig.API_URL);
        propietarioRepo = new PropietarioRepository(BuildConfig.API_URL);

        // Inicializar vistas
        tableContabilidad = view.findViewById(R.id.tableContabilidad);
        btnActualizarRegistro = view.findViewById(R.id.btnActualizarRegistro);
        btnGenerarPdf = view.findViewById(R.id.btnGenerarPdf);
        txtBuscarContabilidad = view.findViewById(R.id.txtbuscarContabilidad);
        txtMesCuota = view.findViewById(R.id.txtMesCuota);
        cvMensajeBienvenidaContabilidad = view.findViewById(R.id.cvMensajeBienvenidaContabilidad);
        cardTabla = view.findViewById(R.id.cardTabla);


        // Configurar RecyclerView
        adapter = new RegistroFinancieroAdapter();
        tableContabilidad.setLayoutManager(new LinearLayoutManager(getContext()));
        tableContabilidad.setAdapter(adapter);

        // Cargar datos iniciales
        registroRepo.listar().observe(getViewLifecycleOwner(), list -> {
            if (list != null) registros = list; else registros = new LinkedList<>();
        });
        propietarioRepo.listar("", "").observe(getViewLifecycleOwner(), list -> {
            if (list != null) propietarios = list; else propietarios = new LinkedList<>();
        });

        // Estado inicial: mensaje visible, tabla oculta, botones deshabilitados
        setEmptyUIState(true);
        // Limpiar el buscador programáticamente sin reentrar
        isUpdatingSearch = true;
        txtBuscarContabilidad.setText("");
        isUpdatingSearch = false;
        btnActualizarRegistro.setEnabled(true);
        btnActualizarRegistro.setText("Registro");
        btnGenerarPdf.setEnabled(false);

        // Registrar launcher para guardar en Descargas/Documentos mediante SAF
        createPdfLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
            Uri uri = result.getData().getData();
            if (uri == null) return;
            String mes = txtBuscarContabilidad.getText().toString().trim();
            RegistroFinancieroDao registroMes = buscarRegistroMes(mes);
            List<InformeTableView> datos = combinarDataTableByMonthPDF(mes, registroMes);
            if (registroMes == null || datos.isEmpty()) {
                showMessage("Sin datos para PDF");
                return;
            }
            try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                if (os == null) {
                    showMessage("No se pudo abrir el destino");
                    return;
                }
                if(modoPDF){
                    new GeneradorPDF().generarReporte(os, registroMes, datos);
                } else {
                    new GeneradorIMG().generarImagen(os, registroMes, datos);
                }
                showMessage("PDF guardado");
            } catch (Exception e) {
                showMessage("Error al guardar PDF");
            }
        });

        // Configurar buscador
        txtBuscarContabilidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingSearch) return;
                String mes = s.toString().trim();
                if (mes.isEmpty()) {
                    adapter.setItems(new LinkedList<>());
                    txtMesCuota.setText("Mes: ---");
                    setEmptyUIState(true);
                    btnActualizarRegistro.setEnabled(true);
                    btnActualizarRegistro.setText("Registro");
                    btnGenerarPdf.setEnabled(false);
                    return;
                }
                List<TablaRegistroItem> combinada = combinarDataTableByMonth(mes);
                adapter.setItems(combinada);
                txtMesCuota.setText(combinada.isEmpty() ? "Mes: ---" : ("Mes: " + mes));
                if(combinada.isEmpty()){
                    showMessage("La fecha ingresada no es válida.\nEjemplo: Junio-2025");
                    setEmptyUIState(true);
                    btnActualizarRegistro.setEnabled(true);
                    btnActualizarRegistro.setText("Registro");
                    btnGenerarPdf.setEnabled(false);
                } else {
                    setEmptyUIState(false);
                    btnActualizarRegistro.setEnabled(true);
                    btnActualizarRegistro.setText("Actualizar");
                    btnGenerarPdf.setEnabled(true);
                }
            }
        });

        // Generar PDF
        btnGenerarPdf.setOnClickListener(v -> {
            String mes = txtBuscarContabilidad.getText().toString().trim();
            if (mes.isEmpty()) {
                showMessage("Primero ingresa un mes. Ejemplo: Junio-2025");
                return;
            }
            RegistroFinancieroDao registroMes = buscarRegistroMes(mes);
            if (registroMes == null) {
                showMessage("No existe registro financiero para " + mes);
                return;
            }
            List<InformeTableView> datos = combinarDataTableByMonthPDF(mes, registroMes);
            if (datos.isEmpty()) {
                showMessage("Sin datos para generar PDF en " + mes);
                return;
            }

            new MaterialAlertDialogBuilder(getContext(), R.style.DialogoRedondoYNegro)
                    .setTitle("Seleccionar Formato")
                    .setItems(new String[]{"Documento (PDF)", "Imagen (IMG)"}, (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        if (which == 0) {
                            modoPDF = true;
                            intent.setType("application/pdf");
                            intent.putExtra(Intent.EXTRA_TITLE, "Informe_" + mes + ".pdf");
                        } else {
                            modoPDF = false;
                            intent.setType("image/jpeg");
                            intent.putExtra(Intent.EXTRA_TITLE, "Informe_" + mes + ".jpg");
                        }
                        createPdfLauncher.launch(intent);
                    })
                    .show();
        });

        btnActualizarRegistro.setOnClickListener(v -> {
            String mes = txtBuscarContabilidad.getText().toString().trim();
            if (mes.isEmpty()) {
                abrirRegistroNuevo();
                return;
            }
            RegistroFinancieroDao registroMes = buscarRegistroMes(mes);
            if (registroMes == null) {
                showMessage("No existe registro para " + mes + ". Se abrirá uno nuevo.");
                abrirRegistroNuevo();
            } else {
                abrirRegistroEdicion(registroMes);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Al volver al fragment, limpiar buscador y UI para evitar estados viejos
        isUpdatingSearch = true;
        txtBuscarContabilidad.setText("");
        isUpdatingSearch = false;
        adapter.setItems(new LinkedList<>());
        txtMesCuota.setText("Mes: ---");
        setEmptyUIState(true);
        btnActualizarRegistro.setEnabled(true);
        btnActualizarRegistro.setText("Registro");
        btnGenerarPdf.setEnabled(false);
    }

    private void abrirRegistroNuevo() {
        Intent intent = new Intent(requireContext(), RegistroFinancieroActivity.class);
        startActivity(intent);
    }

    private void abrirRegistroEdicion(RegistroFinancieroDao registro) {
        Intent intent = new Intent(requireContext(), RegistroFinancieroActivity.class);
        intent.putExtra(RegistroFinancieroActivity.EXTRA_IS_EDIT, true);
        // Solo pasar el ID - el Activity cargará los datos desde la BD
        if (registro.id != null) {
            intent.putExtra(RegistroFinancieroActivity.EXTRA_ID, registro.id);
        }
        startActivity(intent);
    }

    private List<TablaRegistroItem> combinarDataTableByMonth(String mesbuscado) {
        List<TablaRegistroItem> combinedList = new LinkedList<>();
        RegistroFinancieroDao registroMes = null;
        for (RegistroFinancieroDao rf : registros) {
            if (rf.mesCuota != null && rf.mesCuota.equalsIgnoreCase(mesbuscado)) { registroMes = rf; break; }
        }
        if (registroMes == null) { return combinedList; }

        float balanceActual = calcularMontoDebidoHastaMes(mesbuscado);
        for (PropietarioDao p : propietarios) {
            float balancefinal = (p.totalabonado != null ? p.totalabonado : 0f) - balanceActual;
            combinedList.add(new TablaRegistroItem(p.numApto, registroMes.cuotaMensual, registroMes.descripcion, registroMes.montoPagar, balancefinal));
        }
        return combinedList;
    }

    private RegistroFinancieroDao buscarRegistroMes(String mesbuscado) {
        for (RegistroFinancieroDao rf : registros) {
            if (rf.mesCuota != null && rf.mesCuota.equalsIgnoreCase(mesbuscado)) return rf;
        }
        return null;
    }

    private List<InformeTableView> combinarDataTableByMonthPDF(String mesbuscado, RegistroFinancieroDao registroMes) {
        List<InformeTableView> combinedList = new LinkedList<>();
        if (registroMes == null) return combinedList;

        float balanceActual = calcularMontoDebidoHastaMes(mesbuscado);
        for (PropietarioDao p : propietarios) {
            float totalAbonado = p.totalabonado != null ? p.totalabonado : 0f;
            float balanceActualPropietario = totalAbonado - balanceActual;
            String estado = balanceActualPropietario < 0 ? "Rojo" : "Verde";
            combinedList.add(new InformeTableView(
                    registroMes.mesCuota,
                    p.numApto,
                    p.nombrePropietario,
                    estado,
                    totalAbonado,
                    registroMes.cuotaMensual != null ? registroMes.cuotaMensual : 0f,
                    registroMes.descripcion,
                    registroMes.montoPagar != null ? registroMes.montoPagar : 0f,
                    balanceActualPropietario
            ));
        }
        return combinedList;
    }

    private float calcularMontoDebidoHastaMes(String mesLimite) {
        YearMonth fechaLimite = obtenerOrdenMes(mesLimite);
        if (fechaLimite == null) return 0f;
        float totalAcumulado = 0f;
        for (RegistroFinancieroDao registro : registros) {
            YearMonth fechaRegistro = obtenerOrdenMes(registro.mesCuota);
            if (fechaRegistro != null && !fechaRegistro.isAfter(fechaLimite)) {
                totalAcumulado += (registro.montoPagar != null ? registro.montoPagar : 0f);
            }
        }
        return totalAcumulado;
    }

    private YearMonth obtenerOrdenMes(String mesCuota) {
        try {
            if (mesCuota == null || !mesCuota.contains("-")) return null;
            String[] partes = mesCuota.split("-");
            String nombreMes = partes[0].trim().toLowerCase();
            int anio = Integer.parseInt(partes[1].trim());
            int numeroMes;
            switch (nombreMes) {
                case "enero": numeroMes = 1; break;
                case "febrero": numeroMes = 2; break;
                case "marzo": numeroMes = 3; break;
                case "abril": numeroMes = 4; break;
                case "mayo": numeroMes = 5; break;
                case "junio": numeroMes = 6; break;
                case "julio": numeroMes = 7; break;
                case "agosto": numeroMes = 8; break;
                case "septiembre": numeroMes = 9; break;
                case "octubre": numeroMes = 10; break;
                case "noviembre": numeroMes = 11; break;
                case "diciembre": numeroMes = 12; break;
                default: numeroMes = 1; // seguridad
            }
            return YearMonth.of(anio, numeroMes);
        } catch (Exception e) { return YearMonth.now(); }
    }

    private void setEmptyUIState(boolean empty) {
        if (cardTabla != null && cvMensajeBienvenidaContabilidad != null) {
            cardTabla.setVisibility(empty ? View.GONE : View.VISIBLE);
            cvMensajeBienvenidaContabilidad.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
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