package com.zectan.soundroid.Classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.viewbinding.ViewBinding;

import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.ViewModels.HomeViewModel;
import com.zectan.soundroid.ViewModels.MainViewModel;
import com.zectan.soundroid.ViewModels.PlayingViewModel;
import com.zectan.soundroid.ViewModels.PlaylistEditViewModel;
import com.zectan.soundroid.ViewModels.PlaylistImportViewModel;
import com.zectan.soundroid.ViewModels.PlaylistViewViewModel;
import com.zectan.soundroid.ViewModels.PlaylistsViewModel;
import com.zectan.soundroid.ViewModels.SearchViewModel;
import com.zectan.soundroid.ViewModels.SongEditViewModel;

import org.jetbrains.annotations.NotNull;

public abstract class Fragment<T extends ViewBinding> extends androidx.fragment.app.Fragment {
    protected NavController navController;
    protected MainActivity activity;
    protected T B;

    protected MainViewModel mainVM;
    protected HomeViewModel homeVM;
    protected PlayingViewModel playingVM;
    protected PlaylistEditViewModel playlistEditVM;
    protected PlaylistsViewModel playlistsVM;
    protected PlaylistViewViewModel playlistViewVM;
    protected SearchViewModel searchVM;
    protected SongEditViewModel songEditVM;
    protected PlaylistImportViewModel playlistImportVM;

    public Fragment() {
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        assert activity != null;
        navController = activity.navController;

        mainVM = new ViewModelProvider(activity).get(MainViewModel.class);
        homeVM = new ViewModelProvider(activity).get(HomeViewModel.class);
        playingVM = new ViewModelProvider(activity).get(PlayingViewModel.class);
        playlistEditVM = new ViewModelProvider(activity).get(PlaylistEditViewModel.class);
        playlistsVM = new ViewModelProvider(activity).get(PlaylistsViewModel.class);
        playlistViewVM = new ViewModelProvider(activity).get(PlaylistViewViewModel.class);
        searchVM = new ViewModelProvider(activity).get(SearchViewModel.class);
        songEditVM = new ViewModelProvider(activity).get(SongEditViewModel.class);
        playlistImportVM = new ViewModelProvider(activity).get(PlaylistImportViewModel.class);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        activity.hideKeyboard(B.getRoot());

        return B.getRoot();
    }
}
