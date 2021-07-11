package com.zectan.soundroid;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.zectan.soundroid.databinding.ActivityMainBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.MenuItemEvents;
import com.zectan.soundroid.viewmodels.MainViewModel;
import com.zectan.soundroid.viewmodels.PlayingViewModel;
import com.zectan.soundroid.viewmodels.PlaylistEditViewModel;
import com.zectan.soundroid.viewmodels.PlaylistViewViewModel;
import com.zectan.soundroid.viewmodels.SearchViewModel;

// https://www.glyric.com/2018/merlin/aagaya-nilave

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "(SounDroid) MainActivity";
    public static final String DOWNLOAD_CHANNEL_ID = "Downloads";
    public ActivityMainBinding B;
    public NavController navController;
    public NotificationManager notificationManager;
    public MainViewModel mainVM;
    public PlayingViewModel playingVM;
    public PlaylistViewViewModel playlistViewVM;
    public PlaylistEditViewModel playlistEditVM;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        B = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(B.getRoot());
        imm = getSystemService(InputMethodManager.class);
        notificationManager = getSystemService(NotificationManager.class);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // View Model
        mainVM = new ViewModelProvider(this).get(MainViewModel.class);
        playingVM = new ViewModelProvider(this).get(PlayingViewModel.class);
        SearchViewModel searchVM = new ViewModelProvider(this).get(SearchViewModel.class);
        playlistViewVM = new ViewModelProvider(this).get(PlaylistViewViewModel.class);
        playlistEditVM = new ViewModelProvider(this).get(PlaylistEditViewModel.class);

        // Live Observers
        mainVM.error.observe(this, this::handleError);
        searchVM.watch(this);

        // Navigation
        NavHostFragment navHostFragment =
            (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(B.bottomNavigator, navController);

        // Music Player
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
        playingVM.setPlayer(this, player);

        // Playing Screen background
        int[] colors = {getColor(R.color.default_cover_color), getAttributeResource(R.attr.colorSecondary)};
        GradientDrawable newGD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        playingVM.background.setValue(newGD);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mainVM.watch(this);
    }

    public void showKeyboard() {
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void hideKeyboard(View currentFocus) {
        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }

    public void handleError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
        snack(message);
        e.printStackTrace();
    }

    public int getAttributeResource(int id) {
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(id, value, true);
        return value.data;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean handleMenuItemClick(Info info, Song song, MenuItem item) {
        return new MenuItemEvents(this, info, song, item).handle();
    }

    @SuppressLint("NonConstantResourceId")
    public boolean handleMenuItemClick(Info info, Song song, MenuItem item, Runnable openEditPlaylist) {
        return new MenuItemEvents(this, info, song, item, openEditPlaylist).handle();
    }

    public void snack(String message) {
        Snackbar
            .make(B.navHostFragment, message, Snackbar.LENGTH_SHORT)
            .setAction(R.string.done, __ -> {
            })
            .show();
    }

}