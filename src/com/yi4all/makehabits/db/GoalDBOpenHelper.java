package com.yi4all.makehabits.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.yi4all.makehabits.DateUtils;
import com.yi4all.makehabits.MainActivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GoalDBOpenHelper extends OrmLiteSqliteOpenHelper{
	
	public  static final int DATABASE_VERSION = 3;
	public static final String DATABASE_NAME = "goal";
	
	private Dao<GoalModel, Integer> goalDao;
	private Dao<GoalCheckResultModel, Integer> goalCheckDao;
	
	private MainActivity activity = null;

    public GoalDBOpenHelper(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void setActivity(MainActivity activity) {
		this.activity = activity;
	}



	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(GoalDBOpenHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, GoalModel.class);
			TableUtils.createTable(connectionSource, GoalCheckResultModel.class);

			// here we try inserting data in the on-create as a test
			Dao<GoalModel, Integer> dao = getGoalDAO();
			// create some entries in the onCreate
			 GoalModel simple = new GoalModel();
			 simple.setName("Insist check habits before sleep everyday");
			 simple.setDuration(4);
			 simple.setStartDate(new Date());
			 simple.setEndDate(DateUtils.addDateDays(simple.getStartDate(), simple.getDuration()*7));
			 simple.setRepeatMode(0);
			 simple.setWonPercentage(80);
			 simple.setShareToFB(true);
			 simple.setShareToTwitter(true);
			 simple.setSetAlarm(true);
			 simple.setAlarmTime("22:00");
			 
			dao.create(simple);
			
			//generate results
			 generateGoalCheckResults(simple);
			 
			 if(this.activity != null){
				 this.activity.refreshList();
			 }
			 
			Log.i(GoalDBOpenHelper.class.getName(), "created new entries in onCreate: " + simple.getName());
		} catch (SQLException e) {
			Log.e(GoalDBOpenHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(GoalDBOpenHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, GoalModel.class, true);
			TableUtils.dropTable(connectionSource, GoalCheckResultModel.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(GoalDBOpenHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

    public Dao<GoalModel, Integer> getGoalDAO() throws SQLException{
    	if(goalDao == null){
    		goalDao = getDao(GoalModel.class);
    	}
    	return goalDao;
    }
    
    public Dao<GoalCheckResultModel, Integer> getGoalCheckDAO() throws SQLException{
    	if(goalCheckDao == null){
    		goalCheckDao = getDao(GoalCheckResultModel.class);
    	}
    	return goalCheckDao;
    }
    
    public void generateGoalCheckResults(GoalModel goal){
    	try {
    	for(int i=0; i < goal.getTotalTimes(); i++){
    		GoalCheckResultModel gcrm = new GoalCheckResultModel();
    		gcrm.setGoal(goal);
    		gcrm.setHappenDate(DateUtils.defaultFormat((DateUtils.addDateDays(goal.getStartDate() , i))));
    		gcrm.setResult(false);
    		if(goal.getRepeatMode() != 0){
    			int weekday = DateUtils.getWeekday(DateUtils.defaultParse((gcrm.getHappenDate())));
    			for(int day : goal.getAvailableWeekDays()){
    				if(weekday == day){
    					getGoalCheckDAO().create(gcrm);
    					break;
    				}
    			}
    		}else{
    			getGoalCheckDAO().create(gcrm);
    		}
    	}
    	} catch (SQLException e) {
			Log.e(GoalDBOpenHelper.class.getName(), "Can't generate GoalCheckResults ", e);
			throw new RuntimeException(e);
		}
    }
    
    public List<GoalModel> getGoalsByDate(Date date) {
		try {
			Dao<GoalModel, Integer> dao = getGoalDAO();
			QueryBuilder<GoalModel, Integer> queryBuilder = dao.queryBuilder();
			queryBuilder.orderBy(GoalModel.FIELD_START_DATE, false);

			Where<GoalModel, Integer> where = queryBuilder.where();

			where.ge(GoalModel.FIELD_END_DATE,
					DateUtils.getDateWithoutTime(date));
			where.and();
			where.lt(GoalModel.FIELD_START_DATE, DateUtils.getDateWithoutTime(DateUtils.addDateDays(date, 1)));
			where.and();
			where.eq(GoalModel.FIELD_STATUS, 0);

			List<GoalModel> list = dao.query(queryBuilder.prepare());
			List<GoalModel> list2 = new ArrayList<GoalModel>();
			for (GoalModel gm : list) {
				if (gm.getRepeatMode() == 0 || gm.isShownWeekday(date)) {
					list2.add(gm);
				}
			}
			Dao<GoalCheckResultModel, Integer> dao2 = getGoalCheckDAO();
			for (GoalModel gm : list2) {
				QueryBuilder<GoalCheckResultModel, Integer> queryBuilder2 = dao2
						.queryBuilder();
				Where<GoalCheckResultModel, Integer> where2 = queryBuilder2
						.where();
				where2.eq(GoalCheckResultModel.FIELD_GOAL, gm);
				where2.and();
				where2.eq(GoalCheckResultModel.FIELD_HAPPEN_DATE,
						DateUtils.defaultFormat(date));

				GoalCheckResultModel gcrm = dao2.queryForFirst(queryBuilder2
						.prepare());
				if (gcrm != null) {
					gm.setCurrentResult(gcrm.isResult());
				}
			}

			return list2;
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<GoalModel>();
		}
	}
    
    @Override
	public void close() {
		super.close();
		goalDao = null;
		goalCheckDao = null;
	}
}
