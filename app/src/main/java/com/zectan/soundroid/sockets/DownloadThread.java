package com.zectan.soundroid.sockets;

import android.util.Log;

import com.zectan.soundroid.objects.Song;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadThread extends Thread {
    private static final String TAG = "(SounDroid) SongDownloadTask";
    private final Callback callback;
    private final Song song;

    public DownloadThread(Song song, Callback callback) {
        this.callback = callback;
        this.song = song;
    }

    @Override
    public void run() {
        super.run();

        int count;
        try {
            URL fileURL = new URL(String.format("http://soundroid.zectan.com/songs/%s.mp3", song.getId()));
            URLConnection connection = fileURL.openConnection();
            connection.connect();

            int fileSize = connection.getContentLength();
            InputStream input = new BufferedInputStream(fileURL.openStream(), 8192);
            OutputStream output = new FileOutputStream(song.getDirectory());

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
    }

    public interface Callback {
        void onFinish();

        void onProgress(int progress);

        void onError(String message);
    }

}
