package com.zectan.soundroid.ViewModels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.Classes.StrictLiveData;
import com.zectan.soundroid.Connections.SearchSocket;
import com.zectan.soundroid.Models.Info;
import com.zectan.soundroid.Models.SearchResult;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.Utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SearchViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) SearchViewModel";
    public final StrictLiveData<List<SearchResult>> localResults = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<List<SearchResult>> serverResults = new StrictLiveData<>(new ArrayList<>());
    public final StrictLiveData<String> query = new StrictLiveData<>("");
    public final StrictLiveData<Boolean> loading = new StrictLiveData<>(false);
    public final StrictLiveData<String> error = new StrictLiveData<>("");
    public final StrictLiveData<String> message = new StrictLiveData<>("");
    private int search_count = 0;

    public void searchLocal(List<Info> infos, List<Song> songs) {
        if (query.getValue().isEmpty()) {
            loading.postValue(false);
            localResults.postValue(new ArrayList<>());
            serverResults.postValue(new ArrayList<>());
            return;
        }

        List<SearchResult> results = new ArrayList<>();
        String regex = Utils.createRegex(query.getValue());
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        results.addAll(
            infos
                .stream()
                .filter(info -> pattern.matcher(info.getName()).matches())
                .filter(Utils.filterDistinctByKey(Info::getId))
                .map(SearchResult::new)
                .collect(Collectors.toList())
        );
        results.addAll(
            songs
                .stream()
                .filter(song -> pattern.matcher(song.getTitle()).matches() || pattern.matcher(song.getArtiste()).matches())
                .filter(Utils.filterDistinctByKey(Song::getSongId))
                .map(SearchResult::new)
                .collect(Collectors.toList())
        );
        localResults.postValue(results);
    }

    public void searchServer() {
        int search_id = ++search_count;
        if (query.getValue().isEmpty()) {
            loading.postValue(false);
            localResults.postValue(new ArrayList<>());
            serverResults.postValue(new ArrayList<>());
            message.postValue("");
            return;
        }

        loading.postValue(true);
        serverResults.postValue(new ArrayList<>());

        new SearchSocket(query.getValue(), new SearchSocket.Callback() {
            @Override
            public void onError(String message) {
                error.postValue(message);
                loading.postValue(false);
            }

            @Override
            public boolean isInactive() {
                return search_id != search_count;
            }

            @Override
            public void onResult(SearchResult result) {
                List<SearchResult> serverResults = SearchViewModel.this.serverResults.getValue();
                serverResults.add(result);
                SearchViewModel.this.serverResults.postValue(serverResults);
                error.postValue("");
            }

            @Override
            public void onMessage(String message) {
                SearchViewModel.this.message.postValue(message);
            }

            @Override
            public void onDone(List<SearchResult> sortedResults) {
                SearchViewModel.this.serverResults.postValue(sortedResults);
                loading.postValue(false);
            }
        });
    }

}
