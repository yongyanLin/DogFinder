package com.example.dogfinder.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.dogfinder.Activity.CommentActivity;
import com.example.dogfinder.Entity.TokenData;
import com.example.dogfinder.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class CommentNotificationService extends FirebaseMessagingService {
    NotificationManager manager;
    public CommentNotificationService(){

    }


    //upload new token to firestore
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                if(user != null){
                    updateToken(s);
                }
            }
        });

    }
    private void updateToken(String token){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getUid();
        TokenData tokenData = new TokenData(userId,token);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("DeviceTokens").document(auth.getCurrentUser().getUid()).set(tokenData);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),notification);
        r.play();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            r.setLooping(false);
        }
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100,300,300,300};
        v.vibrate(pattern,-1);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"Comment Channel");
        builder.setSmallIcon(R.mipmap.comment);
        Intent result = new Intent(this, CommentActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, result, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle(remoteMessage.getNotification().getTitle());
        builder.setContentText(remoteMessage.getNotification().getBody());
        builder.setContentIntent(pendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);

        manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }



// notificationId is a unique int for each notification that you must define
        manager.notify(100, builder.build());

    }
}

