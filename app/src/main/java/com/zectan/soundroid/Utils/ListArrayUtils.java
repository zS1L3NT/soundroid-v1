package com.zectan.soundroid.Utils;

import com.zectan.soundroid.Models.Song;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListArrayUtils {

    /**
     * Reorder a list to start with a specific song id
     *
     * @param order  Order to change
     * @param songId ID to start with
     * @return New order
     */
    public static List<String> startOrderFromId(List<String> order, String songId) {
        List<String> filtered = order
            .stream()
            .filter(s -> s.equals(songId))
            .collect(Collectors.toList());

        if (filtered.size() == 0) return order;
        int startPosition = order.indexOf(filtered.get(0));

        return startListFromPosition(order, startPosition);
    }

    /**
     * Shuffle a list but maintain the first item in the queue
     *
     * @param order Order to shuffle
     * @return New order
     */
    public static List<String> shuffleOrder(List<String> order) {
        if (order.size() == 0) return order;
        String itemOne = order.get(0);
        order.remove(0);

        Collections.shuffle(order);
        order.add(0, itemOne);
        return order;
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
            .sorted(Comparator.comparingInt(song -> order.indexOf(song.getSongId())))
            .collect(Collectors.toList());
    }

    /**
     * Reorder a list to start with at a specific position
     *
     * @param list          Order to change
     * @param startPosition Position to start at
     * @param <T>           Type
     * @return New Order
     */
    public static <T> List<T> startListFromPosition(List<T> list, int startPosition) {
        List<T> newList = new ArrayList<>();
        for (int i = startPosition; i < list.size(); i++) newList.add(list.get(i));
        for (int i = 0; i < startPosition; i++) newList.add(list.get(i));
        return newList;
    }

    /**
     * Convert a list to an array
     *
     * @param c          Class
     * @param collection Collection to convert
     * @param <T>        Type
     * @return Array type
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Class<T> c, Collection<T> collection) {
        return collection.toArray((T[]) Array.newInstance(c, collection.size()));
    }
}
