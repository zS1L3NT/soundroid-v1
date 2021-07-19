package com.zectan.soundroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.DiffCallbacks.InfoDiffCallback;
import com.zectan.soundroid.databinding.PlaylistListItemBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.utils.MenuBuilder;

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
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new InfoDiffCallback(mInfos, infos));
        diffResult.dispatchUpdatesTo(this);
        mInfos.clear();
        mInfos.addAll(infos);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlaylistViewHolder holder, int position) {
        holder.bind(mInfos.get(position));
    }

    @Override
    public int getItemCount() {
        return mInfos.size();
    }

    public interface Callback extends MenuBuilder.MenuItemCallback<Info> {
        void onPlaylistClicked(Info info);
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
        MainActivity activity = (MainActivity) B.parent.getContext();
        Playlist playlist = new Playlist(info, activity.mainVM.getSongsFromPlaylist(info.getId()));

        String name = info.getName();
        String cover = info.getCover();
        String songCount = info.getOrder().size() + " songs";

        B.titleText.setText(name);
        B.descriptionText.setText(songCount);
        B.downloadedDot.setAlpha(playlist.isDownloaded(activity) ? 1 : 0);
        Glide
            .with(activity)
            .load(cover)
            .placeholder(R.drawable.playing_cover_loading)
            .error(R.drawable.playing_cover_failed)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);
        B.parent.setOnClickListener(__ -> mCallback.onPlaylistClicked(info));

        B.menuClickable.setOnClickListener(v -> MenuBuilder.createMenu(v, MenuBuilder.MenuItems.forPlaylist(new Playlist(info, activity.mainVM.getSongsFromPlaylist(info.getId())), activity), info, mCallback));
    }

}