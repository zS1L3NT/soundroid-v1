package com.zectan.soundroid;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FirebaseRepository {
    public static final String USER_ID = "admin";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FirebaseRepository() {

    }

    public Query playlists(String userId) {
        return db.collection("playlists").whereEqualTo("userId", userId);
    }

    public DocumentReference playlist(String playlistId) {
        return db.collection("playlists").document(playlistId);
    }

    public Query playlistSongs(String playlistId) {
        return db.collection("songs").whereEqualTo("playlistId", playlistId);
    }

    public Query searchPlaylist(String userId, String query) {
        return playlists(userId).whereArrayContains("queries", query.toLowerCase());
    }

    public Query userSongs(String userId) {
        return db.collection("songs").whereEqualTo("userId", userId);
    }

    public Query searchSong(String userId, String query) {
        return userSongs(userId).whereArrayContains("queries", query);
    }

    public CollectionReference songsCollection() {
        return db.collection("songs");
    }

    public CollectionReference playlistsCollection() {
        return db.collection("playlists");
    }
}
