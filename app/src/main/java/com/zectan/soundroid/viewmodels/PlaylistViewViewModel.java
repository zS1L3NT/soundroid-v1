package com.zectan.soundroid.viewmodels;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.Song;

import java.util.List;

public class PlaylistViewViewModel extends ViewModel {
    public MutableLiveData<PlaylistInfo> info = new MutableLiveData<>();
    public MutableLiveData<List<Song>> songs = new MutableLiveData<>();
    public boolean loading = false, firebase = true;
    private Bundle transitionState;

    public PlaylistViewViewModel() {
        // Required empty public constructor
    }

    public Bundle getTransitionState() {
        return transitionState;
    }

    public void setTransitionState(Bundle transitionState) {
        this.transitionState = transitionState;
    }
}
