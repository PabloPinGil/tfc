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
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public CompareViewModel(SmokeRepository repository) {
        this.repository = repository;
    }

    public void loadOtherUserData(String userId) {
        repository.getUserSmokes(userId, new SmokeRepository.OnResultListener<List<Smoke>>() {
            @Override
            public void onResult(List<Smoke> result) {
                if (result != null && !result.isEmpty()) {
                    otherUserData.postValue(result);
                } else {
                    errorMessage.postValue("No se encontraron datos");
                }
            }
        });
    }

    public LiveData<List<Smoke>> getOtherUserData() {
        return otherUserData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
