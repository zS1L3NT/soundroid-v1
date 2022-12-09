package com.zectan.soundroid.Classes;

import com.zectan.soundroid.Env;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class Request {
    private final Callback mCallback;
    private final MediaType mJSON;
    private final String mURL;
    private OkHttpClient mClient;
    private JSONObject mObject;

    /**
     * Base class for a HTTP Request to an endpoint at my server
     *
     * @param URL      Relative path to an endpoint in my server
     * @param callback Callback
     */
    public Request(String URL, Callback callback) {
        mURL = Env.API_URL + URL;
        mJSON = MediaType.parse("application/json; charset=utf-8");
        mObject = new JSONObject();
        mClient = new OkHttpClient();
        mCallback = callback;
    }

    /**
     * Put data in the HTTP request
     *
     * @param key   Key
     * @param value Value
     */
    protected void putData(String key, Object value) {
        try {
            mObject.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set an entire object as the HTTP request data
     *
     * @param object Object
     */
    protected void replaceData(JSONObject object) {
        mObject = object;
    }

    /**
     * Change the client. Used when editing HTTP timeout
     *
     * @param client Client
     */
    protected void replaceClient(OkHttpClient client) {
        mClient = client;
    }

    /**
     * Send the request to the server
     *
     * @param requestType Request Type
     */
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
