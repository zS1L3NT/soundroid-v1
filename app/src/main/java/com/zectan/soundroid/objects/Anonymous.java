package com.zectan.soundroid.objects;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.fragment.FragmentNavigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Anonymous {

    /**
     * Formats the duration for {@link com.zectan.soundroid.fragments.PlayingFragment}
     *
     * @param duration Time to format
     * @return Formatted version of the duration
     */
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

    /**
     * Creates a list order starting
     *
     * @param startValue Value of the first item in the list
     * @param length     Length of the list
     * @return New list
     */
    public static List<Integer> createOrder(int length, int startValue) {
        List<Integer> order = new ArrayList<>();
        for (int i = startValue; i < length; i++) order.add(i);
        for (int i = 0; i < startValue; i++) order.add(i);
        return order;
    }

    /**
     * Creates a list from an old order starting from a certain index
     *
     * @param order      Order to rearrange
     * @param startIndex Index to start list from
     * @return New list
     */
    public static List<Integer> createOrder(List<Integer> order, int startIndex) {
        List<Integer> newOrder = new ArrayList<>();
        for (int i = startIndex; i < order.size(); i++) newOrder.add(order.get(i));
        for (int i = 0; i < startIndex; i++) newOrder.add(i);
        return newOrder;
    }

    /**
     * Changes the order of a list but maintaining an item as the first in the list
     *
     * @param order      Order to rearrange
     * @param startValue Value to start list from
     * @return New list
     */
    public static List<Integer> changeOrder(List<Integer> order, int startValue) {
        int position = order.indexOf(startValue);
        if (position < 0) return order;

        List<Integer> newOrder = new ArrayList<>();
        for (int i = position; i < order.size(); i++) newOrder.add(order.get(i));
        for (int i = 0; i < position; i++) newOrder.add(order.get(i));
        return newOrder;
    }

    /**
     * Shuffles the order but maintaining an item as the first item in the list
     *
     * @param startIndex Index to preserve the order of
     * @param length     Length of the list
     * @return New list
     */
    public static List<Integer> shuffleOrder(int length, int startIndex) {
        List<Integer> shuffle = new ArrayList<>();
        for (int i = 0; i < length; i++) if (i != startIndex) shuffle.add(i);
        Collections.shuffle(shuffle);
        shuffle.add(0, startIndex);
        return shuffle;
    }

    /**
     * Shuffles the order but maintaining an item as the first item in the list
     *
     * @param order      Order
     * @param startIndex Index to preserve the order of
     * @return New list
     */
    public static List<Integer> shuffleOrder(List<Integer> order, int startIndex) {
        List<Integer> shuffle = new ArrayList<>();
        for (int i = 0; i < order.size(); i++) if (i != startIndex) shuffle.add(order.get(i));
        Collections.shuffle(shuffle);
        shuffle.add(0, startIndex);
        return shuffle;
    }

    /**
     * Sorts songs and places them in the order defined in the order list
     *
     * @param songs Songs to sort
     * @param order Order to sort the songs by
     * @return New list
     */
    public static List<Song> sortSongs(List<Song> songs, List<String> order) {
        return songs
            .stream()
            .sorted((song1, song2) -> order.indexOf(song1.getId()) - order.indexOf(song2.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Formats a queue and removes the first item in the queue
     *
     * @param songs Songs to reorder
     * @param order Order to sort the songs by
     * @return New list
     */
    public static List<Song> formatQueue(List<Song> songs, List<Integer> order) {
        List<Song> queue = new ArrayList<>();
        for (int i = 0; i < order.size(); i++) {
            int songsIndex = order.get(i);
            if (songsIndex == -1) {
                queue.add(Song.getDefault());
            } else {
                queue.add(songs.get(songsIndex));
            }
        }

        return order.size() > 0 ? queue.subList(1, order.size()) : new ArrayList<>();
    }

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

    public static FragmentNavigator.Extras makeExtras(View view, String transitionName) {
        return new FragmentNavigator.Extras.Builder().addSharedElement(view, transitionName).build();
    }

    public static class MarginProxy {
        private final View mView;

        public MarginProxy(View view) {
            mView = view;
        }

        public int getLeftMargin() {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            return lp.leftMargin;
        }

        public void setLeftMargin(int margin) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            lp.setMargins(margin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            mView.requestLayout();
        }

        public int getTopMargin() {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            return lp.topMargin;
        }

        public void setTopMargin(int margin) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            lp.setMargins(lp.leftMargin, margin, lp.rightMargin, lp.bottomMargin);
            mView.requestLayout();
        }

        public int getRightMargin() {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            return lp.rightMargin;
        }

        public void setRightMargin(int margin) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            lp.setMargins(lp.leftMargin, lp.topMargin, margin, lp.bottomMargin);
            mView.requestLayout();
        }

        public int getBottomMargin() {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            return lp.bottomMargin;
        }

        public void setBottomMargin(int margin) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, margin);
            mView.requestLayout();
        }
    }

}
