package com.zectan.soundroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zectan.soundroid.R;
import com.zectan.soundroid.objects.Functions;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewAdapter extends RecyclerView.Adapter<PlaylistViewAdapter.ViewHolder> {
    private final onSongClicked onSongClicked;
    private List<Song> songs;

    public PlaylistViewAdapter(onSongClicked onSongClicked) {
        this.onSongClicked = onSongClicked;
        this.songs = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public PlaylistViewAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.song_list_item, parent, false);

        return new PlaylistViewAdapter.ViewHolder(itemView);
    }

    public void updateSongs(List<Song> songs, List<String> order) {
        this.songs = Functions.sortSongs(songs, order);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlaylistViewAdapter.ViewHolder holder, int position) {
        Song song = songs.get(position);
        Context context = holder.itemView.getContext();

        String id = song.getId();
        String title = song.getTitle();
        String artiste = song.getArtiste();
        String cover = song.getCover();
        String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);

        holder.titleText.setText(title);
        holder.artisteText.setText(artiste);
        holder.coverImage.setTransitionName(transitionName);
        Glide
            .with(context)
            .load(cover)
            .error(R.drawable.playing_cover_default)
            .centerCrop()
            .into(holder.coverImage);
        holder.itemView.setOnClickListener(__ -> onSongClicked.run(holder.coverImage, transitionName, song, position));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public interface onSongClicked {
        void run(ImageView cover, String transitionName, Song song, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View itemView;
        public final ImageView coverImage;
        public final TextView titleText, artisteText;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            coverImage = itemView.findViewById(R.id.song_list_item_cover);
            titleText = itemView.findViewById(R.id.song_list_item_title);
            artisteText = itemView.findViewById(R.id.song_list_item_artiste);
        }
    }
}
