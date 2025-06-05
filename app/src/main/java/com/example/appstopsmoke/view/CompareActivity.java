package com.example.appstopsmoke.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appstopsmoke.R;
import com.example.appstopsmoke.data.datasource.FirebaseDataSource;
import com.example.appstopsmoke.data.repository.SmokeRepository;
import com.example.appstopsmoke.viewmodel.CompareViewModel;
import com.example.appstopsmoke.viewmodel.ViewModelFactory;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CompareActivity extends AppCompatActivity {
    private CompareViewModel viewModel;
    private LineChart chart;
    private TextView tvCurrentStats, tvOtherStats;
    private ContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        FirebaseDataSource dataSource = new FirebaseDataSource();
        SmokeRepository repository = new SmokeRepository(dataSource, getApplicationContext());
        ViewModelFactory factory = new ViewModelFactory(repository);

        viewModel = new ViewModelProvider(this, factory).get(CompareViewModel.class);

        EditText etUserId = findViewById(R.id.etUserId);
        Button btnCompare = findViewById(R.id.btnCompare);
        chart = findViewById(R.id.chart);
        tvCurrentStats = findViewById(R.id.tvCurrentStats);
        tvOtherStats = findViewById(R.id.tvOtherStats);

        // Configurar RecyclerView para contactos
        RecyclerView rvContacts = findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        contactsAdapter = new ContactsAdapter();
        rvContacts.setAdapter(contactsAdapter);

        btnCompare.setOnClickListener(v -> {
            String userId = etUserId.getText().toString().trim();
            if (!userId.isEmpty()) {
                viewModel.loadComparisonData(userId);
                showAddContactDialog(userId);
            } else {
                Toast.makeText(this, "Ingresa un ID válido", Toast.LENGTH_SHORT).show();
            }
        });

        // Observadores
        viewModel.getCurrentUserData().observe(this, smokes -> {
            if (smokes != null && !smokes.isEmpty()) {
                updateStats();
                updateChart();
            }
        });

        viewModel.getOtherUserData().observe(this, smokes -> {
            if (smokes != null && !smokes.isEmpty()) {
                updateStats();
                updateChart();
            } else if (smokes == null) {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSavedContacts().observe(this, contacts -> {
            contactsAdapter.setContacts(contacts);
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                // Mostrar progress bar
            } else {
                // Ocultar progress bar
            }
        });

        viewModel.getLoadingError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar gráfica
        setupChart();
    }

    private void showAddContactDialog(String userId) {
        new AlertDialog.Builder(this)
                .setTitle("Guardar contacto")
                .setMessage("¿Deseas guardar este ID de usuario en tus contactos?")
                .setPositiveButton("Guardar", (dialog, which) -> {
                    showNameInputDialog(userId);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showNameInputDialog(String userId) {
        final EditText input = new EditText(this);
        input.setHint("Nombre del contacto");

        new AlertDialog.Builder(this)
                .setTitle("Nombre del contacto")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String contactName = input.getText().toString().trim();
                    if (!contactName.isEmpty()) {
                        viewModel.addContact(userId, contactName);
                    } else {
                        Toast.makeText(this, "Debes ingresar un nombre", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateStats() {
        String currentStats = viewModel.getUserStats(
                viewModel.getCurrentUserData().getValue(),
                "Tus estadísticas"
        );

        String otherStats = viewModel.getUserStats(
                viewModel.getOtherUserData().getValue(),
                "Estadísticas del otro usuario"
        );

        if (!currentStats.isEmpty()) tvCurrentStats.setText(currentStats);
        if (!otherStats.isEmpty()) tvOtherStats.setText(otherStats);
    }

    private void setupChart() {
        // Configuración básica del gráfico
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        // Configurar leyenda
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        // Configurar eje X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(5);

        // Configurar eje Y
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setDrawGridLines(true);

        chart.getAxisRight().setEnabled(false);
    }

    private void updateChart() {
        CompareViewModel.ChartData chartData = viewModel.prepareChartData();
        if (chartData == null) return;

        // Crear datasets
        List<Entry> currentEntries = new ArrayList<>();
        List<Entry> otherEntries = new ArrayList<>();

        for (CompareViewModel.ChartEntry entry : chartData.currentUserEntries) {
            currentEntries.add(new Entry(entry.index, entry.value));
        }

        for (CompareViewModel.ChartEntry entry : chartData.otherUserEntries) {
            otherEntries.add(new Entry(entry.index, entry.value));
        }

        LineDataSet currentDataSet = new LineDataSet(currentEntries, "Tú");
        currentDataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        currentDataSet.setCircleColor(ColorTemplate.MATERIAL_COLORS[0]);
        currentDataSet.setLineWidth(2f);
        currentDataSet.setCircleRadius(4f);
        currentDataSet.setValueTextSize(10f);
        currentDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.valueOf((int) entry.getY());
            }
        });

        LineDataSet otherDataSet = new LineDataSet(otherEntries, "Otro usuario");
        otherDataSet.setColor(ColorTemplate.MATERIAL_COLORS[1]);
        otherDataSet.setCircleColor(ColorTemplate.MATERIAL_COLORS[1]);
        otherDataSet.setLineWidth(2f);
        otherDataSet.setCircleRadius(4f);
        otherDataSet.setValueTextSize(10f);
        otherDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.valueOf((int) entry.getY());
            }
        });

        LineData lineData = new LineData(currentDataSet, otherDataSet);
        chart.setData(lineData);

        // Configurar etiquetas del eje X
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < chartData.dates.size()) {
                    return chartData.dates.get(index);
                }
                return "";
            }
        });

        chart.invalidate(); // refrescar
    }

    // Adapter para la lista de contactos
    private class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
        private List<CompareViewModel.Contact> contacts = new ArrayList<>();

        public void setContacts(Map<String, CompareViewModel.Contact> contactsMap) {
            contacts.clear();
            contacts.addAll(contactsMap.values());
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CompareViewModel.Contact contact = contacts.get(position);
            holder.textView.setText(contact.name);

            holder.itemView.setOnClickListener(v -> {
                // Cargar datos del contacto seleccionado
                viewModel.loadComparisonData(contact.userId);
            });

            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(CompareActivity.this)
                        .setTitle("Eliminar contacto")
                        .setMessage("¿Eliminar " + contact.name + " de tus contactos?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            viewModel.removeContact(contact.userId);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}