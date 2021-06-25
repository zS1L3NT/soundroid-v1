package com.zectan.soundroid.viewmodels;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.zectan.soundroid.FirebaseRepository;
import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.objects.Info;
import com.zectan.soundroid.objects.SearchResult;
import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.sockets.SearchSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) SearchViewModel";
    private static final String USER_ID = "admin";
    private static final int LOCATIONS = 3;
    private final FirebaseRepository repository = new FirebaseRepository();
    public MutableLiveData<String> error = new MutableLiveData<>();
    public StrictLiveData<List<SearchResult>> results = new StrictLiveData<>(new ArrayList<>());
    public StrictLiveData<String> query = new StrictLiveData<>("");
    public StrictLiveData<Boolean> loading = new StrictLiveData<>(false);

    private int search_count = 0;

    public SearchViewModel() {
        // Required empty public constructor
    }

    private void pushToResults(SearchResult result) {
        List<SearchResult> results = this.results.getValue();
        results.add(result);
        this.results.postValue(results);
    }

    /**
     * Search the server for results
     *
     * @param query   Text that the user gave
     * @param context Context for the song object
     */
    public void search(String query, Context context) {
        int search_id = ++search_count;
        if (query.isEmpty()) {
            loading.postValue(false);
            this.results.postValue(new ArrayList<>());
            return;
        }

        loading.postValue(true);
        AtomicInteger responses = new AtomicInteger(0);
        AtomicBoolean first = new AtomicBoolean(true);
        new SearchSocket(query, context, new SearchSocket.Callback() {
            @Override
            public void onResult(SearchResult result) {
                if (first.compareAndSet(true, false)) {
                    SearchViewModel.this.results.postValue(new ArrayList<>());
                }
                pushToResults(result);
                error.postValue(null);
            }

            @Override
            public void onDone(List<SearchResult> sortedResults) {
                if (first.compareAndSet(true, false)) {
                    SearchViewModel.this.results.postValue(new ArrayList<>());
                }
                SearchViewModel.this.results.postValue(sortedResults);
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            }

            @Override
            public void onError(String message) {
                if (first.compareAndSet(true, false)) {
                    SearchViewModel.this.results.postValue(new ArrayList<>());
                }
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
                if (snaps.size() > 0 && first.compareAndSet(true, false)) {
                    results.postValue(new ArrayList<>());
                }
                for (int i = 0; i < snaps.size(); i++) {
                    DocumentSnapshot snap = snaps.getDocuments().get(i);
                    Song song = snap.toObject(Song.class);
                    assert song != null;
                    pushToResults(new SearchResult(song, context));
                }
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            })
            .addOnFailureListener(err -> {
                if (first.compareAndSet(true, false)) {
                    results.postValue(new ArrayList<>());
                }
                error.postValue(err.getMessage());
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            });

        repository.searchPlaylist(USER_ID, query).get()
            .addOnSuccessListener(snaps -> {
                if (snaps.size() > 0 && first.compareAndSet(true, false)) {
                    results.postValue(new ArrayList<>());
                }
                for (int i = 0; i < snaps.size(); i++) {
                    DocumentSnapshot snap = snaps.getDocuments().get(i);
                    Info info = snap.toObject(Info.class);
                    assert info != null;
                    pushToResults(new SearchResult(info));
                }
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            })
            .addOnFailureListener(err -> {
                if (first.compareAndSet(true, false)) {
                    results.postValue(new ArrayList<>());
                }
                error.postValue(err.getMessage());
                if (responses.incrementAndGet() == LOCATIONS) {
                    loading.postValue(false);
                }
            });
    }

}
