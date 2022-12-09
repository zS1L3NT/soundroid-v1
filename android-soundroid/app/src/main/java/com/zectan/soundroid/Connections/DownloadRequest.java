package com.zectan.soundroid.Connections;

import android.content.Context;
import android.util.Log;

import com.zectan.soundroid.Env;
import com.zectan.soundroid.Models.Song;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadRequest {
    private static final String TAG = "(SounDroid) SongDownloadTask";
    private static final String URL = Env.API_URL + "/song/%s/%s.mp3";

    /**
     * Downloads a file from the internet, downloads byte by byte.
     * Using low level input stream, output stream and buffer.
     * Download with a __ prefix when in progress then rename to actual when done
     *
     * @param context             Context
     * @param song                Song to download
     * @param highDownloadQuality Quality of download
     * @param callback            Download Callback
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public DownloadRequest(Context context, Song song, boolean highDownloadQuality, Callback callback) {
        File file = new File(context.getFilesDir(), String.format("/__%s.mp3", song.getSongId()));
        new Thread(() -> {
            int count;
            try {
                URL fileURL = new URL(String.format(URL, highDownloadQuality ? "highest" : "lowest", song.getSongId()));
                URLConnection connection = fileURL.openConnection();
                connection.connect();

                int fileSize = connection.getContentLength();
                InputStream input = new BufferedInputStream(fileURL.openStream(), 8192);
                OutputStream output = new FileOutputStream(file);

                long downloadSize = 0;
                byte[] data = new byte[1024];
                while ((count = input.read(data)) != -1) {
                    downloadSize += count;
                    if (callback.isCancelled()) return;
                    callback.onProgress((int) ((downloadSize * 100) / fileSize));

                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                Log.i(TAG, "DOWNLOAD_SUCCESS: " + song);
                file.renameTo(new File(context.getFilesDir(), String.format("/%s.mp3", song.getSongId())));
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

        boolean isCancelled();
    }

}
