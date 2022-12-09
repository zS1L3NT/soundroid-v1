package com.zectan.soundroid.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.Playable;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.Debounce;
import com.zectan.soundroid.Utils.DownloadProcess;
import com.zectan.soundroid.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DownloadService extends Service {
    private static final String CANCEL = "CANCEL";
    private final IBinder mBinder = new DownloadBinder();

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    private List<Playable> mPlayables;
    private boolean mHighDownloadQuality = true;

    private Playable mCurrentPlayable;
    private int mNotificationID;
    private int mPlaylistIndex;
    private int mDownloadIndex;
    private int mDownloadCount;
    private boolean mFailed;

    /**
     * Runs when an intent was passed to the Service
     *
     * @param intent  Intent
     * @param flags   Flags
     * @param startId Start ID
     * @return START_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (CANCEL.equals(intent.getAction())) {
                cancelDownloads(true);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = getSystemService(NotificationManager.class);
        mNotificationID = Utils.getRandomInt();
        mPlayables = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNotificationManager.cancel(mNotificationID);
    }

    /**
     * Downloads the first playlist in line
     */
    private void downloadFirstPlaylist() {
        if (mPlayables.size() == 0) {
            stopSelf();
            return;
        }

        mCurrentPlayable = mPlayables.get(0);
        mFailed = false;
        mPlaylistIndex = 0;
        mDownloadIndex = 1;
        mDownloadCount = (int) mCurrentPlayable
            .getSongs()
            .stream()
            .filter(s -> !s.isDownloaded(getApplicationContext()))
            .count();

        // Create intents
        Intent intentOpen = new Intent(this, MainActivity.class).setAction(MainActivity.FRAGMENT_PLAYLISTS);
        Intent intentCancel = new Intent(this, DownloadService.class).setAction(CANCEL);
        PendingIntent pendingIntentOpen = PendingIntent.getActivity(getApplicationContext(), 0, intentOpen, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pendingIntentCancel = PendingIntent.getService(getApplicationContext(), 0, intentCancel, PendingIntent.FLAG_IMMUTABLE);

        // Create notification
        mNotificationBuilder = new NotificationCompat
            .Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Downloading Service")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntentOpen)
            .addAction(R.drawable.ic_close, "Cancel", pendingIntentCancel)
            .setSilent(true);
        startForeground(mNotificationID, mNotificationBuilder.build());

        downloadSong(mCurrentPlayable);
    }

    /**
     * Download a song from the playable
     *
     * @param playable Playable
     */
    private void downloadSong(Playable playable) {
        if (isOffline()) {
            cancelDownloads(false);
            return;
        }

        if (mPlaylistIndex == playable.size()) {
            downloadsDone();
            return;
        }

        Song song = mCurrentPlayable.getSongs().get(mPlaylistIndex);
        if (song.isDownloaded(getApplicationContext())) {
            mPlaylistIndex++;
            downloadSong(playable);
            return;
        }

        // Is Cancelled
        if (!playable.equals(mCurrentPlayable)) {
            mFailed = true;
            return;
        }

        mNotificationManager.cancel(mNotificationID);
        mNotificationBuilder
            .setContentTitle(song.getTitle())
            .setProgress(100, 0, true);
        mNotificationManager.notify(mNotificationID, mNotificationBuilder.build());

        // Create a download process to start downloading the song
        Debounce debounce = new Debounce(1000);
        new DownloadProcess(getApplicationContext(), song, mHighDownloadQuality, new DownloadProcess.Callback() {
            @Override
            public boolean isCancelled() {
                if (!playable.equals(mCurrentPlayable)) {
                    debounce.cancel();
                    return true;
                }
                return false;
            }

            @Override
            public void onFinish() {
                mPlaylistIndex++;
                mDownloadIndex++;

                debounce.cancel();
                new Handler(getMainLooper()).post(() -> downloadSong(playable));
            }

            private String getSummaryText() {
                return String.format("Downloading %s/%s", mDownloadIndex, mDownloadCount);
            }

            @Override
            public void showCheckingTime(int minutes, int seconds) {
                String string = "Pinging server...\n" +
                    String.format("Elapsed Time: %sm %ss", minutes, seconds);

                mNotificationBuilder
                    .setContentText("Pinging server...")
                    .setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText(getSummaryText())
                        .bigText(string));

                if (!isCancelled()) mNotificationManager.notify(mNotificationID, mNotificationBuilder.build());
            }

            @Override
            public void showConvertingTime(int attemptIndex, int minutes, int seconds) {
                String string = "Converting to MP3...\n" +
                    String.format("Attempt: %s/%s\n", attemptIndex, DownloadProcess.RETRY_COUNT) +
                    String.format("Elapsed Time: %sm %ss", minutes, seconds);

                mNotificationBuilder
                    .setContentText("Converting to MP3...")
                    .setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText(getSummaryText())
                        .bigText(string));

                if (!isCancelled()) mNotificationManager.notify(mNotificationID, mNotificationBuilder.build());
            }

            @Override
            public void showDownloadProgress(int attemptIndex, int progress) {
                String string = String.format("Progress: %s%%\n", progress) +
                    String.format("Attempt: %s/%s", attemptIndex, DownloadProcess.RETRY_COUNT);

                debounce.post(() -> {
                    mNotificationBuilder
                        .setContentText(progress + "%")
                        .setProgress(100, progress, false)
                        .setStyle(new NotificationCompat.BigTextStyle()
                            .setSummaryText(getSummaryText())
                            .bigText(string));

                    if (!isCancelled())
                        mNotificationManager.notify(mNotificationID, mNotificationBuilder.build());
                });
            }

            @Override
            public void onError() {
                mFailed = true;
                mPlaylistIndex++;
                mDownloadIndex++;
                debounce.cancel();
                downloadSong(playable);
            }
        });
    }

    /**
     * Runs when a playlist has finished downloading
     */
    private void downloadsDone() {
        mNotificationManager.cancel(mNotificationID);
        stopForeground(mNotificationID);

        // Show done notification
        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID)
            .setContentTitle(mFailed ? "Downloads Incomplete" : "Downloading Finished")
            .setContentText(mCurrentPlayable.getInfo().getName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(android.R.drawable.stat_sys_download_done);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
        mCurrentPlayable = null;

        if (mPlayables.size() > 0) mPlayables.remove(0);
        if (mPlayables.size() > 0) {
            downloadFirstPlaylist();
        } else {
            stopSelf();
        }
    }

    /**
     * Cancel the current download
     *
     * @param startNextPlaylist Start downloading next playlist
     */
    private void cancelDownloads(boolean startNextPlaylist) {
        stopForeground(mNotificationID);
        mNotificationManager.cancel(mNotificationID);

        // Show cancelled notification
        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Downloads Cancelled")
            .setContentText(mCurrentPlayable.getInfo().getName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_close);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
        mCurrentPlayable = null;

        if (startNextPlaylist) {
            if (mPlayables.size() > 0) mPlayables.remove(0);
        } else {
            mPlayables.clear();
        }

        if (mPlayables.size() > 0) {
            downloadFirstPlaylist();
        } else {
            stopSelf();
        }
    }

    /**
     * Check if device is offline
     *
     * @return If is offline
     */
    private boolean isOffline() {
        ConnectivityManager cm = getSystemService(ConnectivityManager.class);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) return true;
        return !networkInfo.isConnected();
    }

    /**
     * Push a playable to the queue of downloads
     *
     * @param playable            Playable
     * @param highDownloadQuality Quality of downloads
     * @return True if the download was queued and False if the playlist is already downloading
     */
    public boolean startDownload(Playable playable, boolean highDownloadQuality) {
        mHighDownloadQuality = highDownloadQuality;

        if (isDownloading(playable.getInfo().getId())) return false;

        mPlayables.add(playable);
        if (mPlayables.size() == 1) downloadFirstPlaylist();
        return true;
    }

    /**
     * Stop the download of a playable
     *
     * @param playable Playable
     */
    public void stopDownload(Playable playable) {
        if (mPlayables.size() == 0) return;
        if (mPlayables.get(0).equals(playable)) {
            cancelDownloads(true);
        } else {
            mPlayables.remove(playable);
        }
    }

    /**
     * Check if a playlist is being downloaded
     *
     * @param playlistId Playlist ID to check
     * @return If the playlist is being downloaded
     */
    public boolean isDownloading(String playlistId) {
        return mPlayables.stream().anyMatch(p -> p.getInfo().getId().equals(playlistId));
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
