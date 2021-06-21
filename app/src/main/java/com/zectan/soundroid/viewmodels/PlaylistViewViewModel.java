package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.objects.Info;
import com.zectan.soundroid.objects.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewViewModel extends ViewModel {
    public StrictLiveData<Info> info = new StrictLiveData<>(Info.getEmpty());
    public StrictLiveData<List<Song>> songs = new StrictLiveData<>(new ArrayList<>());
    public boolean loading = false, firebase = true;

    public PlaylistViewViewModel() {
        // Required empty public constructor
    }
}
