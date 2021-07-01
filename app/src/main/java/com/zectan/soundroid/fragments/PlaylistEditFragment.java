package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.PlaylistEditAdapter;
import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.connection.EditPlaylistRequest;
import com.zectan.soundroid.databinding.FragmentPlaylistEditBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.Anonymous;
import com.zectan.soundroid.utils.ListArrayUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class PlaylistEditFragment extends Fragment<FragmentPlaylistEditBinding> {
    private PlaylistEditAdapter playlistEditAdapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistEditBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler Views
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        playlistEditAdapter = new PlaylistEditAdapter(playlistViewVM.songs.getValue());
        B.recyclerView.setAdapter(playlistEditAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setOrientation(DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING);
        B.recyclerView.setReduceItemAlphaOnSwiping(true);

        // Live Observers
        playlistEditVM.info.observe(this, this::onInfoChange);
        playlistEditVM.songs.observe(this, this::onSongsChange);
        playlistEditVM.navigateNow.observe(this, this::onNavigateNowChange);
        playlistEditVM.saving.observe(this, this::onSavingChange);

        mainVM.watchInfoFromPlaylist(this, playlistEditVM.playlistId.getValue(), playlistEditVM.info::setValue);
        mainVM.watchSongsFromPlaylist(this, playlistEditVM.playlistId.getValue(), playlistEditVM.songs::setValue);
        B.backImage.setOnClickListener(__ -> activity.onBackPressed());
        B.saveImage.setOnClickListener(this::onSaveClicked);

        return B.getRoot();
    }

    public void onSaveClicked(View view) {
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
            Anonymous.getQueries(newName)
        );

        new EditPlaylistRequest(newInfo, new EditPlaylistRequest.Callback() {
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

    private void onSongsChange(List<Song> songs) {
        playlistEditAdapter.setDataSet(ListArrayUtils.sortSongs(songs, playlistEditVM.info.getValue().getOrder()));
    }

    private void onInfoChange(Info info) {
        B.nameTextInput.setText(info.getName());
        Glide
            .with(activity)
            .load(info.getCover())
            .placeholder(R.drawable.playing_cover_default)
            .error(R.drawable.playing_cover_default)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);
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