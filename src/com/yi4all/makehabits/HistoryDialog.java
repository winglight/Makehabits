package com.yi4all.makehabits;

import java.util.List;

import com.yi4all.makehabits.R;
import com.yi4all.makehabits.GoalListAdapter.ViewHolder;
import com.yi4all.makehabits.db.GoalCheckResultModel;
import com.yi4all.makehabits.db.GoalModel;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Gather the xmpp settings and create an XMPPConnection
 */
public class HistoryDialog extends Dialog  {
	
	
    private MainActivity context;
    private GoalModel gm;

    public HistoryDialog(MainActivity context, GoalModel gm) {
        super(context);
        this.context = context;
        this.gm = gm;
    }

    protected void onStart() {
        super.onStart();
        
        ListView listView = new ListView(context);
        
        setContentView(listView);
        
        getWindow().setFlags(4, 4);
        setTitle(context.getResources().getString(R.string.history_title, gm.getName()));
        
        final List<GoalCheckResultModel> list = context.getGoalResults(gm);
        
        listView.setAdapter(new GoalResultListAdapter(this.context, list));
    }
    
    class GoalResultListAdapter extends BaseAdapter{

    	private LayoutInflater mInflater;
    	private List<GoalCheckResultModel> list;

    	public GoalResultListAdapter(Context context, List<GoalCheckResultModel> list){
    	this.mInflater = LayoutInflater.from(context);
    	this.list = list;
    	}
    	
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return list.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (list == null || position < 0 || position >= list.size()) {
				TextView title = new TextView(parent.getContext());
				title.setText(R.string.no_data);
				return title;
			}
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.goal_result_list_item, null);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			if (holder == null || holder.date == null) {
				holder = new ViewHolder(convertView);
				if(holder.date == null){
					convertView = mInflater.inflate(R.layout.goal_result_list_item, null);
					holder = new ViewHolder(convertView);
				}
				convertView.setTag(holder);
			}
			
			GoalCheckResultModel gcrm = list.get(position);
			holder.date.setText(DateUtils.formatDate(DateUtils.defaultParse(gcrm.getHappenDate())));
			
			if(gcrm.isResult()){
				holder.baseLL.setBackgroundColor(context.getResources().getColor(R.color.background_green));
				holder.result.setChecked(true);
				holder.result.setText(R.string.done);
			}else{
				holder.baseLL.setBackgroundColor(context.getResources().getColor(R.color.background_red));
				holder.result.setChecked(false);
				holder.result.setText(R.string.missed);
			}
			
			return convertView;
		}
    	
    }
    
    class ViewHolder {
		TextView date = null;
		CheckedTextView result = null;
		LinearLayout baseLL = null;

		ViewHolder(View base) {
			this.date = (TextView) base.findViewById(R.id.happenDateTxt);
			this.result = (CheckedTextView) base.findViewById(R.id.resultTxt);
			this.baseLL = (LinearLayout) base;
		}

	}

}

