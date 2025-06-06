package com.example.appstopsmoke.data.datasource;

import android.util.Log;

import com.example.appstopsmoke.data.model.Smoke;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class FirebaseDataSource {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FirebaseDataSource() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // autenticación anónima automática
        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("Firebase", "Error en autenticación anónima", task.getException());
            }
        });
    }

    // añade una entrada a la base de datos del usuario
    public void registerSmoke(Smoke smoke, OnCompleteListener<Void> listener) {
        // crea un usuario si no existe
        Map<String, Object> user = new HashMap<>();
        user.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(smoke.getUserId())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    db.collection("users").document(smoke.getUserId())
                            .collection("smokes").document(smoke.getId())
                            .set(smoke)
                            .addOnCompleteListener(listener);
                })
                .addOnFailureListener(e -> {
                    listener.onComplete(Tasks.forException(e));
                });
    }

    // obtiene todas las entradas de la base de datos del usuario
    public Task<QuerySnapshot> getUserSmokes(String userId) {
        return db.collection("users").document(userId)
                .collection("smokes")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get();
    }

    // obtiene el ID del usuario actual
    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}
