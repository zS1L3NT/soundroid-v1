package com.zectan.soundroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.R;
import com.zectan.soundroid.objects.Playlist;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private final List<Playlist> playlists;

    public PlaylistAdapter(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.playlist_list_item, parent, false);

        return new PlaylistAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlaylistAdapter.ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);

        String name = playlist.getName();
        String songCount = playlist.size() + " songs";

        holder.nameText.setText(name);
        holder.songCountText.setText(songCount);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View itemView;
        public final ImageView coverImage;
        public final TextView nameText, songCountText;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            coverImage = itemView.findViewById(R.id.playlist_list_item_cover);
            nameText = itemView.findViewById(R.id.playlist_list_item_name);
            songCountText = itemView.findViewById(R.id.playlist_list_item_song_count);
        }
    }

}
