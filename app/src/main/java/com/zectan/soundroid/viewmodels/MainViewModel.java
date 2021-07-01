package com.zectan.soundroid.viewmodels;

import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.SearchResult;
import com.zectan.soundroid.models.Song;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) MainViewModel";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public final StrictLiveData<String> userId = new StrictLiveData<>("admin");
    public final MutableLiveData<Exception> error = new MutableLiveData<>();
    public final StrictLiveData<List<Info>> myInfos = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<List<Song>> mySongs = new StrictLiveData<>(new ArrayList<>());

    public MainViewModel() {

    }

    public void watch(MainActivity activity) {
        Log.d(TAG, "STARTED WATCHING TO FIREBASE VALUES");

        db.collection("playlists")
            .whereEqualTo("userId", userId.getValue())
            .addSnapshotListener(activity, (snaps, error) -> {
                if (error != null) {
                    activity.handleError(error);
                    return;
                }
                assert snaps != null;
                myInfos.postValue(snaps.toObjects(Info.class));
            });

        db.collection("songs")
            .whereEqualTo("userId", userId.getValue())
            .addSnapshotListener(activity, (snaps, error) -> {
                if (error != null) {
                    activity.handleError(error);
                    return;
                }
                assert snaps != null;
                mySongs.postValue(snaps.toObjects(Song.class));
            });
    }

    public List<Song> getSongsFromPlaylist(String playlistId) {
        return mySongs
            .getValue()
            .stream()
            .filter(song -> song.getPlaylistId().equals(playlistId))
            .collect(Collectors.toList());
    }

    public void watchSongsFromPlaylist(LifecycleOwner owner, String playlistId, OnChange<List<Song>> onChange) {
        mySongs.observe(owner, songs -> onChange.run(
            songs
                .stream()
                .filter(song -> song.getPlaylistId().equals(playlistId))
                .collect(Collectors.toList())
        ));
    }

    public @Nullable Info getInfoFromPlaylist(String playlistId) {
        List<Info> infos = myInfos
            .getValue()
            .stream()
            .filter(info -> info.getId().equals(playlistId))
            .collect(Collectors.toList());
        if (infos.size() == 0) return null;
        return infos.get(0);
    }

    public void watchInfoFromPlaylist(LifecycleOwner owner, String playlistId, OnChange<Info> onChange) {
        myInfos.observe(owner, infos_ -> {
            List<Info> infos = infos_
                .stream()
                .filter(info -> info.getId().equals(playlistId))
                .collect(Collectors.toList());
            if (infos.size() != 0) onChange.run(infos.get(0));
        });
    }

    public List<SearchResult> getResultsMatchingQuery(String query) {
        List<SearchResult> results = new ArrayList<>();
        results.addAll(
            myInfos
                .getValue()
                .stream()
                .filter(info -> info.getQueries().contains(query))
                .map(SearchResult::new)
                .collect(Collectors.toList())
        );
        results.addAll(
            mySongs
                .getValue()
                .stream()
                .filter(song -> song.getQueries().contains(query))
                .map(SearchResult::new)
                .collect(Collectors.toList())
        );
        return results;
    }

    public interface OnChange<T> {
        void run(T t);
    }
}
