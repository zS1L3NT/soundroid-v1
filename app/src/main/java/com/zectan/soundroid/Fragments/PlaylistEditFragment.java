package com.zectan.soundroid.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zectan.soundroid.Adapters.PlaylistEditAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Connections.EditPlaylistRequest;
import com.zectan.soundroid.Models.Info;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.ListArrayUtils;
import com.zectan.soundroid.Utils.Utils;
import com.zectan.soundroid.databinding.FragmentPlaylistEditBinding;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;

public class PlaylistEditFragment extends Fragment<FragmentPlaylistEditBinding> {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final List<String> removed = new ArrayList<>();
    private final PlaylistEditAdapter.Callback callback = new PlaylistEditAdapter.Callback() {
        @Override
        public void onRemove(String songId) {
            removed.add(songId);
        }

        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            mItemTouchHelper.startDrag(viewHolder);
        }
    };
    private PlaylistEditAdapter playlistEditAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private Uri newFilePath;
    private final ActivityResultLauncher<Intent> chooseCoverImage = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                newFilePath = result.getData().getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), newFilePath);
                    Glide
                        .with(activity)
                        .load(bitmap)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .centerCrop()
                        .into(B.coverImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    );

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistEditBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler Views
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        playlistEditAdapter = new PlaylistEditAdapter(callback);
        mItemTouchHelper = new ItemTouchHelper(new PlaylistEditAdapter.PlaylistEditItemTouchHelper(playlistEditAdapter));
        B.recyclerView.setAdapter(playlistEditAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        mItemTouchHelper.attachToRecyclerView(B.recyclerView);

        // Live Observers
        playlistEditVM.navigateNow.observe(this, this::onNavigateNowChange);
        playlistEditVM.saving.observe(this, this::onSavingChange);

        B.backImage.setOnClickListener(__ -> navController.navigateUp());
        B.saveImage.setOnClickListener(this::onSaveClicked);
        B.coverImage.setOnClickListener(this::onCoverClicked);
        B.parent.setTransitionListener(activity.getTransitionListener());

        Info info = mainVM.getInfoFromPlaylist(playlistEditVM.playlistId.getValue());
        List<Song> songs = mainVM.getSongsFromPlaylist(playlistEditVM.playlistId.getValue());
        assert info != null;
        playlistEditVM.info.setValue(info);
        playlistEditVM.songs.setValue(songs);

        B.nameTextInput.setText(info.getName());
        Glide
            .with(activity)
            .load(info.getCover())
            .placeholder(R.drawable.playing_cover_loading)
            .error(R.drawable.playing_cover_failed)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);
        playlistEditAdapter.updateSongs(ListArrayUtils.sortSongs(songs, playlistEditVM.info.getValue().getOrder()));
        removed.clear();

        return B.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        activity.hideKeyboard(requireView());
    }

    private void onCoverClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        chooseCoverImage.launch(intent);
    }

    private void onSaveClicked(View view) {
        playlistEditVM.saving.setValue(true);
        Info info = playlistEditVM.info.getValue();
        List<String> order = playlistEditAdapter
            .getSongs()
            .stream()
            .map(Song::getSongId)
            .collect(Collectors.toList());

        String newName;
        if (B.nameTextInput.getText() == null) {
            newName = info.getName();
        } else {
            newName = B.nameTextInput.getText().toString();
        }

        Info newInfo = new Info(
            info.getId(),
            newName,
            info.getCover(),
            info.getColorHex(),
            info.getUserId(),
            order,
            Utils.getQueries(newName)
        );

        StorageReference ref = storage.getReference().child(String.format("playlists/%s.png", info.getId()));
        if (newFilePath != null) {
            ref.putFile(newFilePath)
                .addOnSuccessListener(snap -> ref.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        newInfo.setCover(uri.toString());
                        sendEditPlaylistRequest(newInfo);
                    })
                    .addOnFailureListener(error -> {
                        playlistEditVM.saving.postValue(false);
                        mainVM.error.postValue(error);
                    }))
                .addOnFailureListener(error -> {
                    playlistEditVM.saving.postValue(false);
                    mainVM.error.postValue(error);
                });
        } else {
            sendEditPlaylistRequest(newInfo);
        }
    }

    private void sendEditPlaylistRequest(Info info) {
        new EditPlaylistRequest(info, removed, new EditPlaylistRequest.Callback() {
            @Override
            public void onComplete() {
                playlistEditVM.songs
                    .getValue()
                    .stream()
                    .filter(song -> removed.contains(song.getSongId()))
                    .forEach(song -> song.deleteIfNotUsed(activity, mainVM.mySongs.getValue()));

                playlistEditVM.navigateNow.postValue(1);
                playlistEditVM.saving.postValue(false);
            }

            @Override
            public void onError(String message) {
                mainVM.error.postValue(new Exception(message));
                playlistEditVM.saving.postValue(false);
            }
        });
    }

    private void onSavingChange(Boolean saving) {
        B.saveImage.setEnabled(!saving);
        if (saving) {
            B.saveImage.setAlpha(0f);
            B.loadingCircle.setAlpha(1f);
        } else {
            B.saveImage.setAlpha(1f);
            B.loadingCircle.setAlpha(0f);
        }
    }

    private void onNavigateNowChange(Integer i) {
        if (i == 0) return;
        playlistEditVM.navigateNow.postValue(0);
        activity.onBackPressed();
    }
}