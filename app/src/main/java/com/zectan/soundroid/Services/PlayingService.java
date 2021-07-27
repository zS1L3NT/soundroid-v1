package com.zectan.soundroid.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.zectan.soundroid.Classes.StrictLiveData;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.ListArrayUtils;
import com.zectan.soundroid.Utils.QueueManager;
import com.zectan.soundroid.Utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlayingService extends Service {
    private static final String TAG = "(SounDroid) PlayingService";
    private static final String BACK = "ACTION_BACK";
    private static final String PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    private static final String NEXT = "ACTION_NEXT";
    private final IBinder mBinder = new PlayingService.PlayingBinder();

    public final StrictLiveData<Playlist> playlist = new StrictLiveData<>(Playlist.getEmpty());
    public final StrictLiveData<Song> currentSong = new StrictLiveData<>(Song.getEmpty());
    public final StrictLiveData<Integer> time = new StrictLiveData<>(0);
    public final StrictLiveData<Integer> buffered = new StrictLiveData<>(0);
    public final StrictLiveData<Integer> progress = new StrictLiveData<>(0);
    public final StrictLiveData<Integer> duration = new StrictLiveData<>(0);
    public final StrictLiveData<Boolean> isBuffering = new StrictLiveData<>(false);
    public final StrictLiveData<Boolean> isPlaying = new StrictLiveData<>(false);
    public final StrictLiveData<Boolean> isShuffling = new StrictLiveData<>(false);
    public final StrictLiveData<Boolean> isLooping = new StrictLiveData<>(true);
    public final StrictLiveData<String> error = new StrictLiveData<>("");
    public final StrictLiveData<Boolean> touchingSeekbar = new StrictLiveData<>(false);
    public final StrictLiveData<List<Song>> queue = new StrictLiveData<>(new ArrayList<>());
    public final MutableLiveData<GradientDrawable> background = new MutableLiveData<>();

    public PlayingService.TimeHandler timeHandler;
    public PlayingService.ProgressHandler progressHandler;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private QueueManager mQueueManager;
    private SimpleExoPlayer mPlayer;
    private AudioManager am;
    private AudioFocusRequest afr;
    private Context mContext;
    private Bitmap mDefaultCover;

    private int mNotificationID;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case BACK:
                    playPreviousSong();
                    break;
                case PLAY_PAUSE:
                    if (isPlaying.getValue()) pause();
                    else play();
                    break;
                case NEXT:
                    playNextSong();
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mNotificationManager = getSystemService(NotificationManager.class);
        mNotificationID = Utils.getRandomInt();

        mDefaultCover = BitmapFactory.decodeResource(
            mContext.getResources(),
            R.drawable.playing_cover_failed
        );

        MediaSessionCompat mediaSession = new MediaSessionCompat(mContext, TAG);
        Intent intentOpen = new Intent(this, MainActivity.class).setAction(MainActivity.FRAGMENT_PLAYING);
        Intent intentBack = new Intent(this, PlayingService.class).setAction(BACK);
        Intent intentPlayPause = new Intent(this, PlayingService.class).setAction(PLAY_PAUSE);
        Intent intentNext = new Intent(this, PlayingService.class).setAction(NEXT);
        PendingIntent pendingIntentOpen = PendingIntent.getActivity(mContext, 0, intentOpen, 0);
        PendingIntent pendingIntentBack = PendingIntent.getService(mContext, 0, intentBack, 0);
        PendingIntent pendingIntentPlayPause = PendingIntent.getService(mContext, 0, intentPlayPause, 0);
        PendingIntent pendingIntentNext = PendingIntent.getService(mContext, 0, intentNext, 0);

        mNotificationBuilder = new NotificationCompat.Builder(mContext, MainActivity.PLAYING_CHANNEL_ID)
            .setContentTitle("-")
            .setContentText("-")
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(mDefaultCover)
            .setPriority(Notification.PRIORITY_LOW)
            .setContentIntent(pendingIntentOpen)
            .addAction(R.drawable.controls_back, "Back", pendingIntentBack)
            .addAction(R.drawable.controls_play, "Play", pendingIntentPlayPause)
            .addAction(R.drawable.controls_next, "Back", pendingIntentNext)
            .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mediaSession.getSessionToken()));
        startForeground(mNotificationID, mNotificationBuilder.build());

        mPlayer = new SimpleExoPlayer.Builder(mContext).build();
        mPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean state) {
                mNotificationBuilder.clearActions();
                mNotificationBuilder
                    .addAction(R.drawable.controls_back, "Back", pendingIntentBack)
                    .addAction(state
                            ? R.drawable.controls_pause
                            : R.drawable.controls_play,
                        state
                            ? "Pause"
                            : "Play",
                        pendingIntentPlayPause)
                    .addAction(R.drawable.controls_next, "Back", pendingIntentNext);
                mNotificationManager.notify(mNotificationID, mNotificationBuilder.build());
                isPlaying.postValue(state);
            }

            @Override
            public void onMediaItemTransition(@org.jetbrains.annotations.Nullable MediaItem mediaItem, int reason) {
                PlayingService.this.onMediaItemTransition(mediaItem);
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                isBuffering.postValue(state == Player.STATE_BUFFERING);
                if (state == Player.STATE_ENDED) {
                    mQueueManager.nextSong();
                }

                if (state == Player.STATE_READY) {
                    duration.setValue((int) mPlayer.getDuration() / 1000);
                }
            }

            @Override
            public void onPlayerError(@NotNull ExoPlaybackException err) {
                List<String> NotConvertedYetErrors = new ArrayList<>();
                NotConvertedYetErrors.add("java.io.EOFException");
                NotConvertedYetErrors.add("com.google.android.exoplayer2.ParserException");
                NotConvertedYetErrors.add("com.google.android.exoplayer2.source.UnrecognizedInputFormatException");

                Throwable cause = err.getCause();
                if (cause != null && NotConvertedYetErrors.contains(cause.getClass().getName())) {
                    retry();
                } else {
                    if (Objects.equals(err.getMessage(), "Source error")) {
                        error.postValue("Could not fetch song from server");
                    } else {
                        error.postValue(Objects.requireNonNull(err.getMessage()));
                    }
                }
            }
        });

        am = getSystemService(AudioManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            afr = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this::onAudioFocusChange)
                .build();
        }

        mQueueManager = new QueueManager(
            this,
            mPlayer,
            new ArrayList<>(),
            new ArrayList<>(),
            true
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(mNotificationID);
        mNotificationManager.cancel(mNotificationID);
    }

    public void startPlaylist(Playlist playlist, String songId, boolean highQuality) {
        Log.i(TAG, String.format("Start Playlist (%s)[%s]", playlist.getInfo().getId(), songId));

        this.playlist.setValue(playlist);
        mQueueManager = new QueueManager(
            this,
            mPlayer,
            playlist.getSongs(),
            ListArrayUtils.startOrderFromId(playlist.getInfo().getOrder(), songId),
            highQuality
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
        error.postValue("");
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

    public void stop() {
        mPlayer.stop();
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

    public void clearQueue() {
        currentSong.setValue(Song.getEmpty());
        queue.setValue(new ArrayList<>());
        time.setValue(0);
        duration.setValue(0);
        progress.setValue(0);
        mQueueManager = new QueueManager(
            this,
            mPlayer,
            new ArrayList<>(),
            new ArrayList<>(),
            true
        );
        mPlayer.clearMediaItems();
    }

    public void seekTo(int progress) {
        mPlayer.seekTo(duration.getValue() * progress);
    }

    private void requestAudioFocus() {
        if (am == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            am.requestAudioFocus(afr);
        } else {
            am.requestAudioFocus(
                this::onAudioFocusChange,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            );
        }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    am.abandonAudioFocusRequest(afr);
                } else {
                    am.abandonAudioFocus(this::onAudioFocusChange);
                }
                stop();
                break;
        }
    }

    /**
     * We put this here because I don't want to crazy clutter the onCreate method
     *
     * @param mediaItem Media Item
     */
    private void onMediaItemTransition(@org.jetbrains.annotations.Nullable MediaItem mediaItem) {
        if (timeHandler != null) timeHandler.cancel();
        if (progressHandler != null) progressHandler.cancel();

        time.setValue(0);
        progress.setValue(0);

        if (mediaItem != null) {
            List<Song> songs = playlist
                .getValue()
                .getSongs()
                .stream()
                .filter(s -> s.getSongId().equals(mediaItem.mediaId))
                .collect(Collectors.toList());

            if (songs.size() == 1) {
                Song song = songs.get(0);
                mNotificationBuilder
                    .setContentTitle(song.getTitle())
                    .setContentText(song.getArtiste())
                    .setLargeIcon(mDefaultCover);

                Glide
                    .with(this)
                    .asBitmap()
                    .load(song.getCover())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull @NotNull Bitmap resource, @Nullable @org.jetbrains.annotations.Nullable Transition<? super Bitmap> transition) {
                            mNotificationBuilder.setLargeIcon(resource);
                            mNotificationManager.notify(mNotificationID, mNotificationBuilder.build());
                        }

                        @Override
                        public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {
                            mNotificationBuilder.setLargeIcon(mDefaultCover);
                            mNotificationManager.notify(mNotificationID, mNotificationBuilder.build());
                        }
                    });

                if (!song.equals(Song.getEmpty())) {
                    timeHandler = new TimeHandler(getMainLooper());
                    progressHandler = new ProgressHandler(getMainLooper());

                    timeHandler.start();
                    progressHandler.start();
                }
            } else {
                mNotificationBuilder
                    .setContentTitle("-")
                    .setContentText("-")
                    .setLargeIcon(mDefaultCover);
            }
        } else {
            mNotificationBuilder
                .setContentTitle("-")
                .setContentText("-")
                .setLargeIcon(mDefaultCover);
        }
        mNotificationManager.notify(mNotificationID, mNotificationBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private class TimeHandler extends Handler {
        private boolean mCancelled = false;

        public TimeHandler(@NonNull @NotNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (!mCancelled) {
                if (!touchingSeekbar.getValue())
                    time.setValue((int) (mPlayer.getContentPosition() / 1000));
                sendEmptyMessageDelayed(0, 250);
            }
        }

        public void start() {
            sendEmptyMessage(0);
        }

        public void cancel() {
            mCancelled = true;
        }
    }

    private class ProgressHandler extends Handler {
        private boolean mCancelled = false;

        public ProgressHandler(@NonNull @NotNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (!mCancelled) {
                buffered.setValue((int) ((mPlayer.getBufferedPosition() * 1000) / mPlayer.getDuration()));
                if (!touchingSeekbar.getValue())
                    progress.setValue((int) ((mPlayer.getCurrentPosition() * 1000) / mPlayer.getDuration()));
                sendEmptyMessageDelayed(0, mPlayer.getDuration() / 1000);
            }
        }

        public void start() {
            sendEmptyMessage(0);
        }

        public void cancel() {
            mCancelled = true;
        }
    }

    public class PlayingBinder extends Binder {
        public PlayingService getService() {
            return PlayingService.this;
        }
    }

}
