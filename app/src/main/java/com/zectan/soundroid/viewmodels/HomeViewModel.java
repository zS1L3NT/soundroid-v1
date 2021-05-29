package com.zectan.soundroid.viewmodels;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.objects.Playlist;

public class HomeViewModel extends ViewModel {
    public MutableLiveData<Playlist> playlist = new MutableLiveData<>();
    public boolean requested = false;
    private Bundle transitionState;

    public HomeViewModel() {

    }

    public Bundle getTransitionState() {
        return transitionState;
    }

    public void setTransitionState(Bundle transitionState) {
        this.transitionState = transitionState;
    }
}
