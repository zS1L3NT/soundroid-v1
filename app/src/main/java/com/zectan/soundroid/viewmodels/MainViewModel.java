package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    public final MutableLiveData<Exception> error = new MutableLiveData<>();

    public MainViewModel() {

    }
}
