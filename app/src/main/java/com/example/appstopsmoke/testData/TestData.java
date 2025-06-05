package com.example.appstopsmoke.testData;

import android.content.Context;
import android.util.Log;

import com.example.appstopsmoke.data.datasource.FirebaseDataSource;
import com.example.appstopsmoke.data.repository.SmokeRepository;
import com.example.appstopsmoke.data.model.Smoke;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

// clase para insertar datos de prueba en firestore
public class TestData {

    public static void insertTestData(Context context) {
        FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(authResult -> {
            String userId = authResult.getUser().getUid();
            String testUser = "testUser";

            FirebaseDataSource dataSource = new FirebaseDataSource();
            SmokeRepository repository = new SmokeRepository(dataSource, context);

            List<Smoke> smokesCurrentUser = new ArrayList<>();
            List<Smoke> smokesTestUser = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.set(2025, Calendar.JUNE, 5, 0, 0, 0); // 5 de junio
            long baseTime = calendar.getTimeInMillis();

            for (int i = 1; i <= 7; i++) {
                long dayTimestamp = baseTime - i * 24 * 60 * 60 * 1000;

                // Para usuario actual
                smokesCurrentUser.add(new Smoke(UUID.randomUUID().toString(), dayTimestamp, userId));
                smokesCurrentUser.add(new Smoke(UUID.randomUUID().toString(), dayTimestamp + 60000, userId));

                // Para testUser
                smokesTestUser.add(new Smoke(UUID.randomUUID().toString(), dayTimestamp + 300000, testUser));
            }

            for (Smoke smoke : smokesCurrentUser) {
                repository.registerSmoke(smoke, task -> {
                    if (task.isSuccessful()) {
                        Log.d("TestData", "Cigarro insertado (usuario actual): " + smoke.getTimestamp());
                    } else {
                        Log.e("TestData", "Error al insertar cigarro (usuario actual)");
                    }
                });
            }

            for (Smoke smoke : smokesTestUser) {
                repository.registerSmoke(smoke, task -> {
                    if (task.isSuccessful()) {
                        Log.d("TestData2", "Cigarro insertado (testUser): " + smoke.getTimestamp());
                    } else {
                        Log.e("TestData2", "Error al insertar cigarro (testUser)");
                    }
                });
            }

        }).addOnFailureListener(e -> {
            Log.e("TestData", "Error al autenticar anónimamente", e);
        });
    }
}



/*
Reglas seguras firebase:

rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Reglas para usuarios
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      // Subcolección "smokes"
      match /smokes/{smokeId} {
        allow read: if true;     // Todos pueden leer
        allow write: if request.auth.uid == userId; // Solo dueño
      }
    }
  }
}

Reglas de pruebas:

rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}

 */

