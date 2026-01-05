package com.jrdev.systemmanager.controllers.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class EditarPerfilFragment extends Fragment {

    private TextInputEditText txtNombre;
    private TextInputEditText txtUsuario;
    private ImageView imgAvatarEdit;
    private MaterialButton btnGuardarPerfil;
    private MaterialButton btnCancelarPerfil;

    private UsuarioRepository repository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editar_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar componentes de UI
        txtNombre = view.findViewById(R.id.txtNombre);
        txtUsuario = view.findViewById(R.id.txtUsuario);
        imgAvatarEdit = view.findViewById(R.id.imgAvatarEdit);
        btnGuardarPerfil = view.findViewById(R.id.btnGuardarPerfil);
        btnCancelarPerfil = view.findViewById(R.id.btnCancelarPerfil);

        // Inicializar Repository
        repository = new UsuarioRepository(BuildConfig.API_URL);

        // Cargar datos del usuario actual
        cargarDatosUsuario();

        // Listeners de botones
        btnGuardarPerfil.setOnClickListener(v -> guardarCambios());
        btnCancelarPerfil.setOnClickListener(v -> volver());
    }

    // Se carga los datos para modificar el perfil
    private void cargarDatosUsuario() {
        if (LoginFragment.usuarioLogueado != null) {
            UsuarioDao usuario = LoginFragment.usuarioLogueado;
            txtNombre.setText(usuario.getNombreUsuario());
            txtUsuario.setText(usuario.getUsuario());
        } else {
            showMessage("No hay usuario logueado.");
        }
    }

    // Se guarda los cambios del perfil
    private void guardarCambios() {
        // Obtener valores de los campos
        String nombreNuevo = txtNombre.getText().toString().trim();
        String usuarioNuevo = txtUsuario.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (nombreNuevo.isEmpty() || usuarioNuevo.isEmpty()) {
            showMessage("Por favor, complete todos los campos.");
            return;
        }

        if (LoginFragment.usuarioLogueado == null) {
            showMessage("No hay usuario logueado.");
            return;
        }

        // Mostrar indicador de carga
        btnGuardarPerfil.setEnabled(false);
        btnGuardarPerfil.setText("Guardando...");

        // Crear objeto actualizado
        UsuarioDao usuarioActualizado = new UsuarioDao();
        usuarioActualizado.setIdusuario(LoginFragment.usuarioLogueado.getIdusuario());
        usuarioActualizado.setNombreUsuario(nombreNuevo);
        usuarioActualizado.setUsuario(usuarioNuevo);
        usuarioActualizado.setNumApto(LoginFragment.usuarioLogueado.getNumApto());
        usuarioActualizado.setTipoUsuario(LoginFragment.usuarioLogueado.getTipoUsuario());
        usuarioActualizado.setPasswordUsuario(LoginFragment.usuarioLogueado.getPasswordUsuario());

        Long id = LoginFragment.usuarioLogueado.getIdusuario();
        if (id == null) {
            // Fallback: recuperar ID por usuario actual antes de actualizar
            btnGuardarPerfil.setText("Buscando usuario...");
            repository.listar(LoginFragment.usuarioLogueado.getUsuario())
                .observe(getViewLifecycleOwner(), lista -> {
                    if (lista != null && !lista.isEmpty()) {
                        for (UsuarioDao u : lista) {
                            if (u.getUsuario() != null && u.getUsuario().equalsIgnoreCase(LoginFragment.usuarioLogueado.getUsuario())) {
                                LoginFragment.usuarioLogueado.setIdusuario(u.getIdusuario());
                                usuarioActualizado.setIdusuario(u.getIdusuario());
                                break;
                            }
                        }
                    }

                    btnGuardarPerfil.setText("Guardar Cambios");
                    Long resolvedId = LoginFragment.usuarioLogueado.getIdusuario();
                    if (resolvedId == null) {
                        btnGuardarPerfil.setEnabled(true);
                        showMessage("No se pudo obtener el ID del usuario. Inicie sesión de nuevo.");
                    } else {
                        ejecutarActualizacion(resolvedId, usuarioActualizado);
                    }
                });
        } else {
            ejecutarActualizacion(id, usuarioActualizado);
        }
    }

    private void ejecutarActualizacion(Long id, UsuarioDao usuarioActualizado) {
        repository.actualizar(id, usuarioActualizado)
            .observe(getViewLifecycleOwner(), resultado -> {
                btnGuardarPerfil.setEnabled(true);
                btnGuardarPerfil.setText("Guardar Cambios");

                if (resultado != null) {
                    LoginFragment.usuarioLogueado = resultado;
                    showMessage("Perfil actualizado correctamente.");
                    btnGuardarPerfil.postDelayed(this::volver, 500);
                } else {
                    showMessage("Error al actualizar el perfil. Intente de nuevo.");
                }
            });
    }

    private void volver() {
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack();
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