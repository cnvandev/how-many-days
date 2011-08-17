package ca.uwaterloo.eng.cnvandev.howmanydays;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class IRSAlarms extends ListActivity {
	private AlarmManager alarms = null;
	private Calendar IRS = null;
	private SharedPreferences settings = null;
	private Intent wakeIntent = null;
	public int lastID = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		settings = getPreferences(Context.MODE_PRIVATE);
		alarms = (AlarmManager) getSystemService(ALARM_SERVICE);
		IRS = Calendar.getInstance();
		
		lastID = settings.getInt("lastAlarm", 0);
		
		wakeIntent = new Intent(IRSAlarms.this, AlarmReceiver.class);
	}
	
	public void setAlarm(int days) {
		// Determine the day we're looking for.
		Calendar alarmTime = (Calendar) IRS.clone();
		alarmTime.roll(Calendar.DAY_OF_YEAR, -days);
		
		// Generate an ID for the alarm and store it.
		int ID = lastID;
		
		PendingIntent sender = PendingIntent.getBroadcast(this, ID, wakeIntent, 0);
		alarms.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), sender);
		
		lastID++;
	}
	
	public void removeAlarm(int alarmID) {
		PendingIntent alarmIntent = PendingIntent.getBroadcast(this, alarmID, wakeIntent, 0);
		alarms.cancel(alarmIntent);
	}
}
