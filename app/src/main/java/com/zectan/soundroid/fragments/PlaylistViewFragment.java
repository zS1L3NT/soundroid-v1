package com.zectan.soundroid.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
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
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.PlaylistViewAdapter;
import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.databinding.FragmentPlaylistViewBinding;
import com.zectan.soundroid.objects.Anonymous;
import com.zectan.soundroid.objects.Info;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.sockets.PlaylistLookupSocket;

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
        public void onMenuClicked(Song song) {

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
        B.recyclerView.setHasFixedSize(true);

        // Live Observers
        playlistViewVM.info.observe(activity, this::onInfoChange);
        playlistViewVM.songs.observe(activity, this::onSongsChange);
        B.parent.addTransitionListener(activity.getTransitionListener());
        B.backImage.setOnClickListener(__ -> activity.onBackPressed());

        B.swipeRefresh.setOnRefreshListener(this::loadPlaylistData);
        loadPlaylistData();

        return B.getRoot();
    }

    private void onSongsChange(List<Song> songs) {
        Info info = playlistViewVM.info.getValue();

        playlistViewAdapter.updateSongs(songs, info.getOrder());
    }

    private void onInfoChange(Info info) {
        B.nameText.setText(info.getName());
        Glide
            .with(activity)
            .load(info.getCover())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(B.coverImage);

        Drawable oldGD = B.background.getBackground();
        int[] colors = {Color.parseColor(info.getColorHex()), activity.getColor(R.color.theme_playing_bottom)};
        GradientDrawable newGD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);

        Drawable[] layers = {oldGD, newGD};
        TransitionDrawable transition = new TransitionDrawable(layers);
        B.background.setBackground(transition);
        transition.startTransition(1000);
    }

    private void loadPlaylistData() {
        if (playlistViewVM.loading) return;
        B.swipeRefresh.setRefreshing(true);
        playlistViewVM.loading = true;

        Info info = playlistViewVM.info.getValue();

        if (playlistViewVM.firebase) {
            repository
                .playlist(info.getId())
                .get()
                .addOnSuccessListener(snap -> {
                    Info newInfo = snap.toObject(Info.class);
                    assert newInfo != null;
                    playlistViewVM.info.setValue(newInfo);

                    repository
                        .playlistSongs(info.getId())
                        .get()
                        .addOnSuccessListener(snaps -> {
                            List<Song> songs = snaps.toObjects(Song.class);
                            songs.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));
                            songs.forEach(song -> song.setDirectoryWith(activity));
                            playlistViewVM.songs.setValue(songs);
                            B.swipeRefresh.setRefreshing(false);
                            playlistViewVM.loading = false;
                        })
                        .addOnFailureListener(mainVM.error::postValue);

                })
                .addOnFailureListener(mainVM.error::postValue);
        } else {
            new PlaylistLookupSocket(info, getContext(), new PlaylistLookupSocket.Callback() {
                @Override
                public void onFinish(Playlist playlist) {
                    playlistViewVM.songs.postValue(playlist.getSongs());
                    B.swipeRefresh.setRefreshing(false);
                    playlistViewVM.loading = false;
                }

                @Override
                public void onSong(Song song) {

                }

                @Override
                public void onError(String message) {
                    mainVM.error.postValue(new Exception(message));
                }

                @Override
                public boolean isInactive() {
                    return false;
                }
            });
        }
    }
}