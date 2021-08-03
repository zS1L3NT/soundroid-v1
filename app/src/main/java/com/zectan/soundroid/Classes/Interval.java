package com.zectan.soundroid.Classes;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public class Interval extends Handler {
    private int mDelay;
    private boolean mStarted;
    private boolean mCancelled;

    /**
     * Interval class to run a function at different intervals on the main thread
     *
     * @param looper Main Looper
     * @param delay  Interval delay in ms
     */
    public Interval(@NonNull Looper looper, int delay) {
        super(looper);
        mDelay = delay;
        mStarted = false;
        mCancelled = false;
    }

    /**
     * Handles a message, runs callback if not cancelled
     *
     * @param msg Message
     */
    @Override
    public void handleMessage(@NonNull Message msg) {
        if (!mCancelled) {
            onNextCall();
            sendEmptyMessageDelayed(0, mDelay);
        }
    }

    /**
     * Method that runs after every delay
     */
    public void onNextCall() {
    }

    /**
     * Start the interval immediately
     */
    public void start() {
        mStarted = true;
        sendEmptyMessage(0);
    }

    /**
     * Set the delay
     *
     * @param delay New delay
     */
    public void setDelay(int delay) {
        mDelay = delay;
    }

    /**
     * Cancel the interval
     */
    public void cancel() {
        mCancelled = true;
    }

    /**
     * Check if the interval has started
     *
     * @return If the interval has started
     */
    public boolean isStarted() {
        return mStarted;
    }

}