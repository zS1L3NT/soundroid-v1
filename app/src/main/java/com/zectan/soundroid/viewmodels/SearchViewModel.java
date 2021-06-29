package com.zectan.soundroid.viewmodels;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.connection.SearchSocket;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.SearchResult;
import com.zectan.soundroid.models.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SearchViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) SearchViewModel";
    private static final int LOCATIONS = 3;
    private final FirebaseRepository repository = new FirebaseRepository();
    private final StrictLiveData<List<SearchResult>> serverResults = new StrictLiveData<>(new ArrayList<>());
    private final StrictLiveData<List<SearchResult>> databaseResults = new StrictLiveData<>(new ArrayList<>());
    private final List<Boolean> search_count = new ArrayList<>();
    public final StrictLiveData<List<SearchResult>> results = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<String> query = new StrictLiveData<>("");
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);
    public final MutableLiveData<String> error = new MutableLiveData<>();

    public SearchViewModel() {
        // Required empty public constructor
    }

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
     * Clears the current search results if the search_id is current and the results haven't been cleared <br>
     * This method returns a boolean since StrictLiveData.postValue() doesn't change the value immediately <br>
     * This way, the methods calling this function will know whether to use the current value of a new list <br>
     *
     * @param search_id Search ID passed in
     * @return If the results were cleared
     */
    public boolean postClearOnFirstSearch(int search_id) {
        if ((search_count.size() - 1) == search_id && search_count.get(search_id)) {
            search_count.set(search_id, false);
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
        int search_id = search_count.size();
        if (query.isEmpty()) {
            loading.postValue(false);
            this.results.postValue(new ArrayList<>());
            return;
        }

        loading.postValue(true);
        search_count.add(true);
        AtomicInteger responses = new AtomicInteger(0);
        new SearchSocket(query, context, new SearchSocket.Callback() {
            @Override
            public void onResult(SearchResult result) {
                List<SearchResult> serverResults = postClearOnFirstSearch(search_id)
                    ? new ArrayList<>()
                    : SearchViewModel.this.serverResults.getValue();
                serverResults.add(result);
                SearchViewModel.this.serverResults.postValue(serverResults);
                error.postValue(null);
            }

            @Override
            public void onDone(List<SearchResult> sortedResults) {
                postClearOnFirstSearch(search_id);
                SearchViewModel.this.serverResults.postValue(sortedResults);
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            }

            @Override
            public void onError(String message) {
                postClearOnFirstSearch(search_id);
                error.postValue(message);
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            }

            @Override
            public boolean isInactive() {
                return search_id != search_count.size() - 1;
            }
        });

        repository
            .searchSong(FirebaseRepository.USER_ID, query).get()
            .addOnSuccessListener(snaps -> {
                if (snaps.size() > 0) {
                    List<SearchResult> databaseResults = postClearOnFirstSearch(search_id)
                        ? new ArrayList<>()
                        : this.databaseResults.getValue();
                    databaseResults.addAll(
                        snaps
                            .toObjects(Song.class)
                            .stream()
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
                postClearOnFirstSearch(search_id);
                error.postValue(err.getMessage());
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            });

        repository.searchPlaylist(FirebaseRepository.USER_ID, query).get()
            .addOnSuccessListener(snaps -> {
                if (snaps.size() > 0) {
                    List<SearchResult> databaseResults = postClearOnFirstSearch(search_id)
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
                postClearOnFirstSearch(search_id);
                error.postValue(err.getMessage());
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            });
    }

}
