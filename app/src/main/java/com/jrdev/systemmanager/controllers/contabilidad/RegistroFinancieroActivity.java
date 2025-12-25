package com.jrdev.systemmanager.controllers.contabilidad;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.app.Dialog;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao;
import com.jrdev.systemmanager.DataBaseConnection.repository.RegistroFinancieroRepository;
import com.jrdev.systemmanager.databinding.ActivityRegistroFinancieroBinding;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegistroFinancieroActivity extends AppCompatActivity {

    public static final String EXTRA_IS_EDIT = "extra_is_edit";
    public static final String EXTRA_ID = "extra_id";
    private ActivityRegistroFinancieroBinding binding;
    private RegistroFinancieroRepository registroRepo;
    private com.jrdev.systemmanager.DataBaseConnection.repository.MesCuotaRepository mesCuotaRepo;
    private com.jrdev.systemmanager.DataBaseConnection.repository.PropietarioRepository propietarioRepo;
    private boolean isEdit = false;
    private long registroId = -1;
    private final byte[][] imagenesBytes = new byte[4][];
    private ActivityResultLauncher<Intent> pickImagesLauncher;
    private static final String BASE_URL = "https://api-systemmanager.onrender.com";
        private static final Pattern PATRON_MES = Pattern.compile(
            "^\\s*(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|setiembre|octubre|noviembre|diciembre)\\s*-\\s*\\d{4}\\s*$",
            Pattern.CASE_INSENSITIVE
        );
    private com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao registroOriginal;

    // Simple modal dialog to show progress/messages
    private Dialog progresoDialog;
    private android.widget.TextView dlgTitle;
    private android.widget.TextView dlgStatus;
    private android.widget.ProgressBar dlgProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistroFinancieroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registroRepo = new RegistroFinancieroRepository(BASE_URL);
        mesCuotaRepo = new com.jrdev.systemmanager.DataBaseConnection.repository.MesCuotaRepository(BASE_URL);
        propietarioRepo = new com.jrdev.systemmanager.DataBaseConnection.repository.PropietarioRepository(BASE_URL);

        setupMesAutocomplete();
        setupOtrosToggle();
        setupImagePicker();
        setupRemoveButtons();
        leerExtras();

        binding.btnCancelar.setOnClickListener(v -> finish());
        binding.btnGuardar.setOnClickListener(v -> guardar());
        binding.btnSeleccionarArchivos.setOnClickListener(v -> lanzarPickerImagenes());
    }

    private void mostrarDialogoProgreso(String titulo, String estadoInicial) {
        if (progresoDialog == null) {
            progresoDialog = new Dialog(this);
            progresoDialog.setCancelable(false);
            View content = LayoutInflater.from(this).inflate(com.jrdev.systemmanager.R.layout.dialog_proceso, null);
            progresoDialog.setContentView(content);
            dlgTitle = content.findViewById(com.jrdev.systemmanager.R.id.tvTitle);
            dlgStatus = content.findViewById(com.jrdev.systemmanager.R.id.tvStatus);
            dlgProgress = content.findViewById(com.jrdev.systemmanager.R.id.progressBar);
        }
        dlgTitle.setText(titulo);
        dlgStatus.setText(estadoInicial);
        dlgProgress.setProgress(0);
        progresoDialog.show();
    }

    private void actualizarDialogo(String estado, int porcentaje) {
        if (progresoDialog != null && progresoDialog.isShowing()) {
            dlgStatus.setText(estado);
            dlgProgress.setProgress(Math.max(0, Math.min(100, porcentaje)));
        }
    }

    private void cerrarDialogoProgreso() {
        if (progresoDialog != null && progresoDialog.isShowing()) {
            progresoDialog.dismiss();
        }
    }

    private void setupOtrosToggle() {
        binding.cbOtros.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            // Show/hide the Asunto field container and enable text accordingly
            binding.tilOtros.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            binding.etOtros.setEnabled(isChecked);
            if (!isChecked) {
                binding.etOtros.setText("");
            }
        });
    }

    private void setupMesAutocomplete() {
        mesCuotaRepo.listar().observe(this, opciones -> {
            if (opciones == null || opciones.isEmpty()) {
                mostrar("No se pudo cargar la lista de meses");
                return;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, opciones);
            binding.etMesCuota.setAdapter(adapter);
            // Bloquear escritura y abrir el dropdown al tocar
            binding.etMesCuota.setInputType(android.text.InputType.TYPE_NULL);
            binding.etMesCuota.setKeyListener(null);
            binding.etMesCuota.setFocusable(false);
            binding.etMesCuota.setOnClickListener(v -> binding.etMesCuota.showDropDown());
            binding.tilMesCuota.setEndIconOnClickListener(v -> binding.etMesCuota.showDropDown());
        });
    }

    private void setupImagePicker() {
        pickImagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
            Intent data = result.getData();
            if (data.getClipData() != null) {
                int total = data.getClipData().getItemCount();
                for (int i = 0; i < total; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    agregarImagenDesdeUri(uri);
                }
            } else if (data.getData() != null) {
                agregarImagenDesdeUri(data.getData());
            }
        });
    }

    private void setupRemoveButtons() {
        binding.btnRemove1.setOnClickListener(v -> limpiarSlot(0));
        binding.btnRemove2.setOnClickListener(v -> limpiarSlot(1));
        binding.btnRemove3.setOnClickListener(v -> limpiarSlot(2));
        binding.btnRemove4.setOnClickListener(v -> limpiarSlot(3));
    }

    private void leerExtras() {
        if (getIntent() == null) return;
        isEdit = getIntent().getBooleanExtra(EXTRA_IS_EDIT, false);
        registroId = getIntent().getLongExtra(EXTRA_ID, -1);

        if (isEdit && registroId > 0) {
            // Cargar el registro completo desde la base de datos
            cargarRegistroDesdeRepositorio(registroId);
            binding.btnGuardar.setText("Actualizar Registro");
            // No permitir cambiar la cuota del mes cuando se edita
            binding.etMesCuota.setEnabled(false);
            binding.etMesCuota.setFocusable(false);
            binding.tilMesCuota.setEnabled(false);
        }
    }

    private void guardar() {
        String mes = binding.etMesCuota.getText() != null ? binding.etMesCuota.getText().toString().trim() : "";
        String cuotaTxt = binding.etMontoCuota.getText() != null ? binding.etMontoCuota.getText().toString().trim() : "";
        String montoTxt = binding.etMontoFinal.getText() != null ? binding.etMontoFinal.getText().toString().trim() : "";

        // Solo permitir selección desde la lista; sin validación de formato
        if (mes.isEmpty()) { binding.tilMesCuota.setError("Selecciona el mes desde la lista"); return; }
        binding.tilMesCuota.setError(null);
        if (cuotaTxt.isEmpty()) { mostrar("Ingresa el monto de la cuota"); return; }
        if (montoTxt.isEmpty()) { montoTxt = cuotaTxt; }

        float cuota;
        float monto;
        try {
            cuota = Float.parseFloat(cuotaTxt);
            monto = Float.parseFloat(montoTxt);
        } catch (NumberFormatException e) {
            mostrar("Usa números válidos");
            return;
        }

        String descripcion = construirDescripcion();
        RegistroFinancieroDao dto = new RegistroFinancieroDao();
        dto.mesCuota = mes;
        dto.cuotaMensual = cuota;
        dto.descripcion = descripcion;
        dto.montoPagar = monto;
        dto.img1 = encodeSlot(0);
        dto.img2 = encodeSlot(1);
        dto.img3 = encodeSlot(2);
        dto.img4 = encodeSlot(3);

        if (isEdit && registroId > 0) {
            ejecutarProcesoActualizacion(monto, dto);
        } else {
            ejecutarProcesoCreacion(monto, dto, mes);
        }
    }

    // CREACIÓN: actualizar balances de todos, crear registro y eliminar mes
    private void ejecutarProcesoCreacion(float montoFinal, com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao registro, String mesCuota) {
        mostrarDialogoProgreso("Procesando Pagos", "Iniciando transacción segura...");
        propietarioRepo.listar(null, null).observe(this, propietarios -> {
            if (propietarios == null) {
                cerrarDialogoProgreso();
                mostrar("No se pudo cargar propietarios");
                return;
            }
            final int total = propietarios.size();
            if (total == 0) {
                actualizarDialogo("Guardando registro...", 50);
                continuarCreacionSinBalances(registro, mesCuota);
                return;
            }
            // Secuencial
            actualizarPropietarioSecuencial(propietarios, 0, total, montoFinal, () -> {
                actualizarDialogo("Finalizando y guardando registro...", 90);
                registroRepo.crear(registro).observe(this, creado -> {
                    if (creado == null) {
                        cerrarDialogoProgreso();
                        mostrar("Error al crear registro");
                        return;
                    }
                    mesCuotaRepo.eliminar(mesCuota).observe(this, ok -> {
                        android.util.Log.d("RegistroFinanciero", "Resultado eliminación: " + ok);
                        cerrarDialogoProgreso();
                        if (Boolean.TRUE.equals(ok)) {
                            mostrar("Registro de " + mesCuota + " creado y mes eliminado");
                        } else {
                            mostrar("Registro creado pero no se pudo eliminar el mes");
                        }
                        finish();
                    });
                });
            });
        });
    }

    private void continuarCreacionSinBalances(com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao registro, String mesCuota) {
        registroRepo.crear(registro).observe(this, creado -> {
            if (creado == null) {
                cerrarDialogoProgreso();
                mostrar("Error al crear registro");
                return;
            }
            mesCuotaRepo.eliminar(mesCuota).observe(this, ok -> {
                cerrarDialogoProgreso();
                if (Boolean.TRUE.equals(ok)) {
                    mostrar("Registro creado y mes eliminado");
                } else {
                    mostrar("Registro creado pero no se pudo eliminar el mes");
                }
                finish();
            });
        });
    }

    private interface AccionCompleta { void done(); }

    private void actualizarPropietarioSecuencial(List<com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao> propietarios,
                                                 int idx,
                                                 int total,
                                                 float montoFinal,
                                                 AccionCompleta alTerminar) {
        if (idx >= total) { alTerminar.done(); return; }
        com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao p = propietarios.get(idx);
        actualizarDialogo("Actualizando cuenta: " + (p.nombrePropietario != null ? p.nombrePropietario : "#"+p.idpropietario), (int)((idx * 100f) / Math.max(1,total)));
        Float balanceActual = p.balance != null ? p.balance : 0f;
        float nuevoBalance = balanceActual - montoFinal;
        p.balance = nuevoBalance;
        propietarioRepo.actualizar(p.idpropietario, p).observe(this, resp -> {
            if (resp == null) {
                // Continuar aunque falle uno
            }
            if (nuevoBalance < 0) {
                propietarioRepo.actualizarEstado(p.idpropietario).observe(this, ok -> {
                    actualizarPropietarioSecuencial(propietarios, idx + 1, total, montoFinal, alTerminar);
                });
            } else {
                actualizarPropietarioSecuencial(propietarios, idx + 1, total, montoFinal, alTerminar);
            }
        });
    }

    // ACTUALIZACIÓN: recalcular diferencia, ajustar balances y actualizar registro
    private void ejecutarProcesoActualizacion(float nuevoMontoFinal, com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao registroActualizado) {
        mostrarDialogoProgreso("Actualizando Registro", "Recalculando balances...");
        float montoAnterior = registroOriginal != null && registroOriginal.montoPagar != null ? registroOriginal.montoPagar : 0f;
        final float diferencia = nuevoMontoFinal - montoAnterior;
        if (diferencia == 0f) {
            actualizarDialogo("Actualizando base de datos...", 80);
            registroRepo.actualizar(registroId, registroActualizado).observe(this, resp -> {
                cerrarDialogoProgreso();
                if (resp != null) {
                    mostrar("Registro actualizado");
                    finish();
                } else {
                    mostrar("No se pudo actualizar");
                }
            });
            return;
        }
        propietarioRepo.listar(null, null).observe(this, propietarios -> {
            if (propietarios == null) {
                cerrarDialogoProgreso();
                mostrar("No se pudo cargar propietarios");
                return;
            }
            final int total = propietarios.size();
            ajustarPropietarioSecuencial(propietarios, 0, total, diferencia, () -> {
                actualizarDialogo("Actualizando base de datos...", 90);
                registroRepo.actualizar(registroId, registroActualizado).observe(this, resp -> {
                    cerrarDialogoProgreso();
                    if (resp != null) {
                        mostrar("Registro actualizado");
                        finish();
                    } else {
                        mostrar("No se pudo actualizar");
                    }
                });
            });
        });
    }

    private void ajustarPropietarioSecuencial(List<com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao> propietarios,
                                               int idx,
                                               int total,
                                               float diferencia,
                                               AccionCompleta alTerminar) {
        if (idx >= total) { alTerminar.done(); return; }
        com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao p = propietarios.get(idx);
        actualizarDialogo("Ajustando cuenta: " + (p.nombrePropietario != null ? p.nombrePropietario : "#"+p.idpropietario), (int)((idx * 100f) / Math.max(1,total)));
        Float balanceActual = p.balance != null ? p.balance : 0f;
        float nuevoBalance = balanceActual - diferencia; // si sube la cuota, balance baja
        p.balance = nuevoBalance;
        propietarioRepo.actualizar(p.idpropietario, p).observe(this, resp -> {
            if (resp == null) {
                // seguir
            }
            if (nuevoBalance < 0) {
                propietarioRepo.actualizarEstado(p.idpropietario).observe(this, ok -> {
                    ajustarPropietarioSecuencial(propietarios, idx + 1, total, diferencia, alTerminar);
                });
            } else {
                ajustarPropietarioSecuencial(propietarios, idx + 1, total, diferencia, alTerminar);
            }
        });
    }

    private String construirDescripcion() {
        List<String> partes = new ArrayList<>();
        if (binding.cbAgua.isChecked()) partes.add("Se pago el agua");
        if (binding.cbLuz.isChecked()) partes.add("Se pago la luz");
        if (binding.cbMantenimiento.isChecked()) partes.add("Se pago mantenimiento");
        if (binding.cbOtros.isChecked()) {
            String otros = binding.etOtros.getText() != null ? binding.etOtros.getText().toString().trim() : "";
            if (!otros.isEmpty()) partes.add(otros);
        }
        return TextUtils.join(", ", partes);
    }

    private void lanzarPickerImagenes() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pickImagesLauncher.launch(intent);
    }

    private void agregarImagenDesdeUri(Uri uri) {
        int slot = siguienteSlotLibre();
        if (slot == -1) {
            mostrar("Máximo 4 imágenes");
            return;
        }
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int n;
            while ((n = is.read(data)) != -1) {
                buffer.write(data, 0, n);
            }
            byte[] bytes = buffer.toByteArray();
            imagenesBytes[slot] = bytes;
            actualizarPreview(slot, bytes);
        } catch (Exception e) {
            mostrar("No se pudo cargar la imagen");
        }
    }

    private void actualizarPreview(int slot, byte[] bytes) {
        if (bytes == null || bytes.length == 0) return;
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if (bmp == null) return;
        switch (slot) {
            case 0:
                binding.img1.setImageBitmap(bmp);
                binding.btnRemove1.setVisibility(android.view.View.VISIBLE);
                break;
            case 1:
                binding.img2.setImageBitmap(bmp);
                binding.btnRemove2.setVisibility(android.view.View.VISIBLE);
                break;
            case 2:
                binding.img3.setImageBitmap(bmp);
                binding.btnRemove3.setVisibility(android.view.View.VISIBLE);
                break;
            case 3:
                binding.img4.setImageBitmap(bmp);
                binding.btnRemove4.setVisibility(android.view.View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void limpiarSlot(int slot) {
        imagenesBytes[slot] = null;
        switch (slot) {
            case 0:
                binding.img1.setImageDrawable(null);
                binding.btnRemove1.setVisibility(android.view.View.GONE);
                break;
            case 1:
                binding.img2.setImageDrawable(null);
                binding.btnRemove2.setVisibility(android.view.View.GONE);
                break;
            case 2:
                binding.img3.setImageDrawable(null);
                binding.btnRemove3.setVisibility(android.view.View.GONE);
                break;
            case 3:
                binding.img4.setImageDrawable(null);
                binding.btnRemove4.setVisibility(android.view.View.GONE);
                break;
            default:
                break;
        }
    }

    private int siguienteSlotLibre() {
        for (int i = 0; i < imagenesBytes.length; i++) {
            if (imagenesBytes[i] == null) return i;
        }
        return -1;
    }

    private String encodeSlot(int slot) {
        if (slot < 0 || slot >= imagenesBytes.length) return null;
        byte[] b = imagenesBytes[slot];
        if (b == null || b.length == 0) return null;
        return Base64.encodeToString(b, Base64.NO_WRAP);
    }

    private String capitalizarMes(String mes) {
        if (TextUtils.isEmpty(mes)) return mes;
        mes = mes.toLowerCase(Locale.ROOT);
        if (!mes.contains("-")) return mes.trim();
        String[] partes = mes.split("-");
        if (partes.length != 2) return mes.trim();
        String nombre = partes[0].trim();
        String anio = partes[1].trim();
        if (nombre.isEmpty()) return mes.trim();
        String capitalizado = nombre.substring(0,1).toUpperCase(Locale.ROOT) + nombre.substring(1);
        return capitalizado + "-" + anio;
    }

    private void mostrar(String mensaje) {
        Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
    }

    private void cargarRegistroDesdeRepositorio(long id) {
        registroRepo.obtener(id).observe(this, registro -> {
            if (registro == null) {
                mostrar("No se pudo cargar el registro");
                finish();
                return;
            }
            // Guardar referencia del original para cálculos de actualización
            registroOriginal = registro;

            // Cargar datos del formulario
            if (!TextUtils.isEmpty(registro.mesCuota)) {
                binding.etMesCuota.setText(registro.mesCuota);
            }
            if (registro.cuotaMensual != null && registro.cuotaMensual > 0) {
                binding.etMontoCuota.setText(String.valueOf(registro.cuotaMensual));
            }
            if (registro.montoPagar != null && registro.montoPagar > 0) {
                binding.etMontoFinal.setText(String.valueOf(registro.montoPagar));
            }

            // Cargar descripción y checkboxes
            if (!TextUtils.isEmpty(registro.descripcion)) {
                String descripcion = registro.descripcion.toLowerCase();
                if (descripcion.contains("agua")) binding.cbAgua.setChecked(true);
                if (descripcion.contains("luz")) binding.cbLuz.setChecked(true);
                if (descripcion.contains("mantenimiento")) binding.cbMantenimiento.setChecked(true);
                if (!descripcion.contains("agua") && !descripcion.contains("luz") && !descripcion.contains("mantenimiento")) {
                    binding.cbOtros.setChecked(true);
                    binding.etOtros.setEnabled(true);
                    binding.etOtros.setText(registro.descripcion);
                }
            }

            // Cargar imágenes
            String[] imagenes = {registro.img1, registro.img2, registro.img3, registro.img4};
            for (int i = 0; i < imagenes.length; i++) {
                if (!TextUtils.isEmpty(imagenes[i])) {
                    try {
                        byte[] bytes = Base64.decode(imagenes[i], Base64.DEFAULT);
                        imagenesBytes[i] = bytes;
                        actualizarPreview(i, bytes);
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }
}
