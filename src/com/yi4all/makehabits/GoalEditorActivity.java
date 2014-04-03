package com.yi4all.makehabits;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.yi4all.makehabits.R;
import com.yi4all.makehabits.db.GoalDBOpenHelper;
import com.yi4all.makehabits.db.GoalModel;
import com.yi4all.makehabits.reminder.ReminderManager;
import com.yi4all.makehabits.twitter.DialogError;
import com.yi4all.makehabits.twitter.Twitter;
import com.yi4all.makehabits.twitter.TwitterError;
import com.yi4all.makehabits.twitter.Twitter.DialogListener;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class GoalEditorActivity extends Activity {

	private GoalModel sm;

	private GoalDBOpenHelper dbHelper = null;

	private String alarmTime = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.goal_editor);

		Button saveBtn = (Button) findViewById(R.id.okBtn);
		saveBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveGoal();

			}
		});

		Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// quit
				finish();

			}
		});

		EditText habitTxt = (EditText) findViewById(R.id.habbitTxt);
		SeekBar durationSeekBar = (SeekBar) findViewById(R.id.durationSeekBar);
		final TextView durationWeeksTxt = (TextView) findViewById(R.id.durationWeeksTxt);
		// RadioButton todayRadio = (RadioButton) findViewById(R.id.todayRadio);
		// RadioButton tomorrowRadio = (RadioButton)
		// findViewById(R.id.tomorrowRadio);
		// RadioButton nextWeekRadio = (RadioButton)
		// findViewById(R.id.nextWeekRadio);
		RadioButton everydayRadio = (RadioButton) findViewById(R.id.everydayRadio);
		RadioButton daysOfWeekRadio = (RadioButton) findViewById(R.id.weekRadio);
		final LinearLayout weekdayLL = (LinearLayout) findViewById(R.id.weekdayLL);
		SeekBar conditionSeekBar = (SeekBar) findViewById(R.id.conditionSeekBar);
		final TextView conditionTxt = (TextView) findViewById(R.id.conditionTxt);
		final CheckBox setAlarmChk = (CheckBox) findViewById(R.id.setAlarmChk);
		CheckBox twitterChk = (CheckBox) findViewById(R.id.twitterChk);
		CheckBox facebookChk = (CheckBox) findViewById(R.id.facebookChk);

		TextView alarmTimeTxt = (TextView) findViewById(R.id.alarmTimeTxt);
		alarmTimeTxt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (setAlarmChk.isChecked()) {
					showTimePicker().show();
				}
			}
		});

		durationWeeksTxt.setText(getString(R.string.weeks, 4));
		conditionTxt.setText("75%");
		durationSeekBar.setProgress(3);
		conditionSeekBar.setProgress(25);

		durationSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						durationWeeksTxt.setText(getString(R.string.weeks,
								progress + 1));

					}
				});

		conditionSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						conditionTxt.setText((progress + 50) + "%");

					}
				});

		daysOfWeekRadio
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							weekdayLL.setVisibility(View.VISIBLE);
						} else {
							weekdayLL.setVisibility(View.GONE);
						}

					}
				});

		Intent intent = getIntent();
		sm = (GoalModel) intent.getSerializableExtra("GoalModel");

		if (sm == null) {
			sm = new GoalModel();
			// sm.setPlugin("com.omdasoft.browser.plugin.CheckAdPlugin");
		} else {
			habitTxt.setText(sm.getName());
			everydayRadio.setChecked(sm.getRepeatMode() == 0);
			everydayRadio.setEnabled(false);
			daysOfWeekRadio.setEnabled(false);

			if (sm.getRepeatMode() != 0) {
				weekdayLL.setVisibility(View.VISIBLE);
				setWeekDays(sm.getAvailableWeekDays());
			}
			// todayRadio.setChecked(sm.getRepeatMode()==0);
			// nextWeekRadio.setChecked(sm.getRepeatMode()!=0);
			durationSeekBar.setProgress(sm.getDuration() - 1);
			durationSeekBar.setEnabled(false);

			durationWeeksTxt
					.setText(getString(R.string.weeks, sm.getDuration()));
			conditionTxt.setText(sm.getWonPercentage() + "%");

			conditionSeekBar.setProgress(sm.getWonPercentage() - 50);

			LinearLayout startDateLL = (LinearLayout) findViewById(R.id.startDateLL);
			startDateLL.setVisibility(View.GONE);
			TextView startDateTxt = (TextView) findViewById(R.id.startDateTxt);
			startDateTxt.setVisibility(View.VISIBLE);
			startDateTxt.setText(DateUtils.formatDate(sm.getStartDate()));

			setAlarmChk.setChecked(sm.isSetAlarm());
			if (sm.isSetAlarm()) {
				alarmTime = sm.getAlarmTime();
				updateTimeButtonText();
			}
			twitterChk.setChecked(sm.isShareToTwitter());
			facebookChk.setChecked(sm.isShareToFB());
		}

		setAlarmChk.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					showTimePicker().show();
				}

			}
		});

		twitterChk.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					SocialUtils.authorizeTwitter(GoalEditorActivity.this);

				}

			}
		});

		facebookChk.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					SocialUtils.authorizeFB(GoalEditorActivity.this);
				}

			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		/*
		 * You'll need this in your class to release the helper when done.
		 */
		if (dbHelper != null) {
			OpenHelperManager.releaseHelper();
			dbHelper = null;
		}
	}

	private GoalDBOpenHelper getHelper() {
		if (dbHelper == null) {
			dbHelper = OpenHelperManager
					.getHelper(this, GoalDBOpenHelper.class);
		}
		return dbHelper;
	}

	private TimePickerDialog showTimePicker() {
		Calendar cal = DateUtils.getTimeForAlarm(alarmTime);
		if (cal == null) {
			cal = Calendar.getInstance();
		}

		TimePickerDialog timePicker = new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						alarmTime = DateUtils.formatHourMinute(DateUtils
								.calculateTimeForAlarm(hourOfDay, minute).getTime());
						updateTimeButtonText();
					}
				}, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
				true);

		return timePicker;
	}

	private void updateTimeButtonText() {
		CheckBox setAlarmChk = (CheckBox) findViewById(R.id.setAlarmChk);
		TextView alarmTimeTxt = (TextView) findViewById(R.id.alarmTimeTxt);
		if (setAlarmChk.isChecked()) {
			alarmTimeTxt.setText(getString(R.string.alarm_time_set, alarmTime));
		} else {
			alarmTimeTxt.setText(R.string.alarm_time_not_set);
		}
	}

	public void toggle(View v) {
		CheckedTextView cView = (CheckedTextView) v;
		cView.toggle();
	}

	private List<Integer> getWeekDays() {
		ArrayList<Integer> res = new ArrayList<Integer>();

		CheckedTextView monChkTxt = (CheckedTextView) findViewById(R.id.monChkTxt);
		CheckedTextView tueChkTxt = (CheckedTextView) findViewById(R.id.tueChkTxt);
		CheckedTextView wenChkTxt = (CheckedTextView) findViewById(R.id.wenChkTxt);
		CheckedTextView thuChkTxt = (CheckedTextView) findViewById(R.id.thuChkTxt);
		CheckedTextView friChkTxt = (CheckedTextView) findViewById(R.id.friChkTxt);
		CheckedTextView satChkTxt = (CheckedTextView) findViewById(R.id.satChkTxt);
		CheckedTextView sunChkTxt = (CheckedTextView) findViewById(R.id.sunChkTxt);

		if (monChkTxt.isChecked()) {
			res.add(Calendar.MONDAY);
		}
		if (tueChkTxt.isChecked()) {
			res.add(Calendar.TUESDAY);
		}
		if (wenChkTxt.isChecked()) {
			res.add(Calendar.WEDNESDAY);
		}
		if (thuChkTxt.isChecked()) {
			res.add(Calendar.THURSDAY);
		}
		if (friChkTxt.isChecked()) {
			res.add(Calendar.FRIDAY);
		}
		if (satChkTxt.isChecked()) {
			res.add(Calendar.SATURDAY);
		}
		if (sunChkTxt.isChecked()) {
			res.add(Calendar.SUNDAY);
		}

		return res;
	}

	private void setWeekDays(List<Integer> days) {
		if (days != null && days.size() > 0) {
			CheckedTextView monChkTxt = (CheckedTextView) findViewById(R.id.monChkTxt);
			CheckedTextView tueChkTxt = (CheckedTextView) findViewById(R.id.tueChkTxt);
			CheckedTextView wenChkTxt = (CheckedTextView) findViewById(R.id.wenChkTxt);
			CheckedTextView thuChkTxt = (CheckedTextView) findViewById(R.id.thuChkTxt);
			CheckedTextView friChkTxt = (CheckedTextView) findViewById(R.id.friChkTxt);
			CheckedTextView satChkTxt = (CheckedTextView) findViewById(R.id.satChkTxt);
			CheckedTextView sunChkTxt = (CheckedTextView) findViewById(R.id.sunChkTxt);

			for (int i : days) {
				switch (i) {
				case Calendar.MONDAY: {
					monChkTxt.setChecked(true);
					break;
				}
				case Calendar.TUESDAY: {
					tueChkTxt.setChecked(true);
					break;
				}
				case Calendar.WEDNESDAY: {
					wenChkTxt.setChecked(true);
					break;
				}
				case Calendar.THURSDAY: {
					thuChkTxt.setChecked(true);
					break;
				}
				case Calendar.FRIDAY: {
					friChkTxt.setChecked(true);
					break;
				}
				case Calendar.SATURDAY: {
					satChkTxt.setChecked(true);
					break;
				}
				case Calendar.SUNDAY: {
					sunChkTxt.setChecked(true);
					break;
				}
				}
			}
		}
	}

	private void saveGoal() {
		EditText habitTxt = (EditText) findViewById(R.id.habbitTxt);

		if (habitTxt.getText() == null || habitTxt.getText().length() == 0) {
			ActivityUtils.toastMsg(this, R.string.mustInputHint);
			return;
		}

		SeekBar durationSeekBar = (SeekBar) findViewById(R.id.durationSeekBar);
		RadioButton todayRadio = (RadioButton) findViewById(R.id.todayRadio);
		RadioButton tomorrowRadio = (RadioButton) findViewById(R.id.tomorrowRadio);
		RadioButton everydayRadio = (RadioButton) findViewById(R.id.everydayRadio);
		SeekBar conditionSeekBar = (SeekBar) findViewById(R.id.conditionSeekBar);
		CheckBox setAlarmChk = (CheckBox) findViewById(R.id.setAlarmChk);
		CheckBox twitterChk = (CheckBox) findViewById(R.id.twitterChk);
		CheckBox facebookChk = (CheckBox) findViewById(R.id.facebookChk);

		// save source
		sm.setName(habitTxt.getText().toString());
		sm.setDuration(durationSeekBar.getProgress() + 1);
		if (sm.getId() <= 0)
			if (todayRadio.isChecked()) {
				sm.setStartDate(new Date());
			} else if (tomorrowRadio.isChecked()) {
				sm.setStartDate(DateUtils.addDateDays(new Date(), 1));
			} else {
				sm.setStartDate(DateUtils.getDateOfNextWeek());
			}
		sm.setRepeatMode(everydayRadio.isChecked() ? 0 : 1);
		if (!everydayRadio.isChecked()) {
			List<Integer> weekdays = getWeekDays();

			if (weekdays.size() == 0) {
				ActivityUtils.toastMsg(this, R.string.mustInputWeekHint);
				return;
			}
			// set days of week
			sm.setAvailableWeekDays(weekdays);
		}

		sm.setWonPercentage(conditionSeekBar.getProgress() + 50);
		sm.setShareToFB(facebookChk.isChecked());
		sm.setShareToTwitter(twitterChk.isChecked());

		// calculate expiration date
		sm.setEndDate(DateUtils.addDateDays(sm.getStartDate(),
				sm.getDuration() * 7 - 1));

		// set alarm time
		sm.setSetAlarm(setAlarmChk.isChecked());
		if (setAlarmChk.isChecked()) {
			sm.setAlarmTime(alarmTime);
		}

		try {
			Dao<GoalModel, Integer> dao = getHelper().getGoalDAO();

			if (sm.getId() > 0) {
				// update source

				dao.update(sm);

			} else {
				// insert source
				dao.create(sm);
				getHelper().generateGoalCheckResults(sm);

			}

			ActivityUtils.toastMsg(this, getString(R.string.saveSuccess));

			// Only if goal has started, the alarm should be set or canceled
			if (new Date().after(sm.getStartDate())) {
				// set or cancel alarm
				ReminderManager reminderMgr = new ReminderManager(this);
				if (setAlarmChk.isChecked()) {
					// set new alarm
					reminderMgr.setReminder(sm.getId(),
							DateUtils.getTimeForAlarm(alarmTime));
				} else {
					// cancel alarm
					reminderMgr.cancelReminder(sm.getId());
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			ActivityUtils.toastMsg(this, R.string.saveFailed);
		}
		this.finish();
		return;

	}

	public String getUid() {
		return Settings.Secure.getString(getContentResolver(),
				Settings.Secure.ANDROID_ID);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		SocialUtils.facebook.authorizeCallback(requestCode, resultCode, data);
	}

}
