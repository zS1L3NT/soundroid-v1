package com.zectan.soundroid.adapters;

import android.content.Context;
import android.util.Log;
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
    private static final int FOOTER_VIEW = 1;
    private final Callback mCallback;
    private final List<SearchResult> mResults;

    public SearchAdapter(Callback callback) {
        mCallback = callback;
        mResults = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == FOOTER_VIEW) {
            View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.footer_search, parent, false);

            return new SearchViewHolder(itemView);
        } else {
            View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.song_list_item, parent, false);

            return new SearchViewHolder(itemView, mCallback);
        }

    }

    public void updateResults(List<SearchResult> results, boolean loading) {
        int oldSize = mResults.size();
        int newSize = results.size();
        Log.d(TAG, String.format("START %s %s %s", oldSize, newSize, loading));
        if (newSize < oldSize) {
            mResults.clear();
            mResults.addAll(results);
            if (loading) mResults.add(null);
            notifyItemRangeRemoved(newSize, oldSize - newSize);
        } else if (newSize > oldSize) {
            mResults.clear();
            mResults.addAll(results);
            if (loading) mResults.add(null);
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        } else {
            mResults.clear();
            mResults.addAll(results);
            if (loading) mResults.add(null);
            notifyItemRangeChanged(0, oldSize);
        }
        Log.d(TAG, String.format("END %s %s %s", oldSize, newSize, loading));
    }

    public void updateLoading(boolean loading) {
        if (loading) {
            if (mResults.size() == 0) {
                mResults.add(null);
                notifyItemInserted(0);
            } else if (mResults.get(mResults.size() - 1) != null) {
                mResults.add(null);
                notifyItemInserted(mResults.size() - 1);
            }
        } else {
            if (mResults.size() > 0 && mResults.get(mResults.size() - 1) == null) {
                mResults.remove(mResults.size() - 1);
                notifyItemRemoved(mResults.size());
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchViewHolder holder, int position) {
        holder.bind(mResults, position);
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mResults.get(position) == null) {
            return FOOTER_VIEW;
        }
        return super.getItemViewType(position);
    }

    public interface Callback {
        void onSongClicked(Song song);

        void onPlaylistClicked(PlaylistInfo info);
    }

}

class SearchViewHolder extends RecyclerView.ViewHolder {
    private SongListItemBinding B;
    private SearchAdapter.Callback mCallback;

    public SearchViewHolder(@NonNull @NotNull View itemView, SearchAdapter.Callback callback) {
        super(itemView);
        B = SongListItemBinding.bind(itemView);
        mCallback = callback;
    }

    public SearchViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
    }

    public void bind(List<SearchResult> results, int position) {
        if (B == null) return;

        SearchResult result = results.get(position);
        Context context = B.parent.getContext();

        if (result.getSong() != null) {
            Song song = result.getSong();
            String id = song.getId();
            String title = song.getTitle();
            String artiste = song.getArtiste();
            String cover = song.getCover();
            String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);

            B.titleText.setText(title);
            B.descriptionText.setText(String.format("%s • Song • %s", result.getLocation(), artiste));
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
            B.descriptionText.setText(String.format("%s • Playlist", result.getLocation()));
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