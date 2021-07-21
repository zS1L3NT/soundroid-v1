package com.zectan.soundroid;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zectan.soundroid.Connections.DownloadRequest;
import com.zectan.soundroid.Connections.PingSongRequest;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.Utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DownloadService extends Service {
    private static final int RETRY_COUNT = 3;
    private final IBinder mBinder = new DownloadBinder();
    private NotificationManager mNotificationManager;
    private List<Playlist> mPlaylists;
    private boolean mHighDownloadQuality = true;

    private Playlist mCurrent;
    private int mNotificationID;
    private int mPlaylistIndex;
    private int mDownloadIndex;
    private int mDownloadCount;
    private int mAttemptIndex;
    private boolean mDestroyed;
    private boolean mFailed;

    private TimeHandler mTimeHandler;
    private Date mStartTime;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = getSystemService(NotificationManager.class);
        mNotificationID = Utils.getRandomInt();
        mDestroyed = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNotificationManager.cancel(mNotificationID);
        if (mTimeHandler != null) {
            mTimeHandler.cancel();
        }
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
        mAttemptIndex = 1;
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
        if (!playlist.equals(mCurrent)) {
            mFailed = true;
            return;
        }

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

        mNotificationManager.cancel(mNotificationID);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID)
            .setContentTitle(song.getTitle())
            .setContentText("Converting... (0m 0s)")
            .setStyle(new NotificationCompat.BigTextStyle()
                .setSummaryText(String.format("Downloading %s/%s", mDownloadIndex, mDownloadCount))
                .bigText(String.format("Converting... (0m 0s)\nAttempt %s/%s", mAttemptIndex, RETRY_COUNT)))
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true);
        mNotificationManager.notify(mNotificationID, builder.build());

        if (mTimeHandler != null) mTimeHandler.cancel();
        mTimeHandler = new TimeHandler(getMainLooper(), builder);
        mTimeHandler.sendEmptyMessage(0);
        mStartTime = new Date();

        downloadOnePing(playlist, song, builder);
    }

    private void downloadOnePing(Playlist playlist, Song song, NotificationCompat.Builder builder) {
        new PingSongRequest(song.getSongId(), mHighDownloadQuality, new PingSongRequest.Callback() {
            @Override
            public void onCallback() {
                builder
                    .setContentText("0%")
                    .setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText(String.format("Downloading %s/%s", mDownloadIndex, mDownloadCount))
                        .bigText(String.format("Percent: 0%%\nAttempt: %s/%s", mAttemptIndex, RETRY_COUNT)))
                    .setProgress(100, 0, false);
                mNotificationManager.notify(mNotificationID, builder.build());
                downloadOneStart(playlist, song, builder);
            }

            @Override
            public void onError(String message) {
                song.deleteLocally(getApplicationContext());
                if (mAttemptIndex++ > RETRY_COUNT) {
                    mFailed = true;
                    mPlaylistIndex++;
                    mDownloadIndex++;
                    mAttemptIndex = 1;
                }
                downloadSong(playlist);
            }

            @Override
            public void cancelTimeHandler() {
                if (mTimeHandler != null) mTimeHandler.cancel();
            }

            @Override
            public boolean isContinued() {
                return playlist.equals(mCurrent) && !mDestroyed;
            }
        });
    }

    private void downloadOneStart(Playlist playlist, Song song, NotificationCompat.Builder builder) {
        new DownloadRequest(getApplicationContext(), song, mHighDownloadQuality, new DownloadRequest.Callback() {
            @Override
            public void onFinish() {
                mPlaylistIndex++;
                mDownloadIndex++;
                downloadSong(playlist);
            }

            @Override
            public void onProgress(int progress) {
                builder
                    .setContentText(String.format("%s%%", progress))
                    .setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText(String.format("Downloading %s/%s", mDownloadIndex, mDownloadCount))
                        .bigText(String.format("Percent: %s%%\nAttempt: %s/%s", progress, mAttemptIndex, RETRY_COUNT)))
                    .setProgress(100, progress, false);
                mNotificationManager.notify(mNotificationID, builder.build());
            }

            @Override
            public void onError(String message) {
                song.deleteLocally(getApplicationContext());
                if (mAttemptIndex++ > RETRY_COUNT) {
                    mFailed = true;
                    mPlaylistIndex++;
                    mDownloadIndex++;
                    mAttemptIndex = 1;
                }
                downloadSong(playlist);
            }

            @Override
            public boolean isCancelled() {
                return !playlist.equals(mCurrent) || mDestroyed;
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
        mNotificationManager.cancel(mNotificationID);
        stopForeground(mNotificationID);

        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MainActivity.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Downloads Cancelled")
            .setContentText(mPlaylists.get(0).getInfo().getName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_close);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

        if (mTimeHandler != null) mTimeHandler.cancel();

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

    private class TimeHandler extends Handler {
        private final NotificationCompat.Builder mBuilder;
        private boolean mCancelled;

        public TimeHandler(@NonNull Looper looper, NotificationCompat.Builder builder) {
            super(looper);
            mBuilder = builder;
            mCancelled = false;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mCancelled) return;
            Date nowTime = new Date();

            int time = (int) (nowTime.getTime() - mStartTime.getTime()) / 1000;
            int seconds = time % 60;
            int minutes = time / 60;

            mBuilder
                .setContentText(String.format("Converting... (%sm %ss)", minutes, seconds))
                .setStyle(new NotificationCompat.BigTextStyle()
                    .setSummaryText(String.format("Downloading %s/%s", mDownloadIndex, mDownloadCount))
                    .bigText(String.format("Converting... (%sm %ss)\nAttempt: %s/%s", minutes, seconds, mAttemptIndex, RETRY_COUNT)));
            mNotificationManager.notify(mNotificationID, mBuilder.build());
            sendEmptyMessageDelayed(0, 1000);
        }

        public void cancel() {
            mCancelled = true;
            mTimeHandler = null;
        }

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
}
