package com.yi4all.makehabits;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.yi4all.makehabits.R;
import com.yi4all.makehabits.db.GoalModel;
import com.yi4all.makehabits.db.IDBService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ToggleButton;

public class GoalListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<GoalModel> mList;
	private Date curDate;
	private IDBService service;

	public GoalListAdapter(Context context, IDBService service) {
		mInflater = LayoutInflater.from(context);
		this.service = service;
		curDate = new Date();
		mList = new ArrayList<GoalModel>();
	}

	public GoalListAdapter(Context context, IDBService service, Date curDate) {
		this(context, service);
		this.curDate = curDate;
	}

	public Date getCurDate() {
		return curDate;
	}

	public void setCurDate(Date curDate) {
		this.curDate = curDate;
	}

	public void setmList(List<GoalModel> mList) {
		this.mList = mList;
	}

	@Override
	public int getCount() {
		return mList.size() > 0 ? mList.size() : 1;
	}

	@Override
	public GoalModel getItem(int position) {
		return mList.size() > 0 ? mList.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return mList.size() > 0 ? mList.get(position).getId() : 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mList == null || position < 0 || position >= mList.size()) {
			TextView title = new TextView(parent.getContext());
			title.setText(R.string.no_data);
			title.setTextSize(14); 
			return title;
		}
		// if (convertView == null) {
		convertView = mInflater.inflate(R.layout.goal_list_item, null);
		// }
		ViewHolder holder = (ViewHolder) convertView.getTag();
		if (holder == null || holder.name == null) {
			holder = new ViewHolder(convertView);
			if (holder.name == null) {
				convertView = mInflater.inflate(R.layout.goal_list_item, null);
				holder = new ViewHolder(convertView);
			}
			convertView.setTag(holder);
		}

		// fill in data
		final GoalModel goal = mList.get(position);
		holder.name.setText(goal.getName());
		holder.progress.setText(parent.getContext().getString(
				R.string.goal_progress, goal.getDoneTimes(),
				goal.getTotalTimes() * goal.getWonPercentage() / 100,
				goal.getTotalTimes()));
		if (goal.getFinalResult() == 0) {
			// Ongoing goals screen
			holder.result.setChecked(goal.isCurrentResult());
			holder.doneDate.setVisibility(View.GONE);
			
			//set alarm button
			if(goal.isSetAlarm()){
				holder.alarm.setChecked(true);
				holder.alarm.setText(goal.getAlarmTime());
			}

			// disable result if curDate not between today and end date
			if (!DateUtils
					.isBetweenDate(curDate, new Date(), goal.getEndDate())) {
				holder.result.setClickable(false);
			} else {
				// add listener for buttons
				holder.result
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								// save checked status
								service.setGoalResult(goal, curDate, isChecked);

							}
						});
			}
		} else {
			// Won or failed screen
			holder.result.setVisibility(View.GONE);
			holder.doneDate.setVisibility(View.VISIBLE);
			switch (goal.getFinalResult()) {
			case 1: {
				holder.doneDate.setText(parent.getContext().getString(
						R.string.won_on,
						DateUtils.formatDate(goal.getEndDate())));
				break;
			}
			case 2: {
				holder.doneDate.setText(parent.getContext().getString(
						R.string.failed_on,
						DateUtils.formatDate(goal.getEndDate())));
				break;
			}
			case 3: {
				holder.doneDate.setText(parent.getContext().getString(
						R.string.give_up_on,
						DateUtils.formatDate(goal.getEndDate())));
				break;
			}

			}
		}

		int cp = goal.getDoneTimes();
		int bp = goal.getTotalTimes() - cp;
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				0, bp);
		layoutParams.setMargins(4, 4, 4, 4);
		holder.blank.setLayoutParams(layoutParams);
		layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0, cp);
		layoutParams.setMargins(4, 4, 4, 4);
		holder.indicator.setLayoutParams(layoutParams);

		return convertView;
	}

	class ViewHolder {
		TextView name = null;
		TextView progress = null;
		TextView doneDate = null;
		ToggleButton result = null;
		LinearLayout btnLL = null;
		ImageView blank = null;
		ImageView indicator = null;
		ToggleButton alarm = null;

		ViewHolder(View base) {
			this.name = (TextView) base.findViewById(R.id.goalNameTxt);
			this.progress = (TextView) base.findViewById(R.id.progressTxt);
			this.doneDate = (TextView) base.findViewById(R.id.doneDateTxt);
			this.result = (ToggleButton) base.findViewById(R.id.goalResultTxt);
			this.btnLL = (LinearLayout) base.findViewById(R.id.btnLL);
			this.blank = (ImageView) base.findViewById(R.id.blankImg);
			this.indicator = (ImageView) base.findViewById(R.id.progressImg);
			this.alarm = (ToggleButton) base.findViewById(R.id.alarmBtn);
		}

		public void setButtonsListeners(boolean enable, GoalModel goal,
				Activity activity) {
			for (int i = 0; i < this.btnLL.getChildCount(); i++) {
				View btn = this.btnLL.getChildAt(i);
				if (i == 2) {
					if (enable) {
						alarm.setOnCheckedChangeListener(new MyListener(
										goal, activity, i));
					} else {
						alarm.setOnCheckedChangeListener(null);
					}
				} else {
					if (enable) {
						btn.setOnClickListener(new MyListener(goal, activity, i));
					} else {
						btn.setOnClickListener(null);
					}
				}
			}

		}
	}

}
