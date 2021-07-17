package com.zectan.soundroid.fragments;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.PlaylistEditAdapter;
import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.connection.EditPlaylistRequest;
import com.zectan.soundroid.databinding.FragmentPlaylistEditBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.ListArrayUtils;
import com.zectan.soundroid.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;

public class PlaylistEditFragment extends Fragment<FragmentPlaylistEditBinding> {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final List<String> removed = new ArrayList<>();
    private PlaylistEditAdapter playlistEditAdapter;
    private Uri newFilePath;
    private final ActivityResultLauncher<Intent> chooseCoverImage = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                newFilePath = result.getData().getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), newFilePath);
                    B.coverImage.setImageBitmap(bitmap);
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
        playlistEditAdapter = new PlaylistEditAdapter(removed::add);
        B.recyclerView.setAdapter(playlistEditAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setOrientation(DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING);
        B.recyclerView.setReduceItemAlphaOnSwiping(true);

        // Live Observers
        playlistEditVM.navigateNow.observe(this, this::onNavigateNowChange);
        playlistEditVM.saving.observe(this, this::onSavingChange);

        B.backImage.setOnClickListener(__ -> navController.navigateUp());
        B.saveImage.setOnClickListener(this::onSaveClicked);
        B.coverImage.setOnClickListener(this::onCoverClicked);

        Info info = mainVM.getInfoFromPlaylist(playlistEditVM.playlistId.getValue());
        List<Song> songs = mainVM.getSongsFromPlaylist(playlistEditVM.playlistId.getValue());
        assert info != null;
        playlistEditVM.info.setValue(info);
        playlistEditVM.songs.setValue(songs);

        B.nameTextInput.setText(info.getName());
        Glide
            .with(activity)
            .load(info.getCover())
            .placeholder(R.drawable.playing_cover_default)
            .error(R.drawable.playing_cover_default)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);
        playlistEditAdapter.setDataSet(ListArrayUtils.sortSongs(songs, playlistEditVM.info.getValue().getOrder()));
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
            .getDataSet()
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

        StorageReference ref = storage.getReference().child(String.format("covers/%s.png", info.getId()));
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