package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewbinding.ViewBinding;

import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.viewmodels.HomeViewModel;
import com.zectan.soundroid.viewmodels.OptionsMenuViewModel;
import com.zectan.soundroid.viewmodels.PlayingViewModel;
import com.zectan.soundroid.viewmodels.PlaylistViewViewModel;
import com.zectan.soundroid.viewmodels.PlaylistsViewModel;
import com.zectan.soundroid.viewmodels.SearchViewModel;

import org.jetbrains.annotations.NotNull;

public class Fragment<T extends ViewBinding> extends androidx.fragment.app.Fragment {
    protected FirebaseRepository repository;
    protected NavController navController;
    protected MainActivity activity;
    protected T B;

    protected HomeViewModel homeVM;
    protected OptionsMenuViewModel optionsMenuVM;
    protected PlayingViewModel playingVM;
    protected PlaylistsViewModel playlistsVM;
    protected PlaylistViewViewModel playlistViewVM;
    protected SearchViewModel searchVM;

    public Fragment() {
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        assert activity != null;
        repository = activity.getRepository();
        navController = NavHostFragment.findNavController(this);

        homeVM = new ViewModelProvider(activity).get(HomeViewModel.class);
        optionsMenuVM = new ViewModelProvider(activity).get(OptionsMenuViewModel.class);
        playingVM = new ViewModelProvider(activity).get(PlayingViewModel.class);
        playlistsVM = new ViewModelProvider(activity).get(PlaylistsViewModel.class);
        playlistViewVM = new ViewModelProvider(activity).get(PlaylistViewViewModel.class);
        searchVM = new ViewModelProvider(activity).get(SearchViewModel.class);

        return B.getRoot();
    }
}
