package com.zectan.soundroid.viewmodels;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.ListArrayUtils;
import com.zectan.soundroid.utils.QueueManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public final StrictLiveData<Playlist> playlist = new StrictLiveData<>(Playlist.getEmpty());
    public final StrictLiveData<Song> currentSong = new StrictLiveData<>(Song.getEmpty());
    public final StrictLiveData<Boolean> isBuffering = new StrictLiveData<>(false);
    public final StrictLiveData<Boolean> isPlaying = new StrictLiveData<>(false);
    public final StrictLiveData<Boolean> isShuffling = new StrictLiveData<>(false);
    public final StrictLiveData<Boolean> isLooping = new StrictLiveData<>(true);
    public final StrictLiveData<String> error = new StrictLiveData<>("");
    public final StrictLiveData<List<Song>> queue = new StrictLiveData<>(new ArrayList<>());
    private QueueManager mQueueManager;
    private SimpleExoPlayer mPlayer;
    private boolean initialised = false;
    private AudioManager am;
    private AudioFocusRequest afr;

    public PlayingViewModel() {
        // Required empty public constructor
    }

    public void startPlaylist(Context context, Playlist playlist, String songId) {
        Log.i(TAG, String.format("Start Playlist (%s)[%s]", playlist.getInfo().getId(), songId));

        this.playlist.setValue(playlist);
        mQueueManager = new QueueManager(
            context,
            playlist.getSongs(),
            ListArrayUtils.startOrderFromId(playlist.getInfo().getOrder(), songId),
            isLooping,
            isShuffling,
            currentSong,
            queue,
            mPlayer
        );

        changeSong(songId);
    }

    public void changeSong(String songId) {
        Log.i(TAG, String.format("Changed Song to Song<%s>", songId));
        Song song = mQueueManager.getSongById(songId);

        if (song == null) {
            throw new RuntimeException(String.format("Song not found in playlist: %s", songId));
        }

        requestAudioFocus();
        mQueueManager.goToSong(song);
    }

    public void retry() {
        requestAudioFocus();
        mPlayer.prepare();
        mPlayer.play();
    }

    /**
     * Play the previous song in the queue or reset the song progress
     */
    public void playPreviousSong() {
        requestAudioFocus();
        error.postValue("");
        if (mPlayer.getContentPosition() <= 2000) {
            mQueueManager.backSong();
        } else {
            mPlayer.seekTo(0);
        }
    }

    /**
     * Play the next song in the queue
     */
    public void playNextSong() {
        requestAudioFocus();
        error.postValue("");
        mQueueManager.nextSong();
    }

    public void toggleShuffle() {
        mQueueManager.toggleShuffle();
    }

    public void toggleLoop() {
        mQueueManager.toggleLoop();
    }

    public void play() {
        requestAudioFocus();
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
    public void setPlayer(MainActivity activity, SimpleExoPlayer player) {
        if (initialised) return;
        initialised = true;
        mPlayer = player;
        mPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                PlayingViewModel.this.isPlaying.postValue(isPlaying);
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                PlayingViewModel.this.isBuffering.postValue(state == Player.STATE_BUFFERING);
                if (state == Player.STATE_ENDED) {
                    mQueueManager.nextSong();
                }
            }

            @Override
            public void onPlayerError(@NotNull ExoPlaybackException err) {
                if (Objects.equals(err.getMessage(), "Source error")) {
                    PlayingViewModel.this.error.postValue("Could not fetch song from server");
                } else {
                    PlayingViewModel.this.error.postValue(Objects.requireNonNull(err.getMessage()));
                }
            }
        });

        // Audio Focus
        am = activity.getSystemService(AudioManager.class);
        afr = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAcceptsDelayedFocusGain(true)
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener(this::onAudioFocusChange)
            .build();

        mQueueManager = new QueueManager(
            activity,
            new ArrayList<>(),
            new ArrayList<>(),
            isLooping,
            isShuffling,
            currentSong,
            queue,
            mPlayer
        );
    }

    public void onMoveSong(int oldPosition, int newPosition) {
        Log.i(TAG, String.format("Dragged song from %s to %s", oldPosition, newPosition));
        mQueueManager.moveSong(oldPosition, newPosition);
    }

    public void onRemoveSong(String songId) {
        Log.i(TAG, String.format("Swiped song %s", songId));
        mQueueManager.removeSong(songId);
    }

    public void addToQueue(Song song) {
        Log.i(TAG, String.format("Added song %s", song));
        mQueueManager.addSong(song);
    }

    private void requestAudioFocus() {
        if (am == null || afr == null) return;
        am.requestAudioFocus(afr);
    }

    private void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                play();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                am.abandonAudioFocusRequest(afr);
                pause();
                break;
        }
    }

}
