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
import com.zectan.soundroid.objects.PlaylistInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {
    private final onPlaylistClicked onPlaylistClicked;
    private List<PlaylistInfo> infos = new ArrayList<>();

    public PlaylistsAdapter(onPlaylistClicked onPlaylistClicked) {
        this.onPlaylistClicked = onPlaylistClicked;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.playlist_list_item, parent, false);

        return new PlaylistsAdapter.ViewHolder(itemView);
    }

    public void updateInfos(List<PlaylistInfo> infos) {
        this.infos = infos;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlaylistsAdapter.ViewHolder holder, int position) {
        PlaylistInfo info = infos.get(position);
        Context context = holder.itemView.getContext();

        String name = info.getName();
        String cover = info.getCover();
        String songCount = info.getOrder().size() + " songs";

        holder.nameText.setText(name);
        holder.songCountText.setText(songCount);
        holder.itemView.setOnClickListener(__ -> onPlaylistClicked.run(info));
        Glide
            .with(context)
            .load(cover)
            .error(R.drawable.playing_cover_default)
            .centerCrop()
            .into(holder.coverImage);
    }

    @Override
    public int getItemCount() {
        return infos.size();
    }

    public interface onPlaylistClicked {
        void run(PlaylistInfo info);
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
