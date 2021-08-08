package com.zectan.soundroid;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.Activities.AuthActivity;
import com.zectan.soundroid.Classes.CrashDebugApplication;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.Services.DownloadService;
import com.zectan.soundroid.Services.PlayingService;
import com.zectan.soundroid.Utils.MenuEvents;
import com.zectan.soundroid.ViewModels.MainViewModel;
import com.zectan.soundroid.ViewModels.PlaylistEditViewModel;
import com.zectan.soundroid.ViewModels.PlaylistViewViewModel;
import com.zectan.soundroid.ViewModels.PlaylistsViewModel;
import com.zectan.soundroid.ViewModels.SongEditViewModel;
import com.zectan.soundroid.databinding.ActivityMainBinding;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://www.glyric.com/2018/merlin/aagaya-nilave

public class MainActivity extends CrashDebugApplication {
    private static final String TAG = "(SounDroid) MainActivity";
    public static final String DOWNLOAD_CHANNEL_ID = "Downloads";
    public static final String PLAYING_CHANNEL_ID = "Playing";
    public static final String FRAGMENT_PLAYING = "FRAGMENT_PLAYING";
    public static final String FRAGMENT_PLAYLISTS = "FRAGMENT_PLAYLISTS";
    public final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    public ActivityMainBinding B;
    public NavController mNavController;
    public NotificationManager mNotificationManager;
    public MainViewModel mMainVM;
    public PlaylistsViewModel mPlaylistsVM;
    public PlaylistViewViewModel mPlaylistViewVM;
    public PlaylistEditViewModel mPlaylistEditVM;
    public SongEditViewModel mSongEditVM;
    private InputMethodManager mInputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        B = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(B.getRoot());
        mInputMethodManager = getSystemService(InputMethodManager.class);
        mNotificationManager = getSystemService(NotificationManager.class);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Create View Models
        mMainVM = new ViewModelProvider(this).get(MainViewModel.class);
        mPlaylistsVM = new ViewModelProvider(this).get(PlaylistsViewModel.class);
        mPlaylistViewVM = new ViewModelProvider(this).get(PlaylistViewViewModel.class);
        mPlaylistEditVM = new ViewModelProvider(this).get(PlaylistEditViewModel.class);
        mSongEditVM = new ViewModelProvider(this).get(SongEditViewModel.class);

        // Activate Services
        getDownloadService(service -> {
        });
        getPlayingService(service -> {
        });

