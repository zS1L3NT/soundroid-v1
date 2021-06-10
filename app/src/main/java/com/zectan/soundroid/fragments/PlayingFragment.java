package com.zectan.soundroid.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView;
import com.zectan.soundroid.AnimatedFragment;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.QueueAdapter;
import com.zectan.soundroid.databinding.FragmentPlayingBinding;
import com.zectan.soundroid.objects.Animations;
import com.zectan.soundroid.objects.Functions;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.viewmodels.PlayingViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("UseCompatLoadingForDrawables")
public class PlayingFragment extends AnimatedFragment {
    private static final String TAG = "(SounDroid) PlayingFragment";
    private MainActivity activity;
    private FragmentPlayingBinding B;

    private PlayingViewModel playingVM;

    private final QueueAdapter.Callback callback = new QueueAdapter.Callback() {
        @Override
        public void onSongSelected(Song song) {
            Playlist queue = playingVM.queue.getValue();
            if (queue == null)
                return;

            playingVM.selectSong(queue, queue.getSongs().indexOf(song));
        }

        @Override
        public void onReorder(List<Song> songs) {
            playingVM.reorderQueue(songs);
        }
    };
    private QueueAdapter queueAdapter;
    private boolean touchingSeekbar = false;
    private int finalTouch = 0;

    public PlayingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String transitionName = PlayingFragmentArgs.fromBundle(getArguments()).getTransitionName();
        ViewCompat.setTransitionName(view.findViewById(R.id.cover_image), transitionName);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlayingBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        assert activity != null;

        // View Models
        playingVM = new ViewModelProvider(activity).get(PlayingViewModel.class);

        // Recycler Views
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        queueAdapter = new QueueAdapter(callback);
        B.recyclerView.setAdapter(queueAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setOrientation(DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING);
        B.recyclerView.setReduceItemAlphaOnSwiping(true);

        // Live Observers
        playingVM.sequence.observe(activity, this::onOrderChange);
        playingVM.songDuration.observe(activity, this::onSongDurationChange);
        playingVM.jitteringState.observe(activity, this::onJitteringStateChange);
        playingVM.loadingState.observe(activity, this::onLoadingStateChange);
        playingVM.playingState.observe(activity, this::onPlayingStateChange);
        playingVM.convertingState.observe(activity, this::onConvertingStateChange);
        playingVM.playProgress.observe(activity, this::onPlayProgressChange);
        playingVM.playTime.observe(activity, this::onPlayTimeChange);
        playingVM.convertingError.observe(activity, this::onConvertingErrorChange);
        playingVM.convertingProgress.observe(activity, this::onConvertingProgressChange);
        B.parent.addTransitionListener(activity.getTransitionListener());

        enableControls();
        updateShuffleColor();
        updateLoopColor();
        activity.showNavigator();
        B.shuffleImage.setOnClickListener(this::shufflePlaylist);
        B.shuffleImage.setOnTouchListener(Animations::mediumSqueeze);
        B.backImage.setOnClickListener(this::backSong);
        B.backImage.setOnTouchListener(Animations::mediumSqueeze);
        B.nextImage.setOnClickListener(this::nextSong);
        B.nextImage.setOnTouchListener(Animations::mediumSqueeze);
        B.loopImage.setOnClickListener(this::loopPlaylist);
        B.loopImage.setOnTouchListener(Animations::mediumSqueeze);

        return B.getRoot();
    }

    public void backSong(View v) {
        Integer duration = playingVM.playTime.getValue();
        System.out.println(duration);
        if (duration == null)
            return;

        if (duration <= 2) {
            playingVM.playPreviousSong();
        } else {
            playingVM.seekTo(0);
            playingVM.initialisePlayer();
        }
    }

    public void nextSong(View v) {
        playingVM.playNextSong();
    }

    public void shufflePlaylist(View v) {
        playingVM.shufflingState = !playingVM.shufflingState;
        playingVM.shuffleOrSortOrder();
        updateShuffleColor();
    }

    public void loopPlaylist(View v) {
        playingVM.loopingState = !playingVM.loopingState;
        updateLoopColor();
    }

