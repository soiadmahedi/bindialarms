package com.soiadmahedi.bindialarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.AlarmClock;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AzanAlarmUtils {
	
	private static final String PREFS_NAME = "AzanAlarms";
	private static final String ALARM_DATA_KEY_PREFIX = "alarm_data_";
	private static final String TAG = "AzanAlarmUtils";
	
	public static void scheduleAppAlarm(Context context, int hour, int minute, String title, String description, boolean isRepeating, int requestCode) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		Intent intent = new Intent(context, AzanReceiver.class);
		intent.putExtra("title", title);
		intent.putExtra("description", description);
		intent.putExtra("isRepeating", isRepeating);
		intent.putExtra("requestCode", requestCode);
		
		int flags = PendingIntent.FLAG_UPDATE_CURRENT;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			flags |= PendingIntent.FLAG_IMMUTABLE;
		}
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		if (isRepeating) {
			alarmManager.setInexactRepeating(
			AlarmManager.RTC_WAKEUP,
			calendar.getTimeInMillis(),
			AlarmManager.INTERVAL_DAY,
			pendingIntent
			);
		} else {
			alarmManager.setExactAndAllowWhileIdle(
			AlarmManager.RTC_WAKEUP,
			calendar.getTimeInMillis(),
			pendingIntent
			);
		}
		
		saveAlarmData(context, requestCode, hour, minute, title, description, isRepeating);
		
		Log.d(TAG, "App alarm set: RequestCode=" + requestCode + ", Time=" + hour + ":" + minute + ", Repeating=" + isRepeating);
	}
	
	public static void scheduleAlarmWithSystemClock(Context context, int hour, int minute, String title, String description) {
		Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
		intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
		intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
		intent.putExtra(AlarmClock.EXTRA_MESSAGE, title);
		intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		String finalMessage = title;
		if (description != null && !description.isEmpty()) {
			finalMessage += " - " + description;
		}
		intent.putExtra(AlarmClock.EXTRA_MESSAGE, finalMessage);
		
		try {
			context.startActivity(intent);
			Log.d(TAG, "Redirecting to system clock to set alarm for " + hour + ":" + minute);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Could not find an app to handle ACTION_SET_ALARM");
			// এখানে একটি Toast Message দেখানো যেতে পারে যে কোনো ক্লক অ্যাপ পাওয়া যায়নি।
		}
	}
	
	public static void cancelAppAlarm(Context context, int requestCode) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AzanReceiver.class);
		
		int flags = PendingIntent.FLAG_UPDATE_CURRENT;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			flags |= PendingIntent.FLAG_IMMUTABLE;
		}
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);
		
		alarmManager.cancel(pendingIntent);
		removeAlarmData(context, requestCode);
		
		Log.d(TAG, "Alarm cancelled: RequestCode=" + requestCode);
	}
	
	public static int generateUniqueRequestCode() {
		return (int) System.currentTimeMillis();
	}
	
	public static ArrayList<HashMap<String, Object>> getActiveAlarms(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		ArrayList<HashMap<String, Object>> alarmList = new ArrayList<>();
		Map<String, ?> allEntries = prefs.getAll();
		
		for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(ALARM_DATA_KEY_PREFIX)) {
				try {
					JSONObject json = new JSONObject((String) entry.getValue());
					HashMap<String, Object> map = new HashMap<>();
					map.put("requestCode", json.getInt("requestCode"));
					map.put("hour", json.getInt("hour"));
					map.put("minute", json.getInt("minute"));
					map.put("title", json.getString("title"));
					map.put("description", json.getString("description"));
					map.put("isRepeating", json.getBoolean("isRepeating"));
					alarmList.add(map);
				} catch (Exception e) {
					Log.e(TAG, "Failed to parse alarm data for key: " + key, e);
				}
			}
		}
		return alarmList;
	}
	
	private static void saveAlarmData(Context context, int requestCode, int hour, int minute, String title, String description, boolean isRepeating) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
		try {
			JSONObject json = new JSONObject();
			json.put("requestCode", requestCode);
			json.put("hour", hour);
			json.put("minute", minute);
			json.put("title", title);
			json.put("description", description != null ? description : "");
			json.put("isRepeating", isRepeating);
			
			String key = ALARM_DATA_KEY_PREFIX + requestCode;
			editor.putString(key, json.toString());
			editor.apply();
		} catch (Exception e) {
			Log.e(TAG, "Failed to save alarm data", e);
		}
	}
	
	private static void removeAlarmData(Context context, int requestCode) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		String key = ALARM_DATA_KEY_PREFIX + requestCode;
		editor.remove(key);
		editor.apply();
	}
	
}
