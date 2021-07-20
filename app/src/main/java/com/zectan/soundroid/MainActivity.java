package com.zectan.soundroid;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.classes.CrashDebugApplication;
import com.zectan.soundroid.connection.VersionCheckRequest;
import com.zectan.soundroid.databinding.ActivityMainBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.MenuEvents;
import com.zectan.soundroid.utils.Utils;
import com.zectan.soundroid.viewmodels.MainViewModel;
import com.zectan.soundroid.viewmodels.PlayingViewModel;
import com.zectan.soundroid.viewmodels.PlaylistEditViewModel;
import com.zectan.soundroid.viewmodels.PlaylistViewViewModel;
import com.zectan.soundroid.viewmodels.PlaylistsViewModel;
import com.zectan.soundroid.viewmodels.SearchViewModel;
import com.zectan.soundroid.viewmodels.SongEditViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://www.glyric.com/2018/merlin/aagaya-nilave

public class MainActivity extends CrashDebugApplication {
    private static final String TAG = "(SounDroid) MainActivity";
    public static final String DOWNLOAD_CHANNEL_ID = "Downloads";
    public final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public ActivityMainBinding B;
    public NavController navController;
    public NotificationManager notificationManager;
    public MainViewModel mainVM;
    public PlayingViewModel playingVM;
    public PlaylistsViewModel playlistsVM;
    public PlaylistViewViewModel playlistViewVM;
    public PlaylistEditViewModel playlistEditVM;
    public SongEditViewModel songEditVM;
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
        SearchViewModel searchVM = new ViewModelProvider(this).get(SearchViewModel.class);
        mainVM = new ViewModelProvider(this).get(MainViewModel.class);
        playingVM = new ViewModelProvider(this).get(PlayingViewModel.class);
        playlistsVM = new ViewModelProvider(this).get(PlaylistsViewModel.class);
        playlistViewVM = new ViewModelProvider(this).get(PlaylistViewViewModel.class);
        playlistEditVM = new ViewModelProvider(this).get(PlaylistEditViewModel.class);
        songEditVM = new ViewModelProvider(this).get(SongEditViewModel.class);

        // Live Observers
        mainVM.error.observe(this, this::handleError);
        mainVM.showUpdateDialog.observe(this, this::onShowUpdateDialogChange);
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

        // Notification Channels
        NotificationChannel downloadChannel = new NotificationChannel(
            MainActivity.DOWNLOAD_CHANNEL_ID,
            MainActivity.DOWNLOAD_CHANNEL_ID,
            NotificationManager.IMPORTANCE_DEFAULT
        );
        downloadChannel.setDescription("Download songs for offline listening");
        notificationManager.createNotificationChannel(downloadChannel);

        // Playing Screen background
        int[] colors = {getColor(R.color.default_cover_color), getAttributeResource(R.attr.colorSecondary)};
        GradientDrawable newGD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        playingVM.background.setValue(newGD);

        mainVM.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        new VersionCheckRequest(new VersionCheckRequest.Callback() {
            @Override
            public void onComplete(String version) {
                if (!Utils.versionAtLeast(BuildConfig.VERSION_NAME, version)) {
                    mainVM.showUpdateDialog.postValue(true);
                }
            }

            @Override
            public void onError(String message) {
                handleError(new Exception("Could not check for latest version"));
            }
        });
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

        Map<String, Object> error = new HashMap<>();
        List<String> stack = new ArrayList<>();
        for (StackTraceElement el : e.getStackTrace()) stack.add(el.toString());
        error.put("stack", stack);
        error.put("type", "Safe");
        error.put("date", Calendar.getInstance().getTime().toString());
        error.put("message", e.getMessage());
        error.put("class", e.getClass().getName());

        db.collection("users")
            .document(mainVM.userId)
            .collection("errors")
            .add(error)
            .addOnSuccessListener(__ -> Log.i(TAG, "Error stored successfully"))
            .addOnFailureListener(e_ -> Log.e(TAG, "Error stored unsuccessfully: " + e_.getMessage()));
    }

    public int getAttributeResource(int id) {
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(id, value, true);
        return value.data;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean handleMenuItemClick(Info info, Song song, MenuItem item) {
        return new MenuEvents(this, info, song, item).handle();
    }

    @SuppressLint("NonConstantResourceId")
    public boolean handleMenuItemClick(Info info, Song song, MenuItem item, Runnable runnable) {
        return new MenuEvents(this, info, song, item, runnable).handle();
    }

    public void snack(String message) {
        Snackbar
            .make(B.navHostFragment, message, Snackbar.LENGTH_SHORT)
            .setAction(R.string.done, __ -> {
            })
            .show();
    }

    public void startDownloadService(DownloadServiceCallback callback) {
        if (mainVM.downloadBinder.getValue() != null) {
            callback.onStart(mainVM.downloadBinder.getValue());
        } else {
            Intent downloadIntent = new Intent(this, DownloadService.class);
            startForegroundService(downloadIntent);
            bindService(downloadIntent, mainVM.getDownloadConnection(callback), Context.BIND_AUTO_CREATE);
        }
    }

    private void onShowUpdateDialogChange(Boolean showUpdateDialog) {
        if (showUpdateDialog) {
            new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("Update SounDroid")
                .setMessage("A new version of SounDroid exists, so this version won't work anymore")
                .setPositiveButton("Update", (dialog, which) -> {
                    Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://soundroid.zectan.com")
                    );
                    startActivity(browserIntent);
                    onShowUpdateDialogChange(true);
                })
                .setCancelable(false)
                .show();
        }
    }

    public interface DownloadServiceCallback {
        void onStart(DownloadService.DownloadBinder binder);
    }

}