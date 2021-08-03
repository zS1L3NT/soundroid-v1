package com.zectan.soundroid.Utils;

import android.os.Looper;

import androidx.annotation.NonNull;

import com.zectan.soundroid.Classes.Interval;

public class Debounce {
    private final LoopHandler mLoopHandler;
    private final int mDelay;
    private Runnable mRunnable;

    /**
     * Debounce to prevent spam
     * Fire and wait
     *
     * @param delay Time delay
     */
    public Debounce(int delay) {
        mDelay = delay;
        mLoopHandler = new LoopHandler(Looper.getMainLooper());
    }

    /**
     * Debounce a specific function
     *
     * @param runnable Runnable to be run
     */
    public void post(Runnable runnable) {
        mRunnable = runnable;
        if (!mLoopHandler.isStarted()) {
            mLoopHandler.start();
        }
    }

    /**
     * Cancel the debounce
     */
    public void cancel() {
        mLoopHandler.cancel();
    }

    private class LoopHandler extends Interval {

        public LoopHandler(@NonNull Looper looper) {
            super(looper, mDelay);
        }

        @Override
        public void onNextCall() {
            if (mRunnable != null) mRunnable.run();
            mRunnable = null;
        }
    }

}