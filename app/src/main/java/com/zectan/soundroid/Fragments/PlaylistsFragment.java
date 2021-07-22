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
import com.zectan.soundroid.Models.Info;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.MenuBuilder;
import com.zectan.soundroid.databinding.FragmentPlaylistsBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PlaylistsFragment extends Fragment<FragmentPlaylistsBinding> {
    private static final String TAG = "(SounDroid) PlayingFragment";
    private final PlaylistsAdapter.Callback callback = new PlaylistsAdapter.Callback() {
        @Override
        public void onPlaylistClicked(Info info) {
            playlistViewVM.playlistId.setValue(info.getId());
            playlistViewVM.songs.setValue(new ArrayList<>());
            navController.navigate(PlaylistsFragmentDirections.openPlaylistView());
        }

        @Override
        public boolean onMenuItemClicked(Info info, MenuItem item) {
            return activity.handleMenuItemClick(info, null, item, menuItemRunnable(item));
        }
    };
    private PlaylistsAdapter playlistsAdapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistsBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        playlistsAdapter = new PlaylistsAdapter(callback);
        B.recyclerView.setAdapter(playlistsAdapter);
        B.recyclerView.setLayoutManager(layoutManager);

        // Observers
        playlistsVM.loading.observe(this, B.swipeRefresh::setRefreshing);
        mainVM.myInfos.observe(this, playlistsAdapter::updateInfos);

        B.moreImage.setOnClickListener(this::onMoreImageClicked);
        B.swipeRefresh.setOnRefreshListener(this::onReload);

        return B.getRoot();
    }

    private void onReload() {
        playlistsAdapter.updateInfos(mainVM.myInfos.getValue());
        playlistsVM.loading.postValue(false);
    }

    private void onMoreImageClicked(View view) {
        MenuBuilder.MenuItems items = new MenuBuilder.MenuItems();
        items.addPlaylist();
        items.importPlaylist();

        MenuBuilder.createMenu(view, items, null, (object, item) -> activity.handleMenuItemClick(null, null, item, menuItemRunnable(item)));
    }

    private Runnable menuItemRunnable(MenuItem item) {
        return () -> {
            switch (item.getItemId()) {
                case MenuBuilder.ADD_PLAYLIST:
                    navController.navigate(PlaylistsFragmentDirections.openPlaylistEdit());
                    break;
                case MenuBuilder.IMPORT_PLAYLIST:
                    navController.navigate(PlaylistsFragmentDirections.openPlaylistImport());
                    break;
            }
        };
    }

}