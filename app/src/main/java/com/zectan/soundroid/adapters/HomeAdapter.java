package com.zectan.soundroid.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.SongListItemBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.MenuBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressLint("UseCompatLoadingForDrawables")
public class HomeAdapter extends RecyclerView.Adapter<HomeViewHolder> {
    private final Callback mCallback;
    private final List<Song> mSongs;
    private Song mCurrentSong, mPreviousSong;

    public HomeAdapter(Callback callback) {
        mCallback = callback;
        mSongs = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.song_list_item, parent, false);

        return new HomeViewHolder(itemView, mCallback);
    }

    public void updateSongs(List<Song> songs) {
        List<Song> sortedSongs = songs
            .stream()
            .sorted((song1, song2) -> song1.getTitle().compareTo(song2.getTitle()))
            .collect(Collectors.toList());

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new HomeDiffCallback(
            mSongs,
            sortedSongs,
            mCurrentSong,
            mPreviousSong
        ));
        diffResult.dispatchUpdatesTo(this);
        mSongs.clear();
        mSongs.addAll(sortedSongs);
    }

    public void updateCurrentSong(Song song) {
        mPreviousSong = mCurrentSong;
        mCurrentSong = song;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new HomeDiffCallback(
            mSongs,
            mSongs,
            mCurrentSong,
            mPreviousSong
        ));
        diffResult.dispatchUpdatesTo(this);
    }

    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        holder.bind(mSongs, mCurrentSong, position);
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public interface Callback extends MenuBuilder.MenuItemCallback<Song> {
        void onSongClicked(Playlist playlist, String songId);
    }
}

class HomeViewHolder extends RecyclerView.ViewHolder {
    private final SongListItemBinding B;
    private final HomeAdapter.Callback mCallback;

    public HomeViewHolder(@NonNull @NotNull View itemView, HomeAdapter.Callback callback) {
        super(itemView);
        B = SongListItemBinding.bind(itemView);
        mCallback = callback;
    }

    @SuppressLint("RestrictedApi")
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
            .placeholder(R.drawable.playing_cover_default)
            .error(R.drawable.playing_cover_default)
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

        List<String> order = songs
            .stream()
            .map(Song::getSongId)
            .collect(Collectors.toList());
        Info info = new Info("", "All Songs", order);
        Playlist playlist = new Playlist(info, songs);

        B.parent.setOnClickListener(__ -> mCallback.onSongClicked(playlist, id));

        B.menuClickable.setOnClickListener(v -> MenuBuilder.createMenu(v, MenuBuilder.MenuItems.forSong(song, activity), song, mCallback));
    }
}

class HomeDiffCallback extends DiffUtil.Callback {

    private final List<Song> oldSongs, newSongs;
    private final Song currentSong, previousSong;

    public HomeDiffCallback(List<Song> oldSongs, List<Song> newSongs, Song currentSong, Song previousSong) {
        this.oldSongs = oldSongs;
        this.newSongs = newSongs;
        this.currentSong = currentSong;
        this.previousSong = previousSong;
    }

    @Override
    public int getOldListSize() {
        return oldSongs.size();
    }

    @Override
    public int getNewListSize() {
        return newSongs.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Song oldSong = oldSongs.get(oldItemPosition);
        Song newSong = newSongs.get(newItemPosition);
        return oldSong.getSongId().equals(newSong.getSongId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Song oldSong = oldSongs.get(oldItemPosition);
        Song newSong = newSongs.get(newItemPosition);
        return oldSong.equals(newSong) && !oldSong.equals(currentSong) && !oldSong.equals(previousSong);
    }
}