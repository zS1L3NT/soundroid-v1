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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.zectan.soundroid.anonymous.MarginProxy;
import com.zectan.soundroid.databinding.ActivityMainBinding;
import com.zectan.soundroid.viewmodels.MainViewModel;
import com.zectan.soundroid.viewmodels.PlayingViewModel;
import com.zectan.soundroid.viewmodels.SearchViewModel;

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
        SearchViewModel searchVM = new ViewModelProvider(this).get(SearchViewModel.class);

        // Live Observers
        mainVM.error.observe(this, this::handleError);
        searchVM.watchResults(this);

        NavHostFragment navHostFragment =
            (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(B.bottomNavigator, navController);
        mp = new MarginProxy(B.bottomNavigator);

        SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
        player.setShuffleModeEnabled(true);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
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

    public RelativeLayout getView() {
        return findViewById(R.id.parent);
    }
}