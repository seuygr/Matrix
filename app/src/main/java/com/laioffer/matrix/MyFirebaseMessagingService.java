package com.laioffer.matrix;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

// receieve the message from FCM
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private Context mContext;

    /**
     * Called when message is received.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (mContext == null) {
            mContext = getBaseContext();
        }
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        sendNotification(remoteMessage);

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void sendNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, ControlPanel.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Define pending intent to trigger activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String type = remoteMessage.getData().get("type");
        String description = remoteMessage.getData().get("description");
        Bitmap icon =  BitmapFactory.decodeResource(mContext.getResources(),
                Config.trafficMap.get(type));

        // String uri = remoteMessage.getData().get(imageUri);

        //Create Notification according to builder pattern
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "firebase");

        notificationBuilder
                .setSmallIcon(R.drawable.baseline_notifications_none_black_18dp)
                .setLargeIcon(icon)
                .setContentTitle(type)
                .setContentText(description)
                .setSound(defaultSoundUri)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentIntent(pendingIntent);

        // Get Notification Manager
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Send notification
        notificationManager.notify(0, notificationBuilder.build());
    }
}

