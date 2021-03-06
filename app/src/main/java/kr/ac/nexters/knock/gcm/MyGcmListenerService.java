package kr.ac.nexters.knock.gcm;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;

import kr.ac.nexters.knock.R;
import kr.ac.nexters.knock.dialog.AcceptAuthActivity;
import kr.ac.nexters.knock.dialog.AuthReqActivity;
import kr.ac.nexters.knock.network.IsSuccess;
import kr.ac.nexters.knock.network.NetworkModel;
import kr.ac.nexters.knock.nexters.example.activity.AuthActivity;
import kr.ac.nexters.knock.nexters.example.activity.MainActivity;
import kr.ac.nexters.knock.nexters.example.activity.SplashActivity;
import kr.ac.nexters.knock.nexters.example.activity.TutorialActivity;
import kr.ac.nexters.knock.tools.PreferenceManager;

/**
 * Created by KimCP on 15. 8. 9..
 */
public class MyGcmListenerService   extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private AlertDialog.Builder ab;
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
        String pphone = data.getString("myphone");


        Log.i("data", title + "," + message + "," + sender + "," + type + "," + name);

        // GCM으로 받은 메세지를 디바이스에 알려주는 sendNotification()을 호출한다.
        if(type.equals("auth")){
            //인증요청을 받았다 -> 타이틀, 메시지, 요청을 보낸사람 UID, 이름,pushid
            authNotification(title, message, sender,pushid,name, pphone);
            Log.i("Noti", "auth");
        }else if(type.equals("accept")) {
            //인증승인을 받았다 -> 타이틀, 메세지, 요청을 수락한사람의 uid와 pushid
            acceptNotification(title, message, sender, pushid, name);
            Log.i("Noti", "accept");
        }else if(type.equals("clear")) {
            clear();
        }else if(type.equals("img")){
            setImg(message);
        }
        else{
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

        if(PreferenceManager.getInstance().getWithVib()){
            Vibrator mVib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            long[] vibratePattern = {100, 100, 300};
            mVib.vibrate(300);
            mVib.vibrate(vibratePattern, -1);
        }

        Intent intent = new Intent(MyGcmListenerService.this, MainActivity.class);
        //intent에 data담아서 넘겨줌 -> 애니메이션 동작
        intent.putExtra("animation", "stopRipple");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String pname = PreferenceManager.getInstance().getPname();
        String myname = PreferenceManager.getInstance().getUserName();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.shadow_icon)
                .setContentTitle("KNOCK")
                .setContentText(pname+"님이 "+myname+"님을 노크했어요!")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

    }

    //A가 요청을 보내면 B에게 이게 뜸 그럼 sender는 puid pushid는 ppushid
    private void authNotification(String title, String message, final String sender, final String pushid, String name, String pphone){
        Log.i("auth ppushid", pushid);
        PreferenceManager.getInstance().setPuid(sender);
        PreferenceManager.getInstance().setPpushId(pushid);
        PreferenceManager.getInstance().setPphoneNum(pphone);
        PreferenceManager.getInstance().setPname(name);

        Log.i("TTTT", PreferenceManager.getInstance().getPphoneNum());

        Intent in = new Intent(MyGcmListenerService.this, AuthReqActivity.class);
        in.putExtra("pname",name);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, in,PendingIntent.FLAG_ONE_SHOT);

        try{
            pendingIntent.send();
        }catch (Exception e) {

        }
    }

    //상대방이 요청을 수락했다는 메시지를 받음
    private void acceptNotification(String title,String message, final String sender, final String pushid, String name){
        //타이틀, 메세지, 요청을 수락한사람의 uid와 pushid

        Log.i("GCM accept ppushid", pushid);

        PreferenceManager.getInstance().setPuid(sender);
        PreferenceManager.getInstance().setPpushId(pushid);
        PreferenceManager.getInstance().setPname(name);

        Intent in = new Intent(MyGcmListenerService.this, AcceptAuthActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, in,PendingIntent.FLAG_ONE_SHOT);

        try{
            pendingIntent.send();
        }catch (Exception e) {

        }

       /* AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(title);
        ab.setMessage(message);
        ab.setCancelable(false);
        ab.setIcon(getResources().getDrawable(R.mipmap.ic_launcher));

        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                PreferenceManager.getInstance().setPpushId(pushid);
                PreferenceManager.getInstance().setPuid(sender);
                dialog.dismiss();

                Intent in = new Intent(MyGcmListenerService.this, TutorialActivity.class);
                startActivity(in);
                PreferenceManager.getInstance().setFirst("regok");
            }
        });
        ab.show();*/
    }

    public void clear() {
        PreferenceManager.getInstance().clear();
        PreferenceManager.getInstance().setFirst("");
        Intent in = new Intent(MyGcmListenerService.this, SplashActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, in, PendingIntent.FLAG_ONE_SHOT);

        try{
            pendingIntent.send();
        }catch (Exception e) {

        }
    }


    public void setImg(String img) {
        PreferenceManager.getInstance().setPimg(img);
        Intent bi = new Intent();
        bi.setAction(MainActivity.NOTIFY_ACTIVITY_ACTION);
        sendBroadcast(bi);
    }

    private void authAccept(){
        //승인을 수락함
        NetworkModel.getInstance().authAccept(new NetworkModel.OnNetworkResultListener<IsSuccess>() {
            @Override
            public void onResult(IsSuccess result) {

            }

            @Override
            public void onFail(int code) {
            }
        });
    }
}
