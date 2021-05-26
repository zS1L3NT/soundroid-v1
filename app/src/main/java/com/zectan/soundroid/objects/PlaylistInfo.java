package com.zectan.soundroid.objects;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaylistInfo {
    private String id;
    private String name;
    private String cover;
    private List<String> order;

    public PlaylistInfo() {
    }

    public PlaylistInfo(String id, String name, List<String> order) {
        this.id = id;
        this.name = name;
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCover() {
        return cover;
    }

    public List<String> getOrder() {
        return order;
    }

    @Override
    public @NotNull String toString() {
        return String.format("PlaylistInfo { id: '%s', name: '%s', cover: '%s' }", id, name, cover);
    }
}
