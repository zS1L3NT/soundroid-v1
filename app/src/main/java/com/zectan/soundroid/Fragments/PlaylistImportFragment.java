package com.zectan.soundroid.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Connections.PlaylistImportRequest;
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

        // Listeners
        mPlaylistImportVM.text.observe(this, B.urlTextInput::setText);
        mPlaylistImportVM.loading.observe(this, this::onLoadingChange);
        B.importButton.setOnClickListener(this::onImportButtonClicked);

        return B.getRoot();
    }

    private void onImportButtonClicked(View view) {
        mPlaylistImportVM.loading.setValue(true);
        Editable editable = B.urlTextInput.getText();
        if (editable == null) return;
        mActivity.hideKeyboard(B.getRoot());

        // Check if URL is valid
        String url = editable.toString();
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            mActivity.snack("Invalid URL");
            return;
        }

        new PlaylistImportRequest(url, mMainVM.userId, new PlaylistImportRequest.Callback() {
            @Override
            public void onComplete(String response) {
                mActivity.snack("Adding songs to playlist...");
                mPlaylistImportVM.text.postValue("");
                mPlaylistImportVM.loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                mActivity.warnError(new Exception(message));
                mPlaylistImportVM.loading.postValue(false);
            }
        });
    }

    private void onLoadingChange(Boolean loading) {
        B.progressbar.animate().alpha(loading ? 1 : 0).setDuration(500).start();
        B.importButton.setEnabled(!loading);
        B.urlTextInput.setEnabled(!loading);
    }
}
