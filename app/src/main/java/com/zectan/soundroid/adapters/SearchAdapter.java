package com.zectan.soundroid.adapters;

import android.content.Context;
import android.util.Log;
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
import com.zectan.soundroid.objects.Animations;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private static final String TAG = "(SounDroid) SearchAdapter";
    private final List<Song> songs;
    private final ItemOnClick itemOnClick;

    public SearchAdapter(List<Song> songs, ItemOnClick itemOnClick) {
        this.songs = songs;
        this.itemOnClick = itemOnClick;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.song_list_item, parent, false);

        return new SearchAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchAdapter.ViewHolder holder, int position) {
        Song song = songs.get(position);
        Context context = holder.itemView.getContext();

        String id = song.getId();
        String title = song.getTitle();
        String artiste = song.getArtiste();
        String cover = song.getCover();


        holder.titleText.setText(title);
        holder.artisteText.setText(artiste);
        Glide
                .with(context)
                .load(cover)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.coverImage);
        holder.itemView.setOnTouchListener(Animations::songListItemSqueeze);
        holder.itemView.setOnClickListener(__ -> {
            Log.i(TAG, String.format("SEARCH_RESULT_CLICKED: %s", song));
            String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_cover), id);
            itemOnClick.run(holder.coverImage, transitionName, song, position);
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public interface ItemOnClick {
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
