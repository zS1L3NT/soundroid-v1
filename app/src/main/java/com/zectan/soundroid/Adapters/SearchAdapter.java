package com.zectan.soundroid.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.DiffCallbacks.SearchResultDiffCallback;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.Info;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.SearchResult;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.MenuBuilder;
import com.zectan.soundroid.databinding.SongListItemBinding;

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
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SearchResultDiffCallback(
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
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SearchResultDiffCallback(
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

    public interface Callback extends MenuBuilder.MenuItemCallback<SearchResult> {
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

    @SuppressLint("SetTextI18n")
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
            B.descriptionText.setText(String.format("Song • %s", artiste));
            B.downloadedDot.setAlpha(song.isDownloaded(activity) ? 1 : 0);
            Glide
                .with(activity)
                .load(cover)
                .placeholder(R.drawable.playing_cover_loading)
                .error(R.drawable.playing_cover_failed)
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
            B.menuClickable.setOnClickListener(v -> MenuBuilder.createMenu(v, MenuBuilder.MenuItems.forSong(song, activity, false), result, mCallback));
        } else if (result.getPlaylistInfo() != null) {
            Info info = result.getPlaylistInfo();
            String name = info.getName();
            String cover = info.getCover();

            MenuBuilder.MenuItems items = new MenuBuilder.MenuItems();
            switch (result.getLocation()) {
                case "Local":
                    items.playPlaylist();
                    break;
                case "Server":
                    items.savePlaylist();
                    break;
                default:
                    throw new RuntimeException(String.format("Unknown result location %s", result.getLocation()));
            }

            if (result.getLocation().equals("Local")) {
                Playlist playlist = new Playlist(info, activity.mMainVM.getSongsFromPlaylist(info.getId()));
                B.downloadedDot.setAlpha(playlist.isDownloaded(activity) ? 1 : 0);
            }

            B.titleText.setText(name);
            B.descriptionText.setText("Playlist");
            Glide
                .with(activity)
                .load(cover)
                .placeholder(R.drawable.playing_cover_loading)
                .error(R.drawable.playing_cover_failed)
                .transition(new DrawableTransitionOptions().crossFade())
                .centerCrop()
                .into(B.coverImage);
            B.titleText.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
            B.descriptionText.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
            B.menuClickable.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
            B.parent.setOnClickListener(__ -> mCallback.onPlaylistClicked(info));
            B.menuClickable.setOnClickListener(v -> MenuBuilder.createMenu(
                v,
                items,
                result,
                mCallback
            ));
        }
    }
}