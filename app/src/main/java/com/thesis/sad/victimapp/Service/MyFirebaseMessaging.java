package com.thesis.sad.victimapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.thesis.sad.victimapp.Common.Common;
import com.thesis.sad.victimapp.Helper.NotificationHelper;
import com.thesis.sad.victimapp.R;

import java.util.Locale;
import java.util.Map;


public class MyFirebaseMessaging extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        if(remoteMessage.getNotification().getTitle().equals("Declined")){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(MyFirebaseMessaging.this," "+remoteMessage.getNotification()
                            .getBody(),Toast.LENGTH_SHORT).show();
                }
            });
            LocalBroadcastManager.getInstance(MyFirebaseMessaging.this).sendBroadcast(new Intent(Common.CANCEL_BROADCAST));

        }
        else if(remoteMessage.getNotification().getTitle().equals("Arrived")){
            Toast.makeText(this, "The Ambulance have arrived near you", Toast.LENGTH_LONG).show();
            Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        showArrivedNotificationAPI26(remoteMessage.getNotification().getBody());


                    }else {
                        showArrivedNotification(remoteMessage.getNotification().getBody());

                    }
                }
            });



        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(String body) {
        PendingIntent contentintent = PendingIntent.getActivities(getBaseContext(),0, new Intent[]{new Intent()},PendingIntent.FLAG_ONE_SHOT);
        Uri defaultsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getAmbulanceNotification("Arrived",body,contentintent,defaultsound);
        notificationHelper.getManager().notify(1,builder.build());

    }

    private void showArrivedNotification(String body) {
        PendingIntent contentintent = PendingIntent.getActivities(getBaseContext(),0, new Intent[]{new Intent()},PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_locate)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentintent);
        NotificationManager manager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());



    }

}
