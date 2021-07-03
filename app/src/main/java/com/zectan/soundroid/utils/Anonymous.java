package com.zectan.soundroid.utils;

import android.view.View;

import androidx.navigation.fragment.FragmentNavigator;

import java.util.ArrayList;
import java.util.List;

public class Anonymous {

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

}
