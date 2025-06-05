package com.example.appstopsmoke.data.repository;

import com.example.appstopsmoke.data.datasource.FirebaseDataSource;
import com.example.appstopsmoke.data.model.Smoke;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmokeRepository {
    private final FirebaseDataSource dataSource;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public SmokeRepository(FirebaseDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getCurrentUserId() {
        return dataSource.getCurrentUserId();
    }

    public void registerSmoke(Smoke smoke, OnCompleteListener<Void> listener) {
        dataSource.registerSmoke(smoke, listener);
        updateLastSmokeTimestamp(smoke.getUserId(), smoke.getTimestamp());
    }

    // actualiza en los datos del usuario el timestamp del último cigarro fumado
    private void updateLastSmokeTimestamp(String userId, long timestamp) {
        Map<String, Object> data = new HashMap<>();
        data.put("lastSmokeTimestamp", timestamp);

        db.collection("users")
                .document(userId)
                .update(data);
    }

    // obtiene los datos de los cigarros fumados del usuario
    public void getUserSmokes(String userId, OnResultListener<List<Smoke>> listener) {
        dataSource.getUserSmokes(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Smoke> smokes = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Smoke smoke = document.toObject(Smoke.class);
                    smokes.add(smoke);
                }
                listener.onResult(smokes);
            } else {
                listener.onResult(Collections.emptyList());
            }
        });
    }

    // obtiene la ultima fecha en la que se fumó
    public void getLastSmokeTimestamp(OnResultListener<String> listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onResult(null);
            return;
        }

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long timestamp = documentSnapshot.getLong("lastSmokeTimestamp");
                        listener.onResult(timestamp != null ? String.valueOf(timestamp) : null);
                    } else {
                        listener.onResult(null);
                    }
                })
                .addOnFailureListener(e -> listener.onResult(null));
    }

    public void getTodaySmokeCount(OnResultListener<Integer> listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onResult(0);
            return;
        }

        // obtiene el inicio del día
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        // obtiene la cantidad de cigarros fumados el día de hoy
        db.collection("users").document(userId)
                .collection("smokes")
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listener.onResult(querySnapshot.size());
                })
                .addOnFailureListener(e -> listener.onResult(0));
    }

    public interface OnResultListener<T> {
        void onResult(T result);
    }
}
