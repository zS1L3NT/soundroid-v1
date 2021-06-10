package com.zectan.soundroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.R;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.SearchResult;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private static final String TAG = "(SounDroid) SearchAdapter";
    private static final DiffUtil.ItemCallback<SearchResult> DIFF_CALLBACK = new DiffUtil.ItemCallback<SearchResult>() {
        @Override
        public boolean areItemsTheSame(@NonNull @NotNull SearchResult oldItem, @NonNull @NotNull SearchResult newItem) {
            if (oldItem.getSong() != null) {
                if (newItem.getPlaylistInfo() != null) return false;
                else return oldItem.getSong().getId().equals(newItem.getSong().getId());
            } else {
                if (newItem.getSong() != null) return false;
                else
                    return oldItem.getPlaylistInfo().getId().equals(newItem.getPlaylistInfo().getId());
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull @NotNull SearchResult oldItem, @NonNull @NotNull SearchResult newItem) {
            if (oldItem.getSong() != null) {
                if (newItem.getPlaylistInfo() != null) return false;
                else return oldItem.getSong().equals(newItem.getSong());
            } else {
                if (newItem.getSong() != null) return false;
                else return oldItem.getPlaylistInfo().equals(newItem.getPlaylistInfo());
            }
        }
    };
    private final Callback mCallback;
    private final List<SearchResult> mResults;

    public SearchAdapter(Callback callback) {
        this.mCallback = callback;
        this.mResults = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.song_list_item, parent, false);

        return new SearchAdapter.ViewHolder(itemView);
    }

    public void updateResults(List<SearchResult> results) {
        this.mResults.clear();
        this.mResults.addAll(results);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchAdapter.ViewHolder holder, int position) {
        SearchResult result = mResults.get(position);
        Context context = holder.itemView.getContext();

        if (result.getSong() != null) {
            Song song = result.getSong();
            String id = song.getId();
            String title = song.getTitle();
            String artiste = song.getArtiste();
            String cover = song.getCover();
            String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);

            holder.titleText.setText(title);
            holder.descriptionText.setText(String.format("Song • %s", artiste));
            holder.coverImage.setTransitionName(transitionName);
            holder.itemView.setOnClickListener(__ -> mCallback.onSongClicked(song));

            Glide
                .with(context)
                .load(cover)
                .placeholder(R.drawable.playing_cover_default)
                .transition(new DrawableTransitionOptions())
                .error(R.drawable.playing_cover_default)
                .centerCrop()
                .into(holder.coverImage);
        } else if (result.getPlaylistInfo() != null) {
            PlaylistInfo info = result.getPlaylistInfo();
            String id = info.getId();
            String name = info.getName();
            String cover = info.getCover();
            String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);

            holder.titleText.setText(name);
            holder.descriptionText.setText(context.getString(R.string.playlist));
            holder.coverImage.setTransitionName(transitionName);
            holder.itemView.setOnClickListener(__ -> mCallback.onPlaylistClicked(info));

            Glide
                .with(context)
                .load(cover)
                .error(R.drawable.playing_cover_default)
                .centerCrop()
                .into(holder.coverImage);
        }
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

    public interface Callback {
        void onSongClicked(Song song);

        void onPlaylistClicked(PlaylistInfo info);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View itemView;
        public final ImageView coverImage;
        public final TextView titleText, descriptionText;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            coverImage = itemView.findViewById(R.id.song_list_item_cover);
            titleText = itemView.findViewById(R.id.song_list_item_title);
            descriptionText = itemView.findViewById(R.id.song_list_item_artiste);
        }

    }

}
