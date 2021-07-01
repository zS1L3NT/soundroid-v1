package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.models.Song;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    public final StrictLiveData<List<Song>> songs = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);

    public HomeViewModel() {

    }
}
