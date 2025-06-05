package com.example.appstopsmoke.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.appstopsmoke.data.datasource.FirebaseDataSource;
import com.example.appstopsmoke.data.model.Smoke;
import com.example.appstopsmoke.viewmodel.CompareViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmokeRepository {
    private final FirebaseDataSource dataSource;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Context context;
    private static final String PREFS_NAME = "SmokeTrackerPrefs";
    private static final String CONTACT_IDS_KEY = "contact_ids";
    private static final String CONTACT_NAMES_KEY_PREFIX = "contact_name_";

    public SmokeRepository(FirebaseDataSource dataSource, Context context) {
        this.dataSource = dataSource;
        this.context = context;
    }

    public String getCurrentUserId() {
        return dataSource.getCurrentUserId();
    }

    public void registerSmoke(Smoke smoke, OnCompleteListener<Void> listener) {
        dataSource.registerSmoke(smoke, listener);
        updateLastSmokeTimestamp(smoke.getUserId(), smoke.getTimestamp());
    }

    // actualiza el timestamp del último cigarro registrado para este usuario
    private void updateLastSmokeTimestamp(String userId, long timestamp) {
        Map<String, Object> data = new HashMap<>();
        data.put("lastSmokeTimestamp", timestamp);

        db.collection("users")
                .document(userId)
                .update(data);
    }

    // obtiene todos los cigarros del usuario actual desde firestore
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

    public void getLastSmokeTimestamp(OnResultListener<String> listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onResult(null);
            return;
        }

        // obtiene el timestamp del último cigarro registrado para este usuario
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

    // obtiene el número de cigarros de hoy
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

        // obtiene los cigarros de hoy para el usuario actual
        db.collection("users").document(userId)
                .collection("smokes")
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listener.onResult(querySnapshot.size());
                })
                .addOnFailureListener(e -> listener.onResult(0));
    }

    public Map<String, CompareViewModel.Contact> loadSavedContacts() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, CompareViewModel.Contact> contacts = new HashMap<>();

        // recupera todos los IDs de tus contactos
        Set<String> contactIds = prefs.getStringSet(CONTACT_IDS_KEY, new HashSet<>());

        for (String id : contactIds) {
            String name = prefs.getString(CONTACT_NAMES_KEY_PREFIX + id, null);

            if (name != null) {
                // crea un contacto solo con id y name
                contacts.put(id, new CompareViewModel.Contact(id, name));
            }
        }

        return contacts;
    }

    public void saveContactsToPreferences(Map<String, CompareViewModel.Contact> contacts) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // guarda todos los IDs de los contactos
        Set<String> contactIds = new HashSet<>(contacts.keySet());
        editor.putStringSet(CONTACT_IDS_KEY, contactIds);

        // guarda cada contacto individualmente (solo el nombre)
        for (Map.Entry<String, CompareViewModel.Contact> entry : contacts.entrySet()) {
            String id = entry.getKey();
            CompareViewModel.Contact contact = entry.getValue();
            editor.putString(CONTACT_NAMES_KEY_PREFIX + id, contact.name);
        }

        editor.apply();
    }

    public interface OnResultListener<T> {
        void onResult(T result);
    }
}