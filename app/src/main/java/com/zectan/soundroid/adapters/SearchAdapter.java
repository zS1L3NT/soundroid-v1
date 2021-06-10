package com.zectan.soundroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.SongListItemBinding;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.SearchResult;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
    private static final String TAG = "(SounDroid) SearchAdapter";
    private final Callback mCallback;
    private final List<SearchResult> results;

    public SearchAdapter(Callback callback) {
        mCallback = callback;
        results = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.song_list_item, parent, false);

        return new SearchViewHolder(itemView, mCallback);
    }

    public void updateResults(List<SearchResult> results) {
        this.results.clear();
        this.results.addAll(results);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchViewHolder holder, int position) {
        holder.bind(results.get(position));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public interface Callback {
        void onSongClicked(Song song);

        void onPlaylistClicked(PlaylistInfo info);
    }

}

class SearchViewHolder extends RecyclerView.ViewHolder {
    private final SongListItemBinding B;
    private final SearchAdapter.Callback mCallback;

    public SearchViewHolder(@NonNull @NotNull View itemView, SearchAdapter.Callback callback) {
        super(itemView);
        B = SongListItemBinding.bind(itemView);
        mCallback = callback;
    }

    public void bind(SearchResult result) {
        Context context = B.parent.getContext();

        if (result.getSong() != null) {
            Song song = result.getSong();
            String id = song.getId();
            String title = song.getTitle();
            String artiste = song.getArtiste();
            String cover = song.getCover();
            String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);

            B.titleText.setText(title);
            B.descriptionText.setText(String.format("Song • %s", artiste));
            B.coverImage.setTransitionName(transitionName);
            B.parent.setOnClickListener(__ -> mCallback.onSongClicked(song));

            Glide
                .with(context)
                .load(cover)
                .placeholder(R.drawable.playing_cover_default)
                .transition(new DrawableTransitionOptions())
                .error(R.drawable.playing_cover_default)
                .centerCrop()
                .into(B.coverImage);
        } else if (result.getPlaylistInfo() != null) {
            PlaylistInfo info = result.getPlaylistInfo();
            String id = info.getId();
            String name = info.getName();
            String cover = info.getCover();
            String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);

            B.titleText.setText(name);
            B.descriptionText.setText(context.getString(R.string.playlist));
            B.coverImage.setTransitionName(transitionName);
            B.parent.setOnClickListener(__ -> mCallback.onPlaylistClicked(info));

            Glide
                .with(context)
                .load(cover)
                .error(R.drawable.playing_cover_default)
                .centerCrop()
                .into(B.coverImage);
        }
    }
}