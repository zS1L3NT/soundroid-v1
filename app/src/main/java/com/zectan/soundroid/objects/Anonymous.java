package com.zectan.soundroid.objects;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.fragment.FragmentNavigator;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.source.ShuffleOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Anonymous {

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
     * Changes the order of a list but maintaining an item as the first in the list
     *
     * @param order         Order to rearrange
     * @param startPosition Position to start list from
     * @return New list
     */
    public static List<Integer> changeOrder(List<Integer> order, int startPosition) {
        if (startPosition < 0) return order;

        List<Integer> newOrder = new ArrayList<>();
        for (int i = startPosition; i < order.size(); i++) newOrder.add(order.get(i));
        for (int i = 0; i < startPosition; i++) newOrder.add(order.get(i));
        return newOrder;
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
     * Makes the order for display of the queue. All logic will be done here, just pass the values
     *
     * @param songs        All the songs in default order in a list
     * @param order        Order of the song indexes in an array
     * @param currentIndex Current index of the player
     * @param isLooping    If the player is looping the tracks
     * @return Formatted version of the queue
     */
    public static List<Song> formatQueue(List<Song> songs, int[] order, int currentIndex, boolean isLooping) {
        List<Song> queue = new ArrayList<>();

        List<Integer> listOrder = toListInteger(order);
        int indexOfCurrent = listOrder.indexOf(currentIndex);
        order = toIntArray(
            isLooping
                ? changeOrder(listOrder, indexOfCurrent)
                : listOrder.subList(indexOfCurrent, order.length)
        );

        for (int i = 0; i < order.length; i++)
            if (i != 0)
                queue.add(songs.get(order[i]));

        return queue;
    }

    /**
     * Convert an array of int to a list of integer
     *
     * @param array The array to convert
     * @return The list produced
     */
    public static List<Integer> toListInteger(int[] array) {
        List<Integer> list = new ArrayList<>();
        for (int item : array) {
            list.add(item);
        }
        return list;
    }

    /**
     * Convert a list of integer to an array of int
     *
     * @param list The list to convert
     * @return The array produced
     */
    public static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
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

    /**
     * Class to handle margins in an element
     */
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

    /**
     * I COPIED THIS CLASS FROM {@link com.google.android.exoplayer2.source.ShuffleOrder.DefaultShuffleOrder}
     */
    public static class CustomPlaybackOrder implements ShuffleOrder {

        private final Random random;
        private final int[] order;
        private final int[] indexInOrdered;

        private CustomPlaybackOrder(int[] order, Random random) {
            this.order = order;
            this.random = random;
            this.indexInOrdered = new int[order.length];
            for (int i = 0; i < order.length; i++) {
                indexInOrdered[order[i]] = i;
            }
        }

        public static CustomPlaybackOrder createShuffled(int length) {
            Random random = new Random();
            return new CustomPlaybackOrder(createShuffledList(length, random), random);
        }

        public static CustomPlaybackOrder createOrdered(int length) {
            return new CustomPlaybackOrder(createUnshuffledList(length), new Random());
        }

        private static int[] createShuffledList(int length, Random random) {
            int[] order = new int[length];
            for (int i = 0; i < length; i++) {
                int swapIndex = random.nextInt(i + 1);
                order[i] = order[swapIndex];
                order[swapIndex] = i;
            }
            // My code to set 0 as 0
            int[] my_order = new int[length];
            my_order[0] = 0;
            for (int i = 0, j = 0; i < order.length; i++) {
                if (order[i] == 0) continue;
                my_order[++j] = order[i];
            }
            return my_order;
        }

        private static int[] createUnshuffledList(int length) {
            return toIntArray(createOrder(length, 0));
        }

        public int[] getOrder() {
            return order;
        }

        @Override
        public int getLength() {
            return order.length;
        }

        @Override
        public int getNextIndex(int index) {
            int orderedIndex = indexInOrdered[index];
            return ++orderedIndex < order.length ? order[orderedIndex] : C.INDEX_UNSET;
        }

        @Override
        public int getPreviousIndex(int index) {
            int orderedIndex = indexInOrdered[index];
            return --orderedIndex >= 0 ? order[orderedIndex] : C.INDEX_UNSET;
        }

        @Override
        public int getLastIndex() {
            return order.length > 0 ? order[order.length - 1] : C.INDEX_UNSET;
        }

        @Override
        public int getFirstIndex() {
            return order.length > 0 ? order[0] : C.INDEX_UNSET;
        }

        @Override
        public CustomPlaybackOrder cloneAndInsert(int insertionIndex, int insertionCount) {
            int[] insertionPoints = new int[insertionCount];
            int[] insertionValues = new int[insertionCount];
            for (int i = 0; i < insertionCount; i++) {
                insertionPoints[i] = random.nextInt(order.length + 1);
                int swapIndex = random.nextInt(i + 1);
                insertionValues[i] = insertionValues[swapIndex];
                insertionValues[swapIndex] = i + insertionIndex;
            }
            Arrays.sort(insertionPoints);
            int[] newOrdered = new int[order.length + insertionCount];
            int indexInOldOrdered = 0;
            int indexInInsertionList = 0;
            for (int i = 0; i < order.length + insertionCount; i++) {
                if (indexInInsertionList < insertionCount
                    && indexInOldOrdered == insertionPoints[indexInInsertionList]) {
                    newOrdered[i] = insertionValues[indexInInsertionList++];
                } else {
                    newOrdered[i] = order[indexInOldOrdered++];
                    if (newOrdered[i] >= insertionIndex) {
                        newOrdered[i] += insertionCount;
                    }
                }
            }
            return new CustomPlaybackOrder(newOrdered, new Random(random.nextLong()));
        }

        @Override
        public CustomPlaybackOrder cloneAndRemove(int indexFrom, int indexToExclusive) {
            int numberOfElementsToRemove = indexToExclusive - indexFrom;
            int[] newOrdered = new int[order.length - numberOfElementsToRemove];
            int foundElementsCount = 0;
            for (int i = 0; i < order.length; i++) {
                if (order[i] >= indexFrom && order[i] < indexToExclusive) {
                    foundElementsCount++;
                } else {
                    newOrdered[i - foundElementsCount] =
                        order[i] >= indexFrom ? order[i] - numberOfElementsToRemove : order[i];
                }
            }
            return new CustomPlaybackOrder(newOrdered, new Random(random.nextLong()));
        }

        public CustomPlaybackOrder closeAndMove(int oldPosition, int newPosition) {
            List<Integer> list = Anonymous.toListInteger(order);
            int item = list.remove(oldPosition);
            list.add(newPosition, item);
            return new CustomPlaybackOrder(Anonymous.toIntArray(list), new Random(random.nextLong()));
        }

        @Override
        public CustomPlaybackOrder cloneAndClear() {
            return new CustomPlaybackOrder(createUnshuffledList(/* length= */ 0), new Random(random.nextLong()));
        }

    }

}
