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
    private MutableLiveData<List<Song>> playlists;
    private MutableLiveData<Boolean> searching;

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
            liveSongs().postValue(new ArrayList<>());
            liveSearching().postValue(false);
            return;
        }

        liveSearching().postValue(true);
        new SongSearchTask(text, folder, songs -> {
            if (searchId == searchNumber) {
                Log.d(TAG, "SEARCH_DISPLAYED");
                liveSongs().postValue(songs);
                liveSearching().postValue(false);
            } else {
                Log.d(TAG, "SEARCH_DISCARDED");
            }
        }).start();
    }

    /**
     * Observable for the songs received from API
     *
     * @return Mutable Live Data -> List Song
     */
    public MutableLiveData<List<Song>> liveSongs() {
        if (playlists == null) {
            playlists = new MutableLiveData<>();
        }
        return playlists;
    }

    /**
     * Observable for searching state
     *
     * @return Mutable Live Data -> Boolean
     */
    public MutableLiveData<Boolean> liveSearching() {
        if (searching == null) {
            searching = new MutableLiveData<>();
        }
        return searching;
    }

}
