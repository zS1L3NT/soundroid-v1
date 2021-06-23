package com.zectan.soundroid.anonymous;

import android.view.MotionEvent;
import android.view.View;

public class Animations {

    /**
     * Small sized squeeze animation that is accessible everywhere
     *
     * @param v     View
     * @param event Event
     * @return Boolean
     */
    public static boolean animationSmallSqueeze(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            v.animate().scaleX(1f).scaleY(1f).setDuration(100);
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100);
        }
        return false;
    }

    /**
     * Medium sized squeeze animation that is accessible everywhere
     *
     * @param v     View
     * @param event Event
     * @return Boolean
     */
    public static boolean animationMediumSqueeze(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            v.animate().scaleX(1f).scaleY(1f).setDuration(100);
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100);
        }
        return false;
    }
}
