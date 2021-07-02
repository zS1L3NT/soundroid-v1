package com.zectan.soundroid.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.navigation.NavDirections;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.PlaylistViewAdapter;
import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.connection.PlaylistSongsRequest;
import com.zectan.soundroid.databinding.FragmentPlaylistViewBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.Anonymous;
import com.zectan.soundroid.utils.ListArrayUtils;
import com.zectan.soundroid.utils.MenuItemsBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaylistViewFragment extends Fragment<FragmentPlaylistViewBinding> {
    private final PlaylistViewAdapter.Callback callback = new PlaylistViewAdapter.Callback() {

        @Override
        public void onSongClicked(ImageView cover, String transitionName, String songId) {
            FragmentNavigator.Extras extras = Anonymous.makeExtras(cover, transitionName);
            NavDirections action = PlaylistViewFragmentDirections.openPlaylistSong().setTransitionName(transitionName);
            NavHostFragment.findNavController(PlaylistViewFragment.this).navigate(action, extras);

            Playlist playlist = new Playlist(playlistViewVM.info.getValue(), playlistViewVM.songs.getValue());
            playingVM.startPlaylist(playlist, songId);
        }

        @Override
        public boolean onMenuItemClicked(Song song, MenuItem item) {
            return activity.handleMenuItemClick(playlistViewVM.info.getValue(), song, item, () -> {
                NavDirections action = PlaylistViewFragmentDirections.openEditPlaylist();
                NavHostFragment.findNavController(PlaylistViewFragment.this).navigate(action);
            });
        }
    };
    private PlaylistViewAdapter playlistViewAdapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistViewBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        playlistViewAdapter = new PlaylistViewAdapter(callback);
        B.recyclerView.setAdapter(playlistViewAdapter);
        B.recyclerView.setLayoutManager(layoutManager);

        // Live Observers
        playlistViewVM.info.observe(this, this::onInfoChange);
        playlistViewVM.songs.observe(this, this::onSongsChange);
        playlistViewVM.loading.observe(this, B.swipeRefresh::setRefreshing);

        mainVM.watchInfoFromPlaylist(this, playlistViewVM.playlistId.getValue(), playlistViewVM.info::setValue);
        mainVM.watchSongsFromPlaylist(this, playlistViewVM.playlistId.getValue(), playlistViewVM.songs::setValue);
        B.backImage.setOnClickListener(__ -> navController.navigateUp());
        B.moreImage.setOnClickListener(this::onMoreImageClicked);
        B.swipeRefresh.setOnRefreshListener(this::onReload);

        onReload();

        return B.getRoot();
    }

    public void onReload() {
        String playlistId = playlistViewVM.playlistId.getValue();
        playlistViewVM.loading.setValue(true);

        Info info = mainVM.getInfoFromPlaylist(playlistId);
        if (info == null) {
            fetchFromServer();
        } else {
            playlistViewVM.info.setValue(info);
            playlistViewVM.loading.setValue(false);
            playlistViewVM.songs.setValue(mainVM.getSongsFromPlaylist(playlistId));
            B.recyclerView.scrollToPosition(0);
        }
    }

    public void fetchFromServer() {
        new PlaylistSongsRequest(playlistViewVM.playlistId.getValue(), new PlaylistSongsRequest.Callback() {
            @Override
            public void onComplete(List<Song> songs) {
                playlistViewVM.loading.postValue(false);
                playlistViewVM.songs.postValue(songs);
            }

            @Override
            public void onError(String message) {
                playlistViewVM.loading.postValue(false);
                mainVM.error.postValue(new Exception(message));
            }
        });
    }

    public void onMoreImageClicked(View view) {
        MenuItemsBuilder.createMenu(
            view,
            R.menu.playlist_menu_playlists,
            playlistViewVM.info.getValue(),
            (info, item) -> activity.handleMenuItemClick(info, null, item, () -> {
                NavDirections action = PlaylistViewFragmentDirections.openEditPlaylist();
                NavHostFragment.findNavController(PlaylistViewFragment.this).navigate(action);
            })
        );
    }

    private void onSongsChange(List<Song> songs) {
        playlistViewAdapter.updateSongs(ListArrayUtils.sortSongs(songs, playlistViewVM.info.getValue().getOrder()));
    }

    private void onInfoChange(Info info) {
        B.nameText.setText(info.getName());
        Glide
            .with(activity)
            .load(info.getCover())
            .placeholder(R.drawable.playing_cover_default)
            .error(R.drawable.playing_cover_default)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);

        Drawable oldGD = B.background.getBackground();
        int[] colors = {Color.parseColor(info.getColorHex()), activity.getAttributeResource(R.attr.backgroundColor)};
        GradientDrawable newGD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);

        Drawable[] layers = {oldGD, newGD};
        TransitionDrawable transition = new TransitionDrawable(layers);
        B.background.setBackground(transition);
        transition.startTransition(1000);
    }
}