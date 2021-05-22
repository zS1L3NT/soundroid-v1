package com.zectan.soundroid.tasks;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class SongDownloadTask extends Thread {
    private static final String TAG = "(SounDroid) SongDownloadTask";
    private final File directory;
    private final String fileLink;
    private final Callback callback;

    public SongDownloadTask(File directory, String fileLink, Callback callback) {
        this.directory = directory;
        this.fileLink = fileLink;
        this.callback = callback;
    }

    @Override
    public void run() {
        super.run();

        int count;
        try {
            URL fileURLObject = new URL(fileLink);
            URLConnection connection = fileURLObject.openConnection();
            connection.connect();

            // download the file
            InputStream input = new BufferedInputStream(fileURLObject.openStream(),
                    8192);

            // Output stream
            OutputStream output = new FileOutputStream(directory);

            byte[] data = new byte[1024];
            while ((count = input.read(data)) != -1) {
                // writing data to file


                output.write(data, 0, count);
            }

            // flushing output
            output.flush();
            // closing streams
            output.close();
            input.close();

            Log.i(TAG, "DOWNLOAD_SUCCESS: " + fileLink);
        } catch (Exception e) {
            Log.e(TAG, "DOWNLOAD_FAILURE: " + fileLink);
            e.printStackTrace();
            return;
        }

        callback.run();
    }

    public interface Callback {
        void run();
    }

    public interface Update {
        void run();
    }

}
