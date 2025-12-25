package com.jrdev.systemmanager.controllers.splashscreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.jrdev.systemmanager.R;
import com.jrdev.systemmanager.databinding.ActivitySplashScreenBinding;
import com.jrdev.systemmanager.utilities.Constantes;

public class SplashScreenActivity extends AppCompatActivity {

    private ActivitySplashScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar el layout con ViewBinding
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ocultar la ActionBar (supportActionBar!!.hide())
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Poner pantalla completa (window.setFlags...)
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Cargar imagen con Glide
        Glide.with(this)
                .load(R.drawable.systemmanager_sinfondo) // Asegúrate de tener esta imagen en 'res/drawable'
                .centerCrop()
                .into(binding.ivSplashScreen);

        // Llamar a la función para cambiar de pantalla
        cambiarPantalla();
    }

    private void cambiarPantalla() {
        // Handler y Looper para el retraso (postDelayed)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Crear el Intent para ir a MainActivity
                Intent intent = new Intent(SplashScreenActivity.this, com.jrdev.systemmanager.controllers.main.MainActivity.class);
                startActivity(intent);
                finish(); // Cerrar el Splash para que no se pueda volver atrás
            }
        }, Constantes.DURACION_SPLASH_SCREEN);
    }
}