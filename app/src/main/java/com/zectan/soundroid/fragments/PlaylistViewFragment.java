package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.PlaylistViewAdapter;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.viewmodels.PlayingViewModel;
import com.zectan.soundroid.viewmodels.PlaylistViewViewModel;

import java.util.List;

public class PlaylistViewFragment extends Fragment {
    private MainActivity activity;
    private FirebaseRepository repository;

    private PlaylistViewViewModel playlistViewVM;
    private PlayingViewModel playingVM;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView coverImage;
    private TextView nameText;

    public PlaylistViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_view, container, false);
        activity = (MainActivity) getActivity();
        assert activity != null;
        repository = activity.getRepository();

        // ViewModels
        playlistViewVM = new ViewModelProvider(activity).get(PlaylistViewViewModel.class);
        playingVM = new ViewModelProvider(activity).get(PlayingViewModel.class);

        // Reference Views
        RecyclerView recyclerView = view.findViewById(R.id.playlist_view_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.playlist_view_swipe_refresh);
        coverImage = view.findViewById(R.id.playlist_view_cover);
        nameText = view.findViewById(R.id.playlist_view_name);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        PlaylistViewAdapter playlistViewAdapter = new PlaylistViewAdapter(this::onSongSelected);
        recyclerView.setAdapter(playlistViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
        swipeRefreshLayout.setOnRefreshListener(this::loadFromFirebase);

        playlistViewVM.info.observe(activity, this::loadPlaylistInfo);
        playlistViewVM.songs.observe(activity, songs -> {
            PlaylistInfo info = playlistViewVM.info.getValue();
            if (info == null) return;

            playlistViewAdapter.updateSongs(songs, info.getOrder());
        });

        loadSongsFromFirebase();

        return view;
    }

    private void onSongSelected(ImageView cover, String transitionName, Song song, int position) {
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras
                .Builder()
                .addSharedElement(cover, transitionName)
                .build();
        NavDirections action = PlaylistViewFragmentDirections
                .openPlaylistSong()
                .setTransitionName(transitionName);
        NavHostFragment.findNavController(this).navigate(action, extras);

        PlaylistInfo info = playlistViewVM.info.getValue();
        List<Song> songs = playlistViewVM.songs.getValue();
        if (info == null || songs == null) return;

        Playlist playlist = new Playlist(info, songs);
        playingVM.selectSong(playlist, position);
    }

    private void loadPlaylistInfo(PlaylistInfo info) {
        nameText.setText(info.getName());
        if (info.getCover() != null) {
            Glide
                    .with(activity)
                    .load(info.getCover())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(coverImage);
        }
    }

    private void loadFromFirebase() {
        if (playlistViewVM.requested) return;
        swipeRefreshLayout.setRefreshing(true);
        playlistViewVM.requested = true;

        PlaylistInfo info = playlistViewVM.info.getValue();
        if (info == null) {
            activity.handleError(new Exception("Info not initialised"));
            return;
        }

        repository
                .playlist(info.getId())
                .get()
                .addOnSuccessListener(snap -> {
                    PlaylistInfo newInfo = snap.toObject(PlaylistInfo.class);
                    playlistViewVM.info.setValue(newInfo);

                    loadSongsFromFirebase();
                });
    }

    private void loadSongsFromFirebase() {
        PlaylistInfo info = playlistViewVM.info.getValue();
        if (info == null) {
            activity.handleError(new Exception("Info not initialised"));
            return;
        }

        repository
                .playlistSongs(info.getId())
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Song> songs = snaps.toObjects(Song.class);
                    songs.sort((song1, song2) -> song1.getTitle().compareTo(song2.getTitle()));
                    playlistViewVM.songs.setValue(songs);
                    swipeRefreshLayout.setRefreshing(false);
                    playlistViewVM.requested = false;
                })
                .addOnFailureListener(activity::handleError);
    }
}