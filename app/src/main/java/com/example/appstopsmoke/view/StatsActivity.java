package com.example.appstopsmoke.view;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.appstopsmoke.R;
import com.example.appstopsmoke.data.datasource.FirebaseDataSource;
import com.example.appstopsmoke.data.model.Smoke;
import com.example.appstopsmoke.data.repository.SmokeRepository;
import com.example.appstopsmoke.viewmodel.StatsViewModel;
import com.example.appstopsmoke.viewmodel.ViewModelFactory;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StatsActivity extends AppCompatActivity {
    private StatsViewModel viewModel;
    private LineChart chart;
    private TextView tvTotal, tvAverage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        chart = findViewById(R.id.chart);
        tvTotal = findViewById(R.id.tvTotal);
        tvAverage = findViewById(R.id.tvAverage);

        FirebaseDataSource dataSource = new FirebaseDataSource();
        SmokeRepository repository = new SmokeRepository(dataSource, this);
        ViewModelFactory factory = new ViewModelFactory(repository);

        viewModel = new ViewModelProvider(this, factory).get(StatsViewModel.class);

        viewModel.getSmokesData().observe(this, smokes -> {
            if (smokes != null && !smokes.isEmpty()) {
                updateChart(smokes);
                updateStats(smokes);
            }
        });
    }

    private void updateStats(List<Smoke> smokes) {
        // calcular estadísticas
        int total = smokes.size();

        // calcular días entre el primer y último cigarro
        long first = smokes.get(0).getTimestamp();
        long last = smokes.get(smokes.size()-1).getTimestamp();
        long diffDays = TimeUnit.MILLISECONDS.toDays(last - first) + 1;

        double average = total / (double) diffDays;

        tvTotal.setText(String.valueOf(total));
        tvAverage.setText(String.format(Locale.getDefault(), "%.1f", average));
    }

    private void updateChart(List<Smoke> smokes) {
        // Clase auxiliar para almacenar datos del día
        class DayData {
            int count;
            long timestamp;

            DayData(int count, long timestamp) {
                this.count = count;
                this.timestamp = timestamp;
            }
        }

        // Agrupa cigarros por día usando formato yyyy-MM-dd como clave
        Map<String, DayData> smokesPerDay = new HashMap<>();
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Smoke smoke : smokes) {
            long timestamp = smoke.getTimestamp();
            String dateKey = keyFormat.format(new Date(timestamp));

            DayData existing = smokesPerDay.get(dateKey);
            if (existing == null) {
                smokesPerDay.put(dateKey, new DayData(1, timestamp));
            } else {
                smokesPerDay.put(dateKey, new DayData(existing.count + 1, existing.timestamp));
            }
        }

        // Convierte a lista y ordena cronológicamente por timestamp
        List<Map.Entry<String, DayData>> sortedEntries = new ArrayList<>(smokesPerDay.entrySet());
        Collections.sort(sortedEntries, (entry1, entry2) ->
                Long.compare(entry1.getValue().timestamp, entry2.getValue().timestamp));

        // Crea datos para la gráfica
        List<Entry> entries = new ArrayList<>();
        List<String> displayLabels = new ArrayList<>();
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

        float maxValue = 0;
        for (int i = 0; i < sortedEntries.size(); i++) {
            DayData dayData = sortedEntries.get(i).getValue();
            float value = dayData.count;

            entries.add(new Entry(i, value));
            displayLabels.add(displayFormat.format(new Date(dayData.timestamp)));

            if (value > maxValue) maxValue = value;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Cigarros por día");

        // personaliza la línea
        dataSet.setColor(ContextCompat.getColor(this, R.color.chart_line_color));
        dataSet.setLineWidth(2.5f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // suaviza la línea
        dataSet.setCubicIntensity(0.15f);

        // personaliza los puntos
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.chart_line_color));
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setCircleHoleColor(Color.WHITE);

        // añade relleno debajo de la línea
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.chart_fill_gradient);
            dataSet.setFillDrawable(drawable);
        } else {
            dataSet.setFillColor(ContextCompat.getColor(this, R.color.chart_fill_start));
        }
        dataSet.setDrawFilled(true);

        // personaliza el texto de los valores
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.chart_text_color));
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.valueOf((int) entry.getY());
            }
        });

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // personaliza los ejes
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.chart_text_color));
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(displayLabels)); // Usa las etiquetas cortas
        xAxis.setLabelCount(Math.min(displayLabels.size(), 7)); // Limita las etiquetas mostradas
        xAxis.setGranularity(1f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.chart_text_color));
        leftAxis.setTextSize(10f);
        leftAxis.setGridColor(ContextCompat.getColor(this, R.color.chart_grid_color));
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(maxValue + 2); // añade espacio extra arriba
        leftAxis.setGranularity(1f);
        leftAxis.setLabelCount(6, true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false); // desactiva el eje derecho

        // personaliza la leyenda
        Legend legend = chart.getLegend();
        legend.setTextColor(ContextCompat.getColor(this, R.color.chart_text_color));
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        // configuración general del gráfico
        chart.setDrawMarkers(true);
        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);
        chart.setDoubleTapToZoomEnabled(false);

        // animación
        chart.animateY(1000, Easing.EaseInOutCubic);

        chart.invalidate();
    }
}
