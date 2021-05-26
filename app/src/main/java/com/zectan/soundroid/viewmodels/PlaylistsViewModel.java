package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.objects.PlaylistInfo;

import java.util.List;

public class PlaylistsViewModel extends ViewModel {
    public MutableLiveData<List<PlaylistInfo>> infos = new MutableLiveData<>();
    public boolean requested = false;

    public PlaylistsViewModel() {
        // Required empty public constructor
    }

}
