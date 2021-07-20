package com.zectan.soundroid.ViewModels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.Classes.StrictLiveData;
import com.zectan.soundroid.Models.Info;
import com.zectan.soundroid.Models.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewViewModel extends ViewModel {
    public final StrictLiveData<String> playlistId = new StrictLiveData<>("");
    public final StrictLiveData<Info> info = new StrictLiveData<>(Info.getEmpty());
    public final StrictLiveData<List<Song>> songs = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);

    public PlaylistViewViewModel() {
        // Required empty public constructor
    }
}
