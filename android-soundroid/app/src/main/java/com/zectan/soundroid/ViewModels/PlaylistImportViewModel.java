package com.zectan.soundroid.ViewModels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.Classes.StrictLiveData;

public class PlaylistImportViewModel extends ViewModel {
    public final StrictLiveData<String> text = new StrictLiveData<>("");
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);
}
