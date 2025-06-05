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
    private final MutableLiveData<String> loadingError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public StatsViewModel(SmokeRepository repository) {
        this.repository = repository;
        loadUserData();
    }

    private void loadUserData() {
        isLoading.postValue(true);
        String userId = repository.getCurrentUserId();
        if (userId != null) {
            repository.getUserSmokes(userId, new SmokeRepository.OnResultListener<List<Smoke>>() {
                @Override
                public void onResult(List<Smoke> result) {
                    isLoading.postValue(false);
                    if (result != null && !result.isEmpty()) {
                        smokesData.postValue(result);
                    } else {
                        loadingError.postValue("No hay datos disponibles");
                    }
                }
            });
        } else {
            loadingError.postValue("Usuario no autenticado");
            isLoading.postValue(false);
        }
    }

    public LiveData<List<Smoke>> getSmokesData() {
        return smokesData;
    }

    public LiveData<String> getLoadingError() {
        return loadingError;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
