package com.zectan.soundroid.viewmodels;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.objects.SearchResult;
import com.zectan.soundroid.sockets.SearchSocket;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) SearchViewModel";
    public MutableLiveData<List<SearchResult>> results = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<String> error = new MutableLiveData<>();
    public MutableLiveData<String> search = new MutableLiveData<>();

    private int search_count = 0;

    public SearchViewModel() {
        // Required empty public constructor
    }

    /**
     * Search the server for results
     *
     * @param query   Text that the user gave
     * @param context Context for the song object
     */
    public void search(String query, Context context) {
        int search_id = ++search_count;
        this.results.postValue(new ArrayList<>());
        if (query.isEmpty()) return;

        new SearchSocket(query, context, new SearchSocket.Callback() {
            @Override
            public void onResult(SearchResult result) {
                List<SearchResult> results = SearchViewModel.this.results.getValue();
                assert results != null;
                results.add(result);
                SearchViewModel.this.results.postValue(results);
            }

            @Override
            public void onDone() {

            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }

            @Override
            public boolean isInactive() {
                return search_id != search_count;
            }
        });
    }

}
