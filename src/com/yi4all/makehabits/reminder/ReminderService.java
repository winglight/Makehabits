package com.yi4all.makehabits.reminder;

import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.yi4all.makehabits.MainActivity;
import com.yi4all.makehabits.R;
import com.yi4all.makehabits.db.GoalDBOpenHelper;
import com.yi4all.makehabits.db.GoalModel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ReminderService extends WakeReminderIntentService {

	public ReminderService() {
		super("ReminderService");
	}

	@Override
	void doReminderWork(Intent intent) {
		Log.d("ReminderService", "Doing work.");
		long rowId = intent.getExtras().getLong(GoalModel.FIELD_ID);
		
		GoalDBOpenHelper dbHelper = OpenHelperManager.getHelper(this,
				GoalDBOpenHelper.class);
		
		Dao<GoalModel, Integer> dao;
		String name = "";
		try {
			dao = dbHelper.getGoalDAO();
			GoalModel gm = dao.queryForId((int)rowId);
			
			name = gm.getName();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return;
		}

		NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.putExtra(GoalModel.FIELD_ID, rowId);

		PendingIntent pi = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_ONE_SHOT);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			Notification.Builder builder = new Notification.Builder(this);
			builder.setTicker(getString(R.string.notify_check_goal_result));
			builder.setContentTitle(getString(R.string.notify_goal_name_title, name));
			builder.setSmallIcon(R.drawable.icon);
			builder.setContentText(getString(R.string.notify_goal_check_alarm));
			builder.setContentIntent(pi);
			builder.setDefaults(Notification.DEFAULT_SOUND
					| Notification.DEFAULT_LIGHTS);
			builder.setAutoCancel(true);

			// An issue could occur if user ever enters over 2,147,483,647
			// tasks. (Max int value).
			// I highly doubt this will ever happen. But is good to note.
			int id = (int) ((long) rowId);
			mgr.notify(id, builder.getNotification());
		} else {
			Notification note = new Notification(
					R.drawable.icon,
					getString(R.string.notify_check_goal_result),
					System.currentTimeMillis());
			note.setLatestEventInfo(this,
					getString(R.string.notify_goal_name_title, name),
					getString(R.string.notify_goal_check_alarm), pi);
			note.defaults |= Notification.DEFAULT_SOUND;
			note.flags |= Notification.FLAG_AUTO_CANCEL;

			// An issue could occur if user ever enters over 2,147,483,647
			// tasks. (Max int value).
			// I highly doubt this will ever happen. But is good to note.
			int id = (int) ((long) rowId);
			mgr.notify(id, note);
		}

	}
}
