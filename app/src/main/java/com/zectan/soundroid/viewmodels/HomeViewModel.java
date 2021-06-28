package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.models.Playlist;

public class HomeViewModel extends ViewModel {
    public final MutableLiveData<Playlist> playlist = new MutableLiveData<>();
    public boolean requested = false;

    public HomeViewModel() {

    }
}
