package com.zectan.soundroid.utils;

import com.zectan.soundroid.models.Song;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ListArrayUtils {
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
            .sorted((song1, song2) -> order.indexOf(song1.getSongId()) - order.indexOf(song2.getSongId()))
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

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Class<T> c, Collection<T> collection) {
        return collection.toArray((T[]) Array.newInstance(c, collection.size()));
    }
}
