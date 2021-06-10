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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.PlaylistsAdapter;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.viewmodels.PlaylistViewViewModel;
import com.zectan.soundroid.viewmodels.PlaylistsViewModel;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragment extends Fragment {
    private static final String TAG = "(SounDroid) PlayingFragment";
    private static final String USER_ID = "admin";
    private MainActivity activity;
    private FirebaseRepository repository;

    private PlaylistsViewModel playlistVM;
    private PlaylistViewViewModel playlistViewVM;

    private SwipeRefreshLayout swipeRefreshLayout;

    public PlaylistsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);
        activity = (MainActivity) getActivity();
        assert activity != null;
        repository = activity.getRepository();

        // ViewModels
        playlistVM = new ViewModelProvider(activity).get(PlaylistsViewModel.class);
        playlistViewVM = new ViewModelProvider(activity).get(PlaylistViewViewModel.class);

        // Reference views
        RecyclerView recyclerView = view.findViewById(R.id.playlists_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.playlists_swipe_refresh);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        PlaylistsAdapter playlistsAdapter = new PlaylistsAdapter(this::onPlaylistSelected);
        recyclerView.setAdapter(playlistsAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        // Observers
        playlistVM.infos.observe(activity, playlistsAdapter::updateInfos);

        swipeRefreshLayout.setOnRefreshListener(this::loadFromFirebase);
        if (playlistVM.infos.getValue() == null) loadFromFirebase();
        activity.showNavigator();

        return view;
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
        swipeRefreshLayout.setRefreshing(true);
        playlistVM.requested = true;

        repository
                .playlists(USER_ID)
                .get()
                .addOnSuccessListener(snaps -> {
                    List<PlaylistInfo> infos = snaps.toObjects(PlaylistInfo.class);
                    playlistVM.infos.setValue(infos);
                    swipeRefreshLayout.setRefreshing(false);
                    playlistVM.requested = false;
                })
                .addOnFailureListener(this::handleError);
    }

    private void handleError(Exception e) {
        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, e.getMessage());
    }
}