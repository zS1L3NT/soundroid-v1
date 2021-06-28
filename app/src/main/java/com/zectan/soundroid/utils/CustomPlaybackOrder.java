package com.zectan.soundroid.utils;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.source.ShuffleOrder;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * I COPIED THIS CLASS FROM {@link com.google.android.exoplayer2.source.ShuffleOrder.DefaultShuffleOrder}
 */
public class CustomPlaybackOrder implements ShuffleOrder {

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
        if (length == 0) return order;
        int[] my_order = new int[length];
        my_order[0] = 0;
        for (int i = 0, j = 0; i < order.length; i++) {
            if (order[i] == 0) continue;
            my_order[++j] = order[i];
        }
        return my_order;
    }

    private static int[] createUnshuffledList(int length) {
        return ListArrayHandler.toIntArray(ListArrayHandler.createOrder(length, 0));
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
        List<Integer> list = ListArrayHandler.toListInteger(order);
        int item = list.remove(oldPosition);
        list.add(newPosition, item);
        return new CustomPlaybackOrder(ListArrayHandler.toIntArray(list), new Random(random.nextLong()));
    }

    @Override
    public CustomPlaybackOrder cloneAndClear() {
        return new CustomPlaybackOrder(createUnshuffledList(/* length= */ 0), new Random(random.nextLong()));
    }

}