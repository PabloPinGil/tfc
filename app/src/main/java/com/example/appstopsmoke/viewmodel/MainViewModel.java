package com.example.appstopsmoke.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appstopsmoke.data.model.Smoke;
import com.example.appstopsmoke.data.repository.SmokeRepository;

import java.util.UUID;

public class MainViewModel extends ViewModel {
    private final SmokeRepository repository;
    private final MutableLiveData<Boolean> smokeRegistered = new MutableLiveData<>();


    //constructor que inyecta la dependencia del repositorio
    public MainViewModel(SmokeRepository repository) {
        this.repository = repository;
    }

    // registra una nueva entrada en la base de datos del usuario
    // si el ID del usuario no existe crea un usuario nuevo
    public void registerSmoke() {
        String userId = repository.getCurrentUserId();
        if (userId != null) {
            Smoke smoke = new Smoke(
                    UUID.randomUUID().toString(),
                    System.currentTimeMillis(),
                    userId
            );

            repository.registerSmoke(smoke, task -> {
                smokeRegistered.postValue(task.isSuccessful());
            });
        }
    }

    // indica si la operación funcionó
    public LiveData<Boolean> getSmokeRegistered() {
        return smokeRegistered;
    }
}
