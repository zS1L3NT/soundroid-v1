package com.zectan.soundroid.Utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.Connections.PlaylistDeleteRequest;
import com.zectan.soundroid.Connections.SavePlaylistRequest;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.Playable;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.Models.User;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Services.PlayingService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MenuEvents {
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private final MainActivity mActivity;
    private final Playlist mPlaylist;
    private final Song mSong;
    private final MenuItem mItem;
    private final Runnable mRunnable;

    /**
     * Object that handles a click event of any of the menu items
     *
     * @param activity Activity
     * @param playlist Playlist
     * @param song     Song
     * @param item     Menu item
     * @param runnable Runnable
     */
    public MenuEvents(MainActivity activity, Playlist playlist, Song song, MenuItem item, Runnable runnable) {
        mActivity = activity;
        mPlaylist = playlist;
        mSong = song;
        mItem = item;
        mRunnable = runnable;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean handle() {
        switch (mItem.getItemId()) {
            case MenuBuilder.ADD_TO_PLAYLIST:
                addToPlaylist();
                break;
            case MenuBuilder.ADD_TO_QUEUE:
                addToQueue();
                break;
            case MenuBuilder.EDIT_SONG:
                editSong();
                break;
            case MenuBuilder.OPEN_QUEUE:
                openQueue();
                break;
            case MenuBuilder.CLEAR_QUEUE:
                clearQueue();
                break;
            case MenuBuilder.START_DOWNLOADS:
                startDownloads();
                break;
            case MenuBuilder.STOP_DOWNLOADS:
                stopDownloads();
                break;
            case MenuBuilder.CLEAR_DOWNLOADS:
                clearDownloads();
                break;
            case MenuBuilder.REMOVE_DOWNLOAD:
                removeDownload();
                break;
            case MenuBuilder.SAVE_PLAYLIST:
                savePlaylist();
                break;
            case MenuBuilder.PLAY_PLAYLIST:
                playPlaylist();
                break;
            case MenuBuilder.EDIT_PLAYLIST:
                editPlaylist();
                break;
            case MenuBuilder.DELETE_PLAYLIST:
                deletePlaylist();
                break;
            case MenuBuilder.ADD_PLAYLIST:
                addPlaylist();
                break;
            case MenuBuilder.IMPORT_PLAYLIST:
                importPlaylist();
                break;
            default:
                break;
        }
        return true;
    }

    private void addToPlaylist() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(mActivity).setTitle("Add To Playlist:");
        List<Playlist> playlists = new ArrayList<>(mActivity.mMainVM.myPlaylists.getValue());

        // Items in the dialog box should represent the your playlists
        dialog.setItems(ListArrayUtils.toArray(CharSequence.class, playlists.stream().map(Playlist::getName).collect(Collectors.toList())), (dialog_, i) -> {
            Playlist playlist = playlists.get(i);

            AtomicInteger completed = new AtomicInteger(0);
            OnSuccessListener<Object> onSuccessListener = __ -> {
                if (completed.incrementAndGet() == 2) {
                    mActivity.snack("Song added to playlist");
                }
            };

            // Check if song is in playlist[i]
            boolean inPlaylist = mActivity
                .mMainVM
                .getSongsFromPlaylist(playlist.getId())
                .stream()
                .anyMatch(song -> song.getSongId().equals(mSong.getSongId()));

            if (inPlaylist) {
                mActivity.snack("Song already in playlist!");
            } else {
                // Not in playlist, can add to playlist
                mSong.setPlaylistId(playlist.getId());
                mSong.setUserId(mActivity.mMainVM.userId);
                mDb.collection("songs")
                    .add(mSong.toMap())
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(mActivity::warnError);
                mDb.collection("playlists")
                    .document(playlist.getId())
                    .update("order", FieldValue.arrayUnion(mSong.getSongId()))
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(mActivity::warnError);
            }
        });
        dialog.show();
    }

    private void addToQueue() {
        mActivity.getPlayingService(service -> service.addToQueue(mSong));
        mActivity.snack("Song added to queue");
    }

    private void editSong() {
        mActivity.mSongEditVM.song.setValue(mSong);
        mRunnable.run();
    }

    private void openQueue() {
        mRunnable.run();
    }

    private void clearQueue() {
        mActivity.getPlayingService(PlayingService::clearQueue);
    }

    private void startDownloads() {
        mActivity.getDownloadService(service -> {
            Playable playable = new Playable(mPlaylist, mActivity.mMainVM.getSongsFromPlaylist(mPlaylist.getId()));

            User user = mActivity.mMainVM.myUser.getValue();
            if (service.startDownload(playable, user.getHighDownloadQuality())) {
                mActivity.snack("Download starting");
            } else {
                mActivity.snack("Already downloading playlist...");
            }
        });
    }

    private void stopDownloads() {
        Playable playable = new Playable(mPlaylist, mActivity.mMainVM.getSongsFromPlaylist(mPlaylist.getId()));
        mActivity.getDownloadService(binder -> {
            if (binder.isDownloading(playable.getInfo().getId())) {
                binder.stopDownload(playable);
                mActivity.snack("Downloading stopped");
            } else {
                mActivity.snack("This playlist isn't being downloaded...?");
            }
        });
    }

    private void clearDownloads() {
        new MaterialAlertDialogBuilder(mActivity)
            .setTitle("Delete Downloads?")
            .setMessage("This will delete locally downloaded songs from the playlist!")
            .setNegativeButton("Cancel", (dialog, which) -> {
            })
            .setPositiveButton("Delete", (dialog, which) -> {
                for (Song song : mActivity.mMainVM.getSongsFromPlaylist(mPlaylist.getId())) {
                    song.deleteIfNotUsed(mActivity, mActivity.mMainVM.mySongs.getValue());
                }

                mActivity.snack("Songs deleted locally");
            })
            .show();

    }

    private void removeDownload() {
        mSong.deleteLocally(mActivity);
        mActivity.snack("Song deleted locally");
    }

    private void savePlaylist() {
        mPlaylist.setUserId(mActivity.mMainVM.userId);
        new SavePlaylistRequest(mPlaylist, new SavePlaylistRequest.Callback() {
            @Override
            public void onComplete(String playlistId) {
                mActivity.snack("Saved playlist");
                new Handler(Looper.getMainLooper()).post(mRunnable);
            }

            @Override
            public void onError(String message) {
                mActivity.warnError(new Exception(message));
            }
        });
    }

    private void playPlaylist() {
        List<Song> songs = mActivity.mMainVM.getSongsFromPlaylist(mPlaylist.getId());
        Playable playable = new Playable(mPlaylist, songs);
        mActivity.getPlayingService(service -> service.startPlayable(playable, songs.get(0).getSongId(), mActivity.mMainVM.myUser.getValue().getHighStreamQuality()));

        if (mActivity.mMainVM.myUser.getValue().getOpenPlayingScreen()) {
            mActivity.mNavController.navigate(R.id.fragment_playing);
        }
    }

    private void editPlaylist() {
        mActivity.mPlaylistEditVM.playlistId.setValue(mPlaylist.getId());
        mRunnable.run();
    }

    private void deletePlaylist() {
        new MaterialAlertDialogBuilder(mActivity)
            .setTitle("Delete Playlist?")
            .setMessage("This will delete the entire playlists at once!")
            .setNegativeButton("Cancel", (dialog, which) -> {
            })
            .setPositiveButton("Delete", (dialog, which) -> new PlaylistDeleteRequest(mPlaylist.getId(), new PlaylistDeleteRequest.Callback() {
                @Override
                public void onComplete(String response) {
                    mActivity.snack("Playlist deleted");
                    new Handler(Looper.getMainLooper()).post(mRunnable);
                }

                @Override
                public void onError(String message) {
                    mActivity.warnError(new Exception(message));
                }
            }))
            .show();
    }

    private void addPlaylist() {
        String id = mDb.collection("playlists").document().getId();
        Playlist playlist = new Playlist(
            id,
            "New Playlist",
            "https://firebasestorage.googleapis.com/v0/b/android-soundroid.appspot.com/o/playing_cover_default.png?alt=media&token=e8980e80-ab5d-4f21-8ed4-6bc6e7e06ef7",
            "#7b828b",
            mActivity.mMainVM.userId,
            new ArrayList<>()
        );

        mDb.collection("playlists")
            .document(id)
            .set(playlist.toMap())
            .addOnSuccessListener(snap -> {
                mActivity.mPlaylistEditVM.playlistId.setValue(id);
                mRunnable.run();
            })
            .addOnFailureListener(mActivity::warnError);
    }

    private void importPlaylist() {
        mRunnable.run();
    }

}
