package com.zectan.soundroid.Utils;

import android.graphics.Bitmap;
import android.view.View;

import androidx.navigation.fragment.FragmentNavigator;

import com.zectan.soundroid.BuildConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    /**
     * Shortcut to make navigator extras
     * This method reduces the length of the code
     *
     * @param view           Shared View
     * @param transitionName Transition Name
     * @return Navigator Extras
     */
    public static FragmentNavigator.Extras makeExtras(View view, String transitionName) {
        return new FragmentNavigator.Extras.Builder().addSharedElement(view, transitionName).build();
    }

    public static List<String> getQueries(String str) {
        List<String> queries = new ArrayList<>();
        for (int i = 0; i < str.length(); i++) {
            queries.add(str.substring(0, i + 1).toLowerCase());
        }
        return queries;
    }

    public static int getRandomInt() {
        return (int) (Math.random() * 1000000000);
    }

    public static boolean versionAtLeast(String test) {
        List<String> currentPortions = Arrays.asList(BuildConfig.VERSION_NAME.split("\\."));
        List<String> testPortions = Arrays.asList(test.split("\\."));
        assert currentPortions.size() == 3;
        assert testPortions.size() == 3;

        for (int i = 0; i < 3; i++) {
            if (!currentPortions.get(i).equals(testPortions.get(i))) {
                int currentInt = Integer.parseInt(currentPortions.get(i));
                int testInt = Integer.parseInt(testPortions.get(i));
                return currentInt > testInt;
            }
        }

        return true;
    }

    public static String formatDuration(int duration) {
        StringBuilder hours = new StringBuilder();
        StringBuilder minutes = new StringBuilder();
        StringBuilder seconds = new StringBuilder();

        if (duration >= 3600) {
            hours.append(duration / 3600);
        }
        if (duration >= 60) {
            if (duration >= 3600) {
                // Hours, minutes must be double digits
                int sDuration = duration % 3600;
                minutes.append(sDuration / 60);
                if (minutes.length() == 1) {
                    minutes.insert(0, "0");
                }
            } else {
                // No hours, minutes can be single digit
                minutes.append(duration / 60);
            }
        }
        seconds.append(duration % 60);
        if (seconds.length() == 1) {
            seconds.insert(0, "0");
        }

        StringBuilder formatted = new StringBuilder();
        if (!hours.toString().equals("")) {
            formatted.append(hours).append(":");
        }
        if (minutes.toString().equals("")) {
            formatted.append("0").append(":");
        } else {
            formatted.append(minutes).append(":");
        }
        formatted.append(seconds);

        return formatted.toString();
    }

    public static Bitmap cropSquare(Bitmap bitmap) {
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            return Bitmap.createBitmap(
                bitmap,
                bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                0,
                bitmap.getHeight(),
                bitmap.getHeight()
            );
        } else {
            return Bitmap.createBitmap(
                bitmap,
                0,
                bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                bitmap.getWidth(),
                bitmap.getWidth()
            );
        }
    }
}
