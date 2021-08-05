package com.zectan.soundroid.Models;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private String usnm;
    private String profilePicture;
    private boolean openPlayingScreen;
    private boolean highStreamQuality;
    private boolean highDownloadQuality;
    private int seekDuration;
    private String theme;

    public User() {
    }

    /**
     * Object containing data about a user
     *
     * @param id                  User ID
     * @param usnm                Username
     * @param profilePicture      Profile Picture
     * @param openPlayingScreen   Opens playing screen when click on a song
     * @param highStreamQuality   Wants high quality streaming
     * @param highDownloadQuality Wants high quality downloads
     * @param seekDuration        Seconds to seek when double tap cover
     * @param theme               Theme
     */
    public User(
        String id,
        String usnm,
        String profilePicture,
        boolean openPlayingScreen,
        boolean highStreamQuality,
        boolean highDownloadQuality,
        int seekDuration,
        String theme
    ) {
        this.id = id;
        this.usnm = usnm;
        this.profilePicture = profilePicture;
        this.openPlayingScreen = openPlayingScreen;
        this.highStreamQuality = highStreamQuality;
        this.highDownloadQuality = highDownloadQuality;
        this.seekDuration = seekDuration;
        this.theme = theme;
    }

    /**
     * Create an empty object for a default user
     */
    public static User getEmpty() {
        return new User(
            "",
            "",
            "",
            true,
            true,
            true,
            5,
            "Dark"
        );
    }

    public String getId() {
        return id;
    }

    public String getUsnm() {
        return usnm;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public boolean getOpenPlayingScreen() {
        return openPlayingScreen;
    }

    public boolean getHighStreamQuality() {
        return highStreamQuality;
    }

    public boolean getHighDownloadQuality() {
        return highDownloadQuality;
    }

    public int getSeekDuration() {
        return seekDuration;
    }

    public String getTheme() {
        return theme;
    }

    /**
     * Create a Map Object from a User Object
     *
     * @return Map Object
     */
    public Map<String, Object> toMap() {
        Map<String, Object> object = new HashMap<>();
        object.put("id", id);
        object.put("usnm", usnm);
        object.put("profilePicture", profilePicture);
        object.put("openPlayingScreen", openPlayingScreen);
        object.put("highDownloadQuality", highDownloadQuality);
        object.put("highStreamQuality", highStreamQuality);
        object.put("theme", theme);
        return object;
    }
}
