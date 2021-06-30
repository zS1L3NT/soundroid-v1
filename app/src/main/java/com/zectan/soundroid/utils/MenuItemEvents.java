package com.zectan.soundroid.utils;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.connection.DeletePlaylistRequest;
import com.zectan.soundroid.connection.SavePlaylistRequest;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MenuItemEvents {
    private final MainActivity mActivity;
    private final FirebaseRepository repository;
    private final Info mInfo;
    private final Song mSong;
    private final MenuItem mItem;
    private Runnable mOpenEditPlaylist;

    public MenuItemEvents(MainActivity activity, Info info, Song song, MenuItem item) {
        mActivity = activity;
        mInfo = info;
        mSong = song;
        mItem = item;
        repository = activity.getRepository();
    }

    public MenuItemEvents(MainActivity activity, Info info, Song song, MenuItem item, Runnable openEditPlaylist) {
        mActivity = activity;
        mInfo = info;
        mSong = song;
        mItem = item;
        repository = activity.getRepository();
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
            case R.id.remove_from_playlist:
                removeFromPlaylist();
                break;
            case R.id.add_to_playlists:
                addToPlaylists();
                break;
            case R.id.download_playlist:
                downloadPlaylist();
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

            repository
                .playlist(info.getId())
                .update("order", FieldValue.arrayUnion(mSong.getSongId()))
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(mActivity::handleError);

            repository
                .songsCollection()
                .whereEqualTo("songId", mSong.getSongId())
                .whereEqualTo("playlistId", info.getId())
                .get()
                .addOnSuccessListener(snaps -> {
                    if (snaps.size() == 0) {
                        mSong.setPlaylistId(info.getId());
                        mSong.setUserId(FirebaseRepository.USER_ID);
                        repository
                            .songsCollection()
                            .add(mSong.toMap())
                            .addOnSuccessListener(onSuccessListener)
                            .addOnFailureListener(mActivity::handleError);
                    } else {
                        mActivity.handleError(new Exception("Song already in playlist!"));
                    }
                });


        };

        repository
            .playlists(FirebaseRepository.USER_ID)
            .get()
            .addOnSuccessListener(snaps -> {
                infos.clear();
                infos.addAll(snaps.toObjects(Info.class));
                dialog.setItems(ListArrayUtils.toArray(CharSequence.class, infos.stream().map(Info::getName).collect(Collectors.toList())), onClickListener);
                dialog.show();
            })
            .addOnFailureListener(mActivity::handleError);
    }

    private void addToQueue() {
        mActivity.playingVM.addToQueue(mSong);
        mActivity.snack("Song added to queue");
    }

    private void removeFromPlaylist() {
        AtomicInteger completed = new AtomicInteger(0);
        OnSuccessListener<Object> onSuccessListener = __ -> {
            if (completed.incrementAndGet() == 2) {
                mActivity.snack("Song removed");
                mActivity.playlistViewVM.reload(mActivity::handleError);
            }
        };

        repository
            .playlist(mInfo.getId())
            .update("order", FieldValue.arrayRemove(mSong.getSongId()))
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener(mActivity::handleError);

        repository
            .playlistSongs(mInfo.getId())
            .whereEqualTo("songId", mSong.getSongId())
            .get()
            .addOnSuccessListener(snaps -> {
                if (snaps.size() > 0) {
                    DocumentSnapshot snap = snaps.getDocuments().get(0);
                    repository
                        .songsCollection()
                        .document(snap.getId())
                        .delete()
                        .addOnSuccessListener(onSuccessListener)
                        .addOnFailureListener(mActivity::handleError);
                } else {
                    mActivity.handleError(new Exception("Document not found"));
                }
            })
            .addOnFailureListener(mActivity::handleError);
    }

    private void addToPlaylists() {
        new SavePlaylistRequest(mInfo, FirebaseRepository.USER_ID, new SavePlaylistRequest.Callback() {
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
        // TODO Finish this callback
    }

    private void editPlaylist() {
        mActivity.playlistEditVM.info.setValue(mInfo);
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
