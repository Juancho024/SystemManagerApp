package com.jrdev.systemmanager.controllers.contabilidad;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jrdev.systemmanager.BuildConfig;
import com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao;
import com.jrdev.systemmanager.DataBaseConnection.repository.MesCuotaRepository;
import com.jrdev.systemmanager.DataBaseConnection.repository.PropietarioRepository;
import com.jrdev.systemmanager.DataBaseConnection.repository.RegistroFinancieroRepository;
import com.jrdev.systemmanager.R; // Asegúrate que este R sea el correcto
import com.jrdev.systemmanager.databinding.ActivityRegistroFinancieroBinding;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RegistroFinancieroActivity extends AppCompatActivity {

    public static final String EXTRA_IS_EDIT = "extra_is_edit";
    public static final String EXTRA_ID = "extra_id";

    private ActivityRegistroFinancieroBinding binding;
    private RegistroFinancieroRepository registroRepo;
    private MesCuotaRepository mesCuotaRepo;
    private PropietarioRepository propietarioRepo;

    private boolean isEdit = false;
    private long registroId = -1;
    private com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao registroOriginal; // Para cálculos de update

    private final byte[][] imagenesBytes = new byte[4][];
    private ActivityResultLauncher<Intent> pickImagesLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    // Diálogo de Progreso
    private Dialog progresoDialog;
    private TextView dlgTitle;
    private TextView dlgStatus;
    private ProgressBar dlgProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistroFinancieroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Repositorios
        registroRepo = new RegistroFinancieroRepository(BuildConfig.API_URL);
        mesCuotaRepo = new MesCuotaRepository(BuildConfig.API_URL);
        propietarioRepo = new PropietarioRepository(BuildConfig.API_URL);

        // Configuraciones iniciales
        setupMesAutocomplete();
        setupOtrosToggle();
        setupImagePicker();
        setupRemoveButtons();
        leerExtras();

        // Listeners
        binding.btnCancelar.setOnClickListener(v -> finish());
        binding.btnGuardar.setOnClickListener(v -> guardar());
        binding.btnSeleccionarArchivos.setOnClickListener(v -> lanzarPickerImagenes());
    }

    // ============================================================================================
    // LÓGICA PRINCIPAL DE GUARDADO (Entry Point)
    // ============================================================================================
    private void guardar() {
        // Verificar si el usuario es admin antes de permitir crear/actualizar registros financieros
        if (!esAdmin()) {
            mostrar("Debes tener acceso ADMIN para acceder.");
            return;
        }

        // 1. Obtener datos de la UI
        String mes = binding.etMesCuota.getText() != null ? binding.etMesCuota.getText().toString().trim() : "";
        String cuotaTxt = binding.etMontoCuota.getText() != null ? binding.etMontoCuota.getText().toString().trim() : "";
        String montoTxt = binding.etMontoFinal.getText() != null ? binding.etMontoFinal.getText().toString().trim() : "";

        // 2. Validaciones básicas
        if (mes.isEmpty()) { binding.tilMesCuota.setError("Selecciona el mes"); return; }
        binding.tilMesCuota.setError(null);
        if (cuotaTxt.isEmpty()) { mostrar("Ingresa el monto de la cuota"); return; }
        if (montoTxt.isEmpty()) { montoTxt = cuotaTxt; }

        float cuota, monto;
        try {
            cuota = Float.parseFloat(cuotaTxt);
            monto = Float.parseFloat(montoTxt);
        } catch (NumberFormatException e) {
            mostrar("Números inválidos"); return;
        }

        // 3. Construir Objeto
        RegistroFinancieroDao dto = new RegistroFinancieroDao();
        dto.mesCuota = mes;
        dto.cuotaMensual = cuota;
        dto.descripcion = construirDescripcion();
        dto.montoPagar = monto;
        dto.img1 = encodeSlot(0);
        dto.img2 = encodeSlot(1);
        dto.img3 = encodeSlot(2);
        dto.img4 = encodeSlot(3);

        // 4. Decidir Flujo (Update vs Create)
        if (isEdit && registroId > 0) {
            dto.id = registroId; // Mantener ID
            ejecutarProcesoActualizacion(monto, dto);
        } else {
            ejecutarProcesoCreacion(monto, dto, mes);
        }
    }
    // Variable para filtrar en el Logcat
    private static final String TAG = "DEBUG_FINANZAS";

    // ============================================================================================
    // FLUJO 1: CREACIÓN (Recursivo)
    // ============================================================================================
    private void ejecutarProcesoCreacion(float montoFinal, RegistroFinancieroDao registro, String mesCuota) {
        mostrarDialogoProgreso("Procesando Pagos", "Cargando propietarios...");
        android.util.Log.d(TAG, ">>> INICIANDO PROCESO CREACIÓN");
        android.util.Log.d(TAG, "Datos iniciales -> Mes: '" + mesCuota + "', Monto: " + montoFinal);

        propietarioRepo.listar(null, null).observe(this, propietarios -> {
            if (propietarios == null || propietarios.isEmpty()) {
                android.util.Log.e(TAG, "❌ Error: La lista de propietarios llegó nula o vacía.");
                cerrarDialogoProgreso();
                mostrar("Error: No se cargaron propietarios o la lista está vacía.");
                return;
            }

            android.util.Log.d(TAG, "Lista de propietarios cargada. Total: " + propietarios.size());
            // Iniciar cadena recursiva
            procesarPropietarioCreacionRecursivo(propietarios, 0, montoFinal, registro, mesCuota);
        });
    }

    private void procesarPropietarioCreacionRecursivo(List<com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao> lista,
                                                      int index,
                                                      float montoFinal,
                                                      RegistroFinancieroDao registro,
                                                      String mesCuota) {
        // CASO BASE: Terminamos de recorrer la lista
        if (index >= lista.size()) {
            android.util.Log.d(TAG, "✅ Todos los propietarios han sido procesados. Pasando a guardar registro...");
            guardarRegistroYBorrarMes(registro, mesCuota);
            return;
        }

        // PROCESAR UNO
        var p = lista.get(index);

        float balanceActual = p.balance != null ? p.balance : 0f;
        float nuevoBalance = balanceActual - montoFinal;

        android.util.Log.d(TAG, "Procesando [" + index + "] " + p.nombrePropietario +
                " | Balance: " + balanceActual + " -> " + nuevoBalance);

        actualizarDialogo("Actualizando: " + (p.nombrePropietario != null ? p.nombrePropietario : "Propietario"),
                (int)((index * 100f) / lista.size()));

        p.balance = nuevoBalance;
        p.estado = (nuevoBalance >= 0) ? "Verde" : "Rojo";

        // Llamada BD
        propietarioRepo.actualizar(p.idpropietario, p).observe(this, response -> {
            // Log para verificar que la BD respondió
            android.util.Log.d(TAG, "Respuesta update propietario [" + index + "]: " + response);

            // LLAMADA RECURSIVA AL SIGUIENTE
            procesarPropietarioCreacionRecursivo(lista, index + 1, montoFinal, registro, mesCuota);
        });
    }

    private void guardarRegistroYBorrarMes(RegistroFinancieroDao registro, String mesCuota) {
        actualizarDialogo("Guardando registro...", 90);
        android.util.Log.d(TAG, "Intentando guardar registro financiero en API...");

        // 1. Guardar el registro
        registroRepo.crear(registro).observe(this, registroCreado -> {
            if (registroCreado == null) {
                android.util.Log.e(TAG, "❌ Error fatal: La API devolvió null al crear el registro.");
                cerrarDialogoProgreso();
                mostrar("✗ Error: El servidor no guardó el registro.");
                return;
            }

            android.util.Log.d(TAG, "✅ Registro guardado correctamente. Procediendo a borrar mes.");

            // 2. Intentar borrar el mes
            actualizarDialogo("Eliminando mes de la lista...", 95);

            // TRUCO: Aseguramos limpieza total del string
            String mesALimpiar = mesCuota.trim();

            // LOG CRÍTICO: Aquí verás exactamente qué estás enviando
            android.util.Log.d(TAG, ">>> DELETE REQUEST: Intentando borrar mes: '" + mesALimpiar + "'");
            android.util.Log.d(TAG, ">>> Longitud del string: " + mesALimpiar.length());

            mesCuotaRepo.eliminar(mesALimpiar).observe(this, respuesta -> {
                cerrarDialogoProgreso();

                // LOG DE LA RESPUESTA
                android.util.Log.d(TAG, ">>> DELETE RESPONSE: " + respuesta);

                if (respuesta != null) {
                    // Si es un booleano, verifica que sea TRUE
                    if (respuesta instanceof Boolean && !((Boolean) respuesta)) {
                        android.util.Log.w(TAG, "⚠ La API respondió, pero dijo FALSE (No encontró el mes o falló lógica interna).");
                        mostrar("⚠ Registro guardado, pero el mes NO se borró (No coincidió el nombre).");
                    } else {
                        android.util.Log.d(TAG, "🎉 ¡ÉXITO TOTAL! Mes borrado.");
                        mostrar("✓ Proceso completado correctamente.");
                        finish();
                    }
                } else {
                    android.util.Log.e(TAG, "❌ La API devolvió null o falló la conexión en el DELETE.");
                    mostrar("⚠ Registro guardado, pero falló la conexión al borrar el mes.");
                    finish();
                }
            });
        });
    }

    // ============================================================================================
    // FLUJO 2: ACTUALIZACIÓN (Recursivo)
    // ============================================================================================
    private void ejecutarProcesoActualizacion(float nuevoMontoFinal, RegistroFinancieroDao registroActualizado) {
        mostrarDialogoProgreso("Actualizando Registro", "Calculando diferencias...");
        android.util.Log.d(TAG, ">>> INICIANDO PROCESO ACTUALIZACIÓN");

        float montoAnterior = registroOriginal != null && registroOriginal.montoPagar != null ? registroOriginal.montoPagar : 0f;
        float diferencia = nuevoMontoFinal - montoAnterior;

        android.util.Log.d(TAG, "Monto Viejo: " + montoAnterior + " | Monto Nuevo: " + nuevoMontoFinal + " | Diferencia: " + diferencia);

        // Si no hay diferencia monetaria, solo actualizamos el registro padre
        if (diferencia == 0f) {
            android.util.Log.d(TAG, "No hay diferencia de dinero. Saltando actualización de propietarios.");
            actualizarSoloRegistro(registroActualizado);
            return;
        }

        // Si hay diferencia, debemos ajustar los balances de todos
        propietarioRepo.listar(null, null).observe(this, propietarios -> {
            if (propietarios == null) {
                android.util.Log.e(TAG, "❌ Error al cargar propietarios para actualizar.");
                cerrarDialogoProgreso();
                mostrar("Error al cargar propietarios para actualizar.");
                return;
            }
            android.util.Log.d(TAG, "Propietarios cargados para ajuste: " + propietarios.size());
            procesarPropietarioActualizacionRecursivo(propietarios, 0, diferencia, registroActualizado);
        });
    }

    private void procesarPropietarioActualizacionRecursivo(List<com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao> lista,
                                                           int index,
                                                           float diferencia,
                                                           RegistroFinancieroDao registroActualizado) {
        if (index >= lista.size()) {
            android.util.Log.d(TAG, "✅ Ajuste de balances terminado. Actualizando registro principal...");
            actualizarSoloRegistro(registroActualizado);
            return;
        }

        var p = lista.get(index);

        float balanceActual = p.balance != null ? p.balance : 0f;
        float nuevoBalance = balanceActual - diferencia;

        android.util.Log.d(TAG, "Ajustando [" + index + "] " + p.nombrePropietario +
                " | Balance: " + balanceActual + " - (" + diferencia + ") = " + nuevoBalance);

        actualizarDialogo("Ajustando: " + p.nombrePropietario, (int)((index * 100f) / lista.size()));

        p.balance = nuevoBalance;
        p.estado = (nuevoBalance >= 0) ? "Verde" : "Rojo";

        propietarioRepo.actualizar(p.idpropietario, p).observe(this, response -> {
            procesarPropietarioActualizacionRecursivo(lista, index + 1, diferencia, registroActualizado);
        });
    }

    private void actualizarSoloRegistro(RegistroFinancieroDao registro) {
        actualizarDialogo("Guardando cambios en BD...", 100);

        // NOTA: Asegúrate de usar el ID correcto aquí.
        // Si tu objeto usa 'idregistro', cámbialo abajo. Si usa 'id', déjalo así.
        long idParaActualizar = registro.id; // Asumiendo que es idregistro según tus DAOs anteriores

        android.util.Log.d(TAG, "Enviando update de registro ID: " + idParaActualizar);

        registroRepo.actualizar(idParaActualizar, registro).observe(this, resp -> {
            cerrarDialogoProgreso();
            if (resp != null) {
                android.util.Log.d(TAG, "🎉 Actualización completada con éxito.");
                mostrar("✓ Actualización completada");
                finish();
            } else {
                android.util.Log.e(TAG, "❌ Falló el update del registro principal.");
                mostrar("Error al actualizar el registro en la base de datos.");
            }
        });
    }

    // ============================================================================================
    // UTILIDADES UI Y HELPER
    // ============================================================================================

    private void mostrarDialogoProgreso(String titulo, String estadoInicial) {
        if (progresoDialog == null) {
            progresoDialog = new Dialog(this);
            progresoDialog.setCancelable(false);
            View content = LayoutInflater.from(this).inflate(R.layout.dialog_proceso, null);
            progresoDialog.setContentView(content);
            dlgTitle = content.findViewById(R.id.tvTitle);
            dlgStatus = content.findViewById(R.id.tvStatus);
            dlgProgress = content.findViewById(R.id.progressBar);
            
            // Quitar la sombra/scrim oscuro
            Window window = progresoDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
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

    private void mostrar(String mensaje) {
        Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
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

    // ============================================================================================
    // CONFIGURACIÓN INICIAL Y CARGA DE DATOS
    // ============================================================================================

    private void setupOtrosToggle() {
        binding.cbOtros.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.tilOtros.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            binding.etOtros.setEnabled(isChecked);
            if (!isChecked) binding.etOtros.setText("");
        });
    }

    private void setupMesAutocomplete() {
        mesCuotaRepo.listar().observe(this, opciones -> {
            if (opciones != null && !opciones.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, opciones);
                binding.etMesCuota.setAdapter(adapter);
                binding.etMesCuota.setInputType(android.text.InputType.TYPE_NULL);
                binding.etMesCuota.setKeyListener(null);
                binding.etMesCuota.setOnClickListener(v -> binding.etMesCuota.showDropDown());
                binding.tilMesCuota.setEndIconOnClickListener(v -> binding.etMesCuota.showDropDown());
            }
        });
    }

    private void leerExtras() {
        if (getIntent() == null) return;
        isEdit = getIntent().getBooleanExtra(EXTRA_IS_EDIT, false);
        registroId = getIntent().getLongExtra(EXTRA_ID, -1);

        if (isEdit && registroId > 0) {
            cargarRegistroDesdeRepositorio(registroId);
            binding.btnGuardar.setText("Actualizar Registro");
            binding.etMesCuota.setEnabled(false); // No se puede cambiar el mes al editar
            binding.tilMesCuota.setEnabled(false);
        }
    }

    private void cargarRegistroDesdeRepositorio(long id) {
        mostrarDialogoProgreso("Cargando...", "Obteniendo datos...");
        registroRepo.obtener(id).observe(this, registro -> {
            cerrarDialogoProgreso();
            if (registro == null) {
                mostrar("Error al cargar datos.");
                finish();
                return;
            }
            registroOriginal = registro; // GUARDAR REFERENCIA ORIGINAL

            // Llenar campos
            binding.etMesCuota.setText(registro.mesCuota);
            if (registro.cuotaMensual != null) binding.etMontoCuota.setText(String.valueOf(registro.cuotaMensual));
            if (registro.montoPagar != null) binding.etMontoFinal.setText(String.valueOf(registro.montoPagar));

            // Checkboxes
            if (!TextUtils.isEmpty(registro.descripcion)) {
                String desc = registro.descripcion.toLowerCase();
                binding.cbAgua.setChecked(desc.contains("agua"));
                binding.cbLuz.setChecked(desc.contains("luz"));
                binding.cbMantenimiento.setChecked(desc.contains("mantenimiento"));
                // Lógica simple para 'Otros'
                if (!desc.contains("agua") && !desc.contains("luz") && !desc.contains("mantenimiento")) {
                    binding.cbOtros.setChecked(true);
                    binding.etOtros.setText(registro.descripcion);
                }
            }

            // Imágenes
            cargarImagenEnSlot(0, registro.img1);
            cargarImagenEnSlot(1, registro.img2);
            cargarImagenEnSlot(2, registro.img3);
            cargarImagenEnSlot(3, registro.img4);
        });
    }

    private void cargarImagenEnSlot(int slot, String base64) {
        if (!TextUtils.isEmpty(base64)) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                imagenesBytes[slot] = bytes;
                actualizarPreview(slot, bytes);
            } catch (Exception e) { /* Ignorar error de imagen */ }
        }
    }

    // ============================================================================================
    // GESTIÓN DE IMÁGENES
    // ============================================================================================

    private void setupImagePicker() {
        pickImagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                if (data.getClipData() != null) {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        agregarImagenDesdeUri(data.getClipData().getItemAt(i).getUri());
                    }
                } else if (data.getData() != null) {
                    agregarImagenDesdeUri(data.getData());
                }
            }
        });
        
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Bundle extras = result.getData().getExtras();
                if (extras != null) {
                    Bitmap bitmap = (Bitmap) extras.get("data");
                    if (bitmap != null) {
                        byte[] bytes = bitmapToByteArray(bitmap);
                        agregarImagenDesdeBytes(bytes);
                    }
                }
            }
        });
    }

    private void setupRemoveButtons() {
        binding.btnRemove1.setOnClickListener(v -> limpiarSlot(0));
        binding.btnRemove2.setOnClickListener(v -> limpiarSlot(1));
        binding.btnRemove3.setOnClickListener(v -> limpiarSlot(2));
        binding.btnRemove4.setOnClickListener(v -> limpiarSlot(3));
    }

    private void lanzarPickerImagenes() {
        new MaterialAlertDialogBuilder(this, R.style.DialogoRedondoYNegro)
                .setTitle("Seleccionar foto")
                .setItems(new CharSequence[]{"Seleccionar del dispositivo", "Tomar foto"}, (dialog, which) -> {
                    if (which == 0) {
                        // Seleccionar del dispositivo
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        pickImagesLauncher.launch(intent);
                    } else {
                        // Tomar foto
                        tomarFotoConCamara();
                    }
                })
                .show();
    }

    private void tomarFotoConCamara() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
            return;
        }
        
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
        return stream.toByteArray();
    }

    private void agregarImagenDesdeUri(Uri uri) {
        int slot = siguienteSlotLibre();
        if (slot == -1) { mostrar("Máximo 4 imágenes"); return; }
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int n;
            while ((n = is.read(data)) != -1) buffer.write(data, 0, n);
            byte[] bytes = buffer.toByteArray();
            imagenesBytes[slot] = bytes;
            actualizarPreview(slot, bytes);
        } catch (Exception e) { mostrar("Error al cargar imagen"); }
    }

    private void agregarImagenDesdeBytes(byte[] bytes) {
        int slot = siguienteSlotLibre();
        if (slot == -1) { mostrar("Máximo 4 imágenes"); return; }
        imagenesBytes[slot] = bytes;
        actualizarPreview(slot, bytes);
    }

    private void actualizarPreview(int slot, byte[] bytes) {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if (bmp == null) return;
        switch (slot) {
            case 0: binding.img1.setImageBitmap(bmp); binding.btnRemove1.setVisibility(View.VISIBLE); break;
            case 1: binding.img2.setImageBitmap(bmp); binding.btnRemove2.setVisibility(View.VISIBLE); break;
            case 2: binding.img3.setImageBitmap(bmp); binding.btnRemove3.setVisibility(View.VISIBLE); break;
            case 3: binding.img4.setImageBitmap(bmp); binding.btnRemove4.setVisibility(View.VISIBLE); break;
        }
    }

    private void limpiarSlot(int slot) {
        imagenesBytes[slot] = null;
        switch (slot) {
            case 0: binding.img1.setImageDrawable(null); binding.btnRemove1.setVisibility(View.GONE); break;
            case 1: binding.img2.setImageDrawable(null); binding.btnRemove2.setVisibility(View.GONE); break;
            case 2: binding.img3.setImageDrawable(null); binding.btnRemove3.setVisibility(View.GONE); break;
            case 3: binding.img4.setImageDrawable(null); binding.btnRemove4.setVisibility(View.GONE); break;
        }
    }

    private int siguienteSlotLibre() {
        for (int i = 0; i < imagenesBytes.length; i++) if (imagenesBytes[i] == null) return i;
        return -1;
    }

    private String encodeSlot(int slot) {
        if (slot < 0 || slot >= 4 || imagenesBytes[slot] == null) return null;
        return Base64.encodeToString(imagenesBytes[slot], Base64.NO_WRAP);
    }
    //Validar si es admin
    private boolean esAdmin() {
        if (com.jrdev.systemmanager.controllers.login.LoginFragment.usuarioLogueado == null) {
            return false;
        }
        String tipoUsuario = com.jrdev.systemmanager.controllers.login.LoginFragment.usuarioLogueado.getTipoUsuario();
        return tipoUsuario != null && tipoUsuario.equalsIgnoreCase("admin");
    }
}