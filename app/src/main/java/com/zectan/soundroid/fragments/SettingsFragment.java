package com.zectan.soundroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.AuthActivity;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.databinding.FragmentSettingsBinding;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.models.User;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SettingsFragment extends Fragment<FragmentSettingsBinding> {
    private static final String TAG = "(SounDroid) SettingsFragment";
    private SettingsPreference settingsPreference;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentSettingsBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);
        settingsPreference = new SettingsPreference();

        // Observers
        mainVM.myUser.observe(this, this::onMyUserChange);
        B.logoutButton.setOnClickListener(this::logout);

        activity
            .getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.settings_view, settingsPreference)
            .commit();

        return B.getRoot();
    }

    private void onMyUserChange(User user) {
        settingsPreference.updatePreferences(user);
        B.usnmText.setText(user.getUsnm());
        Glide
            .with(activity)
            .load(user.getProfilePicture())
            .placeholder(R.drawable.playing_cover_loading)
            .error(R.drawable.playing_cover_failed)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.profilePictureImage);
    }

    private void logout(View view) {
        new MaterialAlertDialogBuilder(activity)
            .setTitle("Are you sure?")
            .setMessage("All downloaded songs will be deleted")
            .setNegativeButton("Cancel", (dialog, which) -> {
            })
            .setPositiveButton("Sign Out", (dialog, which) -> {
                List<Song> songs = activity.mainVM.mySongs.getValue();
                for (Song song : songs) {
                    song.deleteLocally(activity);
                }
                playingVM.clearQueue(activity);
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(activity, AuthActivity.class);
                startActivity(intent);
                activity.finish();
            })
            .show();
    }

    public static class SettingsPreference extends PreferenceFragmentCompat {
        private MainActivity activity;
        private DocumentReference userRef;
        private SwitchPreferenceCompat openPlayingScreen;
        private SwitchPreferenceCompat highDownloadQuality;
        private SwitchPreferenceCompat highStreamQuality;

        public SettingsPreference() {

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.fragment_settings_preferences, rootKey);
            openPlayingScreen = findPreference("open_playing_screen");
            highDownloadQuality = findPreference("high_download_quality");
            highStreamQuality = findPreference("high_stream_quality");
            openPlayingScreen.setOnPreferenceChangeListener(this::onOpenPlayingScreenChange);
            highDownloadQuality.setOnPreferenceChangeListener(this::onHighDownloadQualityChange);
            highStreamQuality.setOnPreferenceChangeListener(this::onHighStreamQualityChange);

            Preference clearAllDownloads = findPreference("clear_all_downloads");
            assert clearAllDownloads != null;
            clearAllDownloads.setOnPreferenceClickListener(this::onClearAllDownloadsClick);

            activity = (MainActivity) requireContext();
            if (activity.mainVM != null) updatePreferences(activity.mainVM.myUser.getValue());
        }

        public void updatePreferences(User user) {
            userRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getId());

            if (openPlayingScreen != null)
                openPlayingScreen.setChecked(user.getOpenPlayingScreen());
            if (highDownloadQuality != null)
                highDownloadQuality.setChecked(user.getHighDownloadQuality());
            if (highStreamQuality != null)
                highStreamQuality.setChecked(user.getHighStreamQuality());
        }

        private boolean onOpenPlayingScreenChange(Preference preference, Object o) {
            if (userRef != null)
                userRef
                    .update("openPlayingScreen", Boolean.parseBoolean(o.toString()))
                    .addOnFailureListener(activity::handleError);
            return false;
        }

        private boolean onHighDownloadQualityChange(Preference preference, Object o) {
            if (userRef != null)
                userRef
                    .update("highDownloadQuality", Boolean.parseBoolean(o.toString()))
                    .addOnFailureListener(activity::handleError);
            return false;
        }

        private boolean onHighStreamQualityChange(Preference preference, Object o) {
            if (userRef != null)
                userRef
                    .update("highStreamQuality", Boolean.parseBoolean(o.toString()))
                    .addOnFailureListener(activity::handleError);
            return false;
        }

        private boolean onClearAllDownloadsClick(Preference preference) {
            new MaterialAlertDialogBuilder(activity)
                .setTitle("Are you sure?")
                .setMessage("All downloaded songs will be deleted")
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .setPositiveButton("Delete", (dialog, which) -> {
                    List<Song> songs = activity.mainVM.mySongs.getValue();
                    for (Song song : songs) {
                        song.deleteLocally(activity);
                    }
                    activity.playingVM.clearQueue(activity);
                })
                .show();
            return false;
        }
    }
}