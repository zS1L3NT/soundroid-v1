package com.zectan.soundroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.SongListItemBinding;
import com.zectan.soundroid.objects.Anonymous;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewAdapter extends RecyclerView.Adapter<PlaylistViewViewHolder> {
    private final Callback mCallback;
    private final List<Song> mSongs;

    public PlaylistViewAdapter(Callback callback) {
        mCallback = callback;
        mSongs = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public PlaylistViewViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.song_list_item, parent, false);

        return new PlaylistViewViewHolder(itemView, mCallback);
    }

    public void updateSongs(List<Song> songs, List<String> order) {
        mSongs.clear();
        mSongs.addAll(Anonymous.sortSongs(songs, order));
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlaylistViewViewHolder holder, int position) {
        holder.bind(mSongs, position);
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public interface Callback {
        void onSongClicked(ImageView cover, String transitionName, String songId);

        void onMenuClicked(Song song);
    }
}

class PlaylistViewViewHolder extends RecyclerView.ViewHolder {
    private final SongListItemBinding B;
    private final PlaylistViewAdapter.Callback mCallback;

    public PlaylistViewViewHolder(@NonNull @NotNull View itemView, PlaylistViewAdapter.Callback callback) {
        super(itemView);
        B = SongListItemBinding.bind(itemView);
        mCallback = callback;
    }

    public void bind(List<Song> songs, int position) {
        Song song = songs.get(position);
        Context context = B.parent.getContext();

        String id = song.getId();
        String title = song.getTitle();
        String artiste = song.getArtiste();
        String cover = song.getCover();
        String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);

        B.coverImage.setTransitionName(transitionName);
        B.titleText.setText(title);
        B.descriptionText.setText(artiste);
        B.menuImage.setOnClickListener(__ -> mCallback.onMenuClicked(song));
        Glide
            .with(context)
            .load(cover)
            .placeholder(R.drawable.playing_cover_default)
            .error(R.drawable.playing_cover_default)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);
        B.parent.setOnClickListener(__ -> mCallback.onSongClicked(B.coverImage, transitionName, id));
    }
}
