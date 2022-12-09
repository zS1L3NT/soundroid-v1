package com.zectan.soundroid.Models;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Playlist {
    private String id;
    private String name;
    private String cover;
    private String colorHex;
    private String userId;
    private List<String> order;

    public Playlist() {
    }

    /**
     * Shortcut to create a simple playlist that will not be stored in Firebase
     *
     * @param id    Playlist ID
     * @param name  Name
     * @param order Order
     */
    public Playlist(String id, String name, List<String> order) {
        this.id = id;
        this.name = name;
        this.cover = "";
        this.colorHex = "#7b828b";
        this.userId = "";
        this.order = order;
    }

    /**
     * Object containing data about a playlist
     *
     * @param id       Playlist ID
     * @param name     Name
     * @param cover    Cover
     * @param colorHex Color Hex
     * @param userId   User ID
     * @param order    Order
     */
    public Playlist(String id, String name, String cover, String colorHex, String userId, List<String> order) {
        this.id = id;
        this.name = name;
        this.cover = cover;
        this.colorHex = colorHex;
        this.userId = userId;
        this.order = order;
    }

    /**
     * Create an empty object for a default playlist
     *
     * @return Playlist
     */
    public static Playlist getEmpty() {
        return new Playlist("", "", new ArrayList<>());
    }

    /**
     * Create a Playlist Object from a JSON object
     *
     * @param object JSON Object
     * @return Playlist Object
     * @throws JSONException Error if object is unparsable
     */
    public static Playlist fromJSON(JSONObject object) throws JSONException {
        String id = object.getString("id");
        String name = object.getString("name");
        String cover = object.getString("cover");
        String colorHex = object.getString("colorHex");
        String userId = object.getString("userId");
        JSONArray orderArray = object.getJSONArray("order");
        List<String> order = new ArrayList<>();
        for (int i = 0; i < orderArray.length(); i++)
            order.add(orderArray.getString(i));
        return new Playlist(id, name, cover, colorHex, userId, order);
    }

    /**
     * Create a JSON Object from a Playlist Object
     *
     * @return JSON Object
     */
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("name", name);
            object.put("cover", cover);
            object.put("colorHex", colorHex);
            object.put("userId", userId);
            object.put("order", new JSONArray(order));
            return object;
        } catch (JSONException e) {
            throw new RuntimeException("Could not put items in JSON Object");
        }
    }

    /**
     * Create a Map Object from a Playlist Object
     *
     * @return Map Object
     */
    public Map<String, Object> toMap() {
        Map<String, Object> object = new HashMap<>();
        object.put("id", id);
        object.put("name", name);
        object.put("cover", cover);
        object.put("colorHex", colorHex);
        object.put("userId", userId);
        object.put("order", order);
        return object;
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

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getColorHex() {
        return colorHex;
    }

    public List<String> getOrder() {
        return order;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public @NotNull String toString() {
        return String.format("PlaylistInfo { id: '%s', name: '%s', cover: '%s' }", id, name, cover);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Playlist)) return false;
        Playlist that = (Playlist) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(cover, that.cover) &&
            Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cover, order);
    }
}
