package com.zectan.soundroid.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.DiffCallbacks.LyricDiffCallback;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.LyricListItemBinding;

import java.util.ArrayList;
import java.util.List;

public class PlayingLyricsAdapter extends RecyclerView.Adapter<PlayingLyricsViewHolder> {
    private final List<String> mLyrics;

    public PlayingLyricsAdapter() {
        mLyrics = new ArrayList<>();
    }

    public void updateLyrics(List<String> lyrics) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new LyricDiffCallback(
            mLyrics,
            lyrics
        ));
        diffResult.dispatchUpdatesTo(this);
        mLyrics.clear();
        mLyrics.addAll(lyrics);
    }

    @NonNull
    @Override
    public PlayingLyricsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.lyric_list_item, parent, false);

        return new PlayingLyricsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayingLyricsViewHolder holder, int position) {
        holder.bind(mLyrics.get(position));
    }

    @Override
    public int getItemCount() {
        return mLyrics.size();
    }
}

class PlayingLyricsViewHolder extends RecyclerView.ViewHolder {
    private final LyricListItemBinding B;

    public PlayingLyricsViewHolder(@NonNull View itemView) {
        super(itemView);
        B = LyricListItemBinding.bind(itemView);
    }

    public void bind(String lyric) {
        B.lyricText.setText(lyric);
    }

}