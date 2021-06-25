package com.zectan.soundroid.viewmodels;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.objects.Info;
import com.zectan.soundroid.objects.SearchResult;
import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.sockets.SearchSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SearchViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) SearchViewModel";
    private static final String USER_ID = "admin";
    private static final int LOCATIONS = 3;
    private final FirebaseRepository repository = new FirebaseRepository();
    private final StrictLiveData<List<SearchResult>> serverResults = new StrictLiveData<>(new ArrayList<>());
    private final StrictLiveData<List<SearchResult>> databaseResults = new StrictLiveData<>(new ArrayList<>());
    public MutableLiveData<String> error = new MutableLiveData<>();
    public StrictLiveData<List<SearchResult>> results = new StrictLiveData<>(new ArrayList<>());
    public StrictLiveData<String> query = new StrictLiveData<>("");
    public StrictLiveData<Boolean> loading = new StrictLiveData<>(false);

    private int search_count = 0;
    private AtomicBoolean first;

    public SearchViewModel() {
        // Required empty public constructor
    }

    /**
     * We want <b>serverResults</b> and <b>databaseResults</b> to combine to form <b>results</b>.
     * So we observe both and set results when either changes. We start watching in the MainActivity
     *
     * @param activity MainActivity
     */
    public void watchResults(MainActivity activity) {
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

    public boolean postClearOnFirstSearch() {
        if (first.compareAndSet(true, false)) {
            results.postValue(new ArrayList<>());
            serverResults.postValue(new ArrayList<>());
            databaseResults.postValue(new ArrayList<>());
            return true;
        }
        return false;
    }

    /**
     * Search the server for results
     *
     * @param query   Text that the user gave
     * @param context Context for the song object
     */
    public void search(String query, Context context) {
        int search_id = ++search_count;
        first = new AtomicBoolean(true);
        if (query.isEmpty()) {
            loading.postValue(false);
            this.results.postValue(new ArrayList<>());
            return;
        }

        loading.postValue(true);
        AtomicInteger responses = new AtomicInteger(0);
        new SearchSocket(query, context, new SearchSocket.Callback() {
            @Override
            public void onResult(SearchResult result) {
                postClearOnFirstSearch();
                List<SearchResult> results = SearchViewModel.this.serverResults.getValue();
                results.add(result);
                SearchViewModel.this.serverResults.postValue(results);
                error.postValue(null);
            }

            @Override
            public void onDone(List<SearchResult> sortedResults) {
                postClearOnFirstSearch();
                SearchViewModel.this.serverResults.postValue(sortedResults);
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            }

            @Override
            public void onError(String message) {
                postClearOnFirstSearch();
                error.postValue(message);
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            }

            @Override
            public boolean isInactive() {
                return search_id != search_count;
            }
        });

        repository.searchSong(USER_ID, query).get()
            .addOnSuccessListener(snaps -> {
                if (snaps.size() > 0) {
                    List<SearchResult> databaseResults = postClearOnFirstSearch()
                        ? new ArrayList<>()
                        : this.databaseResults.getValue();
                    databaseResults.addAll(
                        snaps
                            .toObjects(Song.class)
                            .stream()
                            .map(song -> song.setDirectoryWith(context))
                            .map(song -> new SearchResult(song, context))
                            .collect(Collectors.toList())
                    );
                    this.databaseResults.postValue(databaseResults);
                }
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            })
            .addOnFailureListener(err -> {
                postClearOnFirstSearch();
                error.postValue(err.getMessage());
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            });

        repository.searchPlaylist(USER_ID, query).get()
            .addOnSuccessListener(snaps -> {
                if (snaps.size() > 0) {
                    List<SearchResult> databaseResults = postClearOnFirstSearch()
                        ? new ArrayList<>()
                        : this.databaseResults.getValue();
                    databaseResults.addAll(
                        snaps
                            .toObjects(Info.class)
                            .stream()
                            .map(SearchResult::new)
                            .collect(Collectors.toList())
                    );
                    this.databaseResults.postValue(databaseResults);
                }
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            })
            .addOnFailureListener(err -> {
                postClearOnFirstSearch();
                error.postValue(err.getMessage());
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            });
    }

}
