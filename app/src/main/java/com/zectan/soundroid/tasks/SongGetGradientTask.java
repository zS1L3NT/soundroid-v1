package com.zectan.soundroid.tasks;

import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SongGetGradientTask extends Thread {
    private static final String TAG = "(SounDroid) SongSearchTask";
    private static final String API = "http://dl.zectan.com/api/get_dominant_color";
    private final String coverUrl;
    private final Callback callback;

    public SongGetGradientTask(String coverUrl, Callback callback) {
        this.coverUrl = coverUrl;
        this.callback = callback;
    }

    @Override
    public void run() {
        super.run();
        HashMap<String, String> params = new HashMap<>();
        params.put("url", coverUrl);
        String JSON = new JSONObject(params).toString();

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), JSON);
        Request request = new Request.Builder().url(API).post(body).build();

        try {
            OkHttpClient client = new OkHttpClient();
            client.setReadTimeout(1, TimeUnit.MINUTES);
            client.setWriteTimeout(1, TimeUnit.MINUTES);
            client.setConnectTimeout(1, TimeUnit.MINUTES);
            Response response = client.newCall(request).execute();

            if (response.code() == 200) {
                String colorHex = response.body().string();
                callback.run(colorHex);
                Log.i(TAG, "API_GOOD: " + colorHex);
            } else {
                Log.e(TAG, "API_BAD: " + response.body().string());
            }
        } catch (IOException e) {
            Log.e(TAG, "API_CRASH: " + API);
            Log.e(TAG, e.getMessage());
        }
    }

    public interface Callback {
        void run(String colorHex);
    }
}
