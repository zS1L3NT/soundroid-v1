package com.zectan.soundroid.Classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

import java.util.Arrays;

public abstract class Fragment<T extends ViewBinding> extends androidx.fragment.app.Fragment {
    public static final int FLAG_TRANSPARENT_STATUS = 1;
    public static final int FLAG_HIDE_NAVIGATOR = 2;
    public static final int FLAG_IGNORE_NAVIGATOR = 3;
    private final int[] mFlags;
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

    public Fragment() {
        mFlags = new int[0];
    }

    public Fragment(int... flags) {
        mFlags = flags;
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

        mActivity.hideKeyboard(B.getRoot());

        return B.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        Window window = mActivity.getWindow();
        if (flagsContain(FLAG_TRANSPARENT_STATUS)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        window.setStatusBarColor(mActivity.getAttributeResource(R.attr.statusBarBackground));
    }

    private boolean flagsContain(int flag) {
        return Arrays.stream(mFlags).anyMatch(f -> f == flag);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (flagsContain(FLAG_HIDE_NAVIGATOR) && !flagsContain(FLAG_IGNORE_NAVIGATOR)) {
            mActivity.updateNavigator(0);
        }
    }
}
