package com.example.appstopsmoke.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.appstopsmoke.R;
import com.example.appstopsmoke.data.datasource.FirebaseDataSource;
import com.example.appstopsmoke.data.model.Smoke;
import com.example.appstopsmoke.data.repository.SmokeRepository;
import com.example.appstopsmoke.viewmodel.StatsViewModel;
import com.example.appstopsmoke.viewmodel.ViewModelFactory;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {
    private StatsViewModel viewModel;
    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        chart = findViewById(R.id.chart);

        // Crear instancias manualmente
        FirebaseDataSource dataSource = new FirebaseDataSource();
        SmokeRepository repository = new SmokeRepository(dataSource);
        ViewModelFactory factory = new ViewModelFactory(repository);

        viewModel = new ViewModelProvider(this, factory).get(StatsViewModel.class);

        viewModel.getSmokesData().observe(this, smokes -> {
            if (smokes != null && !smokes.isEmpty()) {
                updateChart(smokes);
            }
        });
    }

    private void updateChart(List<Smoke> smokes) {
        // Agrupar cigarros por día
        Map<String, Integer> smokesPerDay = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Smoke smoke : smokes) {
            String date = sdf.format(new Date(smoke.getTimestamp()));
            smokesPerDay.put(date, smokesPerDay.getOrDefault(date, 0) + 1);
        }

        // Crear datos para el gráfico
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>(smokesPerDay.keySet());
        Collections.sort(dates);

        for (int i = 0; i < dates.size(); i++) {
            entries.add(new Entry(i, smokesPerDay.get(dates.get(i))));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Cigarros por día");
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refrescar
    }
}
