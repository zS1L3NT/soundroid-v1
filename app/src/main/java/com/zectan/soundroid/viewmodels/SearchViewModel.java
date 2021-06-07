package com.zectan.soundroid.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.sockets.SearchAllSocket;

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
     * @param text Search text
     */
    public void searchOnline(String text, Context context) {
        int searchId = ++searchNumber;
        if (text.isEmpty()) {
            songs.postValue(new ArrayList<>());
            searching.postValue(false);
            return;
        }

        searching.postValue(true);
        new SearchAllSocket(text, context, new SearchAllSocket.Callback() {
            @Override
            public void onFinish(List<Song> songs) {
                if (searchId == searchNumber) {
                    Log.d(TAG, "SEARCH_DISPLAYED");
                    SearchViewModel.this.songs.postValue(songs);
                    searching.postValue(false);
                } else {
                    Log.d(TAG, "SEARCH_DISCARDED");
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, message);
            }

            @Override
            public boolean isInactive() {
                return searchId != searchNumber;
            }
        });
    }

}
