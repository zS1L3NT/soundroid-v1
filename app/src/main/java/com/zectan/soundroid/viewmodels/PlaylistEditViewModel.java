package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistEditViewModel extends ViewModel {
    private final FirebaseRepository repository = new FirebaseRepository();
    public final StrictLiveData<Info> info = new StrictLiveData<>(Info.getEmpty());
    public final StrictLiveData<List<Song>> songs = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<Integer> navigateNow = new StrictLiveData<>(0);
    private boolean watching = false;

    public PlaylistEditViewModel() {
        // Required empty public constructor
    }

    public void watch(MainActivity activity) {
        if (watching) return;
        watching = true;
        info.observe(activity, __ -> reload(activity::handleError));
    }

    private void reload(OnFailureListener onFailureListener) {
        repository
            .playlistSongs(info.getValue().getId())
            .get()
            .addOnSuccessListener(snaps -> songs.postValue(snaps.toObjects(Song.class)))
            .addOnFailureListener(onFailureListener);
    }

}
