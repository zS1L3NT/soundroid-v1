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
import com.zectan.soundroid.R;
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
    protected NavController mNavController;
    protected MainActivity mActivity;
    protected T B;

    protected MainViewModel mMainVM;
    protected HomeViewModel mHomeVM;
    protected PlayingViewModel mPlayingVM;
    protected PlaylistEditViewModel mPlaylistEditVM;
    protected PlaylistsViewModel mPlaylistsVM;
    protected PlaylistViewViewModel mPlaylistViewVM;
    protected SearchViewModel mSearchVM;
    protected SongEditViewModel mSongEditVM;
    protected PlaylistImportViewModel mPlaylistImportVM;

    public Fragment() {
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();
        assert mActivity != null;
        mNavController = mActivity.navController;

        mMainVM = new ViewModelProvider(mActivity).get(MainViewModel.class);
        mHomeVM = new ViewModelProvider(mActivity).get(HomeViewModel.class);
        mPlayingVM = new ViewModelProvider(mActivity).get(PlayingViewModel.class);
        mPlaylistEditVM = new ViewModelProvider(mActivity).get(PlaylistEditViewModel.class);
        mPlaylistsVM = new ViewModelProvider(mActivity).get(PlaylistsViewModel.class);
        mPlaylistViewVM = new ViewModelProvider(mActivity).get(PlaylistViewViewModel.class);
        mSearchVM = new ViewModelProvider(mActivity).get(SearchViewModel.class);
        mSongEditVM = new ViewModelProvider(mActivity).get(SongEditViewModel.class);
        mPlaylistImportVM = new ViewModelProvider(mActivity).get(PlaylistImportViewModel.class);
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mActivity.getWindow().setStatusBarColor(mActivity.getAttributeResource(R.attr.statusBarBackground));
        mActivity.hideKeyboard(B.getRoot());

        return B.getRoot();
    }
}
