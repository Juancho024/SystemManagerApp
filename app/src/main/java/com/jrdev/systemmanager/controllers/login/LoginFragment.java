package com.jrdev.systemmanager.controllers.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.jrdev.systemmanager.BuildConfig;
import com.jrdev.systemmanager.DataBaseConnection.dao.UsuarioDao;
import com.jrdev.systemmanager.DataBaseConnection.repository.UsuarioRepository;
import com.jrdev.systemmanager.R;
import com.jrdev.systemmanager.utilities.SessionManager;

public class LoginFragment extends Fragment {

    private TextInputEditText txtUser;
    private TextInputEditText txtPassword;
    private CheckBox chkGuardar;
    private MaterialButton btnIniciar;

    private UsuarioRepository repository;
    private SessionManager sessionManager;
    private boolean autoLoginEnProgreso = false;

    // Usuario logueado (estático para acceso desde otras pantallas)
    public static UsuarioDao usuarioLogueado;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar componentes de UI
        txtUser = view.findViewById(R.id.txtUser);
        txtPassword = view.findViewById(R.id.txtPassword);
        chkGuardar = view.findViewById(R.id.chkGuardar);
        btnIniciar = view.findViewById(R.id.btnIniciar);

        // Inicializar Repository y SessionManager
        repository = new UsuarioRepository(BuildConfig.API_URL);
        sessionManager = new SessionManager(requireContext());

        // Cargar datos guardados si existen sesión anterior
        cargarSesionGuardada();

        // Listener del botón
        btnIniciar.setOnClickListener(v -> iniciarSesion());
    }

    // cargar las sesion guardada en el cell
    private void cargarSesionGuardada() {
        SessionManager.SesionData sesion = sessionManager.obtenerSesion();
        if (sesion != null) {
            txtUser.setText(sesion.username);
            txtPassword.setText(sesion.password);
            chkGuardar.setChecked(true);

            // Auto-login con la sesión guardada
            if (!autoLoginEnProgreso) {
                autoLoginEnProgreso = true;
                iniciarSesionConCredenciales(sesion.username, sesion.password, true);
            }
        }
    }

   // iniciar sesion desde api
    private void iniciarSesion() {
        String usuario = txtUser.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        iniciarSesionConCredenciales(usuario, password, false);
    }

    private void iniciarSesionConCredenciales(String usuario, String password, boolean esAuto) {
        if (usuario.isEmpty() || password.isEmpty()) {
            if (!esAuto) {
                showMessage("Por favor, complete el nombre de usuario y contraseña.");
            }
            autoLoginEnProgreso = false;
            return;
        }

        btnIniciar.setEnabled(false);
        btnIniciar.setText(esAuto ? "Restaurando sesión..." : "Autenticando...");

        android.util.Log.d("LoginFragment", (esAuto ? "Auto-login" : "Login manual") + " con usuario: " + usuario);

        repository.login(usuario, password).observe(getViewLifecycleOwner(), usuarioAutenticado -> {
            btnIniciar.setEnabled(true);
            btnIniciar.setText("Iniciar Sesión");
            autoLoginEnProgreso = false;

            if (usuarioAutenticado != null) {
                usuarioLogueado = usuarioAutenticado;

                // Guardar ID del usuario en sesión segura
                sessionManager.guardarIdUsuario(usuarioAutenticado.getIdusuario());
                android.util.Log.d("LoginFragment", "ID del usuario guardado en sesión: " + usuarioAutenticado.getIdusuario());

                if (chkGuardar.isChecked() || esAuto) {
                    sessionManager.guardarSesion(usuario, password);
                    chkGuardar.setChecked(true);
                } else {
                    sessionManager.eliminarSesion();
                }

                showMessage("Bienvenido " + usuarioAutenticado.getNombreUsuario());
                navToPrincipal();
            } else {
                android.util.Log.e("LoginFragment", "Login fallido para usuario: " + usuario);
                if (!esAuto) {
                    showMessage("El usuario o contraseña son incorrectos. Por favor, intente de nuevo.");
                } else {
                    showMessage("No se pudo restaurar la sesión guardada. Inicie sesión nuevamente.");
                }
            }
        });
    }

    // Entre a el menu principal
    private void navToPrincipal() {
        NavController navController = Navigation.findNavController(requireView());
        // Reemplaza 'action_loginFragment_to_principalFragment' con la acción correcta de tu nav graph
        // Si no existe esa acción, debes crearla en navigation/nav_graph.xml
        navController.navigate(R.id.action_loginFragment_to_principalFragment);
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