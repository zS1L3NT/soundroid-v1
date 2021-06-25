package com.zectan.soundroid;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FirebaseRepository {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FirebaseRepository() {

    }

    public Query playlists(String userId) {
        return db.collection("playlists").whereEqualTo(String.format("owners.%s", userId), true);
    }

    public DocumentReference playlist(String playlistId) {
        return db.collection("playlists").document(playlistId);
    }

    public Query playlistSongs(String playlistId) {
        return db.collection("songs").whereArrayContains("playlists", playlistId);
    }

    public Query searchPlaylist(String userId, String query) {
        return db.collection("playlists")
            .whereEqualTo(String.format("owners.%s", userId), true)
            .whereArrayContains("queries", query.toLowerCase());
    }

    public Query userSongs(String userId) {
        return db.collection("songs").whereEqualTo(String.format("owners.%s", userId), true);
    }

    public Query searchSong(String userId, String query) {
        return db.collection("songs")
            .whereEqualTo(String.format("owners.%s", userId), true)
            .whereArrayContains("queries", query.toLowerCase());
    }

    public DocumentReference song(String songId) {
        return db.collection("songs").document(songId);
    }

}
