package com.zectan.soundroid.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.objects.Functions;
import com.zectan.soundroid.objects.MusicPlayer;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.Song;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Queue Number:
 * The location in the Position object that is playing
 * This object doesn't change when shuffling changes
 * Can Be:
 * 0
 * 9
 * <p>
 * Order:
 * The arrangement of the songs in a playlist depending on the shuffle state
 * This object changes as the shuffle state or current song changes
 * This object pushes the first item to the end
 * Can Be:
 * 6 7 8 9 0 1 2 3 4 5
 * 6 4 8 2 5 7 0 9 1 3
 */

// TODO If failed because of internet, try again when reconnected

public class PlayingViewModel extends ViewModel {
    private static final String TAG = "(SounDroid) PlayingViewModel";
    private final MusicPlayer mp = new MusicPlayer();
    public int queueNumber = 0, playNumber = 0;
    public boolean shufflingState = false, loopingState = true;
    public MutableLiveData<Playlist> queue = new MutableLiveData<>();
    public MutableLiveData<Boolean> jitteringState = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadingState = new MutableLiveData<>();
    public MutableLiveData<Boolean> playingState = new MutableLiveData<>();
    public MutableLiveData<Boolean> convertingState = new MutableLiveData<>();
    public MutableLiveData<Integer> playProgress = new MutableLiveData<>();
    public MutableLiveData<Integer> playTime = new MutableLiveData<>();
    public MutableLiveData<Integer> convertingProgress = new MutableLiveData<>();
    public MutableLiveData<Integer> songDuration = new MutableLiveData<>();
    public MutableLiveData<String> convertingError = new MutableLiveData<>();
    public MutableLiveData<List<Integer>> order = new MutableLiveData<>();
    private Timer playProgressTimer, playTimeTimer;

    public PlayingViewModel() {
        // Required empty public constructor
    }

    /**
     * Click event to play a new song to play
     *
     * @param playlist      New playlist for MusicPlayer to loop
     * @param startPosition Position in the playlist you're starting from
     */
    public void selectSong(Playlist playlist, int startPosition) {
        Log.i(TAG, String.format("onSongClicked('%s', %d)", playlist.getInfo().getId(), startPosition));

        Playlist queue = this.queue.getValue();
        if (queue == null
                || !queue.getInfo().getId().equals(playlist.getInfo().getId())
                || playlist.getInfo().getId().equals("")
        ) {
            // If the new clicked song is from a new playlist or has no id
            this.queue.setValue(playlist);
        }

        queueNumber = 0;
        if (shufflingState) {
            order.setValue(Functions.shuffleOrder(startPosition, playlist.size()));
        } else {
            order.setValue(Functions.createOrder(startPosition, playlist.size()));
        }
        recursivelyRunPlaylist();
    }

    /**
     * Method to recursively traverse a playlist
     */
    public void recursivelyRunPlaylist() {
        Log.i(TAG, "recursivelyRunPlaylist()");
        Playlist queue = this.queue.getValue();
        List<Integer> order = this.order.getValue();
        if (queue == null || order == null) return;

        Song song = queue.getSong(order.get(0));
        int playId = ++playNumber;

        mp.pause();
        initialisePlayer();

        // Get the gradient before calling the song processing
        song.getFileLocation(
                link -> {
                    if (playId == playNumber) {
                        mp.setNewData(link, () -> {
                            Log.d(TAG, "SONG_LOADED");

                            mp.setOnCompletionListener(__ -> playNextSong());
                            startPlaying();
                            loadingState.postValue(false);
                            songDuration.postValue(mp.getDuration() / 1000);
                        }, jitteringState::postValue);
                    } else {
                        Log.d(TAG, "SONG_DISCARDED");
                    }
                },
                convertingError::postValue,
                convertingState::postValue,
                convertingProgress::postValue,
                () -> playId == playNumber);
    }

    /**
     * Play the previous song in the queue or reset the song progress
     */
    public void playPreviousSong() {
        Playlist queue = this.queue.getValue();
        List<Integer> order = this.order.getValue();
        if (queue == null || order == null) return;

        if (--queueNumber <= -1) {
            if (loopingState)
                queueNumber += queue.size();
            else {
                if (queueNumber < -1) queueNumber++;
                return;
            }
        }

        int lastItemInOrder = order.get(order.size() - 1);
        this.order.setValue(Functions.changeOrder(order, lastItemInOrder));
        recursivelyRunPlaylist();
    }

    /**
     * Play the next song in the queue
     */
    public void playNextSong() {
        Playlist queue = this.queue.getValue();
        List<Integer> order = this.order.getValue();
        if (queue == null || order == null) return;

        Log.i(TAG, "queueNumber: " + (queueNumber + 1));

        if (++queueNumber >= queue.size()) {
            if (loopingState)
                queueNumber -= queue.size();
            else {
                if (queueNumber > queue.size()) queueNumber--;
                return;
            }
        }

        if (order.size() > 1) {
            int secondItemInOrder = order.get(1);
            this.order.setValue(Functions.changeOrder(order, secondItemInOrder));
        }
        recursivelyRunPlaylist();
    }

    /**
     * Starts the music player
     */
    public void startPlaying() {
        playProgressTimer = new Timer();
        playTimeTimer = new Timer();
        mp.fadeIn();

        playProgressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                playingState.postValue(mp.isPlaying());
                if (mp.isPlaying())
                    playProgress.postValue(mp.getTimePercent());
            }
        }, 0, mp.getDuration() / 1000);
        playTimeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mp.isPlaying())
                    playTime.postValue(mp.getPlayingTime());
            }
        }, 0, 250);
    }

    /**
     * Stops the music player, cancels the intervals for the progressbar and updates the listeners
     */
    public void stopPlaying() {
        playProgressTimer.cancel();
        playTimeTimer.cancel();
        mp.fadeOut(() -> playingState.postValue(false));
    }

    /**
     * Shuffle or sort queue, depending on shuffle state
     */
    public void shuffleOrSortOrder() {
        Playlist queue = this.queue.getValue();
        List<Integer> order = this.order.getValue();
        if (queue == null || order == null) return;

        int value = order.get(0);

        if (shufflingState) {
            this.order.setValue(Functions.shuffleOrder(value, queue.size()));
        } else {
            this.order.setValue(Functions.createOrder(value, queue.size()));
        }
    }

    /**
     * Method to reset the player's visuals to the initial state
     * <br/>
     * <code>
     * LoadingState => true
     * <br/>
     * PlayTime => 0
     * <br/>
     * PlayProgress => 0
     * <br/>
     * SongDuration => 0
     * </code>
     */
    public void initialisePlayer() {
        loadingState.postValue(true);
        playTime.postValue(0);
        songDuration.postValue(0);
        playProgress.postValue(0);
    }

    /**
     * Seeker for the seekbar to change the song timing
     * <br/>
     * <code>
     * LoadingState changed where necessary
     * </code>
     *
     * @param finalTouch Progress value (upon 1000) of the selected position
     */
    public void seekTo(int finalTouch) {
        Log.d(TAG, String.format("seekTo(%d)", finalTouch));

        loadingState.setValue(true);
        mp.seekTo(finalTouch, __ -> loadingState.setValue(false));
    }

    /**
     * Getter for the duration of the current playing song
     *
     * @return Integer
     */
    public int getDuration() {
        return mp.getDuration();
    }

    @Override
    protected void onCleared() {
        mp.pause();
        super.onCleared();
    }
}
