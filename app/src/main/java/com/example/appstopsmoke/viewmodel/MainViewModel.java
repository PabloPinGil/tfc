package com.example.appstopsmoke.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.example.appstopsmoke.data.model.Smoke;
import com.example.appstopsmoke.data.repository.SmokeRepository;

import java.util.UUID;

public class MainViewModel extends ViewModel {
    private final SmokeRepository repository;
    private final MutableLiveData<Boolean> smokeRegistered = new MutableLiveData<>();
    private final MutableLiveData<String> smokeStatus = new MutableLiveData<>();

    public MainViewModel(SmokeRepository repository) {
        this.repository = repository;
        loadSmokeStatus();
    }

    // resgistra en firebase un nuevo cigarro fumado
    public void registerSmoke() {
        String userId = repository.getCurrentUserId();
        if (userId != null) {
            Smoke smoke = new Smoke(
                    UUID.randomUUID().toString(),
                    System.currentTimeMillis(),
                    userId
            );

            repository.registerSmoke(smoke, task -> {
                if (task.isSuccessful()) {
                    smokeRegistered.postValue(true);
                    loadSmokeStatus(); // Actualizar estado después de registrar
                } else {
                    smokeRegistered.postValue(false);
                }
            });
        }
    }

    // decide si mostrar "Llevas X cigarros fumados hoy" o "Llevas X días sin fumar"
    public void loadSmokeStatus() {
        repository.getLastSmokeTimestamp(result -> {
            if (result != null && !result.isEmpty()) {
                long lastSmokeTime = Long.parseLong(result);
                long currentTime = System.currentTimeMillis();

                // Comprobar si es el mismo día
                if (isSameDay(lastSmokeTime, currentTime)) {
                    repository.getTodaySmokeCount(count -> {
                        smokeStatus.postValue("Llevas " + count + " cigarros fumados hoy");
                    });
                } else {
                    long days = TimeUnit.MILLISECONDS.toDays(currentTime - lastSmokeTime);
                    smokeStatus.postValue("Llevas " + days + " días sin fumar");
                }
            } else {
                smokeStatus.postValue("No has fumado recientemente");
            }
        });
    }

    private boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public LiveData<Boolean> getSmokeRegistered() {
        return smokeRegistered;
    }

    public LiveData<String> getSmokeStatus() {
        return smokeStatus;
    }
}
