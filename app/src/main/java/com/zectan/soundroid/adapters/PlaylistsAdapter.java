package com.zectan.soundroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.PlaylistListItemBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.utils.MenuItemsBuilder;

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
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PlaylistsDiffCallback(mInfos, infos));
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

    public interface Callback extends MenuItemsBuilder.MenuItemCallback<Info> {
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
            .placeholder(R.drawable.playing_cover_default)
            .error(R.drawable.playing_cover_default)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);
        B.parent.setOnClickListener(__ -> mCallback.onPlaylistClicked(info));

        B.menuClickable.setOnClickListener(v -> {
            @MenuRes int menu_id;
            Playlist playlist_ = new Playlist(info, activity.mainVM.getSongsFromPlaylist(info.getId()));

            if (playlist_.isDownloaded(activity)) {
                menu_id = R.menu.playlist_menu_playlists_delete;
            } else if (playlist_.hasDownloaded(activity)) {
                menu_id = R.menu.playlist_menu_playlist_both;
            } else {
                menu_id = R.menu.playlist_menu_playlists_download;
            }

            MenuItemsBuilder.createMenu(v, menu_id, info, mCallback);
        });
    }

}

class PlaylistsDiffCallback extends DiffUtil.Callback {

    private final List<Info> oldInfos, newInfos;

    public PlaylistsDiffCallback(List<Info> oldInfos, List<Info> newInfos) {
        this.oldInfos = oldInfos;
        this.newInfos = newInfos;
    }

    @Override
    public int getOldListSize() {
        return oldInfos.size();
    }

    @Override
    public int getNewListSize() {
        return newInfos.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Info oldInfo = oldInfos.get(oldItemPosition);
        Info newInfo = newInfos.get(newItemPosition);
        return oldInfo.getId().equals(newInfo.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Info oldInfo = oldInfos.get(oldItemPosition);
        Info newInfo = newInfos.get(newItemPosition);
        return oldInfo.equals(newInfo);
    }
}