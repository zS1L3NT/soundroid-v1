package com.zectan.soundroid.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.Activities.AuthActivity;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.Models.User;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Services.PlayingService;
import com.zectan.soundroid.databinding.FragmentSettingsBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SettingsFragment extends Fragment<FragmentSettingsBinding> {
    private static final String TAG = "(SounDroid) SettingsFragment";
    private SettingsPreference settingsPreference;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentSettingsBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);
        settingsPreference = new SettingsPreference();

        // Listeners
        mMainVM.myUser.observe(this, this::onMyUserChange);
        B.logoutButton.setOnClickListener(this::logout);

        mActivity
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
            .with(mActivity)
            .load(user.getProfilePicture())
            .placeholder(R.drawable.playing_cover_loading)
            .error(R.drawable.playing_cover_failed)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.profilePictureImage);
    }

    private void logout(View view) {
        new MaterialAlertDialogBuilder(mActivity)
            .setTitle("Are you sure?")
            .setMessage("All downloaded songs will be deleted")
            .setNegativeButton("Cancel", (dialog, which) -> {
            })
            .setPositiveButton("Sign Out", (dialog, which) -> {
                List<Song> songs = mActivity.mMainVM.mySongs.getValue();
                for (Song song : songs) {
                    song.deleteLocally(mActivity);
                }
                mActivity.getPlayingService(PlayingService::clearQueue);
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(mActivity, AuthActivity.class);
                startActivity(intent);
                mActivity.finish();
            })
            .show();
    }

    public static class SettingsPreference extends PreferenceFragmentCompat {
        private MainActivity mActivity;
        private DocumentReference userRef;
        private SwitchPreferenceCompat openPlayingScreen;
        private SwitchPreferenceCompat highDownloadQuality;
        private SwitchPreferenceCompat highStreamQuality;
        private ListPreference seekDuration;
        private ListPreference theme;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.fragment_settings_preferences, rootKey);

            // Reference Views
            openPlayingScreen = findPreference("open_playing_screen");
            highDownloadQuality = findPreference("high_download_quality");
            highStreamQuality = findPreference("high_stream_quality");
            seekDuration = findPreference("seek_duration");
            theme = findPreference("theme");
            Preference clearAllDownloads = findPreference("clear_all_downloads");
            Preference throwError = findPreference("throw_error");
            assert clearAllDownloads != null;
            assert throwError != null;

            // Listeners
            openPlayingScreen.setOnPreferenceChangeListener(this::onOpenPlayingScreenChange);
            highDownloadQuality.setOnPreferenceChangeListener(this::onHighDownloadQualityChange);
            highStreamQuality.setOnPreferenceChangeListener(this::onHighStreamQualityChange);
            seekDuration.setOnPreferenceChangeListener(this::onSeekDurationChange);
            theme.setOnPreferenceChangeListener(this::onThemeChange);
            clearAllDownloads.setOnPreferenceClickListener(this::onClearAllDownloadsClick);
            throwError.setOnPreferenceClickListener(this::onThrowErrorClicked);

            mActivity = (MainActivity) requireContext();
            if (mActivity.mMainVM != null) updatePreferences(mActivity.mMainVM.myUser.getValue());
        }

        /**
         * Update the state of the preferences
         *
         * @param user User
         */
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
            if (seekDuration != null)
                seekDuration.setValue(String.valueOf(user.getSeekDuration()));
            if (theme != null) {
                theme.setValue(user.getTheme());
                theme.setSummary(user.getTheme());
            }
        }

        private boolean onOpenPlayingScreenChange(Preference preference, Object o) {
            if (userRef != null)
                userRef
                    .update("openPlayingScreen", Boolean.parseBoolean(o.toString()))
                    .addOnFailureListener(mActivity::warnError);
            return false;
        }

        private boolean onHighDownloadQualityChange(Preference preference, Object o) {
            if (userRef != null)
                userRef
                    .update("highDownloadQuality", Boolean.parseBoolean(o.toString()))
                    .addOnFailureListener(mActivity::warnError);
            return false;
        }

        private boolean onHighStreamQualityChange(Preference preference, Object o) {
            if (userRef != null)
                userRef
                    .update("highStreamQuality", Boolean.parseBoolean(o.toString()))
                    .addOnFailureListener(mActivity::warnError);
            return false;
        }

        private boolean onSeekDurationChange(Preference preference, Object o) {
            if (userRef != null) {
                userRef
                    .update("seekDuration", Integer.valueOf(o.toString()))
                    .addOnFailureListener(mActivity::warnError);
            }
            return false;
        }

        private boolean onThemeChange(Preference preference, Object o) {
            if (userRef != null)
                userRef
                    .update("theme", o.toString())
                    .addOnFailureListener(mActivity::warnError);
            return false;
        }

        private boolean onClearAllDownloadsClick(Preference preference) {
            new MaterialAlertDialogBuilder(mActivity)
                .setTitle("Are you sure?")
                .setMessage("All downloaded songs will be deleted")
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .setPositiveButton("Delete", (dialog, which) -> {
                    List<Song> songs = mActivity.mMainVM.mySongs.getValue();
                    for (Song song : songs) {
                        song.deleteLocally(mActivity);
                    }
                    mActivity.getPlayingService(PlayingService::clearQueue);
                })
                .show();
            return false;
        }

        private boolean onThrowErrorClicked(Preference preference) {
            throw new RuntimeException("A Runtime Exception was thrown!");
        }
    }
}