package com.zectan.soundroid.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.SongListItemBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.MenuItemsBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressLint("UseCompatLoadingForDrawables")
public class HomeAdapter extends RecyclerView.Adapter<HomeViewHolder> {
    private final Callback mCallback;
    private final List<Song> mSongs;

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
            sortedSongs
        ));
        diffResult.dispatchUpdatesTo(this);
        mSongs.clear();
        mSongs.addAll(sortedSongs);
    }

    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        holder.bind(mSongs, position);
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public interface Callback extends MenuItemsBuilder.MenuItemCallback<Song> {
        void onSongClicked(ImageView cover, String transitionName, Playlist playlist, String songId);
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
    public void bind(List<Song> songs, int position) {
        Song song = songs.get(position);
        Context context = B.parent.getContext();

        String id = song.getSongId();
        String title = song.getTitle();
        String artiste = song.getArtiste();
        String cover = song.getCover();
        String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);

        B.coverImage.setTransitionName(transitionName);
        B.titleText.setText(title);
        B.descriptionText.setText(artiste);
        Glide
            .with(context)
            .load(cover)
            .placeholder(R.drawable.playing_cover_default)
            .error(R.drawable.playing_cover_default)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);

        List<String> order = songs
            .stream()
            .map(Song::getSongId)
            .collect(Collectors.toList());
        Info info = new Info("", "All Songs", order);
        Playlist playlist = new Playlist(info, songs);

        B.parent.setOnClickListener(__ -> mCallback.onSongClicked(B.coverImage, transitionName, playlist, id));
        B.menuClickable.setOnClickListener(v -> MenuItemsBuilder.createMenu(v, R.menu.song_menu, song, mCallback));
    }
}

class HomeDiffCallback extends DiffUtil.Callback {

    private final List<Song> oldSongs, newSongs;

    public HomeDiffCallback(List<Song> oldSongs, List<Song> newSongs) {
        this.oldSongs = oldSongs;
        this.newSongs = newSongs;
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
        return oldSong.equals(newSong);
    }
}