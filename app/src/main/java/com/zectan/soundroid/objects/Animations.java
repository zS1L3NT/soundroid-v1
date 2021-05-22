package com.zectan.soundroid.objects;

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
    public static boolean songListItemSqueeze(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            v.animate().scaleX(1f).scaleY(1f).setDuration(75);
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.animate().scaleX(0.98f).scaleY(0.98f).setDuration(75);
        }
        return false;
    }

    /**
     * Small sized squeeze animation that is accessible everywhere
     *
     * @param v     View
     * @param event Event
     * @return Boolean
     */
    public static boolean smallSqueeze(View v, MotionEvent event) {
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
    public static boolean mediumSqueeze(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            v.animate().scaleX(1f).scaleY(1f).setDuration(100);
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100);
        }
        return false;
    }
}
