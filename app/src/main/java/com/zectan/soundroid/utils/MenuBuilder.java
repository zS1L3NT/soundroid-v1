package com.zectan.soundroid.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.PopupMenu;

import com.zectan.soundroid.DownloadService;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;

import java.util.ArrayList;
import java.util.List;

public class MenuBuilder {
    public static final int ADD_TO_PLAYLIST = 0;
    public static final int ADD_TO_QUEUE = 1;
    public static final int EDIT_SONG = 2;
    public static final int OPEN_QUEUE = 3;
    public static final int CLEAR_QUEUE = 4;
    public static final int START_DOWNLOADS = 5;
    public static final int STOP_DOWNLOADS = 6;
    public static final int CLEAR_DOWNLOADS = 7;
    public static final int REMOVE_DOWNLOAD = 8;
    public static final int SAVE_PLAYLIST = 9;
    public static final int PLAY_PLAYLIST = 10;
    public static final int EDIT_PLAYLIST = 11;
    public static final int DELETE_PLAYLIST = 12;
    public static final int ADD_PLAYLIST = 13;
    public static final int IMPORT_PLAYLIST = 14;

    public static <T> void createMenu(View v, MenuItems items, T object, MenuItemCallback<T> callback) {
        Context context = v.getContext();
        PopupMenu popup = new PopupMenu(context, v);
        for (int i = 0; i < items.getItems().size(); i++) {
            MenuItem item = items.getItems().get(i);
            popup.getMenu().add(0, item.getItemId(), i, item.getTitle()).setIcon(item.getIconId());
        }
        popup.setOnMenuItemClickListener(item -> callback.onMenuItemClicked(object, item));
        inflateIcons(popup, context);
        popup.show();
    }

    @SuppressLint("RestrictedApi")
    private static void inflateIcons(PopupMenu popup, Context context) {
        if (popup.getMenu() instanceof androidx.appcompat.view.menu.MenuBuilder) {
            androidx.appcompat.view.menu.MenuBuilder menuBuilder = (androidx.appcompat.view.menu.MenuBuilder) popup.getMenu();
            menuBuilder.setOptionalIconsVisible(true);
            for (int i = 0; i < menuBuilder.getVisibleItems().size(); i++) {
                MenuItemImpl item = menuBuilder.getVisibleItems().get(i);
                int iconMarginPX = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10f, context.getResources().getDisplayMetrics()
                );

                if (item.getIcon() != null) {
                    Drawable icon = item.getIcon();
                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.colorOnBackground, typedValue, true);
                    icon.setTint(typedValue.data);
                    item.setIcon(new InsetDrawable(icon, iconMarginPX, 0, iconMarginPX, 0));
                }
            }
        }
    }

    public interface MenuItemCallback<T> {
        boolean onMenuItemClicked(T object, android.view.MenuItem item);
    }

    private static class MenuItem {
        private final int mItemId;
        private final String mTitle;
        private final int mIconId;

        public MenuItem(int itemId, String title, int iconId) {
            this.mItemId = itemId;
            this.mTitle = title;
            this.mIconId = iconId;
        }

        public int getItemId() {
            return mItemId;
        }

        public String getTitle() {
            return mTitle;
        }

        public int getIconId() {
            return mIconId;
        }
    }

    public static class MenuItems {
        private final List<MenuItem> mItems;

        public MenuItems() {
            mItems = new ArrayList<>();
        }

        public static MenuItems forPlaylist(Playlist playlist, MainActivity activity) {
            MenuBuilder.MenuItems items = new MenuBuilder.MenuItems();
            if (playlist.isDownloaded(activity)) {
                items.clearDownloads();
            } else {
                // If playlists is being downloaded
                DownloadService.DownloadBinder downloadBinder = activity.mainVM.downloadBinder.getValue();
                if (downloadBinder != null && downloadBinder.isDownloading(playlist.getInfo().getId())) {
                    items.stopDownloads();
                } else {
                    items.startDownloads();
                }

                if (playlist.hasDownloaded(activity)) {
                    items.clearDownloads();
                }
            }
            items.editPlaylist();
            items.deletePlaylist();
            return items;
        }

        public static MenuItems forSong(Song song, Context context, boolean editable) {
            MenuBuilder.MenuItems items = new MenuBuilder.MenuItems();
            if (editable) items.editSong();
            items.addToQueue();
            items.addToPlaylist();
            if (song.isDownloaded(context)) {
                items.removeDownload();
            }
            return items;
        }

        public List<MenuItem> getItems() {
            return mItems;
        }

        /**
         * Add a song to a playlist
         */
        public void addToPlaylist() {
            mItems.add(new MenuItem(ADD_TO_PLAYLIST, "Add To Playlist", R.drawable.ic_add_to_playlist));
        }

        /**
         * Add a song to the queue
         */
        public void addToQueue() {
            mItems.add(new MenuItem(ADD_TO_QUEUE, "Add To Queue", R.drawable.ic_add_to_queue));
        }

        /**
         * Edit song details
         */
        public void editSong() {
            mItems.add(new MenuItem(EDIT_SONG, "Edit Song", R.drawable.ic_edit));
        }

        /**
         * See the queue
         */
        public void openQueue() {
            mItems.add(new MenuItem(OPEN_QUEUE, "Open Queue", R.drawable.ic_queue));
        }

        /**
         * Clear the queue
         */
        public void clearQueue() {
            mItems.add(new MenuItem(CLEAR_QUEUE, "Clear Queue", R.drawable.ic_clear));
        }

        /**
         * Start downloading a playlist
         */
        public void startDownloads() {
            mItems.add(new MenuItem(START_DOWNLOADS, "Start Downloading", R.drawable.ic_download));
        }

        /**
         * Stop downloading a playlist
         */
        public void stopDownloads() {
            mItems.add(new MenuItem(STOP_DOWNLOADS, "Stop Downloading", R.drawable.ic_close));
        }

        /**
         * Delete all downloads in a playlist
         */
        public void clearDownloads() {
            mItems.add(new MenuItem(CLEAR_DOWNLOADS, "Clear Downloads", R.drawable.ic_clear));
        }

        /**
         * Delete download for a specific song
         */
        public void removeDownload() {
            mItems.add(new MenuItem(REMOVE_DOWNLOAD, "Remove Download", R.drawable.ic_clear));
        }

        /**
         * Save a playlist from online search
         */
        public void savePlaylist() {
            mItems.add(new MenuItem(SAVE_PLAYLIST, "Save Playlist", R.drawable.ic_save));
        }

        /**
         * Play a playlist from local search
         */
        public void playPlaylist() {
            mItems.add(new MenuItem(PLAY_PLAYLIST, "Play Playlist", R.drawable.ic_play));
        }

        /**
         * Edit a playlist
         */
        public void editPlaylist() {
            mItems.add(new MenuItem(EDIT_PLAYLIST, "Edit Playlist", R.drawable.ic_edit));
        }

        /**
         * Edit a playlist
         */
        public void deletePlaylist() {
            mItems.add(new MenuItem(DELETE_PLAYLIST, "Delete Playlist", R.drawable.ic_delete));
        }

        /**
         * Create a add playlist
         */
        public void addPlaylist() {
            mItems.add(new MenuItem(ADD_PLAYLIST, "Add Playlist", R.drawable.ic_add));
        }

        /**
         * Generate a playlist from an external platform
         */
        public void importPlaylist() {
            mItems.add(new MenuItem(IMPORT_PLAYLIST, "Import Playlist", R.drawable.ic_download));
        }

    }
}
