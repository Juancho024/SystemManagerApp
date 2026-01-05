package com.jrdev.systemmanager.controllers.main;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.jrdev.systemmanager.R;
import com.jrdev.systemmanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflar el layout (igual que en Kotlin, pero con getLayoutInflater())
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Llamamos al método de configuración de navegación
        setupNavegation();
    }

    private void setupNavegation() {
        // 1. Quitar el tinte predeterminado de los iconos (para que se vean con sus colores originales)
        binding.bottomNavigationView.setItemIconTintList(null);

        // 2. Obtener el NavHostFragment del layout
        // En Java es necesario hacer el "Cast" explícito con (NavHostFragment)
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // 3. Conectar el BottomNavigation con el NavController
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);

            // 4. Listener para ocultar/mostrar la barra de navegación según el fragment
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.loginFragment || 
                    destination.getId() == R.id.cambiarPasswordFragment ||
                    destination.getId() == R.id.editarPerfilFragment) {
                    // Ocultar la barra de navegación en estos fragments
                    binding.bottomNavigationView.setVisibility(View.GONE);
                } else {
                    // Mostrar la barra de navegación en los demás fragments
                    binding.bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}