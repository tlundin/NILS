package com.teraim.nils;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.teraim.nils.DataTypes.Rule;

public class ValidatorListAdapter extends BaseAdapter {

	private static LayoutInflater inflater=null;
	private ArrayList<Rule> data;
	
	public ValidatorListAdapter(Context activity, ArrayList<Rule> data) {
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.data=data;
	}
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int row, View arg1, ViewGroup arg2) {
		View v = inflater.inflate(R.layout.validator_list_row, null);
		TextView tv = (TextView)v.findViewById(R.id.validator_text);
		tv.setText(data.get(row).getName());
		
		return v;
	}

}