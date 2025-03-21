package com.atb.app.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;

import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.atb.app.R;
import com.atb.app.activities.MainActivity;
import com.atb.app.activities.SplashActivity;
import com.atb.app.commons.Commons;
import com.atb.app.preference.PrefConst;
import com.atb.app.preference.Preference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static com.atb.app.application.AppController.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
	public static final String FCM_PARAM = "picture";
	private static final String CHANNEL_NAME = "FCM";
	private static final String CHANNEL_DESC = "Firebase Cloud Messaging";
	private int numMessages = 0;

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		super.onMessageReceived(remoteMessage);
		RemoteMessage.Notification notification = remoteMessage.getNotification();
		Map<String, String> data = remoteMessage.getData();
		Log.i(TAG, "From: " + data.toString());
		sendNotification(notification, data);
	}

	private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data) {
		Bundle bundle = new Bundle();
		bundle.putString("type", data.get("type"));
		bundle.putString("related_id", data.get("related_id"));
		if(data.get("senderId")!= null && data.get("senderId").equals(Commons.senderID))return;
		Intent intent;
		if(Commons.g_mainActivity == null) {
			intent = new Intent(this, SplashActivity.class);
			intent.putExtra("data", bundle);
		}else {
			intent = new Intent(this, MainActivity.class);
			intent.putExtra("data", bundle);
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
				.setContentTitle(notification.getTitle())
				.setContentText(notification.getBody())
				.setAutoCancel(true)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				//.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.win))
				.setContentIntent(pendingIntent)
				.setContentInfo("Hello")
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
				.setColor(getResources().getColor(R.color.colorAccent))
				.setLights(Color.RED, 1000, 300)
				.setDefaults(Notification.DEFAULT_VIBRATE)
				.setNumber(++numMessages)
				.setSmallIcon(R.drawable.notification);

		try {
			String picture = data.get(FCM_PARAM);
			if (picture != null && !"".equals(picture)) {
				URL url = new URL(picture);
				Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				notificationBuilder.setStyle(
						new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(notification.getBody())
				);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					getString(R.string.notification_channel_id), CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
			);
			channel.setDescription(CHANNEL_DESC);
			channel.setShowBadge(true);
			channel.canShowBadge();
			channel.enableLights(true);
			channel.setLightColor(Color.RED);
			channel.enableVibration(true);
			channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});

			assert notificationManager != null;
			notificationManager.createNotificationChannel(channel);
		}

		assert notificationManager != null;
		notificationManager.notify(0, notificationBuilder.build());
	}
}