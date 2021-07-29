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
    private String theme;

    public User() {
    }

    public User(String id, String usnm, String profilePicture, boolean openPlayingScreen, boolean highStreamQuality, boolean highDownloadQuality, String theme) {
        this.id = id;
        this.usnm = usnm;
        this.profilePicture = profilePicture;
        this.openPlayingScreen = openPlayingScreen;
        this.highStreamQuality = highStreamQuality;
        this.highDownloadQuality = highDownloadQuality;
        this.theme = theme;
    }

    public static User getEmpty() {
        return new User(
            "",
            "",
            "",
            true,
            true,
            true,
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

    public String getTheme() {
        return theme;
    }

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
