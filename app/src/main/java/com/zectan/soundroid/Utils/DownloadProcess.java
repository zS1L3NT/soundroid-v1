package com.zectan.soundroid.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Connections.DownloadRequest;
import com.zectan.soundroid.Models.Song;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class DownloadProcess {
    public static final int RETRY_COUNT = 3;
    private final Callback mCallback;
    private final Context mContext;
    private final Song mSong;
    private final boolean mHighDownloadQuality;

    private CheckingHandler mCheckingHandler;
    private ConvertingHandler mConvertingHandler;
    private Date mStartTime;
    private int mAttemptIndex;

    public DownloadProcess(Context context, Song song, boolean highDownloadQuality, Callback callback) {
        mContext = context;
        mSong = song;
        mHighDownloadQuality = highDownloadQuality;
        mCallback = callback;
        mAttemptIndex = 1;

        ping();
    }

    private void ping() {
        mStartTime = new Date();

        if (mCheckingHandler != null) mConvertingHandler.cancel();
        mCheckingHandler = new CheckingHandler(Looper.getMainLooper());
        new PingRequest(new Request.Callback() {
            @Override
            public void onComplete(String response) {
                mCheckingHandler.cancel();
                if (mCallback.isCancelled()) return;

                download();
            }

            @Override
            public void onError(String message) {
                mCheckingHandler.cancel();
                if (mCallback.isCancelled()) return;

                convert();
            }
        });
    }

    private void convert() {
        mStartTime = new Date();

        if (mConvertingHandler != null) mConvertingHandler.cancel();
        mConvertingHandler = new ConvertingHandler(Looper.getMainLooper());
        new SongRequest(new Request.Callback() {
            @Override
            public void onComplete(String response) {
                mConvertingHandler.cancel();
                if (mCallback.isCancelled()) return;

                download();
            }

            @Override
            public void onError(String message) {
                mConvertingHandler.cancel();
                if (mCallback.isCancelled()) return;

                mSong.deleteLocally(mContext);
                mAttemptIndex++;
                if (mAttemptIndex > RETRY_COUNT) {
                    mCallback.onError();
                } else {
                    convert();
                }
            }
        });
    }

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

    private class PingRequest extends Request {
        public PingRequest(Callback callback) {
            super(String.format("/ping/%s/%s.mp3", mHighDownloadQuality ? "highest" : "lowest", mSong.getSongId()), callback);
            sendRequest(RequestType.GET);
        }
    }

    private class SongRequest extends Request {
        public SongRequest(Callback callback) {
            super(String.format("/song/%s/%s.mp3", mHighDownloadQuality ? "highest" : "lowest", mSong.getSongId()), callback);
            replaceClient(
                new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.MINUTES)
                    .writeTimeout(5, TimeUnit.MINUTES)
                    .readTimeout(5, TimeUnit.MINUTES)
                    .build()
            );
            sendRequest(RequestType.GET);
        }
    }

    private class CheckingHandler extends Handler {
        private boolean mCancelled;

        public CheckingHandler(@NonNull Looper looper) {
            super(looper);
            mCancelled = false;

            sendEmptyMessage(0);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mCancelled) return;
            Date nowTime = new Date();

            int time = (int) (nowTime.getTime() - mStartTime.getTime()) / 1000;
            int seconds = time % 60;
            int minutes = time / 60;

            mCallback.showCheckingTime(minutes, seconds);
            sendEmptyMessageDelayed(0, 1000);
        }

        public void cancel() {
            mCancelled = true;
            mCheckingHandler = null;
        }

    }

    private class ConvertingHandler extends Handler {
        private boolean mCancelled;

        public ConvertingHandler(@NonNull Looper looper) {
            super(looper);
            mCancelled = false;

            sendEmptyMessage(0);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mCancelled) return;
            Date nowTime = new Date();

            int time = (int) (nowTime.getTime() - mStartTime.getTime()) / 1000;
            int seconds = time % 60;
            int minutes = time / 60;

            mCallback.showConvertingTime(mAttemptIndex, minutes, seconds);
            sendEmptyMessageDelayed(0, 1000);
        }

        public void cancel() {
            mCancelled = true;
            mConvertingHandler = null;
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
