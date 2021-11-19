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

    private RequestQueue requestQueue;
    private final String postUrl ="https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey = "AAAAEGs21bA:APA91bEXVBLhNJBT9Nn6CaNO0nQ-iDIzY7kKlm9nN808afD4T-ImCXvBLkWEUWw-NpAIJc3MI9Fn7Dt3M3sl2kSY9I-XKbhpj5EdoWFHdRBJvTP1_nTtcE-WPJSY-2K8UeIYMgM_CsqD";

    public NotificationSender(String token, String body, Context context, Activity activity) {
        this.token = token;
        this.body = body;
        this.title = "Receive new comment!";
        this.context = context;
        this.activity = activity;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public void sendNotification(){
        requestQueue = Volley.newRequestQueue(activity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to",token);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title",title);
            notiObject.put("body",body);
            notiObject.put("icon", R.mipmap.comment);
            mainObj.put("notification",notiObject);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
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


                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;

                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
