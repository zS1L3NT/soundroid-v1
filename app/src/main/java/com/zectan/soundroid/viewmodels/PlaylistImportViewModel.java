package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.classes.StrictLiveData;

public class PlaylistImportViewModel extends ViewModel {
    public final StrictLiveData<String> text = new StrictLiveData<>("");
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);

    public PlaylistImportViewModel() {
        // Required empty public constructor
    }
}
