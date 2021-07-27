package com.zectan.soundroid.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.DiffCallbacks.SongsReorderDiffCallback;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.SongReorderListItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayingAdapter extends RecyclerView.Adapter<PlayingViewHolder> implements ItemTouchHelperAdapter {
    private final Callback mCallback;
    private final List<Song> mSongs;

    public PlayingAdapter(Callback callback) {
        mCallback = callback;
        mSongs = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public PlayingViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.song_reorder_list_item, parent, false);

        return new PlayingViewHolder(itemView, mCallback);
    }

    public void updateSongs(List<Song> songs) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SongsReorderDiffCallback(
            mSongs,
            songs
        ));
        diffResult.dispatchUpdatesTo(this);
        mSongs.clear();
        mSongs.addAll(songs);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlayingViewHolder holder, int position) {
        holder.bind(mSongs.get(position));
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mSongs, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mSongs, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        mCallback.onMove(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        Song song = mSongs.remove(position);
        mCallback.onRemove(song.getSongId());
        notifyItemRemoved(position);
    }

    public interface Callback {
        void onSongClicked(Song song);

        void onMove(int oldPosition, int newPosition);

        void onRemove(String songId);

        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public static class PlayingItemTouchHelper extends ItemTouchHelper.Callback {

        private final ItemTouchHelperAdapter mAdapter;

        public PlayingItemTouchHelper(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public int getMovementFlags(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public boolean onMove(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NotNull RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }
}

class PlayingViewHolder extends RecyclerView.ViewHolder {
    public final SongReorderListItemBinding B;
    private final PlayingAdapter.Callback mCallback;

    public PlayingViewHolder(@NotNull View itemView, PlayingAdapter.Callback callback) {
        super(itemView);
        B = SongReorderListItemBinding.bind(itemView);
        mCallback = callback;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void bind(Song song) {
        Context context = B.parent.getContext();

        String title = song.getTitle();
        String artiste = song.getArtiste();
        String cover = song.getCover();

        B.titleText.setText(title);
        B.descriptionText.setText(artiste);
        B.downloadedDot.setAlpha(song.isDownloaded(context) ? 1 : 0);
        Glide
            .with(context)
            .load(cover)
            .placeholder(R.drawable.playing_cover_loading)
            .error(R.drawable.playing_cover_failed)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);
        B.parent.setOnClickListener(__ -> mCallback.onSongClicked(song));
        B.dragImage.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mCallback.onStartDrag(this);
            }
            return false;
        });
    }

}