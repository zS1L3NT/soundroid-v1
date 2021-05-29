package com.zectan.soundroid.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.R;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@SuppressLint("UseCompatLoadingForDrawables")
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private static final String TAG = "(SounDroid) SongAdapter";
    private Playlist playlist;
    private final onSongClicked onSongClicked;

    public HomeAdapter(onSongClicked onSongClicked) {
        this.playlist = new Playlist(new PlaylistInfo("", "All Songs", new ArrayList<>()), new ArrayList<>());
        this.onSongClicked = onSongClicked;
    }

    @NonNull
    @NotNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.song_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    public void updatePlaylist(Playlist playlist) {
        this.playlist = playlist;
        notifyDataSetChanged();
    }

    public void onBindViewHolder(@NonNull HomeAdapter.ViewHolder holder, int position) {
        Song song = playlist.getSong(position);
        Context context = holder.itemView.getContext();

        String id = song.getId();
        String title = song.getTitle();
        String artiste = song.getArtiste();
        String cover = song.getCover();
        String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);

        holder.titleText.setText(title);
        holder.artisteText.setText(artiste);
        Glide
                .with(context)
                .load(cover)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.coverImage);
        holder.itemView.setOnClickListener(__ -> onSongClicked.run(holder.coverImage, transitionName, playlist, position));
    }

    @Override
    public int getItemCount() {
        return playlist.size();
    }

    public interface onSongClicked {
        void run(ImageView cover, String transitionName, Playlist playlist, int position);
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
