package com.example.appstopsmoke.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appstopsmoke.data.model.Smoke;
import com.example.appstopsmoke.data.repository.SmokeRepository;

import java.util.List;

public class CompareViewModel extends ViewModel {
    private final SmokeRepository repository;
    private final MutableLiveData<List<Smoke>> otherUserData = new MutableLiveData<>();

    // constructor que inyecta la dependencia del repositorio
    public CompareViewModel(SmokeRepository repository) {
        this.repository = repository;
    }

    // carga los datos de otro usuario y los almacena en livedata
    public void loadOtherUserData(String userId) {
        repository.getUserSmokes(userId).observeForever(otherUserData::postValue);
    }

    // devuelve los datos del otro usuario
    public LiveData<List<Smoke>> getOtherUserData() {
        return otherUserData;
    }
}
