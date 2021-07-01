package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.utils.Anonymous;

import java.util.ArrayList;

public class PlaylistsViewModel extends ViewModel {
    private static final String USER_ID = "admin";
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PlaylistsViewModel() {
        // Required empty public constructor
    }

    public Task<Void> createPlaylist() {
        String id = db.collection("songs").document().getId();
        Info info = new Info(
            id,
            "New Playlist",
            "",
            "#7b828b",
            USER_ID,
            new ArrayList<>(),
            Anonymous.getQueries("New Playlist")
        );

        return db.collection("playlists")
            .document(id)
            .set(info.toMap());
    }

}
