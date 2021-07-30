package com.zectan.soundroid.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Connections.SongEditRequest;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.FragmentSongEditBinding;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SongEditFragment extends Fragment<FragmentSongEditBinding> {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private Uri newFilePath;
    private final ActivityResultLauncher<Intent> chooseCoverImage = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                newFilePath = result.getData().getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), newFilePath);
                    Glide
                        .with(mActivity)
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
        mPlaylistEditVM.saving.observe(this, this::onSavingChange);

        B.backImage.setOnClickListener(__ -> mNavController.navigateUp());
        B.saveImage.setOnClickListener(this::onSaveClicked);
        B.coverImage.setOnClickListener(this::onCoverClicked);

        Song song = mSongEditVM.song.getValue();
        B.titleTextInput.setText(song.getTitle());
        B.artisteTextInput.setText(song.getArtiste());
        Glide
            .with(mActivity)
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
        mActivity.hideKeyboard(requireView());
    }

    private void onCoverClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        chooseCoverImage.launch(intent);
    }

    private void onSaveClicked(View view) {
        mPlaylistEditVM.saving.setValue(true);
        Song song = mSongEditVM.song.getValue();

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
            song.getUserId()
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
                        mPlaylistEditVM.saving.postValue(false);
                        mMainVM.error.postValue(error);
                    }))
                .addOnFailureListener(error -> {
                    mPlaylistEditVM.saving.postValue(false);
                    mMainVM.error.postValue(error);
                });
        } else {
            sendColorHexRequest(newSong);
        }
    }

    private void sendColorHexRequest(Song song) {
        new SongEditRequest(song, new SongEditRequest.Callback() {
            @Override
            public void onComplete(String response) {
                mPlaylistEditVM.saving.postValue(false);
                new Handler(Looper.getMainLooper()).post(mActivity::onBackPressed);
            }

            @Override
            public void onError(String message) {
                mMainVM.error.postValue(new Exception(message));
                mPlaylistEditVM.saving.postValue(false);
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

}
