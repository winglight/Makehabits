package com.yi4all.makehabits.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.yi4all.makehabits.DateUtils;

@DatabaseTable(tableName = "GOAL")
public class GoalModel implements Serializable {

	public static final String FIELD_START_DATE = "START_DATE";
	public static final String FIELD_END_DATE = "END_DATE";
	public static final String FIELD_STATUS = "STATUS";
	public static final String FIELD_NAME = "NAME";
	public final static String FIELD_SHARED_FB="SHARED_FB";
	public final static String FIELD_SHARED_TW="SHARED_TW";
	public final static String FIELD_ID="id";
	public final static String FIELD_SET_ALARM="SET_ALARM";
	public final static String FIELD_ALARM_TIME="ALARM_TIME";

	@DatabaseField(generatedId = true)
	private long id = -1;
	@DatabaseField(index = true, columnName = FIELD_NAME)
	private String name;
	@DatabaseField
	private int duration; // weeks
	@DatabaseField(index = true, columnName = FIELD_START_DATE)
	private Date startDate;
	@DatabaseField(index = true, columnName = FIELD_END_DATE)
	private Date endDate;
	@DatabaseField
	private int repeatMode; // 0 - every day ; 1 - every week
	@DatabaseField
	private String weekdays;
	// converted weekdays
	private List<Integer> availableWeekDays; // an array of numbers
											// corresponding to seven days of a week
											// 	like, MON,TUE
	@DatabaseField
	private int wonPercentage; // done parts of the goal should be over this
								// percent to win
	@DatabaseField
	private int doneTimes; // how many times success to do the habit

	// private int wonIndicatorTimes; // it equals total times multiply
	// wonPercentage
	
	@DatabaseField( columnName = FIELD_SET_ALARM)
	private boolean setAlarm;
	@DatabaseField( columnName = FIELD_ALARM_TIME)
	private String alarmTime;

	@DatabaseField
	private boolean shareToFB;
	@DatabaseField
	private boolean shareToTwitter;
	@DatabaseField( columnName = FIELD_SHARED_FB)
	private boolean shared2FB;
	@DatabaseField( columnName = FIELD_SHARED_TW)
	private boolean shared2TW;

	@DatabaseField(columnName = FIELD_STATUS)
	private int finalResult; // 0 - ongoing ; 1 - won ; 2 - failed ; 3 - given up

	@ForeignCollectionField(eager = false)
	private Collection<GoalCheckResultModel> resultList;

	private boolean currentResult; // Non-DB field, indicate if it's done today

	public GoalModel() {

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Collection<GoalCheckResultModel> getResultList() {
		return resultList;
	}

	public void setResultList(Collection<GoalCheckResultModel> resultList) {
		this.resultList = resultList;
	}

	public int getRepeatMode() {
		return repeatMode;
	}

	public void setRepeatMode(int repeatMode) {
		this.repeatMode = repeatMode;
	}

	public boolean isSetAlarm() {
		return setAlarm;
	}

	public void setSetAlarm(boolean setAlarm) {
		this.setAlarm = setAlarm;
	}

	public String getAlarmTime() {
		return alarmTime;
	}

	public void setAlarmTime(String alarmTime) {
		this.alarmTime = alarmTime;
	}

	public List<Integer> getAvailableWeekDays() {
		if (availableWeekDays == null) {
			availableWeekDays = new ArrayList<Integer>();
			availableWeekDays.clear();
			if (this.weekdays != null) {
				String[] days = this.weekdays.split(",");
				for (String day : days) {
					int week = Integer.valueOf(day);
					availableWeekDays.add(week);
				}
			}
		}
		return availableWeekDays;
	}

	public void setAvailableWeekDays(List<Integer> availableWeekDays) {
		if(availableWeekDays == null) return;
		
		this.availableWeekDays = availableWeekDays;
		this.weekdays = "";
		for(int i : this.availableWeekDays){
			this.weekdays += String.valueOf(i) + ",";
		}
		if(this.weekdays.endsWith(",")){
			this.weekdays = this.weekdays.substring(0, this.weekdays.length()-1);
		}
	}

	public int getWonPercentage() {
		return wonPercentage;
	}

	public void setWonPercentage(int wonPercentage) {
		this.wonPercentage = wonPercentage;
	}

	public int getDoneTimes() {
		return doneTimes;
	}

	public void setDoneTimes(int doneTimes) {
		this.doneTimes = doneTimes;
	}

	public boolean isShareToFB() {
		return shareToFB;
	}

	public void setShareToFB(boolean shareToFB) {
		this.shareToFB = shareToFB;
	}

	public boolean isShareToTwitter() {
		return shareToTwitter;
	}

	public void setShareToTwitter(boolean shareToTwitter) {
		this.shareToTwitter = shareToTwitter;
	}

	public int getFinalResult() {
		return finalResult;
	}

	public void setFinalResult(int finalResult) {
		this.finalResult = finalResult;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean isCurrentResult() {
		return currentResult;
	}

	public void setCurrentResult(boolean currentResult) {
		this.currentResult = currentResult;
	}

	public boolean isShared2FB() {
		return shared2FB;
	}

	public void setShared2FB(boolean shared2fb) {
		shared2FB = shared2fb;
	}

	public boolean isShared2TW() {
		return shared2TW;
	}

	public void setShared2TW(boolean shared2tw) {
		shared2TW = shared2tw;
	}

	public int getWonIndicatorTimes() {
		return wonPercentage * getTotalTimes();
	}

	public int getCurPercentage() {
		return doneTimes * 100 / getTotalTimes();
	}

	public int getTotalTimes() {
		if (repeatMode == 0) {
			return 7 * duration;
		} else {
				int timesInWeek = getAvailableWeekDays().size();
			return timesInWeek * duration;
		}
	}

	public boolean isShownWeekday(Date date) {
		int weekday = DateUtils.getWeekday(date);
		if (this.repeatMode != 0 && this.availableWeekDays != null) {
			for (int i : this.availableWeekDays) {
				if (weekday == i) {
					return true;
				}
			}
			return false;
		} else {
			return true;
		}
	}

}
