package com.zectan.soundroid.objects;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayer {
    private static final String TAG = "(SounDroid) MusicPlayer";
    final int FADE_DURATION = 250;
    final int FADE_INTERVAL = 25;
    final int MAX_VOLUME = 1;
    final int FADE_STEPS = FADE_DURATION / FADE_INTERVAL;
    final float DELTA_VOLUME = MAX_VOLUME / (float) FADE_STEPS;
    private final MediaPlayer player;
    private Timer fadeInTimer = null, fadeOutTimer = null;
    /**
     * Fade volume config
     */
    private float volume = 1;

    public MusicPlayer() {
        player = new MediaPlayer();
    }

    /**
     * Set's a new file link and prepares the song to be played
     * DOESN'T PLAY THE SONG UNTIL .start() method is called
     *
     * @param source   URL of the song wanting to be played
     * @param callback Custom callback after the song is prepared
     */
    public void setNewData(
            String source,
            Runnable callback,
            JitteringCallback jittering
    ) {
        player.reset();

        player.setOnInfoListener((mp, what, extra) -> {
            switch (what) {
                case 701:
                case 703:
                    jittering.set(true);
                    break;
                case 702:
                    jittering.set(false);
                    break;
                default:
                    break;
            }
            return false;
        });

        try {
            player.setDataSource(source);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Illegal State Exception caught!");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "MEDIA_PLAYER_PREPARE");
        try {
            player.prepareAsync();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Illegal State Exception caught!");
            return;
        }

        player.setOnPreparedListener(__ -> callback.run());
    }

    /**
     * Getter for the playing state of the music player
     *
     * @return Boolean
     */
    public boolean isPlaying() {
        return player.isPlaying();
    }

    /**
     * Clear the music player
     */
    public void reset() {
        player.reset();
    }

    /**
     * Pauses the music player
     */
    public void pause() {
        if (player.isPlaying())
            player.pause();
    }

    /**
     * Getter for the duration of the song in ms
     *
     * @return Integer
     */
    public int getDuration() {
        return player.getDuration();
    }

    /**
     * Getter for the progress value (upon 1000) of the current song playing
     *
     * @return Integer
     */
    public int getTimePercent() {
        double c = player.getCurrentPosition();
        double t = getDuration();
        return (int) ((c / t) * 1000);
    }

    /**
     * Seeker for the seekbar to change the song timing
     *
     * @param finalTouch Progress value (upon 1000) of the selected position
     */
    public void seekTo(double finalTouch, MediaPlayer.OnSeekCompleteListener l) {
        int position = (int) finalTouch * getDuration() / 1000;
        Log.d(TAG, "SEEK_TO: " + position / 1000 + "s");
        player.seekTo(position, MediaPlayer.SEEK_CLOSEST);
        player.setOnSeekCompleteListener(l);
    }

    /**
     * Stop fade out timer if exists
     */
    private void stopOutTimerIfExists() {
        if (fadeOutTimer != null) {
            fadeOutTimer.cancel();
            fadeOutTimer.purge();
            fadeOutTimer = null;
        }
    }

    /**
     * Stop fade in timer is exists
     */
    private void stopInTimerIfExists() {
        if (fadeInTimer != null) {
            fadeInTimer.cancel();
            fadeInTimer.purge();
            fadeInTimer = null;
        }
    }

    /**
     * Fade song in
     */
    public void fadeIn() {
        stopInTimerIfExists();
        stopOutTimerIfExists();

        player.start();
        fadeInTimer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                stopOutTimerIfExists();

                if (volume >= 1f) {
                    stopInTimerIfExists();
                } else {
                    volume += DELTA_VOLUME;
                    player.setVolume(volume, volume);
                }
            }
        };

        fadeInTimer.schedule(timerTask, FADE_INTERVAL, FADE_INTERVAL);
    }

    /**
     * Fade song out
     */
    public void fadeOut(Runnable callback) {
        stopInTimerIfExists();
        stopOutTimerIfExists();

        fadeOutTimer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                stopInTimerIfExists();

                if (volume <= 0f) {
                    callback.run();
                    stopOutTimerIfExists();
                    player.pause();
                } else {
                    volume -= DELTA_VOLUME;
                    player.setVolume(volume, volume);
                }
            }
        };

        fadeOutTimer.schedule(timerTask, FADE_INTERVAL, FADE_INTERVAL);
    }

    /**
     * Getter for the current playing time in seconds
     *
     * @return Integer
     */
    public int getPlayingTime() {
        return player.getCurrentPosition() / 1000;
    }

    /**
     * On song complete listener
     *
     * @param l Listener for when the song finishes
     */
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        player.setOnCompletionListener(l);
    }

    public interface JitteringCallback {
        void set(boolean jittering);
    }
}
