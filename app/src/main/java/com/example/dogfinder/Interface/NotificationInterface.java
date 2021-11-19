package com.example.dogfinder.Interface;

import android.app.Notification;

import com.example.dogfinder.Entity.MyResponse;
import com.example.dogfinder.Entity.NotificationSender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationInterface {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAEGs21bA:APA91bEXVBLhNJBT9Nn6CaNO0nQ-iDIzY7kKlm9nN808afD4T-ImCXvBLkWEUWw-NpAIJc3MI9Fn7Dt3M3sl2kSY9I-XKbhpj5EdoWFHdRBJvTP1_nTtcE-WPJSY-2K8UeIYMgM_CsqD"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body NotificationSender body);
}
