package com.yi4all.makehabits;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.GraphViewSeries;
import com.jjoe64.graphview.GraphView.GraphViewStyle;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.yi4all.makehabits.R;
import com.yi4all.makehabits.GoalListAdapter.ViewHolder;
import com.yi4all.makehabits.db.GoalModel;
import com.yi4all.makehabits.db.IDBService;

public class OngoingFragment extends SherlockFragment{

	private static String LOG_TAG = "OngoingFragment";
	
	private ListView goalList;
	private List<GoalModel> goals;
	private IDBService service;
	
	private GoalListAdapter adapter;
	
	private int selected_item = -1;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		adapter = new GoalListAdapter(activity, (IDBService) activity);
		
		service = (IDBService) activity;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		refreshList();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
		
        // Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_ongoing, container, false);
		
		ImageView addBtn = (ImageView) rootView.findViewById(R.id.addImg);
		addBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setClass(getActivity(),
						GoalEditorActivity.class);
				startActivity(intent);
				
			}
		});
		
		goalList = (ListView) rootView.findViewById(R.id.ongoingGoalList);
		
		goalList.setSelector(android.R.color.white);
		
		goalList.setAdapter(adapter);
		
		goalList.setChoiceMode(ListView.CHOICE_MODE_NONE);
		
		goalList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View child, int position,
					long id) {
				ViewHolder holder = (ViewHolder) child.getTag();
				if(selected_item >= 0){
					//close expanded buttons
					ViewHolder holder2 = (ViewHolder) parent.getChildAt(selected_item).getTag();
					holder2.btnLL.setVisibility(View.GONE);
					
					//unregister listeners of buttons
					holder.setButtonsListeners(false, null, null);
				}
					//show expanded buttons
					holder.btnLL.setVisibility(View.VISIBLE);
				//add listener to all buttons
					holder.setButtonsListeners(true, goals.get(position), OngoingFragment.this.getActivity());
					
					selected_item = position;
			}
		});
		
        return rootView;
    }
	
	private View generateGraphView(){
		/*
		 * init series data
		 */
		Date today = new Date();
		final String todayStr = DateUtils.defaultFormat(today);
		final List<String> labelList = new ArrayList<String>();
		
		// 1.Done goals amounts and date
		List<String[]> amountList = service.getGoalsWonResultByDateRange(DateUtils.addDateDays(today, -150), today);
		int num = amountList.size();
		GraphViewData[] data = new GraphViewData[num];
		GraphViewData[] data2 = new GraphViewData[num];
		for (int i=0; i<num; i++) {
			String[] str = amountList.get(i);
			labelList.add(str[0]);
			Double y1 = Double.valueOf(str[1]);
			Double y2 = Double.valueOf(Integer.valueOf(str[2]) - Integer.valueOf(str[1]));
			data[i] = new GraphViewData(i, y1);
			data2[i] = new GraphViewData(i, y2);
		}
		GraphViewSeries seriesDone = new GraphViewSeries(getString(R.string.doneAmount), new GraphViewStyle(Color.rgb(90, 250, 00), 3), data);

		GraphViewSeries seriesMissed = new GraphViewSeries(getString(R.string.missedAmount), new GraphViewStyle(Color.rgb(200, 50, 00), 3), data2);


		// graph with dynamically generated horizontal and vertical labels
		LineGraphView graphView;
		graphView = new MyLineGraphView( 
				this.getActivity()
				, getString(R.string.graph_name)
				, labelList
				, todayStr
		);
		// add data
		graphView.addSeries(seriesMissed);
		graphView.addSeries(seriesDone);
		// set legend
		graphView.setShowLegend(true);
		graphView.setLegendAlign(LegendAlign.BOTTOM);
		graphView.setLegendWidth(120);
		// set view port, start align to right as possible, size=10
		graphView.setViewPort(num - 6, 7);
		graphView.setScalable(num > 7);
		graphView.setScrollable(num > 7);
		graphView.setmTextColor(Color.BLACK);
		graphView.setmLegendTextColor(Color.BLACK);
		graphView.setCircleRadius(5);
//		graphView.setBackgroundColor(getResources().getColor(android.R.color.black));

		return graphView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
	}
	
	public void refreshList(){
		
		selected_item = -1;
		
		goals = service.getTodayGoals();
		
		adapter.setmList(new ArrayList<GoalModel>(goals));
		
		adapter.setCurDate(new Date());
		
		adapter.notifyDataSetChanged();
		
		((MainActivity)this.getActivity()).getGraphLL().removeAllViews();
		
		((MainActivity)this.getActivity()).getGraphLL().addView(generateGraphView());
	}
	
	class MyLineGraphView extends LineGraphView{
		private List<String> labelList;
		private String todayStr;
		private int latestLabel = -1;

		public MyLineGraphView(Context context, String title) {
			super(context, title);
		}
		
		public MyLineGraphView(Context context, String title, List<String> labelList, String todayStr) {
			super(context, title);
			this.labelList = labelList;
			this.todayStr = todayStr;
		}

		@Override  
		   protected String formatLabel(double value, boolean isValueX) {  
			
			int newValue = ((Double)value).intValue();
			
		      if (isValueX) {  
		    	  if(newValue < 0 || newValue >= labelList.size()) return "";
		    	  if(latestLabel == newValue) return "";
		    	  latestLabel = newValue;
		    	  String realDate = labelList.get(newValue);
		    	  if(realDate.equals(todayStr)){
		    		  return getString(R.string.today);
		    	  }else if(newValue == 0 || newValue == labelList.size()-1){
		    		  return DateUtils.convertDate2MMdd(realDate, true);
		    	  }else{
		    		  return DateUtils.convertDate2MMdd(realDate, false);
		    	  }
		    	  
		      } else{
		    	  return super.formatLabel(value, isValueX); // let the y-value be normal-formatted  
		      }
		   } 
	}
	
}
