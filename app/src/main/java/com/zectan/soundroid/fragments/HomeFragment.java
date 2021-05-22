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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.HomeAdapter;
import com.zectan.soundroid.objects.Song;
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
    private HomeAdapter homeAdapter;

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

        // Reference views
        RecyclerView recyclerView = view.findViewById(R.id.home_recycler_view);
        searchbar = view.findViewById(R.id.home_searchbar);

        // Click listeners
        searchbar.setOnClickListener(__ -> {
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras
                    .Builder()
                    .addSharedElement(searchbar, getString(R.string.TRANSITION_searchbar)).build();
            NavDirections action = HomeFragmentDirections.openSearch();
            NavHostFragment.findNavController(this).navigate(action, extras);
        });

        homeAdapter = new HomeAdapter((song, position) -> {
            NavDirections action = HomeFragmentDirections.openDownloadedSong();
            NavHostFragment.findNavController(this).navigate(action);
            playingVM.selectSong(song, position);
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(homeAdapter);

        loadFromFirebase();

        return view;
    }

    private void loadFromFirebase() {
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
                                        List<Song> sortedSongs = songs
                                                .stream()
                                                .sorted((song1, song2) -> song1.getTitle().compareTo(song2.getTitle()))
                                                .collect(Collectors.toList());
                                        List<String> order = songs
                                                .stream()
                                                .map(Song::getId)
                                                .collect(Collectors.toList());
                                        homeAdapter.updateSongs(order, sortedSongs);
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