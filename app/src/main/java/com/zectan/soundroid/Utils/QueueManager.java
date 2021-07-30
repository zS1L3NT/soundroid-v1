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

    private final boolean mHighQuality;
    private int mPosition;

    public QueueManager(
        PlayingService playingService,
        SimpleExoPlayer player,
        List<Song> songs,
        List<String> order,
        boolean highQuality
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
        mHighQuality = highQuality;
    }

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

    public void addSong(Song song) {
        if (mSongs.stream().anyMatch(s -> s.getSongId().equals(song.getSongId()))) {
            Log.d(TAG, String.format("Song %s already exists in queue, skipping", song));
            return;
        }

        mSongs.add(song);
        mSortedOrder.add(song.getSongId());
        mShuffledOrder.add((int) (Math.random() * mShuffledOrder.size()), song.getSongId());
        if (mSongs.size() == 1) {
            goToSong(song);
        }

        updateLiveQueue();
    }

    public void removeSong(String songId) {
        List<Song> songs = new ArrayList<>(mSongs);
        mSongs.clear();
        mSongs.addAll(songs.stream().filter(s -> !s.getSongId().equals(songId)).collect(Collectors.toList()));
        mSortedOrder.remove(songId);
        mShuffledOrder.remove(songId);
        if (mPosition == mSortedOrder.size()) mPosition--;

        updateLiveQueue();
    }

    public void moveSong(int oldPosition, int newPosition) {
        if (mIsShuffling.getValue()) {
            String currentId = mShuffledOrder.get(mPosition);

            mShuffledOrder.add(
                (newPosition + mPosition) % mShuffledOrder.size(),
                mShuffledOrder.remove((oldPosition + mPosition) % mShuffledOrder.size())
            );

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

    public void toggleLoop() {
        mIsLooping.setValue(!mIsLooping.getValue());
        updateLiveQueue();
    }

    public void toggleShuffle() {
        List<String> shuffledOrder = ListArrayUtils.shuffleOrder(new ArrayList<>(mSortedOrder));

        mIsShuffling.setValue(!mIsShuffling.getValue());
        mShuffledOrder.clear();
        mShuffledOrder.addAll(shuffledOrder);
        updateLiveQueue();
    }

    public @Nullable
    Song getSongById(String id) {
        List<Song> filtered = mSongs
            .stream()
            .filter(s -> s.getSongId().equals(id))
            .collect(Collectors.toList());

        if (filtered.size() == 0) return null;
        return filtered.get(0);
    }

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
        mPlayer.setMediaItem(song.getMediaItem(mContext, mHighQuality));
        mPlayer.prepare();
        mPlayer.play();

        updateLiveQueue();
    }

    public void updateLiveQueue() {
        mQueue.setValue(formatQueue());
    }

}
