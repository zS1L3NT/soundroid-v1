package com.zectan.soundroid.classes;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public abstract class Request {
    private final OkHttpClient mClient;
    private final Callback mCallback;
    private final MediaType mJSON;
    private final String mURL;
    private JSONObject mObject;

    public Request(String URL, Callback callback) {
        mURL = URL;
        mJSON =  MediaType.parse("application/json; charset=utf-8");
        mObject = new JSONObject();
        mClient = new OkHttpClient();
        mCallback = callback;
    }

    protected void putData(String key, Object data) {
        try {
            mObject.put(key, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void replaceData(JSONObject object) {
        mObject = object;
    }

    protected void sendRequest(RequestType requestType) {
        com.squareup.okhttp.Request.Builder builder = new com.squareup.okhttp.Request.Builder().url(mURL);
        RequestBody body = RequestBody.create(mJSON, mObject.toString());

        switch (requestType) {
            case GET:
                builder = builder.get();
                break;
            case POST:
                builder = builder.post(body);
                break;
            case PUT:
                builder = builder.put(body);
                break;
            case DELETE:
                builder = builder.delete(body);
                break;
        }

        com.squareup.okhttp.Request request = builder.build();

        new Thread(() -> {
            try {
                Response response = mClient.newCall(request).execute();
                if (response.code() == 200) {
                    mCallback.onComplete(response.body().string());
                } else {
                    mCallback.onError(response.body().string());
                }
            } catch (IOException e) {
                mCallback.onError(e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public interface Callback {
        void onComplete(String response);

        void onError(String message);
    }

    public enum RequestType {
        GET,
        POST,
        PUT,
        DELETE
    }

}
