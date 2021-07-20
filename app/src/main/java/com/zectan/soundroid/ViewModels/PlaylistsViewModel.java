package com.zectan.soundroid.ViewModels;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.Classes.StrictLiveData;
import com.zectan.soundroid.Models.Info;
import com.zectan.soundroid.Utils.Utils;

import java.util.ArrayList;

public class PlaylistsViewModel extends ViewModel {
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PlaylistsViewModel() {
        // Required empty public constructor
    }

    public Task<Void> createPlaylist(String userId) {
        String id = db.collection("songs").document().getId();
        Info info = new Info(
            id,
            "New Playlist",
            "https://firebasestorage.googleapis.com/v0/b/android-soundroid.appspot.com/o/playing_cover_default.png?alt=media&token=e8980e80-ab5d-4f21-8ed4-6bc6e7e06ef7",
            "#7b828b",
            userId,
            new ArrayList<>(),
            Utils.getQueries("New Playlist")
        );

        return db.collection("playlists")
            .document(id)
            .set(info.toMap());
    }

}
