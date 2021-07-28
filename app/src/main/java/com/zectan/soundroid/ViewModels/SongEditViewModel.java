package com.zectan.soundroid.ViewModels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.Classes.StrictLiveData;
import com.zectan.soundroid.Models.Song;

public class SongEditViewModel extends ViewModel {
    public final StrictLiveData<Song> song = new StrictLiveData<>(Song.getEmpty());
    public final StrictLiveData<Boolean> saving = new StrictLiveData<>(false);
}
