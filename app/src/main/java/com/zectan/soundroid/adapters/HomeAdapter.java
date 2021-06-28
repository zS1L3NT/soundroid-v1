package com.zectan.soundroid.adapters;

import android.annotation.SuppressLint;
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
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.MenuItemsBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@SuppressLint("UseCompatLoadingForDrawables")
public class HomeAdapter extends RecyclerView.Adapter<HomeViewHolder> {
    private final Callback mCallback;
    private Playlist mPlaylist;

    public HomeAdapter(Callback callback) {
        mPlaylist = new Playlist(new Info("", "All Songs", new ArrayList<>()), new ArrayList<>());
        mCallback = callback;
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

    public void updatePlaylist(Playlist playlist) {
        mPlaylist = playlist;
        notifyDataSetChanged();
    }

    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        holder.bind(mPlaylist, position);
    }

    @Override
    public int getItemCount() {
        return mPlaylist.size();
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
    public void bind(Playlist playlist, int position) {
        Song song = playlist.getSong(position);
        Context context = B.parent.getContext();

        String id = song.getId();
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
        B.songClickable.setOnClickListener(__ -> mCallback.onSongClicked(B.coverImage, transitionName, playlist, id));
        B.menuClickable.setOnClickListener(v -> MenuItemsBuilder.createMenu(v, R.menu.song_menu_home, song, mCallback));
    }
}
