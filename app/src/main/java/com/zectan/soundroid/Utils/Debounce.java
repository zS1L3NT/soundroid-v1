package com.zectan.soundroid.Utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public class Debounce {
    private final LoopHandler mLoopHandler;
    private final int mDelay;
    private Callback mCallback;

    public Debounce(int delay) {
        mDelay = delay;
        mLoopHandler = new LoopHandler(Looper.getMainLooper());
    }

    public void post(Callback callback) {
        mCallback = callback;
        if (!mLoopHandler.isStarted()) {
            mLoopHandler.sendEmptyMessage(0);
        }
    }

    public void cancel() {
        mLoopHandler.cancel();
    }

    public interface Callback {
        void run();
    }

    private class LoopHandler extends Handler {
        private boolean mStarted;
        private boolean mCancelled;

        public LoopHandler(@NonNull Looper looper) {
            super(looper);
            mStarted = false;
            mCancelled = false;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            mStarted = true;
            if (!mCancelled) {
                if (mCallback != null) mCallback.run();
                mCallback = null;
                sendEmptyMessageDelayed(0, mDelay);
            }
        }

        public boolean isStarted() {
            return mStarted;
        }

        public void cancel() {
            mCancelled = true;
        }
    }

}