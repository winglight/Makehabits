package com.yi4all.makehabits.db;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "GOAL_CHECK_RESULT")
public class GoalCheckResultModel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7979746094376788843L;
	public final static String FIELD_GOAL="GOAL";
	public final static String FIELD_RESULT="RESULT";
	public final static String FIELD_HAPPEN_DATE="HAPPEN_DATE";
	public final static String FIELD_HAPPEN_WEEK="HAPPEN_WEEK";
	public final static String FIELD_HAPPEN_MONTH="HAPPEN_MONTH";
	public final static String FIELD_HAPPEN_YEAR="HAPPEN_YEAR";
	public final static String FIELD_SHARED_FB="SHARED_FB";
	public final static String FIELD_SHARED_TW="SHARED_TW";

	@DatabaseField(generatedId = true)
	private long id = -1;
	@DatabaseField(canBeNull = false, foreignAutoRefresh = true, foreign = true, columnName = FIELD_GOAL)
	private GoalModel goal;
	@DatabaseField(canBeNull = false, columnName = FIELD_HAPPEN_DATE)
	private String happenDate;
	@DatabaseField( columnName = FIELD_HAPPEN_WEEK)
	private String happenWeek;
	@DatabaseField(columnName = FIELD_HAPPEN_MONTH)
	private String happenMonth;
	@DatabaseField( columnName = FIELD_HAPPEN_YEAR)
	private String happenYear;
	@DatabaseField(canBeNull = false, columnName = FIELD_RESULT)
	private int result=0;
	@DatabaseField( columnName = FIELD_SHARED_FB)
	private boolean shared2FB;
	@DatabaseField( columnName = FIELD_SHARED_TW)
	private boolean shared2TW;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public boolean isResult() {
		return this.result==1;
	}
	public void setResult(boolean result) {
		this.result = result?1:0;
	}
	public GoalModel getGoal() {
		return goal;
	}
	public void setGoal(GoalModel goal) {
		this.goal = goal;
	}
	public String getHappenDate() {
		return happenDate;
	}
	public void setHappenDate(String happenDate) {
		this.happenDate = happenDate;
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
	
	
}
