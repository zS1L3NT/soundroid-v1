package com.zectan.soundroid.utils;

import android.view.View;

import androidx.navigation.fragment.FragmentNavigator;

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

    public static boolean versionAtLeast(String current, String test) {
        List<String> currentPortions = Arrays.asList(current.split("\\."));
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
}
