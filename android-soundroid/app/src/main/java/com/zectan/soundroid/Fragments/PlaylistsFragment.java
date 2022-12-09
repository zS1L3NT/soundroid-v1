package com.zectan.soundroid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.Adapters.PlaylistsAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Utils.MenuBuilder;
import com.zectan.soundroid.databinding.FragmentPlaylistsBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PlaylistsFragment extends Fragment<FragmentPlaylistsBinding> {
    private static final String TAG = "(SounDroid) PlayingFragment";
    private final PlaylistsAdapter.Callback callback = new PlaylistsAdapter.Callback() {
        @Override
        public void onPlaylistClicked(Playlist playlist) {
            mPlaylistViewVM.playlistId.setValue(playlist.getId());
            mPlaylistViewVM.songs.setValue(new ArrayList<>());
            mNavController.navigate(PlaylistsFragmentDirections.openPlaylistView());
        }

        @Override
        public boolean onMenuItemClicked(Playlist playlist, MenuItem item) {
            return mActivity.handleMenuItemClick(playlist, null, item, menuItemRunnable(item));
        }
    };
    private PlaylistsAdapter playlistsAdapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistsBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        playlistsAdapter = new PlaylistsAdapter(callback);
        B.recyclerView.setAdapter(playlistsAdapter);
        B.recyclerView.setLayoutManager(layoutManager);

        // Listeners
        mPlaylistsVM.loading.observe(this, B.swipeRefresh::setRefreshing);
        mMainVM.myPlaylists.observe(this, playlistsAdapter::updateInfos);
        B.moreImage.setOnClickListener(this::onMoreImageClicked);
        B.swipeRefresh.setOnRefreshListener(this::onReload);

        return B.getRoot();
    }

    private void onReload() {
        playlistsAdapter.updateInfos(mMainVM.myPlaylists.getValue());
        mPlaylistsVM.loading.postValue(false);
    }

    private void onMoreImageClicked(View view) {
        MenuBuilder.MenuItems items = new MenuBuilder.MenuItems();
        items.addPlaylist();
        items.importPlaylist();

        MenuBuilder.createMenu(view, items, null, (object, item) -> mActivity.handleMenuItemClick(null, null, item, menuItemRunnable(item)));
    }

    /**
     * Callback for click of menu item
     *
     * @param item Menu item
     * @return Runnable
     */
    private Runnable menuItemRunnable(MenuItem item) {
        return () -> {
            switch (item.getItemId()) {
                case MenuBuilder.ADD_PLAYLIST:
                case MenuBuilder.EDIT_PLAYLIST:
                    mNavController.navigate(PlaylistsFragmentDirections.openPlaylistEdit());
                    break;
                case MenuBuilder.IMPORT_PLAYLIST:
                    mNavController.navigate(PlaylistsFragmentDirections.openPlaylistImport());
                    break;
            }
        };
    }

}