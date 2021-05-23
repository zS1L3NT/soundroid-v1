package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.HomeAdapter;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.viewmodels.HomeViewModel;
import com.zectan.soundroid.viewmodels.PlayingViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {
    private static final String TAG = "(SounDroid) HomeFragment";
    private final String USER_ID = "admin";
    private MainActivity activity;
    private FirebaseRepository repository;

    private HomeViewModel homeVM;

    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchbar;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        activity = (MainActivity) getActivity();
        repository = new FirebaseRepository();
        assert activity != null;

        // ViewModels
        PlayingViewModel playingVM = new ViewModelProvider(activity).get(PlayingViewModel.class);
        homeVM = new ViewModelProvider(activity).get(HomeViewModel.class);

        // Reference views
        RecyclerView recyclerView = view.findViewById(R.id.home_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.home_swipe_refresh);
        searchbar = view.findViewById(R.id.home_searchbar);

        swipeRefreshLayout.setOnRefreshListener(this::loadFromFirebase);
        searchbar.setOnClickListener(__ -> {
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras
                    .Builder()
                    .addSharedElement(searchbar, getString(R.string.TRANSITION_searchbar)).build();
            NavDirections action = HomeFragmentDirections.openSearch();
            NavHostFragment.findNavController(this).navigate(action, extras);
        });
        HomeAdapter homeAdapter = new HomeAdapter((cover, transitionName, song, position) -> {
            cover.setTransitionName(transitionName);
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras
                    .Builder()
                    .addSharedElement(cover, transitionName)
                    .build();
            NavDirections action = HomeFragmentDirections
                    .openDownloadedSong()
                    .setTransitionName(transitionName);
            NavHostFragment.findNavController(this).navigate(action, extras);
            playingVM.selectSong(song, position);
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(homeAdapter);

        // Observers
        homeVM.playlist.observe(activity, homeAdapter::updatePlaylist);

        if (homeVM.playlist.getValue() == null) loadFromFirebase();

        return view;
    }

    private void loadFromFirebase() {
        if (homeVM.requested) return;
        swipeRefreshLayout.setRefreshing(true);
        homeVM.requested = true;
        List<Song> songs = new ArrayList<>();

        repository
                .playlists(USER_ID)
                .get()
                .addOnSuccessListener(playlistSnaps -> {
                    int idealResponses = playlistSnaps.size();
                    AtomicInteger responses = new AtomicInteger();

                    playlistSnaps.forEach(playlistSnap -> {
                        String playlistId = playlistSnap.getId();
                        repository
                                .songs(playlistId)
                                .get()
                                .addOnSuccessListener(songsSnap -> {
                                    List<Song> newSongs = songsSnap.toObjects(Song.class);
                                    songs.addAll(newSongs);
                                    responses.getAndIncrement();

                                    if (responses.get() == idealResponses) {
                                        List<String> order = songs
                                                .stream()
                                                .sorted((song1, song2) -> song1.getTitle().compareTo(song2.getTitle()))
                                                .map(Song::getId)
                                                .collect(Collectors.toList());
                                        homeVM.playlist.setValue(new Playlist(new PlaylistInfo("", "All Songs", order), songs));
                                        swipeRefreshLayout.setRefreshing(false);
                                        homeVM.requested = false;
                                    }
                                })
                                .addOnFailureListener(this::handleError);
                    });
                })
                .addOnFailureListener(this::handleError);
    }

    private void handleError(Exception e) {
        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, e.getMessage());
    }
}