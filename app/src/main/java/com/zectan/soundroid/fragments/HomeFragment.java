package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
        RecyclerView recyclerView = view.findViewById(R.id.song_recycler_view);

        // Click listeners
//        view.findViewById(R.id.header_search).setOnClickListener(__ -> {
//            NavDirections action = HomeFragmentDirections.openSearch();
//            NavHostFragment.findNavController(this).navigate(action);
//        });

        homeAdapter = new HomeAdapter((song, position) -> {
            NavHostFragment.findNavController(this).navigate(HomeFragmentDirections.openDownloadedSong());
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
                                        List<String> order = songs
                                                .stream()
                                                .sorted((song1, song2) -> song1.getTitle().compareTo(song2.getTitle()))
                                                .map(Song::getId)
                                                .collect(Collectors.toList());
                                        homeAdapter.updateSongs(order, songs);
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