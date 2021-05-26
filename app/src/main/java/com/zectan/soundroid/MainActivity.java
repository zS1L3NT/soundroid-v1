package com.zectan.soundroid;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// https://www.glyric.com/2018/merlin/aagaya-nilave

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "(SounDroid) MainActivity";
    private BottomNavigationView bottomNavigationView;
    private InputMethodManager imm;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        repository = new FirebaseRepository();

        // Reference views
        bottomNavigationView = findViewById(R.id.bottom_navigator);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
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