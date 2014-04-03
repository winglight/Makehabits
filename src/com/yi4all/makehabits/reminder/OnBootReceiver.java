package com.yi4all.makehabits.reminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.yi4all.makehabits.DateUtils;
import com.yi4all.makehabits.db.GoalDBOpenHelper;
import com.yi4all.makehabits.db.GoalModel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class OnBootReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = ComponentInfo.class.getCanonicalName();

	@Override
	public void onReceive(Context context, Intent intent) {

		ReminderManager reminderMgr = new ReminderManager(context);

		GoalDBOpenHelper dbHelper = OpenHelperManager.getHelper(context,
				GoalDBOpenHelper.class);

		List<GoalModel> list = dbHelper.getGoalsByDate(new Date());

		for (GoalModel gm : list) {
			if (gm.isSetAlarm()) {
				Log.d(LOG_TAG, "set alarm time:" + DateUtils.getTimeForAlarm(gm.getAlarmTime()));
				reminderMgr.setReminder(gm.getId(), DateUtils.getTimeForAlarm(gm.getAlarmTime()));
			}

		}

		dbHelper.close();
	}
}
