package com.zectan.soundroid.viewmodels;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.zectan.soundroid.adapters.QueueAdapter;
import com.zectan.soundroid.anonymous.CustomPlaybackOrder;
import com.zectan.soundroid.anonymous.ListArrayHandler;
import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Queue Number:
 * The location in the Position object that is playing
 * This object doesn't change when shuffling changes
 * Can Be:
 * 0
 * 9
 * <p>
 * Sequence:
 * The arrangement of the songs in a playlist depending on the shuffle state
 * This object changes as the shuffle state or current song changes
 * This object pushes the first item to the end
 * Can Be:
 * 6 7 8 9 0 1 2 3 4 5
 * 6 4 8 2 5 7 0 9 1 3
 */

public class PlayingViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) PlayingViewModel";
    public final StrictLiveData<Playlist> queue = new StrictLiveData<>(Playlist.getEmpty());
    public final StrictLiveData<Song> currentSong = new StrictLiveData<>(Song.getEmpty());
    public final StrictLiveData<Boolean> isBuffering = new StrictLiveData<>(false);
    public final StrictLiveData<Boolean> isPlaying = new StrictLiveData<>(false);
    public final StrictLiveData<Boolean> isShuffling = new StrictLiveData<>(false);
    public final StrictLiveData<Boolean> isLooping = new StrictLiveData<>(true);
    public final MutableLiveData<String> error = new MutableLiveData<>();
    private CustomPlaybackOrder mOrder;
    private SimpleExoPlayer mPlayer;

    public PlayingViewModel() {
        // Required empty public constructor
    }

    /**
     * Starts a playlist and shuffles it
     *
     * @param playlist New playlist for MusicPlayer to loop
     * @param songId   Id of the song you're starting from
     */
    public void startPlaylist(Playlist playlist, String songId) {
        Log.i(TAG, String.format("Start Playlist (%s)[%s]", playlist.getInfo().getId(), songId));

        selectSong(playlist, songId);

        if (isShuffling.getValue())
            mOrder = CustomPlaybackOrder.createShuffled(playlist.size());
        else
            mOrder = CustomPlaybackOrder.createOrdered(playlist.size());
        mPlayer.setShuffleOrder(mOrder);
    }

    /**
     * Method to play a song without shuffling the queue
     *
     * @param playlist New playlist for the MusicPlayer to play
     * @param songId   Id of the starting song
     */
    public void selectSong(Playlist playlist, String songId) {
        Log.i(TAG, String.format("Play from Playlist (%s)[%s]", playlist.getInfo().getId(), songId));

        // If the new clicked song is from a new playlist or has no id
        this.queue.setValue(playlist);

        mPlayer.stop();
        mPlayer.clearMediaItems();

        List<Integer> sequence = ListArrayHandler.createOrder(playlist.size(), playlist.getIndexOfSong(songId));
        for (int i = 0; i < playlist.size(); i++) {
            Song song = playlist.getSong(sequence.get(i));
            mPlayer.addMediaItem(i, song.getMediaItem());
        }

        mPlayer.prepare();
        mPlayer.play();
    }

    /**
     * Formats the display queue
     *
     * @return List of songs in the queue
     */
    public List<Song> getItemsInQueue() {
        if (mPlayer.getCurrentMediaItem() == null) return new ArrayList<>();
        return ListArrayHandler.formatQueue(
            queue.getValue().getSongs(),
            mOrder.getOrder(),
            queue.getValue().getIndexOfSong(mPlayer.getCurrentMediaItem().mediaId),
            isLooping.getValue()
        );
    }


    /**
     * Play the previous song in the queue or reset the song progress
     */
    public void playPreviousSong() {
        if (mPlayer.getContentPosition() <= 2000) {
            mPlayer.prepare();
            mPlayer.previous();
        } else {
            mPlayer.seekTo(0);
        }
    }

    /**
     * Play the next song in the queue
     */
    public void playNextSong() {
        mPlayer.prepare();
        mPlayer.next();
    }

    public void toggleShuffle(QueueAdapter queueAdapter) {
        isShuffling.setValue(!isShuffling.getValue());
        if (isShuffling.getValue())
            mOrder = CustomPlaybackOrder.createShuffled(queue.getValue().size());
        else
            mOrder = CustomPlaybackOrder.createOrdered(queue.getValue().size());
        queueAdapter.updateQueue(getItemsInQueue());
        mPlayer.setShuffleOrder(mOrder);
    }

    public void toggleLoop(QueueAdapter queueAdapter) {
        int oldRepeatMode = mPlayer.getRepeatMode();
        int newRepeatMode = oldRepeatMode == Player.REPEAT_MODE_ALL ? Player.REPEAT_MODE_OFF : Player.REPEAT_MODE_ALL;
        mPlayer.setRepeatMode(newRepeatMode);
        isLooping.setValue(newRepeatMode == 2);
        queueAdapter.updateQueue(getItemsInQueue());
    }

    public void play() {
        mPlayer.play();
    }

    public void pause() {
        mPlayer.pause();
    }

    public Player getPlayer() {
        return mPlayer;
    }

    /**
     * THIS METHOD SHOULD ONLY BE RUN ONCE
     *
     * @param player Instance of the player. Should only be created once
     */
    public void setPlayer(SimpleExoPlayer player) {
        mPlayer = player;
        mPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                PlayingViewModel.this.isPlaying.postValue(isPlaying);
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                PlayingViewModel.this.isBuffering.postValue(state == Player.STATE_BUFFERING);
            }

            @Override
            public void onMediaItemTransition(@Nullable @org.jetbrains.annotations.Nullable MediaItem mediaItem, int reason) {
                if (mediaItem != null) {
                    Song song = queue.getValue().getSong(mediaItem.mediaId);
                    currentSong.postValue(song);
                    Log.i(TAG, String.format("Song changed to %s", song));
                }
            }

            @Override
            public void onPlayerError(@NotNull ExoPlaybackException error) {
                PlayingViewModel.this.error.postValue(error.getMessage());
            }
        });
    }

    public void onMoveSong(int oldPosition, int newPosition) {
        Log.i(TAG, String.format("Dragged song from %s to %s", oldPosition, newPosition));
        mOrder = mOrder.closeAndMove(oldPosition, newPosition);
        mPlayer.setShuffleOrder(mOrder);
    }

    public void onRemoveSong(String songId) {
        Log.i(TAG, String.format("Swiped song %s", songId));
        Playlist queue = this.queue.getValue();
        int position = mOrder.getOrder()[queue.getIndexOfSong(songId)];
        queue.removeSong(songId);
        this.queue.setValue(queue);

        mPlayer.removeMediaItem(position);
        mOrder = mOrder.cloneAndRemove(position, position + 1);
        mPlayer.setShuffleOrder(mOrder);
    }

}
