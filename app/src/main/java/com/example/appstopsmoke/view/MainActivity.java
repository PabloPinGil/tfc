package com.example.appstopsmoke.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.appstopsmoke.R;
import com.example.appstopsmoke.data.datasource.FirebaseDataSource;
import com.example.appstopsmoke.data.repository.SmokeRepository;
import com.example.appstopsmoke.viewmodel.MainViewModel;
import com.example.appstopsmoke.viewmodel.ViewModelFactory;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDataSource dataSource = new FirebaseDataSource();
        SmokeRepository repository = new SmokeRepository(dataSource);
        ViewModelFactory factory = new ViewModelFactory(repository);

        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        Button btnSmoke = findViewById(R.id.btnSmoke);
        btnSmoke.setOnClickListener(v -> viewModel.registerSmoke());

        // observar resultados
        viewModel.getSmokeRegistered().observe(this, success -> {
            if (success != null) {
                Toast.makeText(this,
                        success ? "Cigarro registrado!" : "Error al registrar",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // botones de navegación
        Button btnStats = findViewById(R.id.btnStats);
        btnStats.setOnClickListener(v ->
                startActivity(new Intent(this, StatsActivity.class)));

        Button btnCompare = findViewById(R.id.btnCompare);
        btnCompare.setOnClickListener(v ->
                startActivity(new Intent(this, CompareActivity.class)));
    }
}
