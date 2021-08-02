package com.zectan.soundroid.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.DiffCallbacks.InfoDiffCallback;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.Playable;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.MenuBuilder;
import com.zectan.soundroid.databinding.PlaylistListItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistViewHolder> {
    private final Callback mCallback;
    private final List<Playlist> mPlaylists = new ArrayList<>();

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

    public void updateInfos(List<Playlist> playlists) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new InfoDiffCallback(mPlaylists, playlists));
        diffResult.dispatchUpdatesTo(this);
        mPlaylists.clear();
        mPlaylists.addAll(playlists);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlaylistViewHolder holder, int position) {
        holder.bind(mPlaylists.get(position));
    }

    @Override
    public int getItemCount() {
        return mPlaylists.size();
    }

    public interface Callback extends MenuBuilder.MenuItemCallback<Playlist> {
        void onPlaylistClicked(Playlist playlist);
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

    public void bind(Playlist playlist) {
        MainActivity activity = (MainActivity) B.parent.getContext();
        Playable playable = new Playable(playlist, activity.mMainVM.getSongsFromPlaylist(playlist.getId()));

        String name = playlist.getName();
        String cover = playlist.getCover();
        String songCount = playlist.getOrder().size() + " songs";

        B.titleText.setText(name);
        B.descriptionText.setText(songCount);
        B.downloadedDot.setAlpha(playable.isDownloaded(activity) ? 1 : 0);
        Glide
            .with(activity)
            .load(cover)
            .placeholder(R.drawable.playing_cover_loading)
            .error(R.drawable.playing_cover_failed)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);
        B.parent.setOnClickListener(__ -> mCallback.onPlaylistClicked(playlist));

        B.menuClickable.setOnClickListener(v -> MenuBuilder.createMenu(v, MenuBuilder.MenuItems.forPlaylist(new Playable(playlist, activity.mMainVM.getSongsFromPlaylist(playlist.getId())), activity), playlist, mCallback));
    }

}