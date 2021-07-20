package com.zectan.soundroid.ViewModels;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.Classes.StrictLiveData;
import com.zectan.soundroid.DownloadService;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.Info;
import com.zectan.soundroid.Models.SearchResult;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.Models.User;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) MainViewModel";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public final MutableLiveData<Exception> error = new MutableLiveData<>();
    public final StrictLiveData<User> myUser = new StrictLiveData<>(User.getEmpty());
    public final StrictLiveData<List<Info>> myInfos = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<List<Song>> mySongs = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<Boolean> showUpdateDialog = new StrictLiveData<>(false);
    public final MutableLiveData<DownloadService.DownloadBinder> downloadBinder = new MutableLiveData<>();
    public String userId;

    public MainViewModel() {

    }

    public ServiceConnection getDownloadConnection(MainActivity.DownloadServiceCallback callback) {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                MainViewModel.this.downloadBinder.postValue((DownloadService.DownloadBinder) binder);
                callback.onStart((DownloadService.DownloadBinder) binder);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                downloadBinder.postValue(null);
            }
        };
    }

    public void watch(MainActivity activity) {
        Log.d(TAG, "STARTED WATCHING FIREBASE VALUES");

        db.collection("users")
            .document(userId)
            .addSnapshotListener(activity, (snap, error) -> {
                if (error != null) {
                    activity.handleError(error);
                    return;
                }
                assert snap != null;
                myUser.postValue(Objects.requireNonNull(snap.toObject(User.class)));
            });

        db.collection("playlists")
            .whereEqualTo("userId", userId)
            .addSnapshotListener(activity, (snaps, error) -> {
                if (error != null) {
                    activity.handleError(error);
                    return;
                }
                assert snaps != null;
                myInfos.postValue(snaps.toObjects(Info.class));
            });

        db.collection("songs")
            .whereEqualTo("userId", userId)
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
