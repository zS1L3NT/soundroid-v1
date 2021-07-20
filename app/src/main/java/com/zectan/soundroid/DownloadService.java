package com.zectan.soundroid;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zectan.soundroid.connection.DownloadRequest;
import com.zectan.soundroid.connection.PingSongRequest;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DownloadService extends Service {
    private final IBinder mBinder = new DownloadBinder();
    private NotificationManager mNotificationManager;
    private List<Playlist> mPlaylists;
    private boolean mHighDownloadQuality = true;

    private Playlist mCurrent;
    private int NOTIFICATION_ID;
    private int mPlaylistIndex;
    private int mDownloadIndex;
    private int mDownloadCount;
    private boolean mDestroyed;
    private boolean mFailed;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = getSystemService(NotificationManager.class);
        mDestroyed = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPlaylists = new ArrayList<>();
        return START_NOT_STICKY;
    }

    private void downloadFirstPlaylist() {
        mCurrent = mPlaylists.get(0);
        mFailed = false;
        mPlaylistIndex = 0;
        mDownloadIndex = 1;
        mDownloadCount = (int) mCurrent
            .getSongs()
            .stream()
            .filter(s -> !s.isDownloaded(getApplicationContext()))
            .count();

        downloadSong(mCurrent);
    }

    private void downloadSong(Playlist playlist) {
        if (!playlist.equals(mCurrent)) {
            mFailed = true;
            return;
        }

        if (isOffline() || mDestroyed) {
            cancelDownloads(false);
            return;
        }

        if ((mPlaylistIndex + 1) == playlist.size()) {
            downloadsDone();
            return;
        }

        Song song = mPlaylists.get(0).getSongs().get(mPlaylistIndex);
        if (song.isDownloaded(getApplicationContext())) {
            mPlaylistIndex++;
            downloadSong(playlist);
            return;
        }

        NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID);
        builder
            .setContentTitle(String.format("%s (%s/%s)", song.getTitle(), mDownloadIndex, mDownloadCount))
            .setContentText("Converting...")
            .setSmallIcon(R.drawable.ic_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup(playlist.getInfo().getId())
            .setSilent(true);
        startForeground(NOTIFICATION_ID, builder.build());

        downloadOnePing(playlist, song, builder, NOTIFICATION_ID);
    }

    private void downloadOnePing(Playlist playlist, Song song, NotificationCompat.Builder builder, int NOTIFICATION_ID) {
        new PingSongRequest(song.getSongId(), mHighDownloadQuality, new PingSongRequest.Callback() {
            @Override
            public void onCallback() {
                builder
                    .setContentText("0%")
                    .setProgress(100, 0, false);
                startForeground(NOTIFICATION_ID, builder.build());
                downloadOneStart(playlist, song, builder, NOTIFICATION_ID);
            }

            @Override
            public void onError(String message) {
                stopForeground(NOTIFICATION_ID);
                song.deleteLocally(getApplicationContext());
                mFailed = true;
                mPlaylistIndex++;
                mDownloadIndex++;
                downloadSong(playlist);
            }

            @Override
            public boolean isContinued() {
                return playlist.equals(mCurrent) && !mDestroyed;
            }
        });
    }

    private void downloadOneStart(Playlist playlist, Song song, NotificationCompat.Builder builder, int NOTIFICATION_ID) {
        new DownloadRequest(getApplicationContext(), song, mHighDownloadQuality, new DownloadRequest.Callback() {
            @Override
            public void onFinish() {
                stopForeground(NOTIFICATION_ID);
                mNotificationManager.cancel(NOTIFICATION_ID);
                mPlaylistIndex++;
                mDownloadIndex++;
                downloadSong(playlist);
            }

            @Override
            public void onProgress(int progress) {
                builder
                    .setContentText(String.format("%s%s", progress, "%"))
                    .setProgress(100, progress, false);
                mNotificationManager.notify(NOTIFICATION_ID, builder.build());
            }

            @Override
            public void onError(String message) {
                stopForeground(NOTIFICATION_ID);
                song.deleteLocally(getApplicationContext());
                mFailed = true;
                mPlaylistIndex++;
                mDownloadIndex++;
                downloadSong(playlist);
            }

            @Override
            public boolean isCancelled() {
                return !playlist.equals(mCurrent) || mDestroyed;
            }
        });
    }

    private void downloadsDone() {
        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID);
        builder
            .setContentTitle(mFailed ? "Downloads Incomplete" : "Downloading Finished")
            .setContentText(mPlaylists.get(0).getInfo().getName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher);
        stopForeground(this.NOTIFICATION_ID);
        mNotificationManager.cancel(this.NOTIFICATION_ID);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

        mPlaylists.remove(0);
        if (mPlaylists.size() > 0) {
            downloadFirstPlaylist();
        } else {
            stopSelf();
        }
    }

    private void cancelDownloads(boolean continue_) {
        mCurrent = null;
        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID);
        builder
            .setContentTitle("Downloads Cancelled")
            .setContentText(mPlaylists.get(0).getInfo().getName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_close);
        stopForeground(this.NOTIFICATION_ID);
        mNotificationManager.cancel(this.NOTIFICATION_ID);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

        if (continue_) {
            mPlaylists.remove(0);
        } else {
            mPlaylists.clear();
        }

        if (mPlaylists.size() > 0) {
            downloadFirstPlaylist();
        } else {
            stopSelf();
        }
    }

    private boolean isOffline() {
        ConnectivityManager cm = getSystemService(ConnectivityManager.class);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) return true;
        return !networkInfo.isConnected();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class DownloadBinder extends Binder {

        public boolean startDownload(Playlist playlist, boolean highDownloadQuality) {
            mHighDownloadQuality = highDownloadQuality;

            if (isDownloading(playlist.getInfo().getId())) {
                return false;
            }
            mPlaylists.add(playlist);
            if (mPlaylists.size() == 1) downloadFirstPlaylist();
            return true;
        }

        public void stopDownload(Playlist playlist) {
            if (mPlaylists.get(0).equals(playlist)) {
                cancelDownloads(true);
            } else {
                mPlaylists.remove(playlist);
            }
        }

        public boolean isDownloading(String playlistId) {
            return mPlaylists.stream().anyMatch(p -> p.getInfo().getId().equals(playlistId));
        }

    }

    @Override
    public void onDestroy() {
        cancelDownloads(false);
        super.onDestroy();
    }
}
