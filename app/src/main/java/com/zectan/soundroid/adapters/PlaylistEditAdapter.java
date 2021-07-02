package com.zectan.soundroid.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.SongReorderListItemBinding;
import com.zectan.soundroid.models.Song;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class PlaylistEditAdapter extends DragDropSwipeAdapter<Song, PlaylistEditViewHolder> {

    public PlaylistEditAdapter() {
        setDataSet(new ArrayList<>());
    }

    @Override
    protected @NotNull PlaylistEditViewHolder getViewHolder(@NotNull View itemView) {
        return new PlaylistEditViewHolder(itemView);
    }

    @Override
    protected @Nullable View getViewToTouchToStartDraggingItem(Song song, @NotNull PlaylistEditViewHolder holder, int i) {
        return holder.getDragImage();
    }

    @Override
    protected void onBindViewHolder(Song song, @NotNull PlaylistEditViewHolder holder, int i) {
        holder.bind(song);
    }
}

class PlaylistEditViewHolder extends DragDropSwipeAdapter.ViewHolder {
    public final SongReorderListItemBinding B;

    public PlaylistEditViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        B = SongReorderListItemBinding.bind(itemView);
    }

    public void bind(Song song) {
        Context context = B.parent.getContext();

        String title = song.getTitle();
        String artiste = song.getArtiste();
        String cover = song.getCover();

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
    }

    public ImageView getDragImage() {
        return B.dragImage;
    }

}