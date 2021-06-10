package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.AnimatedFragment;
import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.HomeAdapter;
import com.zectan.soundroid.databinding.FragmentHomeBinding;
import com.zectan.soundroid.objects.Option;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.viewmodels.HomeViewModel;
import com.zectan.soundroid.viewmodels.OptionsMenuViewModel;
import com.zectan.soundroid.viewmodels.PlayingViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeFragment extends AnimatedFragment {
    private static final String TAG = "(SounDroid) HomeFragment";
    private final String USER_ID = "admin";
    private MainActivity activity;
    private FragmentHomeBinding B;
    private FirebaseRepository repository;

    private HomeViewModel homeVM;
    private PlayingViewModel playingVM;
    private OptionsMenuViewModel optionsMenuVM;
    
    public HomeFragment() {
        // Required empty public constructor
    }
    
    private final HomeAdapter.Callback homeAdapterCallback = new HomeAdapter.Callback() {
        @Override
        public void onSongClicked(ImageView cover, String transitionName, Playlist playlist, int position) {
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras
                .Builder()
                .addSharedElement(cover, transitionName)
                .build();
            NavDirections action = HomeFragmentDirections.openDownloadedSong().setTransitionName(transitionName);
            NavHostFragment.findNavController(HomeFragment.this).navigate(action, extras);
            playingVM.selectSong(playlist, position);
            homeVM.setTransitionState(B.parent.getTransitionState());
        }
        
        @Override
        public void onMenuClicked(Song song) {
            NavDirections action = HomeFragmentDirections.openOptionsMenu();
            NavHostFragment.findNavController(HomeFragment.this).navigate(action);
            homeVM.setTransitionState(B.parent.getTransitionState());

            optionsMenuVM.url.setValue(song.getCover());
            optionsMenuVM.title.setValue(song.getTitle());
            optionsMenuVM.colorHex.setValue(song.getColorHex());
            optionsMenuVM.description.setValue(song.getArtiste());

            List<Option> options = new ArrayList<>();
            options.add(Option.addToQueue());
            options.add(Option.addToPlaylist());
            options.add(Option.download(song));
            optionsMenuVM.options.setValue(options);
        }
    };

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentHomeBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        assert activity != null;
        repository = activity.getRepository();

        // ViewModels
        homeVM = new ViewModelProvider(activity).get(HomeViewModel.class);
        playingVM = new ViewModelProvider(activity).get(PlayingViewModel.class);
        optionsMenuVM = new ViewModelProvider(activity).get(OptionsMenuViewModel.class);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        HomeAdapter homeAdapter = new HomeAdapter(homeAdapterCallback);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setAdapter(homeAdapter);

        // Live Observers
        homeVM.playlist.observe(activity, homeAdapter::updatePlaylist);
        B.parent.addTransitionListener(activity.getTransitionListener());

        if (homeVM.getTransitionState() != null) {
            B.parent.setTransitionState(homeVM.getTransitionState());
            homeVM.setTransitionState(null);
        }
        B.swipeRefresh.setOnRefreshListener(this::loadSongsData);
        B.searchbar.setOnClickListener(this::onSearchbarClicked);
        if (homeVM.playlist.getValue() == null) loadSongsData();
        activity.showNavigator();

        return B.getRoot();
    }
    
    private void onSearchbarClicked(View view) {
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras
            .Builder()
            .addSharedElement(B.searchbar, getString(R.string.TRANSITION_searchbar)).build();
        NavDirections action = HomeFragmentDirections.openSearch();
        NavHostFragment.findNavController(this).navigate(action, extras);
        homeVM.setTransitionState(B.parent.getTransitionState());
    }

    private void loadSongsData() {
        if (homeVM.requested) return;
        B.swipeRefresh.setRefreshing(true);
        homeVM.requested = true;

        repository
            .userSongs(USER_ID)
            .get()
            .addOnSuccessListener(snaps -> {
                List<Song> songs = snaps.toObjects(Song.class);
                List<String> order = songs
                    .stream()
                    .sorted((song1, song2) -> song1.getTitle().compareTo(song2.getTitle()))
                    .map(Song::getId)
                    .collect(Collectors.toList());
                songs.forEach(song -> song.setDirectoryWith(requireContext()));
                homeVM.playlist.setValue(new Playlist(new PlaylistInfo("", "All Songs", order), songs));
                B.swipeRefresh.setRefreshing(false);
                homeVM.requested = false;
            })
            .addOnFailureListener(this::handleError);
    }
    
    private void handleError(Exception e) {
        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, e.getMessage());
    }
}