package com.yi4all.makehabits.db;

import java.util.Date;
import java.util.List;

public interface IDBService {

	/******For Goals List*******/
	//get today's ongoing goals with today's execution result
	public List<GoalModel> getTodayGoals();
	//get ongoing goals of one day, which day can't be after today
	public List<GoalModel> getGoalsByDate(Date date);
	//get all of goals won or failed
	public List<GoalModel> getGoalsByResult(boolean flag);
	
	/*****For Graph Statistics ******/
	//get amounts of goals won of one day, which day can't be after today
	public Integer getGoalsDoneResultByDate(Date date);
	//get amounts of goals failed of one day, which day can't be after today
	public Integer getGoalsFailedResultByDate(Date date);
	//get a series of numbers which is amount of goals won of days in a period, which day can't be after today
	public List<String[]> getGoalsWonResultByDateRange(Date begin, Date end);
	
	/******For Goals Results ******/
	//set execution for the goal on one day
	public boolean setGoalResult(GoalModel gm, Date date, boolean result);
	//start over the goal
	public boolean startOverGoal(GoalModel gm, Date date);
	//give up the goal
	public boolean giveUpGoal(GoalModel gm, Date date);
	//get all of results by goal
	public List<GoalCheckResultModel> getGoalResults(GoalModel gm);
	
}
