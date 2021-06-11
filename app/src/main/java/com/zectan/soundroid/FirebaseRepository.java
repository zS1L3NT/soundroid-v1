package com.zectan.soundroid;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FirebaseRepository {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FirebaseRepository() {

    }

    public Query playlists(String userId) {
        return db.collection("playlists").whereArrayContains("users", userId);
    }

    public DocumentReference playlist(String playlistId) {
        return db.collection("playlists").document(playlistId);
    }

    public Query playlistSongs(String playlistId) {
        return db.collection("songs").whereArrayContains("playlists", playlistId);
    }

    public Query searchPlaylist(String userId, String query) {
        // ! REALLY NEEDS DATABASE REWORK
        return db.collection("playlists")
            .whereArrayContains("users", userId)
            .whereEqualTo("name", query);
    }

    public Query userSongs(String userId) {
        return db.collection("songs").whereArrayContains("users", userId);
    }

    public Query searchSong(String userId, String query) {
        // ! REALLY NEEDS DATABASE REWORK
        return db.collection("songs")
            .whereArrayContains("users", userId)
            .whereEqualTo("title", query);
    }

    public DocumentReference song(String songId) {
        return db.collection("songs").document(songId);
    }

    public DocumentReference user(String userId) {
        return db.collection("users").document(userId);
    }

}
