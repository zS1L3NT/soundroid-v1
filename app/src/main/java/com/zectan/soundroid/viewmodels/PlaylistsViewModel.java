package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.utils.Anonymous;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsViewModel extends ViewModel {
    private final FirebaseRepository repository = new FirebaseRepository();
    public final MutableLiveData<List<Info>> infos = new MutableLiveData<>();
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);
    private boolean watching = false;

    public PlaylistsViewModel() {
        // Required empty public constructor
    }

    public void watch(MainActivity activity) {
        if (watching) return;
        watching = true;
        repository
            .playlists(FirebaseRepository.USER_ID)
            .addSnapshotListener(activity, (snaps, error) -> {
                if (error == null) {
                    assert snaps != null;
                    infos.postValue(snaps.toObjects(Info.class));
                } else {
                    activity.handleError(error);
                }
            });
    }

    public void reload(OnFailureListener onFailureListener) {
        if (loading.getValue()) return;
        loading.setValue(true);
        repository
            .playlists(FirebaseRepository.USER_ID)
            .get()
            .addOnSuccessListener(snaps -> {
                loading.postValue(false);
                if (!snaps.isEmpty()) {
                    infos.postValue(snaps.toObjects(Info.class));
                } else {
                    onFailureListener.onFailure(new Exception("Result not found in database"));
                }
            })
            .addOnFailureListener(onFailureListener);
    }

    public Task<Void> createPlaylist() {
        String id = repository.songsCollection().document().getId();
        Info info = new Info(
            id,
            "New Playlist",
            "",
            "#7b828b",
            FirebaseRepository.USER_ID,
            new ArrayList<>(),
            Anonymous.getQueries("New Playlist")
        );

        return repository
            .playlist(id)
            .set(info.toMap());
    }

}
