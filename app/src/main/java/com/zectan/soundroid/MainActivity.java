package com.zectan.soundroid;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.zectan.soundroid.anonymous.MarginProxy;
import com.zectan.soundroid.databinding.ActivityMainBinding;
import com.zectan.soundroid.viewmodels.MainViewModel;
import com.zectan.soundroid.viewmodels.PlayingViewModel;

// https://www.glyric.com/2018/merlin/aagaya-nilave

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "(SounDroid) MainActivity";
    private ActivityMainBinding B;
    private InputMethodManager imm;
    private FirebaseRepository repository;
    private MarginProxy mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        B = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(B.getRoot());
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        repository = new FirebaseRepository();

        // View Model
        MainViewModel mainVM = new ViewModelProvider(this).get(MainViewModel.class);
        PlayingViewModel playingVM = new ViewModelProvider(this).get(PlayingViewModel.class);

        // Live Observers
        mainVM.error.observe(this, this::handleError);

        NavHostFragment navHostFragment =
            (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(B.bottomNavigator, navController);
        mp = new MarginProxy(B.bottomNavigator);

        SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
        player.setShuffleModeEnabled(true);
        playingVM.setPlayer(player);
    }

    public FirebaseRepository getRepository() {
        return repository;
    }

    public void hideNavigator() {
        ValueAnimator va = ValueAnimator.ofInt(mp.getBottomMargin(), -B.bottomNavigator.getHeight()).setDuration(250);
        va.addUpdateListener(animation -> mp.setBottomMargin((int) animation.getAnimatedValue()));
        va.start();
    }

    public void showNavigator() {
        ValueAnimator va = ValueAnimator.ofInt(mp.getBottomMargin(), 0).setDuration(250);
        va.addUpdateListener(animation -> mp.setBottomMargin((int) animation.getAnimatedValue()));
        va.start();
    }

    public void showKeyboard() {
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void hideKeyboard(View currentFocus) {
        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }

    public void handleError(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        Log.e(TAG, e.getMessage());
    }

    public int getAttributeResource(int id) {
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(id, value, true);
        return value.data;
    }

    public MotionLayout.TransitionListener getTransitionListener() {
        return new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
                if (i == motionLayout.getStartState()) {
                    mp.setBottomMargin(0);
                }
            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {
                mp.setBottomMargin((int) (v * -B.bottomNavigator.getHeight()));
            }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                if (i == motionLayout.getEndState()) {
                    mp.setBottomMargin(-B.bottomNavigator.getHeight());
                } else {
                    mp.setBottomMargin(0);
                }
            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {
            }
        };
    }

    public RelativeLayout getView() {
        return findViewById(R.id.parent);
    }
}