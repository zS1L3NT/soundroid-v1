package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.objects.Info;
import com.zectan.soundroid.objects.Song;

import java.util.List;

public class PlaylistViewViewModel extends ViewModel {
    public MutableLiveData<Info> info = new MutableLiveData<>();
    public MutableLiveData<List<Song>> songs = new MutableLiveData<>();
    public boolean loading = false, firebase = true;

    public PlaylistViewViewModel() {
        // Required empty public constructor
    }
}
