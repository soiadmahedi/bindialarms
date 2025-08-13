package com.soiadmahedi.bindialarms;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class PermissionUtils {
	
	public static boolean hasExactAlarmPermission(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			return alarmManager.canScheduleExactAlarms();
		}
		return true; 
	}
	
	public static void requestExactAlarmPermission(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
			intent.setData(Uri.parse("package:" + context.getPackageName()));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}
    
	public static void requestIgnoreBatteryOptimizationsIfNeeded(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			String packageName = context.getPackageName();
			
			if (!pm.isIgnoringBatteryOptimizations(packageName)) {
				Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
				intent.setData(Uri.parse("package:" + packageName));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				try {
					context.startActivity(intent);
					Toast.makeText(context, "Please allow battery optimization exclusion.", Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(context, "Unable to open battery optimization settings.", Toast.LENGTH_SHORT).show();
				}
			} else {
				Log.d("BatteryOpt", "Already ignoring battery optimizations.");
			}
		}
	}
	
	public static boolean isIgnoringBatteryOptimizations(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			return pm.isIgnoringBatteryOptimizations(context.getPackageName());
		}
		return true;
	}
}
