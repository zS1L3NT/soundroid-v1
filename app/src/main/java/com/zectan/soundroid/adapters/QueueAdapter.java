package com.zectan.soundroid.adapters;

import android.content.Context;
import android.view.View;

import com.bumptech.glide.Glide;
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter;
import com.zectan.soundroid.R;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QueueAdapter extends DragDropSwipeAdapter<Song, QueueViewHolder> {
    private static final String TAG = "(SounDroid) QueueAdapter";
    private final Callback callback;
    
    public QueueAdapter(Callback callback) {
        this.callback = callback;
    }
    
    public void updateQueue(List<Song> queue) {
        if (!getDataSet().equals(queue)) {
            setDataSet(queue);
        }
    }
    
    @Override
    protected @NotNull QueueViewHolder getViewHolder(@NotNull View itemView) {
        return new QueueViewHolder(itemView);
    }
    
    @Override
    protected @Nullable View getViewToTouchToStartDraggingItem(Song song, @NotNull QueueViewHolder holder, int i) {
        return holder.dragImage;
    }
    
    @Override
    protected void onBindViewHolder(Song song, @NotNull QueueViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        
        String id = song.getId();
        String title = song.getTitle();
        String artiste = song.getArtiste();
        String cover = song.getCover();
        
        holder.idText.setText(id);
        holder.titleText.setText(title);
        holder.artisteText.setText(artiste);
        Glide
            .with(context)
            .load(cover)
            .error(R.drawable.playing_cover_default)
            .centerCrop()
            .into(holder.coverImage);
        holder.itemView.setOnClickListener(__ -> callback.onSongSelected(song));
    }
    
    @Override
    protected void onDragFinished(Song song, @NotNull QueueViewHolder holder) {
        super.onDragFinished(song, holder);
        callback.onReorder(getDataSet());
    }
    
    @Override
    protected void onSwipeAnimationFinished(@NotNull QueueViewHolder holder) {
        super.onSwipeAnimationFinished(holder);
        callback.onReorder(getDataSet());
    }
    
    public interface Callback {
        void onSongSelected(Song song);
        void onReorder(List<Song> songs);
    }
}
