package com.jrdev.systemmanager.controllers.calendario;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.jrdev.systemmanager.R;
import com.jrdev.systemmanager.controllers.login.LoginFragment;
import com.jrdev.systemmanager.utilities.SessionManager;

public class CalendarioFragment extends Fragment {

    private TextView txtNombreUsuario;
    private TextView txtTipoUsuario;
    private TextView btnCambiarUsuario;
    private TextView btnCambiarPassword;
    private MaterialButton btnCerrarSesion;
    private MaterialSwitch switchNotificaciones;
    private TextView btnAyuda;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario);
        txtTipoUsuario = view.findViewById(R.id.txtTipoUsuario);
        btnCambiarUsuario = view.findViewById(R.id.btnCambiarUsuario);
        btnCambiarPassword = view.findViewById(R.id.btnCambiarPassword);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        switchNotificaciones = view.findViewById(R.id.switchNotificaciones);
        btnAyuda = view.findViewById(R.id.btnAyuda);

        // Cargar datos del usuario actual
        cargarDatosUsuario();

        // Configurar listeners
        btnCambiarUsuario.setOnClickListener(v -> navTo(R.id.action_page_calendario_to_editarPerfilFragment));
        btnCambiarPassword.setOnClickListener(v -> navTo(R.id.action_page_calendario_to_cambiarPasswordFragment));
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        btnAyuda.setOnClickListener(v -> mostrarAyuda());
    }

    // Muestra el tipo de usuario y el nombre en el primer materialcard
    private void cargarDatosUsuario() {
        if (LoginFragment.usuarioLogueado != null) {
            txtNombreUsuario.setText(LoginFragment.usuarioLogueado.getNombreUsuario());
            txtTipoUsuario.setText(LoginFragment.usuarioLogueado.getTipoUsuario());
        }
    }

    private void navTo(int actionId) {
        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(actionId);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al navegar", Toast.LENGTH_SHORT).show();
        }
    }

    // boton cerrar sesion y borra la informacion guardada internamente
    private void cerrarSesion() {
        // Limpiar datos de usuario
        LoginFragment.usuarioLogueado = null;

        // Limpiar sesión guardada
        SessionManager sessionManager = new SessionManager(requireContext());
        sessionManager.cerrarSesionCompleta();

        // Navegar a login
        try {
            NavController navController = Navigation.findNavController(requireView());
            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav, true)
                    .build();
            navController.navigate(R.id.loginFragment, null, opts);
            Toast.makeText(getContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al cerrar sesión", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarAyuda() {
        Toast.makeText(getContext(), "Ayuda y Soporte\n\nVersión: 3.0\nPara más información, contacte al administrador.", Toast.LENGTH_LONG).show();
    }
}