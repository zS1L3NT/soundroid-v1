package com.zectan.soundroid.Classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.viewbinding.ViewBinding;

import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.ViewModels.HomeViewModel;
import com.zectan.soundroid.ViewModels.MainViewModel;
import com.zectan.soundroid.ViewModels.PlaylistEditViewModel;
import com.zectan.soundroid.ViewModels.PlaylistImportViewModel;
import com.zectan.soundroid.ViewModels.PlaylistViewViewModel;
import com.zectan.soundroid.ViewModels.PlaylistsViewModel;
import com.zectan.soundroid.ViewModels.SearchViewModel;
import com.zectan.soundroid.ViewModels.SongEditViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Fragment<T extends ViewBinding> extends androidx.fragment.app.Fragment {
    protected NavController mNavController;
    protected MainActivity mActivity;
    protected T B;

    protected MainViewModel mMainVM;
    protected HomeViewModel mHomeVM;
    protected PlaylistEditViewModel mPlaylistEditVM;
    protected PlaylistsViewModel mPlaylistsVM;
    protected PlaylistViewViewModel mPlaylistViewVM;
    protected SearchViewModel mSearchVM;
    protected SongEditViewModel mSongEditVM;
    protected PlaylistImportViewModel mPlaylistImportVM;

    private final boolean mTransparentStatus;

    public Fragment() {
        mTransparentStatus = false;
    }

    public Fragment(boolean transparentStatus) {
        mTransparentStatus = transparentStatus;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();
        assert mActivity != null;
        mNavController = mActivity.mNavController;

        mMainVM = new ViewModelProvider(mActivity).get(MainViewModel.class);
        mHomeVM = new ViewModelProvider(mActivity).get(HomeViewModel.class);
        mPlaylistEditVM = new ViewModelProvider(mActivity).get(PlaylistEditViewModel.class);
        mPlaylistsVM = new ViewModelProvider(mActivity).get(PlaylistsViewModel.class);
        mPlaylistViewVM = new ViewModelProvider(mActivity).get(PlaylistViewViewModel.class);
        mSearchVM = new ViewModelProvider(mActivity).get(SearchViewModel.class);
        mSongEditVM = new ViewModelProvider(mActivity).get(SongEditViewModel.class);
        mPlaylistImportVM = new ViewModelProvider(mActivity).get(PlaylistImportViewModel.class);

        mActivity.updateNavigator(1);
        mActivity.hideKeyboard(B.getRoot());

        if (mTransparentStatus) {
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        mActivity.getWindow().setStatusBarColor(mActivity.getAttributeResource(R.attr.statusBarBackground));

        return B.getRoot();
    }
}
