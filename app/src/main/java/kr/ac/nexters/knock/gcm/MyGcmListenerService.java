package kr.ac.nexters.knock.gcm;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import kr.ac.nexters.knock.R;
import kr.ac.nexters.knock.nexters.example.activity.MainActivity;

/**
 * Created by KimCP on 15. 8. 9..
 */
public class MyGcmListenerService   extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     *
     * @param from SenderID 값을 받아온다.
     * @param data Set형태로 GCM으로 받은 데이터 payload이다.
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String title = data.getString("title");
        String message = data.getString("message");
        String sender = data.getString("sender");
        String type = data.getString("type");
        String name = data.getString("name");
        String pushid = data.getString("pushid");

        Log.i("data", title + "," + message + "," + sender + "," + type + "," + name);

        // GCM으로 받은 메세지를 디바이스에 알려ddf주는 sendNotification()을 호출한다.
        if(type.equals("auth")){
            //pushid is null
            authNotification(title, message, sender,pushid);
            Log.i("Noti", "auth");
        }else if(type.equals("accept")) {
            acceptNotification(title,message,sender, pushid);
            Log.i("Noti", "accept");
        }else{
            sendNotification(title, message);
            Log.i("Noti", "msg");
        }

    }


    /**
     * 실제 디바에스에 GCM으로부터 받은 메세지를 알려주는 함수이다. 디바이스 Notification Center에 나타난다.
     * @param title
     * @param message
     */
    private void sendNotification(String title, String message) {
        Intent intent = new Intent(MyGcmListenerService.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void authNotification(String title, String message,String sender,String pushid){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,PendingIntent.FLAG_ONE_SHOT);

        //Intent in = new Intent(MyGcmListenerService.this, AcceptActivity.class);
        Intent in = new Intent(MyGcmListenerService.this, MainActivity.class);
        in.putExtra("sender", sender); //요청을 보낸 user pid => ppid
        in.putExtra("pushid", pushid); //

        PendingIntent contentIntent = PendingIntent.getActivity(this,0, in,0);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(contentIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void acceptNotification(String title,String message,String sender, String pushid){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,PendingIntent.FLAG_ONE_SHOT);

        //Intent inte = new Intent(MyGcmListenerService.this, ConnectionActivity.class);
        Intent inte = new Intent(MyGcmListenerService.this, MainActivity.class);

        inte.putExtra("sender", sender);
        inte.putExtra("ppushid", pushid);

        PendingIntent contentIntent = PendingIntent.getActivity(this,0, inte,0);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        notificationBuilder.setContentIntent(contentIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}