package com.example.appstopsmoke.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appstopsmoke.data.datasource.FirebaseDataSource;
import com.example.appstopsmoke.data.model.Smoke;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SmokeRepository {
    private final FirebaseDataSource dataSource;
    private final MutableLiveData<List<Smoke>> smokesLiveData = new MutableLiveData<>();

    // inicia el repositorio con los datos del firebase
    public SmokeRepository(FirebaseDataSource dataSource) {
        this.dataSource = dataSource;
    }

    // registra una nueva entrada en la base del usuario y notifica al listener
    public void registerSmoke(Smoke smoke, OnCompleteListener<Void> listener) {
        dataSource.registerSmoke(smoke, listener);
    }

    // obtiene las entradas del usuario y actualiza el livedata
    public LiveData<List<Smoke>> getUserSmokes(String userId) {
        dataSource.getUserSmokes(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Smoke> smokes = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    smokes.add(doc.toObject(Smoke.class));
                }
                smokesLiveData.setValue(smokes);
            } else {
                smokesLiveData.setValue(null);
            }
        });
        return smokesLiveData;
    }

    // devuelve el ID del usuario actual
    public String getCurrentUserId() {
        return dataSource.getCurrentUserId();
    }
}
