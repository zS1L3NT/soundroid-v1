package com.zectan.soundroid.classes;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public abstract class Request {
    private final Callback mCallback;
    private final MediaType mJSON;
    private final String mURL;
    private OkHttpClient mClient;
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

    protected void replaceClient(OkHttpClient client) {
        mClient = client;
    }

    protected void sendRequest(RequestType requestType) {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(mURL);
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

        okhttp3.Request request = builder.build();

        new Thread(() -> {
            try {
                Response response = mClient.newCall(request).execute();
                String res = response.body() != null ? response.body().string() : "";
                if (response.code() == 200) {
                    mCallback.onComplete(res);
                } else {
                    mCallback.onError(res);
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
