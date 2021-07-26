package com.zectan.soundroid;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.Utils.DownloadProcess;
import com.zectan.soundroid.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DownloadService extends Service {
    private final IBinder mBinder = new DownloadBinder();
    private NotificationManager mNotificationManager;
    private List<Playlist> mPlaylists;
    private boolean mHighDownloadQuality = true;

    private Playlist mCurrent;
    private int mNotificationID;
    private int mPlaylistIndex;
    private int mDownloadIndex;
    private int mDownloadCount;
    private boolean mDestroyed;
    private boolean mFailed;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = getSystemService(NotificationManager.class);
        mNotificationID = Utils.getRandomInt();
        mPlaylists = new ArrayList<>();
        mDestroyed = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
        mNotificationManager.cancel(mNotificationID);
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Downloading Playlist")
            .setContentText(mCurrent.getInfo().getName())
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true);
        startForeground(mNotificationID, builder.build());

        downloadSong(mCurrent);
    }

    private void downloadSong(Playlist playlist) {
        if (isOffline() || mDestroyed) {
            cancelDownloads(false);
            return;
        }

        if (mPlaylistIndex == playlist.size()) {
            downloadsDone();
            return;
        }

        Song song = mPlaylists.get(0).getSongs().get(mPlaylistIndex);
        if (song.isDownloaded(getApplicationContext())) {
            mPlaylistIndex++;
            downloadSong(playlist);
            return;
        }

        // Is Cancelled
        if (!playlist.equals(mCurrent)) {
            mFailed = true;
            return;
        }

        mNotificationManager.cancel(mNotificationID);
        NotificationCompat.Builder builder = new NotificationCompat
            .Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID)
            .setContentTitle(song.getTitle())
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true);
        mNotificationManager.notify(mNotificationID, builder.build());

        new DownloadProcess(getApplicationContext(), song, mHighDownloadQuality, new DownloadProcess.Callback() {
            @Override
            public boolean isCancelled() {
                return !playlist.equals(mCurrent) || mDestroyed;
            }

            @Override
            public void onFinish() {
                mPlaylistIndex++;
                mDownloadIndex++;

                new Handler(getMainLooper()).post(() -> downloadSong(playlist));
            }

            private String getSummaryText() {
                return String.format("Downloading %s/%s", mDownloadIndex, mDownloadCount);
            }

            @Override
            public void showCheckingTime(int minutes, int seconds) {
                String string = "Pinging server...\n" +
                    String.format("Elapsed Time: %sm %ss", minutes, seconds);

                builder
                    .setContentText("Pinging server...")
                    .setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText(getSummaryText())
                        .bigText(string));

                if (!isCancelled()) mNotificationManager.notify(mNotificationID, builder.build());
            }

            @Override
            public void showConvertingTime(int attemptIndex, int minutes, int seconds) {
                String string = "Converting...\n" +
                    String.format("Attempt: %s/%s\n", attemptIndex, DownloadProcess.RETRY_COUNT) +
                    String.format("Elapsed Time: %sm %ss", minutes, seconds);

                builder
                    .setContentText("Converting...")
                    .setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText(getSummaryText())
                        .bigText(string));

                if (!isCancelled()) mNotificationManager.notify(mNotificationID, builder.build());
            }

            @Override
            public void showDownloadProgress(int attemptIndex, int progress) {
                String string = String.format("Progress: %s%%\n", progress) +
                    String.format("Attempt: %s/%s", attemptIndex, DownloadProcess.RETRY_COUNT);

                builder
                    .setContentText(progress + "%")
                    .setProgress(100, progress, false)
                    .setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText(getSummaryText())
                        .bigText(string));

                if (!isCancelled()) mNotificationManager.notify(mNotificationID, builder.build());
            }

            @Override
            public void onError() {
                mFailed = true;
                mPlaylistIndex++;
                mDownloadIndex++;
                downloadSong(playlist);
            }
        });
    }

    private void downloadsDone() {
        mNotificationManager.cancel(mNotificationID);
        stopForeground(mNotificationID);

        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID)
            .setContentTitle(mFailed ? "Downloads Incomplete" : "Downloading Finished")
            .setContentText(mPlaylists.get(0).getInfo().getName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(android.R.drawable.stat_sys_download_done);
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
        stopForeground(mNotificationID);
        mNotificationManager.cancel(mNotificationID);

        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Downloads Cancelled")
            .setContentText(mPlaylists.get(0).getInfo().getName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_close);
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
}
