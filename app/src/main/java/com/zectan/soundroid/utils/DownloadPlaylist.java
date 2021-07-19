package com.zectan.soundroid.utils;

import android.app.Notification;
import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;

import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.connection.DownloadRequest;
import com.zectan.soundroid.connection.PingSongRequest;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Song;

import java.util.List;
import java.util.stream.Collectors;

public class DownloadPlaylist {
    private final MainActivity mActivity;
    private final NotificationManager mNotificationManager;
    private final List<Song> mSongs;
    private final Info mInfo;
    private final boolean mHighQuality;

    private int SUMMARY_ID;
    private int mNextInQueue;
    private int mDownloaded;
    private boolean mFailed;
    private boolean mIsCancelled;

    public DownloadPlaylist(MainActivity activity, Info info, boolean highQuality) {
        mActivity = activity;
        mNotificationManager = activity.notificationManager;
        mInfo = info;
        mNextInQueue = 0;
        mDownloaded = 0;
        mFailed = false;
        mIsCancelled = false;
        mHighQuality = highQuality;
        mSongs = ListArrayUtils.sortSongs(
            mActivity.mainVM.getSongsFromPlaylist(mInfo.getId()),
            info.getOrder()
        )
            .stream()
            .filter(song -> !song.isDownloaded(mActivity))
            .collect(Collectors.toList());

        List<DownloadPlaylist> downloads = mActivity.mainVM.downloads.getValue();
        if (downloads.contains(this)) {
            activity.snack("Already downloading this playlist...");
            return;
        }

        downloads.add(this);
        mActivity.mainVM.downloads.setValue(downloads);

        SUMMARY_ID = Utils.getRandomInt();
        Notification summaryNotification = new NotificationCompat.Builder(mActivity, MainActivity.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Downloading playlist")
            .setContentText(mInfo.getName())
            .setSmallIcon(R.drawable.ic_launcher)
            .setStyle(new NotificationCompat.InboxStyle().setSummaryText(mInfo.getName()))
            .setGroup(mInfo.getId())
            .setGroupSummary(true)
            .build();
        mNotificationManager.notify(SUMMARY_ID, summaryNotification);

        for (int i = 0; i < 3; i++) {
            downloadOne();
        }
    }

    public void downloadOne() {
        if (mNextInQueue == mSongs.size()) return;
        int indexInQueue = mNextInQueue++;
        Song song = mSongs.get(indexInQueue);

        if (mIsCancelled) {
            mFailed = true;
            song.deleteLocally(mActivity);
            return;
        }

        if (song.isDownloaded(mActivity)) {
            downloadNext();
            return;
        }

        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity, MainActivity.DOWNLOAD_CHANNEL_ID);
        builder
            .setContentTitle(String.format("%s (%s/%s)", song.getTitle(), indexInQueue + 1, mSongs.size()))
            .setContentText("Converting...")
            .setSmallIcon(R.drawable.ic_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup(mInfo.getId())
            .setSilent(true);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

        downloadOnePing(song, builder, NOTIFICATION_ID);
    }

    private void downloadOnePing(Song song, NotificationCompat.Builder builder, int NOTIFICATION_ID) {
        new PingSongRequest(song.getSongId(), mHighQuality, new PingSongRequest.Callback() {
            @Override
            public void onCallback() {
                if (mIsCancelled) return;
                builder
                    .setContentText("0%")
                    .setProgress(100, 0, false);
                mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                downloadOneStart(song, builder, NOTIFICATION_ID);
            }

            @Override
            public void onError(String message) {
                if (mIsCancelled) return;
                mNotificationManager.cancel(NOTIFICATION_ID);
                song.deleteLocally(mActivity);
                mFailed = true;
                downloadNext();
            }
        });
    }

    private void downloadOneStart(Song song, NotificationCompat.Builder builder, int NOTIFICATION_ID) {
        new DownloadRequest(mActivity, song, mHighQuality, new DownloadRequest.Callback() {
            @Override
            public void onFinish() {
                mNotificationManager.cancel(NOTIFICATION_ID);
                downloadNext();
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
                mNotificationManager.cancel(NOTIFICATION_ID);
                song.deleteLocally(mActivity);
                mFailed = true;
                downloadNext();
            }

            @Override
            public boolean isCancelled() {
                return mIsCancelled;
            }
        });
    }

    /**
     * Download next item in the queue
     * If the queue is finished, run {@link DownloadPlaylist#downloadDone()}
     */
    private void downloadNext() {
        if (++mDownloaded == mSongs.size()) {
            downloadDone();
        } else {
            downloadOne();
        }
    }

    /**
     * Display the downloading finished notification
     */
    private void downloadDone() {
        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity, MainActivity.DOWNLOAD_CHANNEL_ID);
        builder
            .setContentTitle(mFailed ? "Downloads Incomplete" : "Downloading Finished")
            .setContentText(mInfo.getName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher);
        mNotificationManager.cancel(SUMMARY_ID);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

        List<DownloadPlaylist> downloads = mActivity.mainVM.downloads.getValue();
        mActivity.mainVM.downloads.postValue(
            downloads
                .stream()
                .filter(d -> !d.getPlaylistId().equals(mInfo.getId()))
                .collect(Collectors.toList())
        );
    }

    public String getPlaylistId() {
        return mInfo.getId();
    }

    public void cancel() {
        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity, MainActivity.DOWNLOAD_CHANNEL_ID);
        builder
            .setContentTitle("Downloads Cancelled")
            .setContentText(mInfo.getName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_close);
        mNotificationManager.cancel(SUMMARY_ID);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
        mIsCancelled = true;
    }

}
