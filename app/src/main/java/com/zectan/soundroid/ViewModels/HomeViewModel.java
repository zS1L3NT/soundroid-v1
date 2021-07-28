package com.zectan.soundroid.ViewModels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.Classes.StrictLiveData;
import com.zectan.soundroid.Models.Song;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    public final StrictLiveData<List<Song>> songs = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);
}
