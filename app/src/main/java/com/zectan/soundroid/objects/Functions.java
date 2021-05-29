package com.zectan.soundroid.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Functions {

    public static String formatDate(int duration) {
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

    public static List<Integer> createOrder(int startValue, int length) {
        List<Integer> order = new ArrayList<>();
        for (int i = startValue; i < length; i++) order.add(i);
        for (int i = 0; i < startValue; i++) order.add(i);
        return order;
    }

    public static List<Integer> changeOrder(List<Integer> order, int startValue) {
        int position = order.indexOf(startValue);
        if (position < 0) return order;

        List<Integer> newOrder = new ArrayList<>();
        for (int i = position; i < order.size(); i++) newOrder.add(order.get(i));
        for (int i = 0; i < position; i++) newOrder.add(order.get(i));
        return newOrder;
    }

    public static List<Integer> shuffleOrder(int startValue, int length) {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < length; i++) if (i != startValue) order.add(i);
        Collections.shuffle(order);
        order.add(0, startValue);
        return order;
    }

    public static List<Song> sortSongs(List<Song> songs, List<String> order) {
        return songs
                .stream()
                .sorted((song1, song2) -> order.indexOf(song1.getId()) - order.indexOf(song2.getId()))
                .collect(Collectors.toList());
    }

    public static List<Song> formQueue(List<Song> songs, List<Integer> order, int queueNumber, boolean looping) {
        List<Song> queue = new ArrayList<>();
        for (int i = 0; i < order.size(); i++) {
            queue.add(songs.get(order.get(i)));
        }

        return queue.subList(looping ? 1 : (queueNumber + 1), order.size());
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println((int) (Math.random() * 10));
        }
    }

}
