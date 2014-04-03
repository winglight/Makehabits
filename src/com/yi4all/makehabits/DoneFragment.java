package com.yi4all.makehabits;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;
import com.yi4all.makehabits.R;
import com.yi4all.makehabits.GoalListAdapter.ViewHolder;
import com.yi4all.makehabits.db.GoalModel;
import com.yi4all.makehabits.db.IDBService;

public class DoneFragment extends SherlockFragment {

	private static String LOG_TAG = "DoneFragment";

	private boolean resultFlag;// true - won; false - failed or given up

	private ListView goalList;
	private List<GoalModel> goals;
	private IDBService service;

	private GoalListAdapter adapter;

	public DoneFragment(boolean result) {
		this.resultFlag = result;
	}

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
		View rootView = inflater.inflate(R.layout.fragment_done, container,
				false);

		goalList = (ListView) rootView.findViewById(R.id.doneGoalList);

		goalList.setSelector(android.R.color.white);

		goalList.setAdapter(adapter);

		goalList.setChoiceMode(ListView.CHOICE_MODE_NONE);

		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
	}
	
	public void refreshList(){
		
		goals = service.getGoalsByResult(resultFlag);
		
		adapter.setmList(new ArrayList<GoalModel>(goals));
		
		adapter.setCurDate(new Date());
		
		adapter.notifyDataSetChanged();
		
		((MainActivity)getActivity()).refreshTitles();
		
	}

}
