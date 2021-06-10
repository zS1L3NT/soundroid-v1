package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.adapters.PlaylistsAdapter;
import com.zectan.soundroid.databinding.FragmentPlaylistsBinding;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.viewmodels.PlaylistViewViewModel;
import com.zectan.soundroid.viewmodels.PlaylistsViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragment extends Fragment {
    private static final String TAG = "(SounDroid) PlayingFragment";
    private static final String USER_ID = "admin";
    private MainActivity activity;
    private FragmentPlaylistsBinding B;
    private FirebaseRepository repository;

    private PlaylistsViewModel playlistVM;
    private PlaylistViewViewModel playlistViewVM;

    public PlaylistsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistsBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        assert activity != null;
        repository = activity.getRepository();

        // ViewModels
        playlistVM = new ViewModelProvider(activity).get(PlaylistsViewModel.class);
        playlistViewVM = new ViewModelProvider(activity).get(PlaylistViewViewModel.class);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        PlaylistsAdapter playlistsAdapter = new PlaylistsAdapter(this::onPlaylistSelected);
        B.recyclerView.setAdapter(playlistsAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setHasFixedSize(true);

        // Observers
        playlistVM.infos.observe(activity, playlistsAdapter::updateInfos);

        B.swipeRefresh.setOnRefreshListener(this::loadFromFirebase);
        if (playlistVM.infos.getValue() == null) loadFromFirebase();
        activity.showNavigator();

        return B.getRoot();
    }

    private void onPlaylistSelected(PlaylistInfo info) {
        NavDirections action = PlaylistsFragmentDirections.openPlaylistView();
        NavHostFragment.findNavController(this).navigate(action);
        playlistViewVM.info.setValue(info);
        playlistViewVM.songs.setValue(new ArrayList<>());
        playlistViewVM.firebase = true;
    }

    private void loadFromFirebase() {
        if (playlistVM.requested) return;
        B.swipeRefresh.setRefreshing(true);
        playlistVM.requested = true;

        repository
                .playlists(USER_ID)
                .get()
                .addOnSuccessListener(snaps -> {
                    List<PlaylistInfo> infos = snaps.toObjects(PlaylistInfo.class);
                    playlistVM.infos.setValue(infos);
                    B.swipeRefresh.setRefreshing(false);
                    playlistVM.requested = false;
                })
                .addOnFailureListener(this::handleError);
    }

    private void handleError(Exception e) {
        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, e.getMessage());
    }
}