package com.example.appstopsmoke.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.appstopsmoke.R;
import com.example.appstopsmoke.data.datasource.FirebaseDataSource;
import com.example.appstopsmoke.data.repository.SmokeRepository;
import com.example.appstopsmoke.viewmodel.CompareViewModel;
import com.example.appstopsmoke.viewmodel.ViewModelFactory;

public class CompareActivity extends AppCompatActivity {
    private CompareViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        FirebaseDataSource dataSource = new FirebaseDataSource();
        SmokeRepository repository = new SmokeRepository(dataSource);
        ViewModelFactory factory = new ViewModelFactory(repository);

        viewModel = new ViewModelProvider(this, factory).get(CompareViewModel.class);

        EditText etUserId = findViewById(R.id.etUserId);
        Button btnCompare = findViewById(R.id.btnCompare);
        TextView tvResult = findViewById(R.id.tvResult);

        btnCompare.setOnClickListener(v -> {
            String userId = etUserId.getText().toString().trim();
            if (!userId.isEmpty()) {
                viewModel.loadOtherUserData(userId);
            } else {
                Toast.makeText(this, "Ingresa un ID válido", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getOtherUserData().observe(this, smokes -> {
            if (smokes != null) {
                tvResult.setText("Usuario ha fumado: " + smokes.size() + " cigarros");
            } else {
                tvResult.setText("Error al cargar datos del usuario");
            }
        });
    }
}
