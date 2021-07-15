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
import com.zectan.soundroid.databinding.SongListItemBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.SearchResult;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.MenuItemsBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
    private static final String TAG = "(SounDroid) SearchAdapter";
    private final Callback mCallback;
    private final List<SearchResult> mResults;
    private Song mCurrentSong, mPreviousSong;

    public SearchAdapter(Callback callback) {
        mCallback = callback;
        mResults = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.song_list_item, parent, false);

        return new SearchViewHolder(itemView, mCallback);
    }

    public void updateResults(List<SearchResult> results) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SearchDiffCallback(
            mResults,
            results,
            mCurrentSong,
            mPreviousSong
        ));
        diffResult.dispatchUpdatesTo(this);
        mResults.clear();
        mResults.addAll(results);
    }

    public void updateCurrentSong(Song song) {
        mPreviousSong = mCurrentSong;
        mCurrentSong = song;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SearchDiffCallback(
            mResults,
            mResults,
            mCurrentSong,
            mPreviousSong
        ));
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchViewHolder holder, int position) {
        holder.bind(mResults, mCurrentSong, position);
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

    public interface Callback extends MenuItemsBuilder.MenuItemCallback<SearchResult> {
        void onSongClicked(Song song);

        void onPlaylistClicked(Info info);
    }

}

class SearchViewHolder extends RecyclerView.ViewHolder {
    private final SongListItemBinding B;
    private final SearchAdapter.Callback mCallback;

    public SearchViewHolder(@NonNull @NotNull View itemView, SearchAdapter.Callback callback) {
        super(itemView);
        B = SongListItemBinding.bind(itemView);
        mCallback = callback;
    }

    public void bind(List<SearchResult> results, Song currentSong, int position) {
        if (B == null) return;

        SearchResult result = results.get(position);
        MainActivity activity = (MainActivity) B.parent.getContext();

        if (result.getSong() != null) {
            Song song = result.getSong();
            String title = song.getTitle();
            String artiste = song.getArtiste();
            String cover = song.getCover();

            B.titleText.setText(title);
            B.descriptionText.setText(String.format("%s • Song • %s", result.getLocation(), artiste));
            B.downloadedDot.setAlpha(song.isDownloaded(activity) ? 1 : 0);
            Glide
                .with(activity)
                .load(cover)
                .placeholder(R.drawable.playing_cover_default)
                .error(R.drawable.playing_cover_default)
                .transition(new DrawableTransitionOptions().crossFade())
                .centerCrop()
                .into(B.coverImage);

            if (currentSong == null || song.getSongId().equals(currentSong.getSongId())) {
                B.titleText.setTextColor(activity.getColor(R.color.green));
                B.descriptionText.setTextColor(activity.getColor(R.color.green));
                B.menuClickable.setTextColor(activity.getColor(R.color.green));
            } else {
                B.titleText.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
                B.descriptionText.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
                B.menuClickable.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
            }

            B.parent.setOnClickListener(__ -> mCallback.onSongClicked(song));
            B.menuClickable.setOnClickListener(v -> {
                @MenuRes int menu_id;
                if (song.isDownloaded(v.getContext())) {
                    menu_id = R.menu.song_menu_downloaded;
                } else {
                    menu_id = R.menu.song_menu;
                }

                MenuItemsBuilder.createMenu(v, menu_id, result, mCallback);
            });
        } else if (result.getPlaylistInfo() != null) {
            Info info = result.getPlaylistInfo();
            String name = info.getName();
            String cover = info.getCover();
            @MenuRes int menu_id;

            switch (result.getLocation()) {
                case "Local":
                    menu_id = R.menu.playlist_menu_search_local;
                    break;
                case "Server":
                    menu_id = R.menu.playlist_menu_search_server;
                    break;
                default:
                    throw new RuntimeException(String.format("Unknown result location %s", result.getLocation()));
            }

            if (result.getLocation().equals("Local")) {
                Playlist playlist = new Playlist(info, activity.mainVM.getSongsFromPlaylist(info.getId()));
                B.downloadedDot.setAlpha(playlist.isDownloaded(activity) ? 1 : 0);
            }

            B.titleText.setText(name);
            B.descriptionText.setText(String.format("%s • Playlist", result.getLocation()));
            Glide
                .with(activity)
                .load(cover)
                .error(R.drawable.playing_cover_default)
                .centerCrop()
                .into(B.coverImage);
            B.titleText.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
            B.descriptionText.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
            B.menuClickable.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
            B.parent.setOnClickListener(__ -> mCallback.onPlaylistClicked(info));
            B.menuClickable.setOnClickListener(v -> MenuItemsBuilder.createMenu(
                v,
                menu_id,
                result,
                mCallback
            ));
        }
    }
}

class SearchDiffCallback extends DiffUtil.Callback {

    private final List<SearchResult> oldResults, newResults;
    private final Song currentSong, previousSong;

    public SearchDiffCallback(List<SearchResult> oldResults, List<SearchResult> newResults, Song currentSong, Song previousSong) {
        this.oldResults = oldResults;
        this.newResults = newResults;
        this.currentSong = currentSong;
        this.previousSong = previousSong;
    }

    @Override
    public int getOldListSize() {
        return oldResults.size();
    }

    @Override
    public int getNewListSize() {
        return newResults.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        SearchResult oldResult = oldResults.get(oldItemPosition);
        SearchResult newResult = newResults.get(newItemPosition);
        return oldResult.getId().equals(newResult.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        SearchResult oldResult = oldResults.get(oldItemPosition);
        SearchResult newResult = newResults.get(newItemPosition);
        return oldResult.equals(newResult)
            && (currentSong == null || !currentSong.getSongId().equals(oldResult.getId()))
            && (previousSong == null || !previousSong.getSongId().equals(oldResult.getId()));
    }
}