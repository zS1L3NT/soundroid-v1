package com.zectan.soundroid.utils;

import com.zectan.soundroid.models.Song;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ListArrayUtils {
    public static List<String> startOrderFromId(List<String> order, String songId) {
        List<String> filtered = order
            .stream()
            .filter(s -> s.equals(songId))
            .collect(Collectors.toList());

        if (filtered.size() == 0) return order;
        int startPosition = order.indexOf(filtered.get(0));

        return startListFromPosition(order, startPosition);
    }

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
            .sorted((song1, song2) -> order.indexOf(song1.getSongId()) - order.indexOf(song2.getSongId()))
            .collect(Collectors.toList());
    }

    public static <T> List<T> startListFromPosition(List<T> list, int startPosition) {
        List<T> newList = new ArrayList<>();
        for (int i = startPosition; i < list.size(); i++) newList.add(list.get(i));
        for (int i = 0; i < startPosition; i++) newList.add(list.get(i));
        return newList;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Class<T> c, Collection<T> collection) {
        return collection.toArray((T[]) Array.newInstance(c, collection.size()));
    }
}
