package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.connection.PlaylistSongsRequest;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewViewModel extends ViewModel {
    private final FirebaseRepository repository = new FirebaseRepository();
    public final MutableLiveData<Info> info = new MutableLiveData<>();
    public final StrictLiveData<List<Song>> songs = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);
    private boolean watching = false;

    public PlaylistViewViewModel() {
        // Required empty public constructor
    }

    public void watch(MainActivity activity) {
        if (watching) return;
        watching = true;
        info.observe(activity, __ -> reload(activity::handleError));
    }

    public void reload(OnFailureListener onFailureListener) {
        if (loading.getValue()) return;
        loading.postValue(true);
        Info info = this.info.getValue();
        if (info == null) return;
        repository
            .playlistSongs(info.getId())
            .get()
            .addOnSuccessListener(snaps -> {
                if (!snaps.isEmpty()) {
                    songs.postValue(snaps.toObjects(Song.class));
                    loading.postValue(false);
                } else {
                    fetchServer(onFailureListener, info);
                }
            })
            .addOnFailureListener(__ -> fetchServer(onFailureListener, info));
    }

    private void fetchServer(OnFailureListener onFailureListener, Info info) {
        new PlaylistSongsRequest(info.getId(), new PlaylistSongsRequest.Callback() {
            @Override
            public void onComplete(List<Song> songs) {
                loading.postValue(false);
                PlaylistViewViewModel.this.songs.postValue(songs);
            }

            @Override
            public void onError(String message) {
                onFailureListener.onFailure(new Exception(message));
                loading.postValue(false);
            }
        });
    }
}
