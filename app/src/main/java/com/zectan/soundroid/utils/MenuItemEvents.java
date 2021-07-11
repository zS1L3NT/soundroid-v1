package com.zectan.soundroid.utils;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.connection.DeletePlaylistRequest;
import com.zectan.soundroid.connection.SavePlaylistRequest;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MenuItemEvents {
    private static final String USER_ID = "admin";
    private final MainActivity mActivity;
    private final Info mInfo;
    private final Song mSong;
    private final MenuItem mItem;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Runnable mOpenEditPlaylist;

    public MenuItemEvents(MainActivity activity, Info info, Song song, MenuItem item) {
        mActivity = activity;
        mInfo = info;
        mSong = song;
        mItem = item;
    }

    public MenuItemEvents(MainActivity activity, Info info, Song song, MenuItem item, Runnable openEditPlaylist) {
        mActivity = activity;
        mInfo = info;
        mSong = song;
        mItem = item;
        mOpenEditPlaylist = openEditPlaylist;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean handle() {
        switch (mItem.getItemId()) {
            case R.id.add_to_playlist:
                addToPlaylist();
                break;
            case R.id.add_to_queue:
                addToQueue();
                break;
            case R.id.play_playlist:
                playPlaylist();
                break;
            case R.id.save_playlist:
                savePlaylist();
                break;
            case R.id.download_playlist:
                downloadPlaylist();
                break;
            case R.id.clear_downloads:
                deleteDownloads();
                break;
            case R.id.edit_playlist:
                editPlaylist();
                break;
            case R.id.delete_playlist:
                deletePlaylist();
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

            db.collection("playlists")
                .document(info.getId())
                .update("order", FieldValue.arrayUnion(mSong.getSongId()))
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(mActivity::handleError);

            boolean inPlaylist = mActivity
                .mainVM
                .mySongs
                .getValue()
                .stream()
                .anyMatch(song -> song.getSongId().equals(mSong.getSongId()));

            if (inPlaylist) {
                mActivity.handleError(new Exception("Song already in playlist!"));
            } else {
                mSong.setPlaylistId(info.getId());
                mSong.setUserId(USER_ID);
                db.collection("songs")
                    .add(mSong.toMap())
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

    private void playPlaylist() {
        List<Song> songs = mActivity.mainVM.getSongsFromPlaylist(mInfo.getId());
        Playlist playlist = new Playlist(mInfo, songs);
        mActivity.playingVM.startPlaylist(mActivity, playlist, songs.get(0).getSongId(), mActivity.mainVM.myUser.getValue().getHighStreamQuality());

        if (mActivity.mainVM.myUser.getValue().getOpenPlayingScreen()) {
            mActivity.navController.navigate(R.id.fragment_playing);
        }
    }

    private void savePlaylist() {
        mInfo.setUserId(USER_ID);
        new SavePlaylistRequest(mInfo, new SavePlaylistRequest.Callback() {
            @Override
            public void onComplete() {
                mActivity.snack("Saved playlist");
            }

            @Override
            public void onError(String message) {
                mActivity.handleError(new Exception(message));
            }
        });
    }

    private void downloadPlaylist() {
        new DownloadPlaylist(mActivity, mInfo, mActivity.mainVM.myUser.getValue().getHighDownloadQuality());
    }

    private void deleteDownloads() {
        List<Song> songs = mActivity.mainVM.getSongsFromPlaylist(mInfo.getId());

        for (Song song : songs) {
            song.deleteLocally(mActivity);
        }

        mActivity.snack("Songs deleted");
    }

    private void editPlaylist() {
        mActivity.playlistEditVM.playlistId.setValue(mInfo.getId());
        mOpenEditPlaylist.run();
    }

    private void deletePlaylist() {
        new DeletePlaylistRequest(mInfo.getId(), new DeletePlaylistRequest.Callback() {
            @Override
            public void onComplete() {
                mActivity.snack("Playlist deleted");
            }

            @Override
            public void onError(String message) {
                mActivity.handleError(new Exception(message));
            }
        });
    }

}
