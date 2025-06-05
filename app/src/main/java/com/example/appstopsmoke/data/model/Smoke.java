package com.example.appstopsmoke.data.model;

public class Smoke {
    private String id;
    private long timestamp;
    private String userId;

    // constructor vacío necesario para Firestore
    public Smoke() {}

    public Smoke(String id, long timestamp, String userId) {
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // getters y setters
    public String getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
}
