package com.yi4all.makehabits.reminder;

import java.util.Calendar;

import com.yi4all.makehabits.db.GoalModel;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ReminderManager {

	private Context mContext; 
	private AlarmManager mAlarmManager;
	
	public ReminderManager(Context context) {
		mContext = context; 
		mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}
	
	public void setReminder(long taskId, Calendar when) {
		
		if(when == null) return ;
		
        Intent i = new Intent(mContext, OnAlarmReceiver.class);
        i.putExtra(GoalModel.FIELD_ID, taskId); 

        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT); 
        
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pi);
	}
	
	public void cancelReminder(long taskId){
		Intent i = new Intent(mContext, OnAlarmReceiver.class);
        i.putExtra(GoalModel.FIELD_ID, taskId); 

        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT); 
        
        mAlarmManager.cancel(pi);
	}
}
