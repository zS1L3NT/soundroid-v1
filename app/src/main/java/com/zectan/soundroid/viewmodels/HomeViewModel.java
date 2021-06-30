package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;

import java.util.List;
import java.util.stream.Collectors;

public class HomeViewModel extends ViewModel {
    private final FirebaseRepository repository = new FirebaseRepository();
    public final StrictLiveData<Playlist> playlist = new StrictLiveData<>(Playlist.getEmpty());
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);
    private boolean watching = false;

    public HomeViewModel() {

    }

    public void watch(MainActivity activity) {
        if (watching) return;
        watching = true;
        repository
            .userSongs(FirebaseRepository.USER_ID)
            .addSnapshotListener(activity, (snaps, error) -> {
                if (error == null) {
                    assert snaps != null;
                    List<Song> songs = snaps.toObjects(Song.class);
                    List<String> order = songs
                        .stream()
                        .sorted((song1, song2) -> song1.getTitle().compareTo(song2.getTitle()))
                        .map(Song::getSongId)
                        .collect(Collectors.toList());
                    Info info = new Info("", "Downloads", order);
                    playlist.postValue(new Playlist(info, songs));
                } else {
                    activity.handleError(error);
                }
            });
    }

    public void reload(OnFailureListener onFailureListener) {
        if (loading.getValue()) return;
        loading.setValue(true);
        repository
            .userSongs(FirebaseRepository.USER_ID)
            .get()
            .addOnSuccessListener(snaps -> {
                loading.postValue(false);
                List<Song> songs = snaps.toObjects(Song.class);
                List<String> order = songs
                    .stream()
                    .sorted((song1, song2) -> song1.getTitle().compareTo(song2.getTitle()))
                    .map(Song::getSongId)
                    .collect(Collectors.toList());
                Info info = new Info("", "Downloads", order);
                playlist.postValue(new Playlist(info, songs));
            })
            .addOnFailureListener(onFailureListener);
    }
}
