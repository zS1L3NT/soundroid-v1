package com.zectan.soundroid;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zectan.soundroid.viewmodels.HomeViewModel;
import com.zectan.soundroid.viewmodels.PlaylistViewViewModel;

// https://www.glyric.com/2018/merlin/aagaya-nilave

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "(SounDroid) MainActivity";
    private BottomNavigationView bottomNavigationView;
    private InputMethodManager imm;
    private FirebaseRepository repository;

    private HomeViewModel homeVM;
    private PlaylistViewViewModel playlistViewVM;

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO View model encapsulation

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        repository = new FirebaseRepository();

        // View Models
        homeVM = new ViewModelProvider(this).get(HomeViewModel.class);
        playlistViewVM = new ViewModelProvider(this).get(PlaylistViewViewModel.class);

        // Reference views
        bottomNavigationView = findViewById(R.id.bottom_navigator);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;

        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItem);
        bottomNavigationView.setOnNavigationItemReselectedListener(this::onNavigationItem);
    }

    private boolean onNavigationItem(MenuItem item) {
        String name = item.getTitle().toString();

        NavOptions options = new NavOptions
                .Builder()
                .setEnterAnim(android.R.anim.fade_in)
                .setExitAnim(R.anim.fade_out)
                .setPopEnterAnim(R.anim.fade_in)
                .setPopExitAnim(R.anim.fade_out)
                .build();

        switch (name) {
            case "Home":
                homeVM.setTransitionState(null);
                if (bottomNavigationView.getSelectedItemId() == item.getItemId()) break;
                navController.navigate(R.id.fragment_home, null, options);
                break;
            case "Playing":
                if (bottomNavigationView.getSelectedItemId() == item.getItemId()) break;
                navController.navigate(R.id.fragment_playing, null, options);
                break;
            case "Playlist":
                playlistViewVM.setTransitionState(null);
                if (bottomNavigationView.getSelectedItemId() == item.getItemId()) break;
                navController.navigate(R.id.fragment_playlists, null, options);
                break;
            default:
                Log.e(TAG, "Unknown navigation name: " + name);
                break;
        }

        return true;
    }

    public FirebaseRepository getRepository() {
        return repository;
    }

    public void showBottomNavigator() {
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public void hideBottomNavigator() {
        bottomNavigationView.setVisibility(View.GONE);
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
}