package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.motion.widget.MotionLayout;
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
import com.zectan.soundroid.sockets.PlaylistLookupSocket;
import com.zectan.soundroid.viewmodels.PlayingViewModel;
import com.zectan.soundroid.viewmodels.PlaylistViewViewModel;

import java.util.List;

public class PlaylistViewFragment extends Fragment {
    private MainActivity activity;
    private FirebaseRepository repository;
    private PlaylistViewAdapter playlistViewAdapter;

    private PlaylistViewViewModel playlistViewVM;
    private PlayingViewModel playingVM;

    private SwipeRefreshLayout swipeRefreshLayout;
    private MotionLayout motionLayout;
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
        ImageView backImage = view.findViewById(R.id.playlist_view_back);
        swipeRefreshLayout = view.findViewById(R.id.playlist_view_swipe_refresh);
        motionLayout = view.findViewById(R.id.playlist_view_motion_layout);
        coverImage = view.findViewById(R.id.playlist_view_cover);
        nameText = view.findViewById(R.id.playlist_view_name);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        playlistViewAdapter = new PlaylistViewAdapter(this::onSongSelected);
        recyclerView.setAdapter(playlistViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        // Live Observers
        playlistViewVM.info.observe(activity, this::onInfoChange);
        playlistViewVM.songs.observe(activity, this::onSongsChange);
        motionLayout.addTransitionListener(activity.getTransitionListener());
        backImage.setOnClickListener(__ -> activity.onBackPressed());

        if (playlistViewVM.getTransitionState() != null) {
            motionLayout.setTransitionState(playlistViewVM.getTransitionState());
            playlistViewVM.setTransitionState(null);
        }
        swipeRefreshLayout.setOnRefreshListener(this::loadPlaylistData);
        loadPlaylistData();

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
        playlistViewVM.setTransitionState(motionLayout.getTransitionState());
    }

    private void onSongsChange(List<Song> songs) {
        PlaylistInfo info = playlistViewVM.info.getValue();
        if (info == null) return;

        playlistViewAdapter.updateSongs(songs, info.getOrder());
    }

    private void onInfoChange(PlaylistInfo info) {
        nameText.setText(info.getName());
        if (info.getCover() != null) {
            Glide
                .with(activity)
                .load(info.getCover())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(coverImage);
        }
    }

    private void loadPlaylistData() {
        if (playlistViewVM.loading) return;
        swipeRefreshLayout.setRefreshing(true);
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
                            swipeRefreshLayout.setRefreshing(false);
                            playlistViewVM.loading = false;
                        })
                        .addOnFailureListener(activity::handleError);
                });
        } else {
            new PlaylistLookupSocket(info, getContext(), new PlaylistLookupSocket.Callback() {
                @Override
                public void onFinish(Playlist playlist) {
                    playlistViewVM.songs.postValue(playlist.getSongs());
                    swipeRefreshLayout.setRefreshing(false);
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