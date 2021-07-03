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

    private int nextInQueue;
    private int downloaded;

    public DownloadPlaylist(MainActivity activity, Info info) {
        mActivity = activity;
        mNotificationManager = activity.notificationManager;
        mInfo = info;
        mSongs = mActivity.mainVM.getSongsFromPlaylist(mInfo.getId());
        nextInQueue = 0;
        downloaded = 0;

        List<String> downloading = mActivity.mainVM.downloading.getValue();
        if (downloading.contains(mInfo.getId())) {
            activity.snack("Already downloading this playlist...");
            return;
        }

        downloading.add(mInfo.getId());
        mActivity.mainVM.downloading.setValue(downloading);

        int SUMMARY_ID = Anonymous.getRandomInt();
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
        if (nextInQueue == mSongs.size()) return;
        int indexInQueue = nextInQueue++;
        Song song = mSongs.get(indexInQueue);

        if (song.isDownloaded(mActivity)) {
            if (++downloaded == mSongs.size()) {
                sendDone();
            } else {
                downloadOne();
            }
            return;
        }

        int NOTIFICATION_ID = Anonymous.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity, MainActivity.DOWNLOAD_CHANNEL_ID);
        builder
            .setContentTitle(String.format("%s (%s/%s)", song.getTitle(), indexInQueue + 1, mSongs.size()))
            .setContentText("0%")
            .setSmallIcon(R.drawable.ic_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup(mInfo.getId())
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, false);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

        new DownloadRequest(song, mActivity, new DownloadRequest.Callback() {
            @Override
            public void onFinish() {
                builder
                    .setProgress(0, 0, false)
                    .setContentText("Done")
                    .setSmallIcon(R.drawable.ic_check);
                mNotificationManager.cancel(NOTIFICATION_ID);
                if (++downloaded == mSongs.size()) {
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
                builder
                    .setProgress(0, 0, false)
                    .setContentText("Failed")
                    .setSmallIcon(R.drawable.ic_close);
                mNotificationManager.cancel(NOTIFICATION_ID);
                mNotificationManager.notify(Anonymous.getRandomInt(), builder.build());
                if (++downloaded == mSongs.size()) {
                    sendDone();
                } else {
                    downloadOne();
                }
            }
        });
    }

    private void sendDone() {
        int NOTIFICATION_ID = Anonymous.getRandomInt();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity, MainActivity.DOWNLOAD_CHANNEL_ID);
        builder
            .setContentTitle("Downloading Finished")
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
