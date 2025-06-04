package com.example.appstopsmoke.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appstopsmoke.data.model.Smoke;
import com.example.appstopsmoke.data.repository.SmokeRepository;

import java.util.List;

public class StatsViewModel extends ViewModel {
    private final SmokeRepository repository;
    private final MutableLiveData<List<Smoke>> smokesData = new MutableLiveData<>();


    // constructor que inicializa el viewmodel con el repositorio y carga los datos del usuario
    public StatsViewModel(SmokeRepository repository) {
        this.repository = repository;
        loadUserData();
    }


    // carga los datos del usuario actual desde el repositorio
    private void loadUserData() {
        String userId = repository.getCurrentUserId();
        if (userId != null) {
            repository.getUserSmokes(userId);
        }
    }


    // livedata con los datos del usuario
    public LiveData<List<Smoke>> getSmokesData() {
        return repository.getUserSmokes(repository.getCurrentUserId());
    }
}
