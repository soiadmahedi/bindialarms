package com.soiadmahedi.bindialarms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.app.NotificationChannel;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AzanReceiver extends BroadcastReceiver {

    public static MediaPlayer player;
    public static final String CHANNEL_ID = "AZAN_CHANNEL";
    public static final String ACTION_STOP_AZAN = "com.soiadmahedi.bindialarms.ACTION_STOP_AZAN";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Azan Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("This channel is for Azan alarm notifications.");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        if (ACTION_STOP_AZAN.equals(intent.getAction())) {
            stopAzanAndNotification(context, intent);
            return;
        }

        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        int requestCode = intent.getIntExtra("requestCode", (int) System.currentTimeMillis());

        if (title == null || title.isEmpty()) {
            title = "আজানের সময় হয়েছে";
        }
        if (description == null || description.isEmpty()) {
            description = "সালাতের জন্য প্রস্তুতি নিন।";
        }

        try {
            if (player != null && player.isPlaying()) {
                player.stop();
                player.release();
                player = null;
            }

            Uri azanSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.getPackageName() + "/raw/azan");

            if (requestCode == 1) { // ফজরের জন্য আলাদা আজান
                azanSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + context.getPackageName() + "/raw/fjrazan");
            }

            player = new MediaPlayer();
            player.setDataSource(context, azanSoundUri);
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(false);
            player.prepare();
            player.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(context,
                requestCode, 
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(context, AzanReceiver.class);
        stopIntent.setAction(ACTION_STOP_AZAN);
        stopIntent.putExtra("NOTIFICATION_ID", requestCode); 
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context,
                requestCode + 1, 
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setContentIntent(openAppPendingIntent) 
                .setAutoCancel(true) 
                .addAction(R.drawable.icon_close_round, "বন্ধ করুন", stopPendingIntent)
                .addAction(R.drawable.icon_access_time_round, "সালাতের টাইম দেখুন", openAppPendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify(requestCode, builder.build());
    }

    private void stopAzanAndNotification(Context context, Intent intent) {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }

        int notificationId = intent.getIntExtra("NOTIFICATION_ID", -1);
        if (notificationId != -1) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(notificationId);
        }
    }
}
