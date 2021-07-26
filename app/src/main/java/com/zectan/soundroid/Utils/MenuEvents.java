package com.zectan.soundroid.Utils;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.Connections.DeletePlaylistRequest;
import com.zectan.soundroid.Connections.SavePlaylistRequest;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.Info;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.Models.User;
import com.zectan.soundroid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MenuEvents {
    private final MainActivity mActivity;
    private final Info mInfo;
    private final Song mSong;
    private final MenuItem mItem;
    FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private Runnable mRunnable;

    public MenuEvents(MainActivity activity, Info info, Song song, MenuItem item) {
        mActivity = activity;
        mInfo = info;
        mSong = song;
        mItem = item;
    }

    public MenuEvents(MainActivity activity, Info info, Song song, MenuItem item, Runnable runnable) {
        mActivity = activity;
        mInfo = info;
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
        List<Info> infos = new ArrayList<>();
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(mActivity).setTitle("Add To Playlist");

        DialogInterface.OnClickListener onClickListener = (dialog_, i) -> {
            Info info = infos.get(i);

            AtomicInteger completed = new AtomicInteger(0);
            OnSuccessListener<Object> onSuccessListener = __ -> {
                if (completed.incrementAndGet() == 2) {
                    mActivity.snack("Song added");
                }
            };

            boolean inPlaylist = mActivity
                .mainVM
                .getSongsFromPlaylist(info.getId())
                .stream()
                .anyMatch(song -> song.getSongId().equals(mSong.getSongId()));

            if (inPlaylist) {
                mActivity.handleError(new Exception("Song already in playlist!"));
            } else {
                mSong.setPlaylistId(info.getId());
                mSong.setUserId(mActivity.mainVM.userId);
                mDb.collection("songs")
                    .add(mSong.toMap())
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(mActivity::handleError);
                mDb.collection("playlists")
                    .document(info.getId())
                    .update("order", FieldValue.arrayUnion(mSong.getSongId()))
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(mActivity::handleError);
            }
        };

        infos.addAll(mActivity.mainVM.myInfos.getValue());
        dialog.setItems(ListArrayUtils.toArray(CharSequence.class, infos.stream().map(Info::getName).collect(Collectors.toList())), onClickListener);
        dialog.show();
    }

    private void addToQueue() {
        mActivity.playingVM.addToQueue(mSong);
        mActivity.snack("Song added to queue");
    }

    private void editSong() {
        mActivity.songEditVM.song.setValue(mSong);
        mRunnable.run();
    }

    private void openQueue() {
        mRunnable.run();
    }

    private void clearQueue() {
        mActivity.playingVM.clearQueue(mActivity);
        mActivity.snack("Cleared queue");
    }

    private void startDownloads() {
        mActivity.getDownloadService(binder -> {
            Playlist playlist = new Playlist(mInfo, mActivity.mainVM.getSongsFromPlaylist(mInfo.getId()));

            User user = mActivity.mainVM.myUser.getValue();
            if (binder.startDownload(playlist, user.getHighDownloadQuality())) {
                mActivity.snack("Download starting");
            } else {
                mActivity.snack("Already downloading playlist...");
            }
        });
    }

    private void stopDownloads() {
        Playlist playlist = new Playlist(mInfo, mActivity.mainVM.getSongsFromPlaylist(mInfo.getId()));
        mActivity.getDownloadService(binder -> {
            if (binder.isDownloading(playlist.getInfo().getId())) {
                binder.stopDownload(playlist);
                mActivity.snack("Download stopped");
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
                for (Song song : mActivity.mainVM.getSongsFromPlaylist(mInfo.getId())) {
                    song.deleteIfNotUsed(mActivity, mActivity.mainVM.mySongs.getValue());
                }

                mActivity.snack("Songs deleted");
            })
            .show();

    }

    private void removeDownload() {
        mSong.deleteLocally(mActivity);
        mActivity.snack("Song deleted locally");
    }

    private void savePlaylist() {
        mInfo.setUserId(mActivity.mainVM.userId);
        new SavePlaylistRequest(mInfo, new SavePlaylistRequest.Callback() {
            @Override
            public void onComplete(String playlistId) {
                mActivity.snack("Saved playlist");
                new Handler(Looper.getMainLooper()).post(mRunnable);
            }

            @Override
            public void onError(String message) {
                mActivity.handleError(new Exception(message));
            }
        });
    }

    private void playPlaylist() {
        List<Song> songs = mActivity.mainVM.getSongsFromPlaylist(mInfo.getId());
        Playlist playlist = new Playlist(mInfo, songs);
        mActivity.playingVM.startPlaylist(mActivity, playlist, songs.get(0).getSongId(), mActivity.mainVM.myUser.getValue().getHighStreamQuality());

        if (mActivity.mainVM.myUser.getValue().getOpenPlayingScreen()) {
            mActivity.navController.navigate(R.id.fragment_playing);
        }
    }

    private void editPlaylist() {
        mActivity.playlistEditVM.playlistId.setValue(mInfo.getId());
        mRunnable.run();
    }

    private void deletePlaylist() {
        new MaterialAlertDialogBuilder(mActivity)
            .setTitle("Delete Playlist?")
            .setMessage("This will delete the entire playlists at once!")
            .setNegativeButton("Cancel", (dialog, which) -> {
            })
            .setPositiveButton("Delete", (dialog, which) -> new DeletePlaylistRequest(mInfo.getId(), new DeletePlaylistRequest.Callback() {
                @Override
                public void onComplete(String response) {
                    mActivity.snack("Playlist deleted");
                    new Handler(Looper.getMainLooper()).post(mRunnable);
                }

                @Override
                public void onError(String message) {
                    mActivity.handleError(new Exception(message));
                }
            }))
            .show();
    }

    private void addPlaylist() {
        String id = mDb.collection("playlists").document().getId();
        Info info = new Info(
            id,
            "New Playlist",
            "https://firebasestorage.googleapis.com/v0/b/android-soundroid.appspot.com/o/playing_cover_default.png?alt=media&token=e8980e80-ab5d-4f21-8ed4-6bc6e7e06ef7",
            "#7b828b",
            mActivity.mainVM.userId,
            new ArrayList<>(),
            Utils.getQueries("New Playlist")
        );

        mDb.collection("playlists")
            .document(id)
            .set(info.toMap())
            .addOnSuccessListener(snap -> {
                mActivity.snack("Created Playlist");
                mActivity.playlistEditVM.playlistId.setValue(id);
                mRunnable.run();
            })
            .addOnFailureListener(mActivity::handleError);
    }

    private void importPlaylist() {
        mRunnable.run();
    }

}
