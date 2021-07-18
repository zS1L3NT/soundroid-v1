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
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zectan.soundroid.R;
import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.connection.SongEditRequest;
import com.zectan.soundroid.databinding.FragmentSongEditBinding;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class SongEditFragment extends Fragment<FragmentSongEditBinding> {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
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

    public SongEditFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentSongEditBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Observers
        playlistEditVM.navigateNow.observe(this, this::onNavigateNowChange);
        playlistEditVM.saving.observe(this, this::onSavingChange);

        B.backImage.setOnClickListener(__ -> navController.navigateUp());
        B.saveImage.setOnClickListener(this::onSaveClicked);
        B.coverImage.setOnClickListener(this::onCoverClicked);

        Song song = songEditVM.song.getValue();
        B.titleTextInput.setText(song.getTitle());
        B.artisteTextInput.setText(song.getArtiste());
        Glide
            .with(activity)
            .load(song.getCover())
            .placeholder(R.drawable.playing_cover_loading)
            .error(R.drawable.playing_cover_failed)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);

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
        Song song = songEditVM.song.getValue();

        String newTitle;
        if (B.titleTextInput.getText() == null) {
            newTitle = song.getTitle();
        } else {
            newTitle = B.titleTextInput.getText().toString();
        }

        String newArtiste;
        if (B.artisteTextInput.getText() == null) {
            newArtiste = song.getArtiste();
        } else {
            newArtiste = B.artisteTextInput.getText().toString();
        }

        Song newSong = new Song(
            song.getSongId(),
            newTitle,
            newArtiste,
            song.getCover(),
            song.getColorHex(),
            song.getPlaylistId(),
            song.getUserId(),
            Utils.getQueries(newTitle)
        );

        StorageReference ref = storage.getReference().child(String.format("songs/%s.png", song.getSongId()));
        if (newFilePath != null) {
            ref.putFile(newFilePath)
                .addOnSuccessListener(snap -> ref.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        newSong.setCover(uri.toString());
                        sendColorHexRequest(newSong);
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
            sendColorHexRequest(newSong);
        }
    }

    private void sendColorHexRequest(Song song) {
        new SongEditRequest(song, new SongEditRequest.Callback() {
            @Override
            public void onComplete() {
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
