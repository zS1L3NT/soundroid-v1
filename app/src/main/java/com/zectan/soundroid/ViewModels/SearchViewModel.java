package com.zectan.soundroid.ViewModels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.Classes.StrictLiveData;
import com.zectan.soundroid.Connections.SearchSocket;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) SearchViewModel";
    private final StrictLiveData<List<SearchResult>> serverResults = new StrictLiveData<>(new ArrayList<>());
    private final StrictLiveData<List<SearchResult>> databaseResults = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<List<SearchResult>> results = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<String> query = new StrictLiveData<>("");
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);
    public final StrictLiveData<String> message = new StrictLiveData<>("");
    public final StrictLiveData<String> error = new StrictLiveData<>("");
    private int search_count = 0;

    /**
     * We want <b>serverResults</b> and <b>databaseResults</b> to combine to form <b>results</b>.
     * So we observe both and set results when either changes. We start watching in the MainActivity
     *
     * @param activity MainActivity
     */
    public void watch(MainActivity activity) {
        serverResults.observe(activity, serverResults -> {
            List<SearchResult> databaseResults = this.databaseResults.getValue();
            List<SearchResult> results = new ArrayList<>();
            results.addAll(databaseResults);
            results.addAll(serverResults);
            this.results.setValue(results);
        });

        databaseResults.observe(activity, databaseResults -> {
            List<SearchResult> serverResults = this.serverResults.getValue();
            List<SearchResult> results = new ArrayList<>();
            results.addAll(databaseResults);
            results.addAll(serverResults);
            this.results.setValue(results);
        });
    }

    /**
     * Search the server for results
     *
     * @param query Text that the user gave
     */
    public void search(String query, MainViewModel mainVM) {
        int search_id = ++search_count;
        if (query.isEmpty()) {
            loading.postValue(false);
            results.postValue(new ArrayList<>());
            message.postValue("");
            serverResults.postValue(new ArrayList<>());
            databaseResults.postValue(new ArrayList<>());
            return;
        }

        loading.postValue(true);
        results.postValue(new ArrayList<>());
        serverResults.postValue(new ArrayList<>());
        databaseResults.postValue(mainVM.getResultsMatchingQuery(query));

        new SearchSocket(query, new SearchSocket.Callback() {
            @Override
            public void onResult(SearchResult result) {
                List<SearchResult> serverResults = SearchViewModel.this.serverResults.getValue();
                serverResults.add(result);
                SearchViewModel.this.serverResults.postValue(serverResults);
                error.postValue("");
            }

            @Override
            public void onDone(List<SearchResult> sortedResults) {
                SearchViewModel.this.serverResults.postValue(sortedResults);
                loading.postValue(false);
            }

            @Override
            public void onMessage(String message) {
                SearchViewModel.this.message.postValue(message);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                loading.postValue(false);
            }

            @Override
            public boolean isInactive() {
                return search_id != search_count;
            }
        });

    }

}
