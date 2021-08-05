package com.zectan.soundroid.Utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.zectan.soundroid.Classes.StrictLiveData;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.Services.PlayingService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QueueManager {
    private static final String TAG = "(SounDroid) QueueManager";
    private final Context mContext;
    private final SimpleExoPlayer mPlayer;

    private final StrictLiveData<List<Song>> mQueue;
    private final StrictLiveData<Boolean> mIsLooping;
    private final StrictLiveData<Boolean> mIsShuffling;
    private final StrictLiveData<Song> mCurrentSong;

    private final List<Song> mSongs;
    private final List<String> mSortedOrder;
    private final List<String> mShuffledOrder;

    private final boolean mHighDownloadQuality;
    private int mPosition;

    /**
     * Create a queue manager who's sole purpose is to manage the queue
     * and update the current playing song being shown
     *
     * @param playingService      Playing Service
     * @param player              Player
     * @param songs               All the songs in the queue
     * @param order               Order of the playlist
     * @param highDownloadQuality Quality of downloads
     */
    public QueueManager(
        PlayingService playingService,
        SimpleExoPlayer player,
        List<Song> songs,
        List<String> order,
        boolean highDownloadQuality
    ) {
        mContext = playingService.getApplicationContext();
        mPlayer = player;

        mQueue = playingService.queue;
        mIsLooping = playingService.isLooping;
        mIsShuffling = playingService.isShuffling;
        mCurrentSong = playingService.currentSong;

        mSongs = new ArrayList<>(songs);
        mSortedOrder = new ArrayList<>(order);
        mShuffledOrder = ListArrayUtils.shuffleOrder(new ArrayList<>(order));
        mHighDownloadQuality = highDownloadQuality;
    }

    /**
     * Create the object which orders the queue
     *
     * @return Song list
     */
    private List<Song> formatQueue() {
        List<Song> queue;

        if (mIsLooping.getValue() && mIsShuffling.getValue()) {
            queue = ListArrayUtils
                .startListFromPosition(
                    mShuffledOrder
                        .stream()
                        .map(this::getSongById)
                        .collect(Collectors.toList()),
                    mPosition
                );
        } else if (mIsShuffling.getValue()) {
            queue = mShuffledOrder
                .stream()
                .map(this::getSongById)
                .collect(Collectors.toList())
                .subList(
                    mPosition,
                    mShuffledOrder.size()
                );
        } else if (mIsLooping.getValue()) {
            queue = ListArrayUtils
                .startListFromPosition(
                    mSortedOrder
                        .stream()
                        .map(this::getSongById)
                        .collect(Collectors.toList()),
                    mPosition
                );
        } else {
            queue = mSortedOrder
                .stream()
                .map(this::getSongById)
                .collect(Collectors.toList())
                .subList(
                    mPosition,
                    mSortedOrder.size()
                );
        }

        if (queue.size() > 0) queue.remove(0);
        return queue;
    }

    /**
     * Skip to the next song in the queue
     * Changes the position in the orders that is being played
     */
    public void nextSong() {
        if (mSongs.size() == 0) return;
        int position = mPosition;
        int lastPosition = mSortedOrder.size() - 1;

        if (position == lastPosition) {
            // End of the queue, check for looping
            if (mIsLooping.getValue()) {
                // Go to the start of the queue
                mPosition = 0;
            } else {
                return;
            }
            // else: Don't change a thing
        } else {
            // Not end of the queue
            mPosition = position + 1;
        }

        updateLiveSong();
    }

    /**
     * Rewind to the previous song in the queue
     * Changes the position in the orders that is being played
     */
    public void backSong() {
        if (mSongs.size() == 0) return;
        int position = mPosition;
        int lastPosition = mSortedOrder.size() - 1;

        if (position == 0) {
            // Start of the queue, check for looping
            if (mIsLooping.getValue()) {
                // Go to the end of the queue
                mPosition = lastPosition;
            }
            // else: Don't change a thing
        } else {
            // Not the start of the queue
            mPosition = position - 1;
        }

        updateLiveSong();
    }

    /**
     * Starts a song in the current queue
     * Reorders the list of songs and sets the current playing to index 0
     *
     * @param song Song to start from
     */
    public void goToSong(Song song) {
        mPosition = 0;
        List<String> sortedOrder = new ArrayList<>(ListArrayUtils.startOrderFromId(mSortedOrder, song.getSongId()));
        mSortedOrder.clear();
        mSortedOrder.addAll(sortedOrder);
        List<String> shuffleOrder = ListArrayUtils.shuffleOrder(new ArrayList<>(mSortedOrder));
        mShuffledOrder.clear();
        mShuffledOrder.addAll(shuffleOrder);

        updateLiveSong();
    }

    /**
     * Add a song to the queue
     * Adds the song to the bottom of the orders
     *
     * @param song Song to add
     */
    public void addSong(Song song) {
        if (mSongs.stream().anyMatch(s -> s.getSongId().equals(song.getSongId()))) {
            Log.d(TAG, String.format("Song %s already exists in queue, skipping", song));
            return;
        }

        mSongs.add(song);
        mSortedOrder.add(mPosition, song.getSongId());
        mShuffledOrder.add(mPosition, song.getSongId());
        mPosition++;
        if (mSongs.size() == 1) {
            goToSong(song);
        }

        updateLiveQueue();
    }

    /**
     * Remove a song from the queue
     * Removes the id from the orders
     *
     * @param songId Song id to remove
     */
    public void removeSong(String songId) {
        List<Song> songs = new ArrayList<>(mSongs);
        mSongs.clear();
        mSongs.addAll(songs.stream().filter(s -> !s.getSongId().equals(songId)).collect(Collectors.toList()));
        mSortedOrder.remove(songId);
        mShuffledOrder.remove(songId);
        if (mPosition == mSortedOrder.size()) mPosition--;

        updateLiveQueue();
    }

    /**
     * Move a song in the queue to another position
     *
     * @param oldPosition Old position in the queue it was at
     * @param newPosition New position in the queue it was at
     */
    public void moveSong(int oldPosition, int newPosition) {
        if (mIsShuffling.getValue()) {
            String currentId = mShuffledOrder.get(mPosition);

            mShuffledOrder.add(
                (newPosition + mPosition) % mShuffledOrder.size(),
                mShuffledOrder.remove((oldPosition + mPosition) % mShuffledOrder.size())
            );

            // This checks if the moved item went past beyond start/end of the queue
            if (mShuffledOrder.indexOf(currentId) > mPosition) {
                mShuffledOrder.add(
                    mShuffledOrder.size() - 2,
                    mShuffledOrder.remove(0)
                );
            } else if (mShuffledOrder.indexOf(currentId) < mPosition) {
                mShuffledOrder.add(
                    0,
                    mShuffledOrder.remove(mShuffledOrder.size() - 2)
                );
            }
        } else {
            String currentId = mSortedOrder.get(mPosition);

            mSortedOrder.add(
                (newPosition + mPosition) % mSortedOrder.size(),
                mSortedOrder.remove((oldPosition + mPosition) % mSortedOrder.size())
            );

            // This checks if the moved item went past beyond start/end of the queue
            if (mSortedOrder.indexOf(currentId) > mPosition) {
                mSortedOrder.add(
                    mSortedOrder.size() - 2,
                    mSortedOrder.remove(0)
                );
            } else if (mSortedOrder.indexOf(currentId) < mPosition) {
                mSortedOrder.add(
                    0,
                    mSortedOrder.remove(mSortedOrder.size() - 2)
                );
            }
        }

        updateLiveQueue();
    }

    /**
     * Toggle loop
     */
    public void toggleLoop() {
        mIsLooping.setValue(!mIsLooping.getValue());
        updateLiveQueue();
    }

    /**
     * Toggle shuffle
     */
    public void toggleShuffle() {
        List<String> shuffledOrder = ListArrayUtils.shuffleOrder(new ArrayList<>(mSortedOrder));

        mIsShuffling.setValue(!mIsShuffling.getValue());
        mShuffledOrder.clear();
        mShuffledOrder.addAll(shuffledOrder);
        updateLiveQueue();
    }

    /**
     * Fetch a song by its ID
     *
     * @param id ID
     * @return Song object
     */
    @Nullable
    public Song getSongById(String id) {
        List<Song> filtered = mSongs
            .stream()
            .filter(s -> s.getSongId().equals(id))
            .collect(Collectors.toList());

        if (filtered.size() == 0) return null;
        return filtered.get(0);
    }

    /**
     * Get all songs
     *
     * @return All songs
     */
    public List<Song> getSongs() {
        return mSongs;
    }

    /**
     * Change the current playing song to he first song in the queue
     */
    private void updateLiveSong() {
        Song song;
        if (mIsShuffling.getValue()) {
            song = getSongById(mShuffledOrder.get(mPosition));
        } else {
            song = getSongById(mSortedOrder.get(mPosition));
        }
        assert song != null;
        mCurrentSong.setValue(song);

        mPlayer.stop();
        mPlayer.setMediaItem(song.getMediaItem(mContext, mHighDownloadQuality));
        mPlayer.prepare();
        mPlayer.play();

        updateLiveQueue();
    }

    /**
     * Update the queue with the latest queue
     */
    public void updateLiveQueue() {
        mQueue.setValue(formatQueue());
    }

}
