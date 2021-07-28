package com.zectan.soundroid.ViewModels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.Classes.StrictLiveData;

public class PlaylistsViewModel extends ViewModel {
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);
}
