package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistEditViewModel extends ViewModel {
    public final StrictLiveData<String> playlistId = new StrictLiveData<>("");
    public final StrictLiveData<Info> info = new StrictLiveData<>(Info.getEmpty());
    public final StrictLiveData<List<Song>> songs = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<Integer> navigateNow = new StrictLiveData<>(0);
    public final StrictLiveData<Boolean> saving = new StrictLiveData<>(false);

    public PlaylistEditViewModel() {
        // Required empty public constructor
    }

}
