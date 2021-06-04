package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.objects.Option;

import java.util.List;

public class OptionsMenuViewModel extends ViewModel {
    public MutableLiveData<String> url = new MutableLiveData<>();
    public MutableLiveData<String> title = new MutableLiveData<>();
    public MutableLiveData<String> description = new MutableLiveData<>();
    public MutableLiveData<List<Option>> options = new MutableLiveData<>();
    
    public OptionsMenuViewModel() {
    
    }
    
}