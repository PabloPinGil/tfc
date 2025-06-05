package com.example.appstopsmoke.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appstopsmoke.data.model.Smoke;
import com.example.appstopsmoke.data.repository.SmokeRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CompareViewModel extends ViewModel {
    private final SmokeRepository repository;
    private final MutableLiveData<List<Smoke>> currentUserData = new MutableLiveData<>();
    private final MutableLiveData<List<Smoke>> otherUserData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Contact>> savedContacts = new MutableLiveData<>();
    private final MutableLiveData<String> loadingError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public CompareViewModel(SmokeRepository repository) {
        this.repository = repository;
        loadSavedContacts();
    }

    public void loadComparisonData(String otherUserId) {
        isLoading.postValue(true);
        String currentUserId = repository.getCurrentUserId();

        if (currentUserId != null && otherUserId != null) {
            // Cargar datos del usuario actual
            repository.getUserSmokes(currentUserId, currentSmokes -> {
                if (currentSmokes != null && !currentSmokes.isEmpty()) {
                    currentUserData.postValue(currentSmokes);
                }

                // Cargar datos del otro usuario
                repository.getUserSmokes(otherUserId, otherSmokes -> {
                    isLoading.postValue(false);
                    if (otherSmokes != null && !otherSmokes.isEmpty()) {
                        otherUserData.postValue(otherSmokes);
                    } else {
                        loadingError.postValue("No se encontraron datos para este usuario");
                    }
                });
            });
        } else {
            loadingError.postValue("Error de autenticación");
            isLoading.postValue(false);
        }
    }

    public void addContact(String userId, String contactName) {
        Map<String, Contact> contacts = savedContacts.getValue();
        if (contacts == null) {
            contacts = new HashMap<>();
        }

        contacts.put(userId, new Contact(userId, contactName));
        savedContacts.postValue(contacts);
        saveContactsToPreferences(contacts);
    }

    public void removeContact(String userId) {
        Map<String, Contact> contacts = savedContacts.getValue();
        if (contacts != null && contacts.containsKey(userId)) {
            contacts.remove(userId);
            savedContacts.postValue(contacts);
            saveContactsToPreferences(contacts);
        }
    }

    private void loadSavedContacts() {
        Map<String, Contact> contacts = repository.loadSavedContacts();
        savedContacts.postValue(contacts != null ? contacts : new HashMap<>());
    }

    private void saveContactsToPreferences(Map<String, Contact> contacts) {
        repository.saveContactsToPreferences(contacts);
    }

    public ChartData prepareChartData() {
        List<Smoke> currentSmokes = currentUserData.getValue();
        List<Smoke> otherSmokes = otherUserData.getValue();

        if (currentSmokes == null || otherSmokes == null ||
                currentSmokes.isEmpty() || otherSmokes.isEmpty()) {
            return null;
        }

        // Agrupar datos por día para ambos usuarios
        Map<String, DayData> currentSmokesPerDay = groupSmokesByDay(currentSmokes);
        Map<String, DayData> otherSmokesPerDay = groupSmokesByDay(otherSmokes);

        // Combinar todos los días únicos y ordenar por fecha real
        Set<String> allDatesSet = new HashSet<>();
        allDatesSet.addAll(currentSmokesPerDay.keySet());
        allDatesSet.addAll(otherSmokesPerDay.keySet());

        List<String> allDates = new ArrayList<>(allDatesSet);

        // Ordenar por fecha cronológicamente usando el timestamp
        Collections.sort(allDates, (date1, date2) -> {
            DayData day1 = currentSmokesPerDay.containsKey(date1) ?
                    currentSmokesPerDay.get(date1) : otherSmokesPerDay.get(date1);
            DayData day2 = currentSmokesPerDay.containsKey(date2) ?
                    currentSmokesPerDay.get(date2) : otherSmokesPerDay.get(date2);
            return Long.compare(day1.timestamp, day2.timestamp);
        });

        // Crear conjuntos de datos para el gráfico
        List<ChartEntry> currentEntries = new ArrayList<>();
        List<ChartEntry> otherEntries = new ArrayList<>();
        List<String> displayLabels = new ArrayList<>();

        for (int i = 0; i < allDates.size(); i++) {
            String dateKey = allDates.get(i);

            // Obtener los datos del día para ambos usuarios
            DayData currentDay = currentSmokesPerDay.get(dateKey);
            DayData otherDay = otherSmokesPerDay.get(dateKey);

            int currentCount = currentDay != null ? currentDay.count : 0;
            int otherCount = otherDay != null ? otherDay.count : 0;

            // Usar el timestamp del día que tenga datos, o el primero disponible
            long dayTimestamp = currentDay != null ? currentDay.timestamp : otherDay.timestamp;

            // Crear label corto para visualización
            String displayLabel = formatDateForDisplay(dayTimestamp);

            currentEntries.add(new ChartEntry(i, currentCount, "Tú"));
            otherEntries.add(new ChartEntry(i, otherCount, "Otro"));
            displayLabels.add(displayLabel);
        }

        return new ChartData(currentEntries, otherEntries, displayLabels);
    }

    public String getUserStats(List<Smoke> smokes, String userName) {
        if (smokes == null || smokes.isEmpty()) return "";

        // Calcular estadísticas
        int total = smokes.size();

        // Ordenar por timestamp
        Collections.sort(smokes, Comparator.comparingLong(Smoke::getTimestamp));

        long first = smokes.get(0).getTimestamp();
        long last = smokes.get(smokes.size()-1).getTimestamp();
        long diffDays = TimeUnit.MILLISECONDS.toDays(last - first) + 1;

        double average = total / (double) diffDays;

        return String.format(Locale.getDefault(),
                "%s: Total %d cigarros, %.1f por día",
                userName, total, average);
    }

    private Map<String, DayData> groupSmokesByDay(List<Smoke> smokes) {
        Map<String, DayData> smokesPerDay = new HashMap<>();
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Smoke smoke : smokes) {
            long timestamp = smoke.getTimestamp();
            String dateKey = keyFormat.format(new Date(timestamp));

            DayData existing = smokesPerDay.get(dateKey);
            if (existing == null) {
                // Crear nuevo DayData con el timestamp del primer cigarro de ese día
                smokesPerDay.put(dateKey, new DayData(1, timestamp));
            } else {
                // Incrementar contador, mantener el timestamp original del día
                smokesPerDay.put(dateKey, new DayData(existing.count + 1, existing.timestamp));
            }
        }

        return smokesPerDay;
    }

    private String formatDateForDisplay(long timestamp) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
        return displayFormat.format(new Date(timestamp));
    }

    public LiveData<List<Smoke>> getCurrentUserData() {
        return currentUserData;
    }

    public LiveData<List<Smoke>> getOtherUserData() {
        return otherUserData;
    }

    public LiveData<Map<String, Contact>> getSavedContacts() {
        return savedContacts;
    }

    public LiveData<String> getLoadingError() {
        return loadingError;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Clases de datos internas
    public static class ChartData {
        public final List<ChartEntry> currentUserEntries;
        public final List<ChartEntry> otherUserEntries;
        public final List<String> dates;

        public ChartData(List<ChartEntry> currentUserEntries,
                         List<ChartEntry> otherUserEntries,
                         List<String> dates) {
            this.currentUserEntries = currentUserEntries;
            this.otherUserEntries = otherUserEntries;
            this.dates = dates;
        }
    }

    public static class ChartEntry {
        public final int index;
        public final int value;
        public final String label;

        public ChartEntry(int index, int value, String label) {
            this.index = index;
            this.value = value;
            this.label = label;
        }
    }

    public static class Contact {
        public final String userId;
        public final String name;

        public Contact(String userId, String name) {
            this.userId = userId;
            this.name = name;
        }
    }

    // Nueva clase para almacenar datos del día con timestamp
    private static class DayData {
        public final int count;
        public final long timestamp;

        public DayData(int count, long timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }
    }
}
