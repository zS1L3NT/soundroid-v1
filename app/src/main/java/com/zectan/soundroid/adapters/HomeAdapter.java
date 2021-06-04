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
import com.zectan.soundroid.R;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@SuppressLint("UseCompatLoadingForDrawables")
public class HomeAdapter extends RecyclerView.Adapter<SongViewHolder> {
    private static final String TAG = "(SounDroid) SongAdapter";
    private Playlist playlist;
    private final Callback callback;
    
    public HomeAdapter(Callback Callback) {
        this.playlist = new Playlist(new PlaylistInfo("", "All Songs", new ArrayList<>()), new ArrayList<>());
        this.callback = Callback;
    }
    
    @NonNull
    @NotNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.song_list_item, parent, false);
        
        return new SongViewHolder(itemView);
    }
    
    public void updatePlaylist(Playlist playlist) {
        this.playlist = playlist;
        notifyDataSetChanged();
    }
    
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = playlist.getSong(position);
        Context context = holder.itemView.getContext();
        
        String id = song.getId();
        String title = song.getTitle();
        String artiste = song.getArtiste();
        String cover = song.getCover();
        String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);
        
        holder.titleText.setText(title);
        holder.artisteText.setText(artiste);
        holder.menuImage.setOnClickListener(__ -> callback.onMenuClicked(song));
        Glide
            .with(context)
            .load(cover)
            .into(holder.coverImage);
        holder.itemView.setOnClickListener(__ -> callback.onSongClicked(holder.coverImage, transitionName, playlist, position));
    }
    
    @Override
    public int getItemCount() {
        return playlist.size();
    }
    
    public interface Callback {
        void onSongClicked(ImageView cover, String transitionName, Playlist playlist, int position);
        
        void onMenuClicked(Song song);
    }
}
