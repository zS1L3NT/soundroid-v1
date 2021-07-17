package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.connection.ImportPlaylistRequest;
import com.zectan.soundroid.databinding.FragmentPlaylistImportBinding;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

public class PlaylistImportFragment extends Fragment<FragmentPlaylistImportBinding> {

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistImportBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Observers
        playlistImportVM.text.observe(this, B.urlTextInput::setText);
        playlistImportVM.loading.observe(this, this::onLoadingChange);

        B.importButton.setOnClickListener(this::onImportButtonClicked);

        return B.getRoot();
    }

    private void onImportButtonClicked(View view) {
        playlistImportVM.loading.setValue(true);
        Editable editable = B.urlTextInput.getText();
        if (editable == null) return;
        activity.hideKeyboard(B.getRoot());

        String url = editable.toString();
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            activity.snack("Invalid URL");
        }

        new ImportPlaylistRequest(url, mainVM.userId, new ImportPlaylistRequest.Callback() {
            @Override
            public void onComplete() {
                activity.snack("Adding songs to playlist...");
                playlistImportVM.text.postValue("");
                playlistImportVM.loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                activity.handleError(new Exception(message));
                playlistImportVM.loading.postValue(false);
            }
        });
    }

    private void onLoadingChange(Boolean loading) {
        B.progressbar.animate().alpha(loading ? 1 : 0).setDuration(500).start();
        B.importButton.setEnabled(!loading);
        B.urlTextInput.setEnabled(!loading);
    }
}
