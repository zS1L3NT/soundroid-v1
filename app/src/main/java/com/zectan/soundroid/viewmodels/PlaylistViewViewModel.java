package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewViewModel extends ViewModel {
    public final StrictLiveData<Info> info = new StrictLiveData<>(Info.getEmpty());
    public final StrictLiveData<List<Song>> songs = new StrictLiveData<>(new ArrayList<>());
    public boolean loading = false, firebase = true;

    public PlaylistViewViewModel() {
        // Required empty public constructor
    }
}
