package com.zectan.soundroid.connection;

import android.content.Context;
import android.util.Log;

import com.zectan.soundroid.models.Song;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadRequest {
    private static final String TAG = "(SounDroid) SongDownloadTask";
    private static final String URL = "http://soundroid.zectan.com/song/%s/%s.mp3";

    public DownloadRequest(Context context, Song song, boolean highQuality, Callback callback) {
        File file = new File(context.getFilesDir(), String.format("/%s.mp3", song.getSongId()));
        new Thread(() -> {
            int count;
            try {
                URL fileURL = new URL(String.format(URL, highQuality ? "highest" : "lowest", song.getSongId()));
                URLConnection connection = fileURL.openConnection();
                connection.connect();

                int fileSize = connection.getContentLength();
                InputStream input = new BufferedInputStream(fileURL.openStream(), 8192);
                OutputStream output = new FileOutputStream(file);

                long downloadSize = 0;
                byte[] data = new byte[1024];
                while ((count = input.read(data)) != -1) {
                    downloadSize += count;
                    callback.onProgress((int) ((downloadSize * 100) / fileSize));

                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                Log.i(TAG, "DOWNLOAD_SUCCESS: " + song);
                callback.onFinish();
            } catch (Exception e) {
                Log.e(TAG, "DOWNLOAD_FAILURE: " + song);
                callback.onError(e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public interface Callback {
        void onFinish();

        void onProgress(int progress);

        void onError(String message);
    }

}
