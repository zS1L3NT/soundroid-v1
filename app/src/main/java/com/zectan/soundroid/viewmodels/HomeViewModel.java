package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.objects.Playlist;

public class HomeViewModel extends ViewModel {
    public MutableLiveData<Playlist> playlist = new MutableLiveData<>();
    public boolean requested = false;

    public HomeViewModel() {

    }
}