    //
    public void playPauseSong(View v) {
        Boolean playingState = playingVM.playingState.getValue();
        Boolean loadingState = playingVM.loadingState.getValue();
        if (loadingState != null && loadingState)
            return;
        if (playingState != null && playingState) {
            playingVM.stopPlaying();
        } else {
            try {
                playingVM.startPlaying();
            } catch (Exception ignored) {

            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableControls() {
        B.playPauseImage.setOnClickListener(this::playPauseSong);
        B.playPauseImage.setOnTouchListener(Animations::smallSqueeze);
        B.playPauseMiniImage.setOnClickListener(this::playPauseSong);
        B.playPauseMiniImage.setOnTouchListener(Animations::mediumSqueeze);
        B.playingSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    finalTouch = progress;

                    double percent = (double) progress / 1000;
                    double durationS = (double) playingVM.getDuration() / 1000;
                    int selectedTime = (int) (percent * durationS);
                    B.timeText.setText(Functions.formatDate(selectedTime));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                touchingSeekbar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playingVM.seekTo(finalTouch);
                touchingSeekbar = false;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void disableControls() {
        View.OnClickListener click = c -> {
        };
        View.OnTouchListener touch = (v, event) -> false;
        B.playPauseImage.setOnClickListener(click);
        B.playPauseImage.setOnTouchListener(touch);
        B.playPauseMiniImage.setOnClickListener(click);
        B.playPauseMiniImage.setOnTouchListener(touch);
        B.playingSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void updateShuffleColor() {
        B.shuffleImage.setColorFilter(
            ContextCompat.getColor(activity,
                playingVM.shufflingState ? R.color.theme_button_on : R.color.theme_button_off),
            android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void updateLoopColor() {
        B.loopImage.setColorFilter(
            ContextCompat.getColor(activity,
                playingVM.loopingState ? R.color.theme_button_on : R.color.theme_button_off),
            android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void onOrderChange(List<Integer> order) {
        Playlist queue = playingVM.queue.getValue();
        String colorHex;

        if (queue == null) {
            queueAdapter.updateQueue(new ArrayList<>());
            B.playlistNameText.setText("-");
            B.coverImage.setImageDrawable(activity.getDrawable(R.drawable.playing_cover_default));
            B.titleText.setText("-");
            B.descriptionText.setText("-");
            colorHex = "#7b828b";
        } else {
            queueAdapter.updateQueue(Functions.formQueue(queue.getSongs(), order));

            Song song = queue.getSong(order.get(0));
            String title = song.getTitle();
            String artiste = song.getArtiste();
            String cover = song.getCover();
            colorHex = song.getColorHex();

            B.playlistNameText.setText(queue.getInfo().getName());
            B.titleText.setText(title);
            B.descriptionText.setText(artiste);
            Glide
                .with(activity)
                .load(cover)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.playing_cover_default)
                .centerCrop()
                .into(B.coverImage);
        }

        Drawable oldGD = B.parent.getBackground();
        int[] colors = {Color.parseColor(colorHex), activity.getColor(R.color.theme_playing_bottom)};
        GradientDrawable newGD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);

        Drawable[] layers = {oldGD, newGD};
        TransitionDrawable transition = new TransitionDrawable(layers);
        B.parent.setBackground(transition);
        transition.startTransition(500);
    }

    private void onSongDurationChange(int duration) {
        B.lengthText.setText(Functions.formatDate(duration));
    }

    private void onJitteringStateChange(boolean jittering) {
        B.loadingCircle.setAlpha(jittering ? 1 : 0);
        B.parent.requestLayout();
    }

    private void onLoadingStateChange(boolean loading) {
        B.loadingCircle.setAlpha(loading ? 1 : 0);
        B.parent.requestLayout();
    }

    private void onPlayingStateChange(boolean playing) {
        B.playPauseImage.setImageDrawable(
            activity.getDrawable(playing ? R.drawable.controls_pause_filled : R.drawable.controls_play_filled));
        B.playPauseMiniImage
            .setImageDrawable(activity.getDrawable(playing ? R.drawable.controls_pause : R.drawable.controls_play));
    }

    private void onConvertingStateChange(boolean converting) {
        float alpha = B.convertingProgressIndicator.getAlpha();
        ValueAnimator alphaAnimation = ValueAnimator.ofFloat(alpha, converting ? 1 : 0).setDuration(500);

        if (!converting) B.playingSeekbar.setVisibility(View.VISIBLE);
        alphaAnimation.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            B.convertingProgressIndicator.setAlpha(val);
            B.playingSeekbar.setAlpha(1f - val);
        });
        alphaAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (converting) B.playingSeekbar.setVisibility(View.INVISIBLE);
            }
        });
        alphaAnimation.start();
    }

    private void onPlayProgressChange(int time) {
        if (!touchingSeekbar) {
            B.playingSeekbar.setProgress(time);
        }
    }

    private void onPlayTimeChange(int time) {
        if (!touchingSeekbar) {
            B.timeText.setText(Functions.formatDate(time));
        }
    }

    private void onConvertingErrorChange(String message) {
        if (!message.isEmpty()) {
            B.errorText.setText(message);
            new Handler().postDelayed(() -> B.coverImage.setOnClickListener(__ -> {
                playingVM.convertingError.setValue("");
                playingVM.recursivelyRunPlaylist();
            }), 1000);

            ValueAnimator darkenAnimation = ValueAnimator
                .ofArgb(activity.getColor(R.color.white), activity.getColor(R.color.theme_3)).setDuration(1000);
            darkenAnimation.addUpdateListener(animation -> B.coverImage.setColorFilter((int) animation.getAnimatedValue(),
                PorterDuff.Mode.MULTIPLY));
            darkenAnimation.start();

            ValueAnimator messageAnimation = ValueAnimator.ofFloat(0f, 1f).setDuration(500);
            messageAnimation.addUpdateListener(animation -> {
                float alpha = (float) animation.getAnimatedValue();
                B.errorText.setAlpha(alpha);
                B.retryText.setAlpha(alpha);
            });
            messageAnimation.setStartDelay(500);
            messageAnimation.start();

            disableControls();
            playingVM.loadingState.setValue(false);
        } else {
            B.coverImage.setOnClickListener(__ -> {
            });
            B.coverImage.clearColorFilter();
            B.errorText.setAlpha(0f);
            B.retryText.setAlpha(0f);

            enableControls();
        }
    }

    private void onConvertingProgressChange(int progress) {
        B.convertingProgressIndicator.setProgressCompat(progress, true);
    }
}