        // Setup Navigation
        NavHostFragment navHostFragment =
            (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        mNavController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(B.bottomNavigator, mNavController);

        // Create Notification Channels
        NotificationChannel downloadChannel = new NotificationChannel(
            MainActivity.DOWNLOAD_CHANNEL_ID,
            MainActivity.DOWNLOAD_CHANNEL_ID,
            NotificationManager.IMPORTANCE_DEFAULT
        );
        downloadChannel.setDescription("Download songs for offline listening");
        mNotificationManager.createNotificationChannel(downloadChannel);
        NotificationChannel playingChannel = new NotificationChannel(
            MainActivity.PLAYING_CHANNEL_ID,
            MainActivity.PLAYING_CHANNEL_ID,
            NotificationManager.IMPORTANCE_DEFAULT
        );
        playingChannel.setDescription("Current playing song notification");
        mNotificationManager.createNotificationChannel(playingChannel);

        // Set User ID in ViewModel
        mMainVM.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set playing screen background
        int[] colors = {getColor(R.color.default_cover_color), getAttributeResource(R.attr.colorSecondary)};
        GradientDrawable newGD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        getPlayingService(service -> service.background.setValue(newGD));

        // Redirect if the intent is from a notification
        getPlayingService(service -> {
            Intent intent = getIntent();
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case FRAGMENT_PLAYING:
                        mNavController.navigate(R.id.fragment_playing_controls);
                        break;
                    case FRAGMENT_PLAYLISTS:
                        mNavController.navigate(R.id.fragment_playlists);
                        break;
                }
            }
        });
    }

    /**
     * Watch the firebase values again because Firebase
     * watching stops when activity stops
     */
    @Override
    protected void onStart() {
        super.onStart();
        mMainVM.watch(this);
    }

    /**
     * Display the keyboard
     */
    public void showKeyboard() {
        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * Hide the keyboard
     *
     * @param currentFocus View to hide the keyboard from...?
     */
    public void hideKeyboard(View currentFocus) {
        mInputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }

    /**
     * Update the visibility of the navigator
     * Hides when visibility is 0 so that it can allow click events below it
     *
     * @param alpha Alpha
     */
    public void updateNavigator(float alpha) {
        B.bottomNavigator.setVisibility(alpha == 0 ? View.GONE : View.VISIBLE);
        B.bottomNavigator.setAlpha(alpha);
    }

    public void snack(String message) {
        Snackbar
            .make(B.navHostFragment, message, Snackbar.LENGTH_SHORT)
            .setAction(R.string.done, __ -> {
            })
            .show();
    }

    /**
     * Warn admin of the error
     * Display the error first then upload the error to firebase
     *
     * @param e Exception
     */
    public void warnError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
        snack(message);
        e.printStackTrace();

        Map<String, Object> error = new HashMap<>();
        List<String> stack = new ArrayList<>();
        for (StackTraceElement el : e.getStackTrace()) stack.add(el.toString());
        error.put("stack", stack);
        error.put("type", "Safe");
        error.put("date", Calendar.getInstance().getTime().toString());
        error.put("message", e.getMessage());
        error.put("class", e.getClass().getName());
        error.put("userId", mMainVM.userId);

        mDb.collection("errors")
            .add(error)
            .addOnSuccessListener(__ -> Log.i(TAG, "Error stored successfully"))
            .addOnFailureListener(e_ -> Log.e(TAG, "Error stored unsuccessfully: " + e_.getMessage()));
    }

    /**
     * Shortcut to get the resource value from R.attr.?
     *
     * @param id ID of the attr
     * @return Value of the attr
     */
    public int getAttributeResource(int id) {
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(id, value, true);
        return value.data;
    }

    /**
     * Method to handle click events from menus
     *
     * @param playlist Playlist related to click event
     * @param song     Song related to click event
     * @param item     Menu item clicked
     * @param runnable Runnable related to click event
     * @return true because the event was handled
     */
    @SuppressLint("NonConstantResourceId")
    public boolean handleMenuItemClick(Playlist playlist, Song song, MenuItem item, Runnable runnable) {
        return new MenuEvents(this, playlist, song, item, runnable).handle();
    }

    /**
     * Update the theme with the theme from Firestore
     *
     * @param theme Theme
     */
    public void updateTheme(@Nullable String theme) {
        if (theme != null) {
            switch (theme) {
                case "Light":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "Dark":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case "System":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
        }
    }

    /**
     * Assert that download service is alive then get the service
     *
     * @param callback Callback
     */
    public void getDownloadService(DownloadServiceCallback callback) {
        if (mMainVM.downloadService.getValue() != null) {
            callback.onStart(mMainVM.downloadService.getValue());
        } else {
            Intent downloadIntent = new Intent(this, DownloadService.class);
            startService(downloadIntent);
            bindService(downloadIntent, mMainVM.getDownloadConnection(callback), Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * Assert that playing service is alive then get the service
     *
     * @param callback Callback
     */
    public void getPlayingService(PlayingServiceCallback callback) {
        if (mMainVM.playingService.getValue() != null) {
            callback.onStart(mMainVM.playingService.getValue());
        } else {
            Intent playingIntent = new Intent(this, PlayingService.class);
            startForegroundService(playingIntent);
            bindService(playingIntent, mMainVM.getPlayingConnection(callback), Context.BIND_AUTO_CREATE);
        }
    }

    public interface DownloadServiceCallback {
        void onStart(DownloadService service);
    }

    public interface PlayingServiceCallback {
        void onStart(PlayingService service);
    }

}