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
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.Models.User;
import com.zectan.soundroid.Services.DownloadService;
import com.zectan.soundroid.Services.PlayingService;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) MainViewModel";
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    public final StrictLiveData<User> myUser = new StrictLiveData<>(User.getEmpty());
    public final StrictLiveData<List<Playlist>> myPlaylists = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<List<Song>> mySongs = new StrictLiveData<>(new ArrayList<>());

    public final MutableLiveData<DownloadService> downloadService = new MutableLiveData<>();
    public final MutableLiveData<PlayingService> playingService = new MutableLiveData<>();

    public String userId;

    /**
     * Connect to download service
     *
     * @param callback Callback
     * @return ServiceConnection object
     */
    public ServiceConnection getDownloadConnection(MainActivity.DownloadServiceCallback callback) {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                DownloadService.DownloadBinder downloadBinder = (DownloadService.DownloadBinder) binder;
                downloadService.postValue(downloadBinder.getService());
                callback.onStart(downloadBinder.getService());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                downloadService.postValue(null);
            }
        };
    }

    /**
     * Connect to playing service
     *
     * @param callback Callback
     * @return ServiceConnection object
     */
    public ServiceConnection getPlayingConnection(MainActivity.PlayingServiceCallback callback) {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                PlayingService.PlayingBinder playingBinder = (PlayingService.PlayingBinder) binder;
                playingService.postValue(playingBinder.getService());
                callback.onStart(playingBinder.getService());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                playingService.postValue(null);
            }
        };
    }

    /**
     * Watch values from firebase and update respective livedata when the values change
     *
     * @param activity Activity
     */
    public void watch(MainActivity activity) {
        Log.d(TAG, "STARTED WATCHING FIREBASE VALUES");

        mDb.collection("users")
            .document(userId)
            .addSnapshotListener(activity, (snap, error) -> {
                if (error != null) {
                    activity.warnError(error);
                    return;
                }
                assert snap != null;
                User user = Objects.requireNonNull(snap.toObject(User.class));
                myUser.postValue(user);
                activity.updateTheme(user.getTheme());
            });

        mDb.collection("playlists")
            .whereEqualTo("userId", userId)
            .addSnapshotListener(activity, (snaps, error) -> {
                if (error != null) {
                    activity.warnError(error);
                    return;
                }
                assert snaps != null;
                myPlaylists.postValue(snaps.toObjects(Playlist.class));
            });

        mDb.collection("songs")
            .whereEqualTo("userId", userId)
            .addSnapshotListener(activity, (snaps, error) -> {
                if (error != null) {
                    activity.warnError(error);
                    return;
                }
                assert snaps != null;
                mySongs.postValue(snaps.toObjects(Song.class));
            });
    }

    /**
     * Fetch songs with a playlistId as defined
     *
     * @param playlistId ID of playlist
     * @return The list of song objects in the playlist
     */
    public List<Song> getSongsFromPlaylist(String playlistId) {
        return mySongs
            .getValue()
            .stream()
            .filter(song -> song.getPlaylistId().equals(playlistId))
            .collect(Collectors.toList());
    }

    /**
     * Watch songs with a playlistId as defined
     *
     * @param owner      LifeCycle owner
     * @param playlistId ID of playlist
     * @param onChange   Callback
     */
    public void watchSongsFromPlaylist(LifecycleOwner owner, String playlistId, OnChange<List<Song>> onChange) {
        mySongs.observe(owner, songs -> onChange.run(
            songs
                .stream()
                .filter(song -> song.getPlaylistId().equals(playlistId))
                .collect(Collectors.toList())
        ));
    }

    /**
     * Fetch playlist with a playlistId as defined
     *
     * @param playlistId ID of playlist
     */
    public @Nullable Playlist getPlaylistFromPlaylists(String playlistId) {
        List<Playlist> playlists = myPlaylists
            .getValue()
            .stream()
            .filter(info -> info.getId().equals(playlistId))
            .collect(Collectors.toList());
        if (playlists.size() == 0) return null;
        return playlists.get(0);
    }

    /**
     * Watch a playlist with a playlistId as defined
     *
     * @param owner      LifeCycle owner
     * @param playlistId ID of playlist
     * @param onChange   Callback
     */
    public void watchPlaylistFromPlaylists(LifecycleOwner owner, String playlistId, OnChange<Playlist> onChange) {
        myPlaylists.observe(owner, infos_ -> {
            List<Playlist> playlists = infos_
                .stream()
                .filter(info -> info.getId().equals(playlistId))
                .collect(Collectors.toList());
            if (playlists.size() != 0) onChange.run(playlists.get(0));
        });
    }

    public interface OnChange<T> {
        void run(T t);
    }
}
