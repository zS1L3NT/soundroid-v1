package com.zectan.soundroid.Fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.Adapters.PlaylistViewAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Connections.PlaylistSongsRequest;
import com.zectan.soundroid.Models.Info;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.ListArrayUtils;
import com.zectan.soundroid.Utils.MenuBuilder;
import com.zectan.soundroid.databinding.FragmentPlaylistViewBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaylistViewFragment extends Fragment<FragmentPlaylistViewBinding> {
    private Boolean isLocal;

    private final PlaylistViewAdapter.Callback callback = new PlaylistViewAdapter.Callback() {

        @Override
        public void onSongClicked(String songId) {
            Playlist playlist = new Playlist(mPlaylistViewVM.info.getValue(), mPlaylistViewVM.songs.getValue());
            mPlayingVM.startPlaylist(mActivity, playlist, songId, mMainVM.myUser.getValue().getHighStreamQuality());

            if (mMainVM.myUser.getValue().getOpenPlayingScreen()) {
                mNavController.navigate(PlaylistViewFragmentDirections.openPlaying());
            }
        }

        @Override
        public boolean onMenuItemClicked(Song song, MenuItem item) {
            return mActivity.handleMenuItemClick(mPlaylistViewVM.info.getValue(), song, item, () -> mNavController.navigate(PlaylistViewFragmentDirections.openEditSong()));
        }

        @Override
        public boolean isLocal() {
            return isLocal;
        }
    };
    private PlaylistViewAdapter playlistViewAdapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistViewBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        playlistViewAdapter = new PlaylistViewAdapter(callback);
        B.recyclerView.setAdapter(playlistViewAdapter);
        B.recyclerView.setLayoutManager(layoutManager);

        // Live Observers
        mPlaylistViewVM.info.observe(this, this::onInfoChange);
        mPlaylistViewVM.songs.observe(this, this::onSongsChange);
        mPlaylistViewVM.loading.observe(this, B.swipeRefresh::setRefreshing);
        mPlayingVM.currentSong.observe(this, playlistViewAdapter::updateCurrentSong);

        mMainVM.watchInfoFromPlaylist(this, mPlaylistViewVM.playlistId.getValue(), mPlaylistViewVM.info::setValue);
        mMainVM.watchSongsFromPlaylist(this, mPlaylistViewVM.playlistId.getValue(), mPlaylistViewVM.songs::setValue);
        B.backImage.setOnClickListener(__ -> mNavController.navigateUp());
        B.moreImage.setOnClickListener(this::onMoreImageClicked);
        B.swipeRefresh.setOnRefreshListener(this::onReload);
        B.parent.setTransitionListener(mActivity.getTransitionListener());

        onReload();

        return B.getRoot();
    }

    public void onReload() {
        String playlistId = mPlaylistViewVM.playlistId.getValue();
        mPlaylistViewVM.loading.setValue(true);

        Info info = mMainVM.getInfoFromPlaylist(playlistId);
        isLocal = info != null;
        if (info == null) {
            fetchFromServer();
        } else {
            mPlaylistViewVM.info.setValue(info);
            mPlaylistViewVM.loading.setValue(false);
            mPlaylistViewVM.songs.setValue(mMainVM.getSongsFromPlaylist(playlistId));
            B.recyclerView.scrollToPosition(0);
        }
    }

    public void fetchFromServer() {
        new PlaylistSongsRequest(mPlaylistViewVM.playlistId.getValue(), new PlaylistSongsRequest.Callback() {
            @Override
            public void onComplete(List<Song> songs) {
                mPlaylistViewVM.loading.postValue(false);
                mPlaylistViewVM.songs.postValue(songs);
            }

            @Override
            public void onError(String message) {
                mPlaylistViewVM.loading.postValue(false);
                mMainVM.error.postValue(new Exception(message));
            }
        });
    }

    public void menuItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case MenuBuilder.EDIT_PLAYLIST:
                mNavController.navigate(PlaylistViewFragmentDirections.openPlaylistEdit());
                break;
            case MenuBuilder.DELETE_PLAYLIST:
            case MenuBuilder.SAVE_PLAYLIST:
                mNavController.navigate(PlaylistViewFragmentDirections.openPlaylists());
                break;
        }
    }

    public void onMoreImageClicked(View view) {
        if (isLocal == null) return;

        Info info = mPlaylistViewVM.info.getValue();
        List<Song> songs = mActivity.mainVM.getSongsFromPlaylist(info.getId());
        Playlist playlist = new Playlist(info, songs);

        MenuBuilder.MenuItems items = new MenuBuilder.MenuItems();
        if (isLocal) {
            items = MenuBuilder.MenuItems.forPlaylist(playlist, mActivity);
        } else {
            items.savePlaylist();
        }

        MenuBuilder.createMenu(
            view,
            items,
            mPlaylistViewVM.info.getValue(),
            (info_, item) -> mActivity.handleMenuItemClick(info_, null, item, () -> menuItemClicked(item))
        );
    }

    private void onSongsChange(List<Song> songs) {
        playlistViewAdapter.updateSongs(ListArrayUtils.sortSongs(songs, mPlaylistViewVM.info.getValue().getOrder()));
    }

    private void onInfoChange(Info info) {
        mActivity.getWindow().setStatusBarColor(Color.parseColor(info.getColorHex()));
        B.toolbarBackground.setBackgroundColor(Color.parseColor(info.getColorHex()));
        B.nameText.setText(info.getName());
        Glide
            .with(mActivity)
            .load(info.getCover())
            .placeholder(R.drawable.playing_cover_loading)
            .error(R.drawable.playing_cover_failed)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);

        Drawable oldGD = B.background.getBackground();
        int[] colors = {Color.parseColor(info.getColorHex()), mActivity.getAttributeResource(R.attr.backgroundColor)};
        GradientDrawable newGD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);

        Drawable[] layers = {oldGD, newGD};
        TransitionDrawable transition = new TransitionDrawable(layers);
        B.background.setBackground(transition);
        transition.startTransition(1000);
    }
}