package com.yi4all.makehabits;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.yi4all.makehabits.R;
import com.yi4all.makehabits.db.GoalDBOpenHelper;
import com.yi4all.makehabits.db.GoalModel;
import com.yi4all.makehabits.reminder.ReminderManager;

class MyListener implements OnClickListener, OnCheckedChangeListener{

	private GoalModel goal;
	private int mode = 0;
	private Activity activity;
	
	MyListener(GoalModel goal, Activity activity, int mode){
		this.goal = goal;
		this.activity = activity;
		this.mode = mode;
	}
	

	@Override
	public void onClick(View v) {
		switch(mode){
		case 0:{
			//pop up history view
				HistoryDialog mDialog = new HistoryDialog((MainActivity) this.activity, goal);

				    mDialog.show();
			break;
		}
		case 1:{
			//pop up goal editor screen
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClass(this.activity,
					GoalEditorActivity.class);
			intent.putExtra("GoalModel", this.goal);
			this.activity.startActivity(intent);
			break;
		}
		case 3:{
			new AlertDialog.Builder(this.activity)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.confirm_startover_title)
			.setMessage(R.string.confirm_startover_content)
			.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();

							//set goal start over
							((MainActivity) activity).startOverGoal(goal, new Date());
						}

					}).setNegativeButton(android.R.string.cancel, null).show();
			
			break;
		}
		case 4:{
			new AlertDialog.Builder(this.activity)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.confirm_giveup_title)
			.setMessage(R.string.confirm_giveup_content)
			.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();

							//set goal give up
							((MainActivity) activity).giveUpGoal(goal, new Date());
						}

					}).setNegativeButton(android.R.string.cancel, null).show();
			
			break;
		}
			
		}
		
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			
				if(isChecked){
				showTimePicker(buttonView).show();
				}else{
					ReminderManager reminderMgr = new ReminderManager(buttonView.getContext());
					reminderMgr.cancelReminder(goal.getId());
				}
		
	}
	
	private TimePickerDialog showTimePicker(final CompoundButton buttonView) {
			Calendar cal = Calendar.getInstance();

		TimePickerDialog timePicker = new TimePickerDialog(buttonView.getContext(),
				new TimePickerDialog.OnTimeSetListener() {

			
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						String alarmTime = DateUtils.formatHourMinute(DateUtils.getTimeForAlarm(hourOfDay, minute).getTime());
						buttonView.setText(alarmTime);
						
						goal.setSetAlarm(true);
						goal.setAlarmTime(alarmTime);
						
						try {
							GoalDBOpenHelper dbHelper = OpenHelperManager
									.getHelper(buttonView.getContext(), GoalDBOpenHelper.class);
							Dao<GoalModel, Integer> dao = dbHelper.getGoalDAO();
							dao.update(goal);
							
							ReminderManager reminderMgr = new ReminderManager(buttonView.getContext());
							reminderMgr.setReminder(goal.getId(), DateUtils.getTimeForAlarm(alarmTime));
							
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
				true);

		return timePicker;
	}
	
}