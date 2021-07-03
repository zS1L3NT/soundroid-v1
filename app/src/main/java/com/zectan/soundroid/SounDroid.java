package com.zectan.soundroid;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.core.app.NotificationManagerCompat;

public class SounDroid extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationChannel downloadChannel = new NotificationChannel(
            MainActivity.DOWNLOAD_CHANNEL_ID,
            "Downloads",
            NotificationManager.IMPORTANCE_DEFAULT
        );
        downloadChannel.setDescription("Download songs for offline listening");

        NotificationManagerCompat notificationManager = getSystemService(NotificationManagerCompat.class);
        notificationManager.createNotificationChannel(downloadChannel);
    }
}
