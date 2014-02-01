package com.teraim.nils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teraim.nils.dynamic.types.Rule;

public class ValidatorListAdapter extends BaseAdapter {

	private static LayoutInflater inflater=null;
	private  Map<Rule,Boolean> data;
	private Context ctx;
	
	public ValidatorListAdapter(Context activity, Map<Rule,Boolean> data) {
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.data=data;
		this.ctx = activity;
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
		ImageView iv = (ImageView)v.findViewById(R.id.validator_list_image);
		
		Iterator<Entry<Rule, Boolean>> it =data.entrySet().iterator();
		int i = 0; Entry<Rule, Boolean>e = null;
		while (i++<=row&&it.hasNext())
			e = it.next();
		if (e!=null) {
			tv.setText(e.getKey().getName());
			iv.setImageDrawable( ctx.getResources().getDrawable((e.getValue()?R.drawable.green:R.drawable.red)));
		}
		return v;

	}

}