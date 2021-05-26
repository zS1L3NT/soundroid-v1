package com.zectan.soundroid.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.tasks.SongSearchTask;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) SearchViewModel";
    public final MutableLiveData<List<Song>> songs = new MutableLiveData<>();
    public final MutableLiveData<Boolean> searching = new MutableLiveData<>();

    private int searchNumber = 0;

    public SearchViewModel() {
        // Required empty public constructor
    }

    /**
     * Search online using the custom Thread
     *
     * @param text   Search text
     * @param folder Directory to store files
     */
    public void searchOnline(String text, String folder) {
        int searchId = ++searchNumber;
        if (text.isEmpty()) {
            songs.postValue(new ArrayList<>());
            searching.postValue(false);
            return;
        }

        searching.postValue(true);
        new SongSearchTask(text, folder, songs -> {
            if (searchId == searchNumber) {
                Log.d(TAG, "SEARCH_DISPLAYED");
                this.songs.postValue(songs);
                searching.postValue(false);
            } else {
                Log.d(TAG, "SEARCH_DISCARDED");
            }
        }).start();
    }

}
