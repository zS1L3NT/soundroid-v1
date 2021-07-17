package com.zectan.soundroid.utils;

import android.app.Notification;
import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;

import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.connection.DownloadRequest;
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

    private int mNextInQueue;
    private int mDownloaded;
    private int mFailed;

    public DownloadPlaylist(MainActivity activity, Info info, boolean highQuality) {
        mActivity = activity;
        mNotificationManager = activity.notificationManager;
        mInfo = info;
        mNextInQueue = 0;
        mDownloaded = 0;
        mFailed = 0;
        mHighQuality = highQuality;
        mSongs = ListArrayUtils.sortSongs(
            mActivity.mainVM.getSongsFromPlaylist(mInfo.getId()),
            info.getOrder()
        )
            .stream()
            .filter(song -> !song.isDownloaded(mActivity))
            .collect(Collectors.toList());

        List<String> downloading = mActivity.mainVM.downloading.getValue();
        if (downloading.contains(mInfo.getId())) {
            activity.snack("Already downloading this playlist...");
            return;
        }

        downloading.add(mInfo.getId());
        mActivity.mainVM.downloading.setValue(downloading);

        int SUMMARY_ID = Utils.getRandomInt();
        Notification summaryNotification = new NotificationCompat.Builder(mActivity, MainActivity.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Downloading songs")
            .setContentText(mInfo.getName())
            .setSmallIcon(R.drawable.ic_launcher)
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

        if (!mActivity.mainVM.downloading.getValue().contains(mInfo.getId())) {
            mFailed++;
            song.deleteLocally(mActivity);
            if (++mDownloaded == mSongs.size()) {
                sendDone();
            } else {
                downloadOne();
            }
            return;
        }

        if (song.isDownloaded(mActivity)) {
            if (++mDownloaded == mSongs.size()) {
                sendDone();
            } else {
                downloadOne();
            }
            return;
        }

        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity, MainActivity.DOWNLOAD_CHANNEL_ID);
        builder
            .setContentTitle(String.format("%s (%s/%s)", song.getTitle(), indexInQueue + 1, mSongs.size()))
            .setContentText("0%")
            .setSmallIcon(R.drawable.ic_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup(mInfo.getId())
            .setSilent(true)
            .setProgress(100, 0, false);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

        new DownloadRequest(mActivity, song, mHighQuality, new DownloadRequest.Callback() {
            @Override
            public void onFinish() {
                builder
                    .setProgress(0, 0, false)
                    .setContentText("Done")
                    .setSmallIcon(R.drawable.ic_check);
                mNotificationManager.cancel(NOTIFICATION_ID);
                if (++mDownloaded == mSongs.size()) {
                    sendDone();
                } else {
                    downloadOne();
                }
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
                mFailed++;
                if (++mDownloaded == mSongs.size()) {
                    sendDone();
                } else {
                    downloadOne();
                }
            }

            @Override
            public boolean isCancelled() {
                return !mActivity.mainVM.downloading.getValue().contains(mInfo.getId());
            }
        });
    }

    private void sendDone() {
        int NOTIFICATION_ID = Utils.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity, MainActivity.DOWNLOAD_CHANNEL_ID);
        builder
            .setContentTitle(mFailed == 0 ? "Downloading Finished" : "Downloads Incomplete")
            .setContentText(mInfo.getName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher)
            .setGroup(mInfo.getId());
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

        List<String> downloading = mActivity.mainVM.downloading.getValue();
        mActivity.mainVM.downloading.postValue(
            downloading
                .stream()
                .filter(id -> !id.equals(mInfo.getId()))
                .collect(Collectors.toList())
        );
    }

}
