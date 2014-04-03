package com.yi4all.makehabits;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.adapters.AdWhirlAdapter;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.yi4all.makehabits.R;
import com.yi4all.makehabits.db.GoalCheckResultModel;
import com.yi4all.makehabits.db.GoalDBOpenHelper;
import com.yi4all.makehabits.db.GoalModel;
import com.yi4all.makehabits.db.IDBService;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends SherlockFragmentActivity implements
		ActionBar.TabListener, ViewPager.OnPageChangeListener, IDBService,
		AdWhirlInterface {

	private final static String LOG_TAG = "MainActivity";

	private final static int SHARE_MODE_DONE = 1;
	private final static int SHARE_MODE_MISSED = 2;
	private final static int SHARE_MODE_WON = 3;
	private final static int SHARE_MODE_FAILED = 4;
	private final static int SHARE_MODE_GIVEUP = 5;

	public final static int MSG_MODE_PREPARE_TWITTER = 1;
	public final static int MSG_MODE_PREPARE_FACEBOOK = 2;
	public final static int MSG_MODE_AUTHORIZED_TWITTER = 3;
	public final static int MSG_MODE_AUTHORIZED_FACEBOOK = 4;

	private ViewPager mViewPager;
	private Menu mOptionsMenu;

	private OngoingFragment mOngoingFragment;
	private DoneFragment mWonFragment;
	private DoneFragment mFailedFragment;
	
	private LinearLayout graphLL;

	private GoalDBOpenHelper dbHelper = null;

	private boolean isPreparedTwitter;
	private boolean isPreparedFacebook;

	private Handler mPrepareShareHandler = new Handler() {

		private boolean shownTwitterDialog;
		private boolean shownFacebookDialog;

		@Override
		public void handleMessage(Message msg) {
			Log.d(LOG_TAG,
					String.format("Handler.handleMessage(): msg=%s", msg));
			// This is where the main activity thread receives messages
			// Put your handling of incoming messages posted by other threads
			// here
			int mode = msg.arg1;
			if (mode == MSG_MODE_PREPARE_TWITTER && (!shownTwitterDialog)) {
				// open twitter prepare dailog
				new AlertDialog.Builder(MainActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.confirm_title)
						.setMessage(R.string.confirm_set_fb)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();

										SocialUtils
												.authorizeFB(MainActivity.this);
									}

								}).setNegativeButton(android.R.string.cancel, null).show();
				shownTwitterDialog = true;
			} else if (mode == MSG_MODE_PREPARE_FACEBOOK
					&& (!shownFacebookDialog)) {
				// open facebook prepare dailog
				new AlertDialog.Builder(MainActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.confirm_title)
						.setMessage(R.string.confirm_set_tw)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();

										SocialUtils
												.authorizeTwitter(MainActivity.this);

									}

								}).setNegativeButton(android.R.string.cancel, null).show();
				shownFacebookDialog = true;
			} else if (mode == MSG_MODE_AUTHORIZED_TWITTER) {
				isPreparedTwitter = true;
			} else if (mode == MSG_MODE_AUTHORIZED_FACEBOOK) {
				isPreparedFacebook = true;
			}

			super.handleMessage(msg);
		}
	};

	public Handler getmPrepareShareHandler() {
		return mPrepareShareHandler;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		FragmentManager fm = getSupportFragmentManager();

		mViewPager = (ViewPager) findViewById(R.id.pager);
		// Phone setup
		mViewPager.setAdapter(new HomePagerAdapter(fm));
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setPageMarginDrawable(R.drawable.grey_border_inset_lr);
		mViewPager.setPageMargin(getResources().getDimensionPixelSize(
				R.dimen.padding_small));

		final ActionBar actionBar = getSupportActionBar();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
		}

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.addTab(actionBar.newTab()
				.setText(getString(R.string.title_ongoing, 0))
				.setTabListener(this));
		actionBar
				.addTab(actionBar.newTab()
						.setText(getString(R.string.title_won, 0))
						.setTabListener(this));
		actionBar.addTab(actionBar.newTab()
				.setText(getString(R.string.title_failed, 0))
				.setTabListener(this));

		getSupportActionBar().setHomeButtonEnabled(false);

		refreshTitles();
		
		graphLL = (LinearLayout) findViewById(R.id.hitoryGraph);

		autoCheckNUpdateGoalStatus();

		initAds();
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
			dbHelper.setActivity(this);
		}
		return dbHelper;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mOptionsMenu = menu;
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		setupMenuItem(menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// case R.id.menu_settings:
		// ;
		// return true;

		case R.id.menu_help:
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://www.yi4all.com/?p=968"));
			startActivity(browserIntent);
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	private void setupMenuItem(Menu menu) {
		// TODO Auto-generated method stub

	}

	private class HomePagerAdapter extends FragmentPagerAdapter {

		public HomePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return (mOngoingFragment = new OngoingFragment());

			case 1:
				return mWonFragment = new DoneFragment(true);

			case 2:
				return mFailedFragment = new DoneFragment(false);
			}
			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int position) {
		getSupportActionBar().setSelectedNavigationItem(position);

		int titleId = -1;
		switch (position) {
		case 0:
			titleId = R.string.title_ongoing;
			graphLL.setVisibility(View.VISIBLE);
			break;
		case 1:
			titleId = R.string.title_won;
			graphLL.setVisibility(View.GONE);
			break;
		case 2:
			titleId = R.string.title_failed;
			graphLL.setVisibility(View.GONE);
			break;
		}

//		this.setTitle(titleId);

	}

	@Override
	public void onTabReselected(Tab arg0,
			FragmentTransaction fragmentTransaction) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition());

	}

	@Override
	public void onTabUnselected(Tab arg0,
			FragmentTransaction fragmentTransaction) {

	}

	public LinearLayout getGraphLL() {
		return graphLL;
	}

	private void initAds() {
		// AdWhirlManager.setConfigExpireTimeout(1000 * 60 * 5);
		//
		// AdWhirlTargeting.setTestMode(false);
		//
		// AdWhirlLayout adWhirlLayout =
		// (AdWhirlLayout)findViewById(R.id.adwhirl_layout);
		//
		// int diWidth = 960;
		// int diHeight = 100;
		// int density = (int) getResources().getDisplayMetrics().density;
		//
		// adWhirlLayout.setAdWhirlInterface(this);
		// adWhirlLayout.setMaxWidth((int)(diWidth * density));
		// adWhirlLayout.setMaxHeight((int)(diHeight * density));

		// ad initialization
		// Create the adView
		AdView adView = new AdView(this, AdSize.BANNER, "a150512fc07f739");
		// Lookup your LinearLayout assuming it��s been given
		// the attribute android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) findViewById(R.id.ad_layout);
		// Add the adView to it
		layout.addView(adView);
		// Initiate a generic request to load it with an ad
		adView.loadAd(new AdRequest());
	}

	@Override
	public List<GoalModel> getTodayGoals() {
		Date today = new Date();
		return getGoalsByDate(today);
	}

	private void autoCheckNUpdateGoalStatus() {
		Runnable task = new Runnable() {

			@Override
			public void run() {
				try {
					Log.d(LOG_TAG, "autoCheckNUpdateGoalStatus begining...");

					// 1.get all of goals expiration(end date = start date +
					// total times - 1 , end date < today )
					Date today = new Date();
					Dao<GoalModel, Integer> dao = getHelper().getGoalDAO();
					QueryBuilder<GoalModel, Integer> queryBuilder = dao
							.queryBuilder();
					queryBuilder.orderBy(GoalModel.FIELD_START_DATE, false);

					Where<GoalModel, Integer> where = queryBuilder.where();

					where.lt(GoalModel.FIELD_END_DATE,
							DateUtils.getDateWithoutTime(today));
					where.and();
					where.eq(GoalModel.FIELD_STATUS, 0);

					List<GoalModel> list = dao.query(queryBuilder.prepare());

					for (GoalModel gm : list) {
						// 2.set status = 1 or 2(done times > expected times)
						int doneTimes = gm.getDoneTimes();
						int expectedTimes = gm.getTotalTimes()
								* gm.getWonPercentage() / 100;
						if (doneTimes >= expectedTimes) {
							// won
							gm.setFinalResult(1);

							// 3.share to FB,twitter
							runShareThread(gm, null, SHARE_MODE_WON,
									gm.getEndDate());
						} else {
							// failed
							gm.setFinalResult(2);

							// 3.share to FB,twitter
							runShareThread(gm, null, SHARE_MODE_FAILED,
									gm.getEndDate());
						}
						Log.d(LOG_TAG, "updated gm:" + gm.getName()
								+ " with final result:" + gm.getFinalResult());
						dao.update(gm);

					}

					// 4.share goal yesterday, those are missed to share
					Dao<GoalCheckResultModel, Integer> dao2 = getHelper()
							.getGoalCheckDAO();
					QueryBuilder<GoalCheckResultModel, Integer> queryBuilder2 = dao2
							.queryBuilder();

					Where<GoalCheckResultModel, Integer> where2 = queryBuilder2
							.where();

					where2.and(where2.eq(
							GoalCheckResultModel.FIELD_HAPPEN_DATE, DateUtils
									.defaultFormat(DateUtils.addDateDays(today,
											-1))), 
											where2.or(
													where2.eq(
							GoalCheckResultModel.FIELD_SHARED_FB, false),
							where2.eq(GoalCheckResultModel.FIELD_SHARED_TW,
									false)));

					List<GoalCheckResultModel> list2 = dao2.query(queryBuilder2
							.prepare());

					for (GoalCheckResultModel gcrm : list2) {
						// 5.share unshared gcrms yesterday
						if(gcrm.isResult()){
							runShareThread(gcrm.getGoal(), gcrm, SHARE_MODE_DONE,
									DateUtils.addDateDays(today, -1));
						}else{
						runShareThread(gcrm.getGoal(), gcrm, SHARE_MODE_MISSED,
								DateUtils.addDateDays(today, -1));
						}
					}

					// 6.refresh list
					refreshList();
					refreshTitles();

				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

		};
		new Thread(task).start();
	}

	@Override
	public List<GoalModel> getGoalsByDate(Date date) {
		return getHelper().getGoalsByDate(date);
	}

	@Override
	public List<GoalModel> getGoalsByResult(boolean flag) {
		try {
			Dao<GoalModel, Integer> dao = getHelper().getGoalDAO();
			QueryBuilder<GoalModel, Integer> queryBuilder = dao.queryBuilder();
			queryBuilder.orderBy(GoalModel.FIELD_START_DATE, false);

			Where<GoalModel, Integer> where = queryBuilder.where();

			if (flag) {
				where.eq(GoalModel.FIELD_STATUS, 1);
			} else {
				where.ge(GoalModel.FIELD_STATUS, 2);
			}

			return dao.query(queryBuilder.prepare());

		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<GoalModel>();
		}
	}

	@Override
	public List<String[]> getGoalsWonResultByDateRange(Date begin, Date end) {
		List<String[]> res = new ArrayList<String[]>();

		try {
			Dao<GoalCheckResultModel, Integer> dao = getHelper()
					.getGoalCheckDAO();
			QueryBuilder<GoalCheckResultModel, Integer> queryBuilder = dao
					.queryBuilder();

			queryBuilder
					.selectRaw(GoalCheckResultModel.FIELD_HAPPEN_DATE
							+ ", SUM(" + GoalCheckResultModel.FIELD_RESULT
							+ ")" + ", Count("
							+ GoalCheckResultModel.FIELD_HAPPEN_DATE + ")");
			queryBuilder.groupBy(GoalCheckResultModel.FIELD_HAPPEN_DATE);
			queryBuilder.orderBy(GoalCheckResultModel.FIELD_HAPPEN_DATE, true);

			Where<GoalCheckResultModel, Integer> where = queryBuilder.where();
			where.le(GoalCheckResultModel.FIELD_HAPPEN_DATE,
					DateUtils.defaultFormat(end));
			where.and();
			where.ge(GoalCheckResultModel.FIELD_HAPPEN_DATE,
					DateUtils.defaultFormat(begin));

			res = dao.queryRaw(queryBuilder.prepareStatementString())
					.getResults();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public Integer getGoalsDoneResultByDate(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getGoalsFailedResultByDate(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setGoalResult(GoalModel gm, Date date, boolean result) {
		try {
			// 1.update GoalResult by date
			Dao<GoalCheckResultModel, Integer> dao = getHelper()
					.getGoalCheckDAO();
			QueryBuilder<GoalCheckResultModel, Integer> queryBuilder = dao
					.queryBuilder();
			Where<GoalCheckResultModel, Integer> where = queryBuilder.where();
			where.eq(GoalCheckResultModel.FIELD_GOAL, gm);
			where.and();
			where.eq(GoalCheckResultModel.FIELD_HAPPEN_DATE,
					DateUtils.defaultFormat(date));

			GoalCheckResultModel gcrm = dao.queryForFirst(queryBuilder
					.prepare());

			gcrm.setResult(result);

			dao.update(gcrm);

			// 2.update Goal done times
			Dao<GoalModel, Integer> dao2 = getHelper().getGoalDAO();
			gm.setDoneTimes(gm.getDoneTimes() + ((result) ? 1 : -1));
			dao2.update(gm);

			// 3.refresh list
			refreshList();

			// 4.share to FB,twitter
			runShareThread(gm, gcrm, (result) ? SHARE_MODE_DONE
					: SHARE_MODE_MISSED, date);

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void runShareThread(GoalModel gm, GoalCheckResultModel gcrm,
			int shareMode, Date happenDate) {
		// prepare facebook settings
		if (gm.isShareToFB() && (!gm.isShared2FB())) {
			if (!SocialUtils.isFBPrepared(MainActivity.this)) {
				Message msg = Message.obtain();
				msg.arg1 = MSG_MODE_PREPARE_FACEBOOK;
				MainActivity.this.mPrepareShareHandler.sendMessage(msg);

			} else {
				isPreparedFacebook = true;
			}
		}

		// prepare twitter settings
		if (gm.isShareToTwitter() && (!gm.isShared2TW())) {
			if (!SocialUtils.isTWPrepared(MainActivity.this)) {
				Message msg = Message.obtain();
				msg.arg1 = MSG_MODE_PREPARE_TWITTER;
				MainActivity.this.mPrepareShareHandler.sendMessage(msg);

			} else {
				isPreparedTwitter = true;
			}
		}

		String content = "";

		switch (shareMode) {
		case SHARE_MODE_DONE: {
			content = getString(R.string.share_done_content, gm.getName(),
					DateUtils.formatDate(happenDate));
			runShareThread(gcrm, content);
			break;
		}
		case SHARE_MODE_MISSED: {
			content = getString(R.string.share_missed_content, gm.getName(),
					DateUtils.formatDate(happenDate));
			runShareThread(gcrm, content);
			break;
		}
		case SHARE_MODE_WON: {
			content = getString(R.string.share_won_content, gm.getName(),
					DateUtils.formatDate(happenDate));
			runShareThread(gm, content);
			break;
		}
		case SHARE_MODE_FAILED: {
			content = getString(R.string.share_failed_content, gm.getName(),
					DateUtils.formatDate(happenDate));
			runShareThread(gm, content);
			break;
		}
		case SHARE_MODE_GIVEUP: {
			content = getString(R.string.share_giveup_content, gm.getName(),
					DateUtils.formatDate(happenDate));
			runShareThread(gm, content);
			break;
		}

		}

	}

	class ShareRunnable implements Runnable {

		public final static int MODE_TW = 0;
		public final static int MODE_FB = 1;

		private GoalModel gm = null;
		private GoalCheckResultModel gcrm = null;
		private String content;
		private int mode; // 0 - twitter; 1 - facebook

		public ShareRunnable(GoalModel gm, String content, int mode) {
			this.gm = gm;
			this.content = content;
			this.mode = mode;
		}

		public ShareRunnable(GoalCheckResultModel gcrm, String content, int mode) {
			this.gcrm = gcrm;
			this.content = content;
			this.mode = mode;
		}

		@Override
		public void run() {

			int loop = 0;
			while (loop < 20) {
				Log.d(LOG_TAG, "ShareRunnable loop:" + loop);

				if ((mode == MODE_FB && isPreparedFacebook)
						|| (mode == MODE_TW && isPreparedTwitter)) {
					boolean flag;
					if (mode == 1) {
						flag = SocialUtils
								.shareToFB(MainActivity.this, content);
					} else {
						flag = SocialUtils.shareToTwitter(MainActivity.this,
								content);
					}
					if (flag) {
						try {
							if (gm != null) {
								Dao<GoalModel, Integer> dao = getHelper()
										.getGoalDAO();
								if (mode == MODE_FB) {
									gm.setShared2FB(true);
								} else {
									gm.setShared2TW(true);
								}

								dao.update(gm);

								Log.d(LOG_TAG,
										"ShareRunnable gm updated:"
												+ gm.getName());
							} else if (gcrm != null) {
								Dao<GoalCheckResultModel, Integer> dao = getHelper()
										.getGoalCheckDAO();
								if (mode == MODE_FB) {
									gcrm.setShared2FB(true);
								} else {
									gcrm.setShared2TW(true);
								}
								dao.update(gcrm);

								Log.d(LOG_TAG, "ShareRunnable gcrm updated:"
										+ gcrm.getGoal().getName());
							}

						} catch (SQLException e) {
							e.printStackTrace();
						}

					}
					loop = 100;
				} else {
					loop++;
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}

		}
	}

	private void runShareThread(final GoalModel gm, final String content) {

		if (gm.isShareToFB() && (!gm.isShared2FB())) {
			ShareRunnable shareThread = new ShareRunnable(gm, content,
					ShareRunnable.MODE_FB);

			new Thread(shareThread).start();
		}

		if (gm.isShareToTwitter() && (!gm.isShared2TW())) {
			ShareRunnable shareThread = new ShareRunnable(gm, content,
					ShareRunnable.MODE_TW);

			new Thread(shareThread).start();
		}

	}

	private void runShareThread(final GoalCheckResultModel gcrm,
			final String content) {
		final GoalModel gm = gcrm.getGoal();

		if (gm.isShareToFB() && (!gcrm.isShared2FB())) {
			ShareRunnable shareThread = new ShareRunnable(gcrm, content,
					ShareRunnable.MODE_FB);

			new Thread(shareThread).start();
		}

		if (gm.isShareToTwitter() && (!gcrm.isShared2TW())) {
			ShareRunnable shareThread = new ShareRunnable(gcrm, content,
					ShareRunnable.MODE_TW);

			new Thread(shareThread).start();
		}
	}

	public void refreshList() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mOngoingFragment != null)
					mOngoingFragment.refreshList();

			}
		});

	}

	public void refreshTitles() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				try {
					final ActionBar actionBar = getSupportActionBar();

					// 1.get ongoing goals amount
					Dao<GoalModel, Integer> dao = getHelper().getGoalDAO();
					QueryBuilder<GoalModel, Integer> qb = dao.queryBuilder();
					// select 2 aggregate functions as the return
					qb.selectRaw("Count(" + GoalModel.FIELD_NAME + ")");
					Where<GoalModel, Integer> where = qb.where();
					where.eq(GoalModel.FIELD_STATUS, 0);
					// the results will contain 2 string values for the min and
					// max
					GenericRawResults<String[]> results = dao.queryRaw(qb
							.prepareStatementString());
					String[] values = results.getFirstResult();
					int amount = Integer.valueOf(values[0]);

					actionBar.getTabAt(0).setText(
							getString(R.string.title_ongoing, amount));

					// 2.get won goals amount
					QueryBuilder<GoalModel, Integer> qb2 = dao.queryBuilder();
					// select 2 aggregate functions as the return
					qb2.selectRaw("Count(" + GoalModel.FIELD_NAME + ")");
					Where<GoalModel, Integer> where2 = qb2.where();
					where2.eq(GoalModel.FIELD_STATUS, 1);
					// the results will contain 2 string values for the min and
					// max
					results = dao.queryRaw(qb2.prepareStatementString());
					values = results.getFirstResult();
					amount = Integer.valueOf(values[0]);

					actionBar.getTabAt(1).setText(
							getString(R.string.title_won, amount));

					// 3.get failed or given up goals
					QueryBuilder<GoalModel, Integer> qb3 = dao.queryBuilder();
					// select 2 aggregate functions as the return
					qb3.selectRaw("Count(" + GoalModel.FIELD_NAME + ")");
					Where<GoalModel, Integer> where3 = qb3.where();
					where3.ge(GoalModel.FIELD_STATUS, 2);
					// the results will contain 2 string values for the min and
					// max
					results = dao.queryRaw(qb3.prepareStatementString());
					values = results.getFirstResult();
					amount = Integer.valueOf(values[0]);

					actionBar.getTabAt(2).setText(
							getString(R.string.title_failed, amount));

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public boolean startOverGoal(GoalModel gm, Date date) {
		try {
			// 1.delete all of goal check results
			Dao<GoalCheckResultModel, Integer> dao = getHelper()
					.getGoalCheckDAO();

			dao.delete(gm.getResultList());
			// 2.set start date to today, end date to today+duration*7 and
			// generate goal check results
			Dao<GoalModel, Integer> dao2 = getHelper().getGoalDAO();

			gm.setStartDate(new Date());
			gm.setEndDate(DateUtils.addDateDays(gm.getStartDate(),
					gm.getDuration() * 7 - 1));
			dao2.update(gm);

			getHelper().generateGoalCheckResults(gm);
			// 3.toast
			toastMsg(R.string.start_over_sucess);
			// 4.refresh list
			refreshList();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean giveUpGoal(GoalModel gm, Date date) {
		try {
			// 1.set goal status to 3, end date to today
			Dao<GoalModel, Integer> dao = getHelper().getGoalDAO();
			gm.setFinalResult(3);
			gm.setEndDate(new Date());
			dao.update(gm);
			// 2.delete all of goal check results
			Dao<GoalCheckResultModel, Integer> dao2 = getHelper()
					.getGoalCheckDAO();

			dao2.delete(gm.getResultList());
			// 3.toast
			toastMsg(R.string.give_up_sucess);
			// 4.refresh list
			refreshList();
			// 5.share to FB,twitter
			runShareThread(gm, null, SHARE_MODE_GIVEUP, new Date());

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<GoalCheckResultModel> getGoalResults(GoalModel gm) {
		try {
			// 1.update GoalResult by date
			Dao<GoalCheckResultModel, Integer> dao = getHelper()
					.getGoalCheckDAO();
			QueryBuilder<GoalCheckResultModel, Integer> queryBuilder = dao
					.queryBuilder();
			Where<GoalCheckResultModel, Integer> where = queryBuilder.where();
			where.eq(GoalCheckResultModel.FIELD_GOAL, gm);
			where.and();
			where.le(GoalCheckResultModel.FIELD_HAPPEN_DATE,
					DateUtils.defaultFormat(new Date()));

			return dao.query(queryBuilder.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<GoalCheckResultModel>();
		}
	}

	public void toastMsg(int resId, String... args) {
		final String msg = this.getString(resId, args);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	@Override
	public void adWhirlGeneric() {
		AdWhirlAdapter.setGoogleAdSenseCompanyName("yi4all.com");
		AdWhirlAdapter.setGoogleAdSenseAppName("makehabits");
	}
}
