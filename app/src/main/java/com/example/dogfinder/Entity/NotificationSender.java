package com.example.dogfinder.Entity;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dogfinder.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotificationSender {
    String token;
    String body;
    String title;
    Context context;
    Activity activity;

    private RequestQueue queue;
    private final String messagingUrl ="https://fcm.googleapis.com/fcm/send";
    private final String Key = "AAAAEGs21bA:APA91bEXVBLhNJBT9Nn6CaNO0nQ-iDIzY7kKlm9nN808afD4T-ImCXvBLkWEUWw-NpAIJc3MI9Fn7Dt3M3sl2kSY9I-XKbhpj5EdoWFHdRBJvTP1_nTtcE-WPJSY-2K8UeIYMgM_CsqD";

    public NotificationSender(String token, String body, Context context, Activity activity) {
        this.token = token;
        this.body = body;
        this.title = "Receive new comment!";
        this.context = context;
        this.activity = activity;
    }


    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void sendCommentNotification(){
        queue = Volley.newRequestQueue(activity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to",token);
            JSONObject object = new JSONObject();
            object.put("title",title);
            object.put("body",body);
            object.put("icon", R.mipmap.comment);
            mainObj.put("notification",object);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, messagingUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("content-type", "application/json");
                    map .put("authorization", "key=" + Key);
                    return map;

                }
            };
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
