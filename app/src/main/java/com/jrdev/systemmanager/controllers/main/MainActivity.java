package com.jrdev.systemmanager.controllers.main;

import android.os.Bundle;

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
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
        }
    }
}