package com.zectan.soundroid.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.QueueListItemBinding;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QueueAdapter extends DragDropSwipeAdapter<Song, QueueViewHolder> {
    private static final String TAG = "(SounDroid) QueueAdapter";
    private final Callback mCallback;
    private int startDragPosition;
    private String startSwipeId;
    private int startSwipeDataSetSize;

    public QueueAdapter(Callback callback) {
        mCallback = callback;
    }

    public void updateQueue(List<Song> queue) {
        if (!getDataSet().equals(queue)) {
            setDataSet(queue);
        }
    }
    
    @Override
    protected @NotNull QueueViewHolder getViewHolder(@NotNull View itemView) {
        return new QueueViewHolder(itemView, mCallback);
    }

    @Override
    protected @Nullable View getViewToTouchToStartDraggingItem(Song song, @NotNull QueueViewHolder holder, int i) {
        return holder.getDragImage();
    }

    @Override
    protected void onBindViewHolder(Song song, @NotNull QueueViewHolder holder, int position) {
        holder.bind(getDataSet().get(position));
    }

    @Override
    protected void onDragStarted(Song song, @NotNull QueueViewHolder holder) {
        super.onDragStarted(song, holder);
        startDragPosition = holder.getAdapterPosition();
    }

    @Override
    protected void onDragFinished(Song song, @NotNull QueueViewHolder holder) {
        super.onDragFinished(song, holder);
        mCallback.onMove(startDragPosition, holder.getAdapterPosition());
    }

    @Override
    protected void onSwipeStarted(Song song, @NotNull QueueViewHolder holder) {
        super.onSwipeStarted(song, holder);
        startSwipeId = song.getId();
        startSwipeDataSetSize = getItemCount();
    }

    @Override
    protected void onSwipeAnimationFinished(@NotNull QueueViewHolder holder) {
        super.onSwipeAnimationFinished(holder);
        if (startSwipeDataSetSize != getItemCount()) {
            mCallback.onRemove(startSwipeId);
        }
    }

    public interface Callback {
        void onSongSelected(Song song);

        void onMove(int oldPosition, int newPosition);

        void onRemove(String songId);
    }
}

class QueueViewHolder extends DragDropSwipeAdapter.ViewHolder {
    public final QueueListItemBinding B;
    private final QueueAdapter.Callback mCallback;

    public QueueViewHolder(@NonNull @NotNull View itemView, QueueAdapter.Callback callback) {
        super(itemView);
        B = QueueListItemBinding.bind(itemView);
        mCallback = callback;
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
        B.parent.setOnClickListener(__ -> mCallback.onSongSelected(song));
    }

    public ImageView getDragImage() {
        return B.dragImage;
    }

}