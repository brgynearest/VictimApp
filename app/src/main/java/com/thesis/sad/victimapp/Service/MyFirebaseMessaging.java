package com.thesis.sad.victimapp.Service;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessaging extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);

        // it send the firebase message with contain lat and lng from Rider app
        //so we need convert message to lnanlan

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(MyFirebaseMessaging.this," "+remoteMessage.getNotification()
                        .getBody(),Toast.LENGTH_LONG).show();

            }
        });




    }
}
