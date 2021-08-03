package com.zectan.soundroid.Utils;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.zectan.soundroid.Classes.Interval;
import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Connections.DownloadRequest;
import com.zectan.soundroid.Connections.SongFullRequest;
import com.zectan.soundroid.Connections.SongPingRequest;
import com.zectan.soundroid.Models.Song;

import java.util.Date;

public class DownloadProcess {
    public static final int RETRY_COUNT = 3;
    private final Callback mCallback;
    private final Context mContext;
    private final Song mSong;
    private final boolean mHighDownloadQuality;

    private CheckingInterval mCheckingInterval;
    private ConvertingInterval mConvertingInterval;
    private Date mStartTime;
    private int mAttemptIndex;

    /**
     * Goes through the network process of downloading a song
     *
     * @param context             Context
     * @param song                Song to download
     * @param highDownloadQuality Quality of download
     * @param callback            Download Callback
     */
    public DownloadProcess(Context context, Song song, boolean highDownloadQuality, Callback callback) {
        mContext = context;
        mSong = song;
        mHighDownloadQuality = highDownloadQuality;
        mCallback = callback;
        mAttemptIndex = 1;

        ping();
    }

    /**
     * Ping the server to check if the song exists
     */
    private void ping() {
        mStartTime = new Date();

        if (mCheckingInterval != null) mConvertingInterval.cancel();
        mCheckingInterval = new CheckingInterval(Looper.getMainLooper());
        new SongPingRequest(mSong, mHighDownloadQuality, new Request.Callback() {
            @Override
            public void onComplete(String response) {
                mCheckingInterval.cancel();
                mCheckingInterval = null;
                if (mCallback.isCancelled()) return;

                download();
            }

            @Override
            public void onError(String message) {
                mCheckingInterval.cancel();
                mCheckingInterval = null;
                if (mCallback.isCancelled()) return;

                convert();
            }
        });
    }

    /**
     * Runs if a ping failed, meaning song conversion from
     * YouTube video to WEBM is needed
     */
    private void convert() {
        mStartTime = new Date();

        if (mConvertingInterval != null) mConvertingInterval.cancel();
        mConvertingInterval = new ConvertingInterval(Looper.getMainLooper());
        new SongFullRequest(mSong, mHighDownloadQuality, new Request.Callback() {
            @Override
            public void onComplete(String response) {
                mConvertingInterval.cancel();
                mConvertingInterval = null;
                if (mCallback.isCancelled()) return;

                download();
            }

            @Override
            public void onError(String message) {
                mConvertingInterval.cancel();
                mConvertingInterval = null;
                if (mCallback.isCancelled()) return;

                mSong.deleteLocally(mContext);

                // Increase the number of convert attempts
                // Server may take multiple attempts to convert a song
                mAttemptIndex++;
                if (mAttemptIndex > RETRY_COUNT) {
                    mCallback.onError();
                } else {
                    convert();
                }
            }
        });
    }

    /**
     * If a ping is successful, or a song is converted successfully,
     * start downloading the song using the DownloadRequest class
     */
    private void download() {
        if (!mCallback.isCancelled()) mCallback.showDownloadProgress(mAttemptIndex, 0);
        new DownloadRequest(mContext, mSong, mHighDownloadQuality, new DownloadRequest.Callback() {
            @Override
            public void onFinish() {
                mCallback.onFinish();
            }

            @Override
            public void onProgress(int progress) {
                mCallback.showDownloadProgress(mAttemptIndex, progress);
            }

            @Override
            public void onError(String message) {
                mSong.deleteLocally(mContext);
                mAttemptIndex++;
                if (mAttemptIndex > RETRY_COUNT) {
                    mCallback.onError();
                } else {
                    download();
                }
            }

            @Override
            public boolean isCancelled() {
                return mCallback.isCancelled();
            }
        });
    }

    private class CheckingInterval extends Interval {

        public CheckingInterval(@NonNull Looper looper) {
            super(looper, 1000);
            start();
        }

        @Override
        public void onNextCall() {
            Date nowTime = new Date();

            int time = (int) (nowTime.getTime() - mStartTime.getTime()) / 1000;
            int seconds = time % 60;
            int minutes = time / 60;

            mCallback.showCheckingTime(minutes, seconds);
        }

    }

    private class ConvertingInterval extends Interval {

        public ConvertingInterval(@NonNull Looper looper) {
            super(looper, 1000);
            start();
        }

        @Override
        public void onNextCall() {
            Date nowTime = new Date();

            int time = (int) (nowTime.getTime() - mStartTime.getTime()) / 1000;
            int seconds = time % 60;
            int minutes = time / 60;

            mCallback.showConvertingTime(mAttemptIndex, minutes, seconds);
        }
    }

    public interface Callback {
        boolean isCancelled();

        void onFinish();

        void showCheckingTime(int minutes, int seconds);

        void showConvertingTime(int attemptIndex, int minutes, int seconds);

        void showDownloadProgress(int attemptIndex, int progress);

        void onError();
    }
}
