package com.zectan.soundroid.Utils;

import android.graphics.Bitmap;
import android.view.View;

import androidx.navigation.fragment.FragmentNavigator;

import com.zectan.soundroid.BuildConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

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

    /**
     * Generate a random int
     *
     * @return Integer
     */
    public static int getRandomInt() {
        return (int) (Math.random() * 1000000000);
    }

    /**
     * Check if the current app version is up to date with the latest version
     *
     * @param latest Latest version
     * @return true if up to date and false if not
     */
    public static boolean versionAtLeast(String latest) {
        List<String> currentPortions = Arrays.asList(BuildConfig.VERSION_NAME.split("\\."));
        List<String> testPortions = Arrays.asList(latest.split("\\."));
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

    /**
     * Format a duration integer to form a nice number for the player
     *
     * @param duration TIme duration in seconds
     * @return Duration formatted
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
     * Crop the bitmap to a square
     *
     * @param bitmap Bitmap to crop
     * @return Squared bitmap
     */
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

    /**
     * Create a list filterer to only allow unique items by a specific property
     *
     * @param keyExtractor Specific property
     * @param <T>          Type
     * @return Continuation in the stream
     */
    public static <T> Predicate<T> filterDistinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * Create custom Regex expression to search through songs and playlists
     * Splits the query into characters and the number of times you typed it
     * Uses regex to check if the character appears n times
     *
     * @param query Query
     * @return Regex string
     */
    public static String createRegex(String query) {
        // Determine character duplicates
        HashMap<Character, Integer> characterCount = new HashMap<>();
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            if (characterCount.containsKey(c)) {
                characterCount.put(c, Objects.requireNonNull(characterCount.get(c)) + 1);
            } else {
                characterCount.put(c, 1);
            }
        }

        // Model: .*(?=(.*X.*){1})(?=(.*Y.*){2}).*
        // X, Y -> the character
        // 1, 2 -> the character count
        // (?=(...)) -> Positive lookahead on the string
        // (?=(.*X.*)) -> See if character exists anywhere in string
        // (?=(.*X.*){1}) -> Check if character exists n time in string

        StringBuilder regex = new StringBuilder(".*");
        for (Character character : characterCount.keySet()) {
            int count = Objects.requireNonNull(characterCount.get(character));
            regex.append(String.format("(?=(.*%s.*){%s})", character, count));
        }
        return regex.append(".*").toString();
    }
}
