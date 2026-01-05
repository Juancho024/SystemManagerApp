package com.jrdev.systemmanager.controllers.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.jrdev.systemmanager.BuildConfig;
import com.jrdev.systemmanager.DataBaseConnection.repository.UsuarioRepository;
import com.jrdev.systemmanager.R;
import com.jrdev.systemmanager.utilities.SessionManager;

public class CambiarPasswordFragment extends Fragment {

    private TextInputEditText txtPassActual;
    private TextInputEditText txtPassNueva;
    private TextInputEditText txtPassConfirmar;
    private MaterialButton btnActualizarPass;

    private UsuarioRepository repository;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cambiar_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar componentes de UI
        txtPassActual = view.findViewById(R.id.txtPassActual);
        txtPassNueva = view.findViewById(R.id.txtPassNueva);
        txtPassConfirmar = view.findViewById(R.id.txtPassConfirmar);
        btnActualizarPass = view.findViewById(R.id.btnActualizarPass);

        // Inicializar Repository
        repository = new UsuarioRepository(BuildConfig.API_URL);
        sessionManager = new SessionManager(requireContext());

        // Listener del botón
        btnActualizarPass.setOnClickListener(v -> cambiarPassword());
    }

    private void cambiarPassword() {
        // Obtener valores de los campos
        String passActual = txtPassActual.getText().toString().trim();
        String passNueva = txtPassNueva.getText().toString().trim();
        String passConfirmar = txtPassConfirmar.getText().toString().trim();

        // Logging para debug
        android.util.Log.d("CambiarPassword", "passActual length: " + passActual.length() + " | passCodigos: " + getCharCodes(passActual));
        android.util.Log.d("CambiarPassword", "passNueva length: " + passNueva.length() + " | passCodigos: " + getCharCodes(passNueva));

        // Validar que los campos no estén vacíos
        if (passActual.isEmpty() || passNueva.isEmpty() || passConfirmar.isEmpty()) {
            showMessage("Por favor, complete todos los campos.");
            return;
        }

        // Validar que la nueva contraseña tenga al menos 4 caracteres
        if (passNueva.length() < 4) {
            showMessage("La nueva contraseña debe tener al menos 4 caracteres.");
            return;
        }

        // Validar que las contraseñas nuevas coincidan
        if (!passNueva.equals(passConfirmar)) {
            showMessage("Las contraseñas nuevas no coinciden.");
            return;
        }

        // Mostrar indicador de carga
        btnActualizarPass.setEnabled(false);
        btnActualizarPass.setText("Actualizando...");

        // Obtener ID del usuario logueado desde SessionManager
        Long idUsuario = sessionManager.obtenerIdUsuario();
        android.util.Log.d("CambiarPassword", "Cambiando contraseña para usuario con ID: " + idUsuario);
        if (idUsuario == null || idUsuario == -1L) {
            showMessage("Error de sesión: vuelva a iniciar sesión.");
            btnActualizarPass.setEnabled(true);
            btnActualizarPass.setText("Actualizar Contraseña");
            return;
        }

        // Llamar al repository para cambiar contraseña
        repository.cambiarPassword(idUsuario, passActual, passNueva).observe(getViewLifecycleOwner(), resultado -> {
            btnActualizarPass.setEnabled(true);
            btnActualizarPass.setText("Actualizar Contraseña");

            if (resultado != null && resultado) {
                // Cambio exitoso
                showMessage("Contraseña actualizada correctamente.");
                // Limpiar campos
                txtPassActual.setText("");
                txtPassNueva.setText("");
                txtPassConfirmar.setText("");
            } else {
                // Error en el cambio (probablemente contraseña actual incorrecta)
                showMessage("La contraseña actual es incorrecta o ocurrió un error. Intente de nuevo.");
            }
        });
    }

    private String getCharCodes(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            sb.append((int) c).append(",");
        }
        return sb.toString();
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