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
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCover() {
        return cover;
    }
    
    public List<String> getOrder() {
        return order;
    }
    
    public void setOrder(List<String> order) {
        this.order = order;
    }
    
    @Override
    public @NotNull String toString() {
        return String.format("PlaylistInfo { id: '%s', name: '%s', cover: '%s' }", id, name, cover);
    }
}
