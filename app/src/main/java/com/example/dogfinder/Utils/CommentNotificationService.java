package com.example.dogfinder.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.dogfinder.Entity.TokenData;
import com.example.dogfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class CommentNotificationService extends FirebaseMessagingService {
    public CommentNotificationService(){
    }

    //upload new token to firestore
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        TokenData tokenData = new TokenData(s);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        firebaseFirestore.collection("DeviceTokens").document(auth.getCurrentUser().getUid()).set(tokenData);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getNotification() != null){
            String body = remoteMessage.getNotification().getBody();
            String title = remoteMessage.getNotification().getTitle();
            showNotification(body);
        }
    }

    public void showNotification(String body){
        String channel_id = "Comment";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channel_id,
                    "Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("This is Dog Finder Notification");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channel_id);

            builder.setSmallIcon(R.mipmap.comment)
                    .setContentTitle("Receiving new comment!")
                    .setContentText(body)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAllowSystemGeneratedContextualActions(true)
                    .setAutoCancel(true)
                    .build();
            NotificationManagerCompat.from(this).notify(1,builder.build());
        }
    }
}
