package com.zectan.soundroid.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.DiffCallbacks.SongsDiffCallback;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.MenuBuilder;
import com.zectan.soundroid.databinding.SongListItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewAdapter extends RecyclerView.Adapter<PlaylistViewViewHolder> {
    private final Callback mCallback;
    private final List<Song> mSongs;
    private Song mCurrentSong, mPreviousSong;

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

    public void updateSongs(List<Song> songs) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SongsDiffCallback(
            mSongs,
            songs,
            mCurrentSong,
            mPreviousSong
        ));
        diffResult.dispatchUpdatesTo(this);
        mSongs.clear();
        mSongs.addAll(songs);
    }

    public void updateCurrentSong(Song song) {
        mPreviousSong = mCurrentSong;
        mCurrentSong = song;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SongsDiffCallback(
            mSongs,
            mSongs,
            mCurrentSong,
            mPreviousSong
        ));
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlaylistViewViewHolder holder, int position) {
        holder.bind(mSongs, mCurrentSong, position);
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public interface Callback extends MenuBuilder.MenuItemCallback<Song> {
        void onSongClicked(String songId);

        boolean isLocal();
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

    public void bind(List<Song> songs, Song currentSong, int position) {
        Song song = songs.get(position);
        MainActivity activity = (MainActivity) B.parent.getContext();

        String id = song.getSongId();
        String title = song.getTitle();
        String artiste = song.getArtiste();
        String cover = song.getCover();

        B.titleText.setText(title);
        B.descriptionText.setText(artiste);
        B.downloadedDot.setAlpha(song.isDownloaded(activity) ? 1 : 0);
        Glide
            .with(activity)
            .load(cover)
            .placeholder(R.drawable.playing_cover_loading)
            .error(R.drawable.playing_cover_failed)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);

        if (song.equals(currentSong)) {
            B.titleText.setTextColor(activity.getColor(R.color.green));
            B.descriptionText.setTextColor(activity.getColor(R.color.green));
            B.menuClickable.setTextColor(activity.getColor(R.color.green));
        } else {
            B.titleText.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
            B.descriptionText.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
            B.menuClickable.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
        }

        B.parent.setOnClickListener(__ -> mCallback.onSongClicked(id));
        B.menuClickable.setOnClickListener(v -> {
            MenuBuilder.MenuItems items = new MenuBuilder.MenuItems();
            if (mCallback.isLocal()) {
                items = MenuBuilder.MenuItems.forSong(song, activity, true);
            } else {
                items.addToQueue();
                items.addToPlaylist();
            }

            MenuBuilder.createMenu(v, items, song, mCallback);
        });
    }
}