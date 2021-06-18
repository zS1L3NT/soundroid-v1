package com.zectan.soundroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.PlaylistListItemBinding;
import com.zectan.soundroid.objects.Info;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistViewHolder> {
    private final Callback mCallback;
    private final List<Info> mInfos = new ArrayList<>();

    public PlaylistsAdapter(Callback callback) {
        mCallback = callback;
    }

    @NonNull
    @NotNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.playlist_list_item, parent, false);

        return new PlaylistViewHolder(itemView, mCallback);
    }

    public void updateInfos(List<Info> infos) {
        mInfos.clear();
        mInfos.addAll(infos);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlaylistViewHolder holder, int position) {
        holder.bind(mInfos.get(position));
    }

    @Override
    public int getItemCount() {
        return mInfos.size();
    }

    public interface Callback {
        void run(Info info);
    }

}

class PlaylistViewHolder extends RecyclerView.ViewHolder {
    private final PlaylistListItemBinding B;
    private final PlaylistsAdapter.Callback mCallback;

    public PlaylistViewHolder(@NonNull @NotNull View itemView, PlaylistsAdapter.Callback callback) {
        super(itemView);
        B = PlaylistListItemBinding.bind(itemView);
        mCallback = callback;
    }

    public void bind(Info info) {
        Context context = B.parent.getContext();

        String name = info.getName();
        String cover = info.getCover();
        String songCount = info.getOrder().size() + " songs";

        B.titleText.setText(name);
        B.descriptionText.setText(songCount);
        B.parent.setOnClickListener(__ -> mCallback.run(info));
        Glide
            .with(context)
            .load(cover)
            .placeholder(R.drawable.playing_cover_default)
            .error(R.drawable.playing_cover_default)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);
    }

}