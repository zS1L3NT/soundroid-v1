package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.zectan.soundroid.adapters.PlaylistViewAdapter;
import com.zectan.soundroid.databinding.FragmentPlaylistViewBinding;
import com.zectan.soundroid.objects.Anonymous;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.sockets.PlaylistLookupSocket;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaylistViewFragment extends Fragment<FragmentPlaylistViewBinding> {
    private PlaylistViewAdapter playlistViewAdapter;

    private final PlaylistViewAdapter.Callback callback = new PlaylistViewAdapter.Callback() {
        @Override
        public void onSongClicked(ImageView cover, String transitionName, int position) {
            FragmentNavigator.Extras extras = Anonymous.makeExtras(cover, transitionName);
            NavDirections action = PlaylistViewFragmentDirections.openPlaylistSong().setTransitionName(transitionName);
            NavHostFragment.findNavController(PlaylistViewFragment.this).navigate(action, extras);

            PlaylistInfo info = playlistViewVM.info.getValue();
            List<Song> songs = playlistViewVM.songs.getValue();
            if (info == null || songs == null) return;

            Playlist playlist = new Playlist(info, songs);
            playingVM.selectSong(playlist, position);
            playlistViewVM.setTransitionState(B.parent.getTransitionState());
        }

        @Override
        public void onMenuClicked(Song song) {

        }
    };

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistViewBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        playlistViewAdapter = new PlaylistViewAdapter(callback);
        B.recyclerView.setAdapter(playlistViewAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setHasFixedSize(true);

        // Live Observers
        playlistViewVM.info.observe(activity, this::onInfoChange);
        playlistViewVM.songs.observe(activity, this::onSongsChange);
        B.parent.addTransitionListener(activity.getTransitionListener());
        B.backImage.setOnClickListener(__ -> activity.onBackPressed());

        if (playlistViewVM.getTransitionState() != null) {
            B.parent.setTransitionState(playlistViewVM.getTransitionState());
            playlistViewVM.setTransitionState(null);
        }
        B.swipeRefresh.setOnRefreshListener(this::loadPlaylistData);
        loadPlaylistData();

        return B.getRoot();
    }

    private void onSongsChange(List<Song> songs) {
        PlaylistInfo info = playlistViewVM.info.getValue();
        if (info == null) return;

        playlistViewAdapter.updateSongs(songs, info.getOrder());
    }

    private void onInfoChange(PlaylistInfo info) {
        B.nameText.setText(info.getName());
        if (info.getCover() != null) {
            Glide
                .with(activity)
                .load(info.getCover())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(B.coverImage);
        }
    }

    private void loadPlaylistData() {
        if (playlistViewVM.loading) return;
        B.swipeRefresh.setRefreshing(true);
        playlistViewVM.loading = true;

        PlaylistInfo info = playlistViewVM.info.getValue();
        if (info == null) {
            activity.handleError(new Exception("Info not initialised"));
            return;
        }

        if (playlistViewVM.firebase) {
            repository
                .playlist(info.getId())
                .get()
                .addOnSuccessListener(snap -> {
                    PlaylistInfo newInfo = snap.toObject(PlaylistInfo.class);
                    playlistViewVM.info.setValue(newInfo);

                    repository
                        .playlistSongs(info.getId())
                        .get()
                        .addOnSuccessListener(snaps -> {
                            List<Song> songs = snaps.toObjects(Song.class);
                            songs.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));
                            songs.forEach(song -> song.setDirectoryWith(requireContext()));
                            playlistViewVM.songs.setValue(songs);
                            B.swipeRefresh.setRefreshing(false);
                            playlistViewVM.loading = false;
                        })
                        .addOnFailureListener(activity::handleError);
                });
        } else {
            new PlaylistLookupSocket(info, getContext(), new PlaylistLookupSocket.Callback() {
                @Override
                public void onFinish(Playlist playlist) {
                    playlistViewVM.songs.postValue(playlist.getSongs());
                    B.swipeRefresh.setRefreshing(false);
                    playlistViewVM.loading = false;
                }

                @Override
                public void onError(String message) {
                    activity.handleError(new Exception(message));
                }

                @Override
                public boolean isInactive() {
                    return false;
                }
            });
        }

    }
}