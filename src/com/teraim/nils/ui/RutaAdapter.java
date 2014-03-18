package com.teraim.nils.ui;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.teraim.nils.GlobalState;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Variable;

public class RutaAdapter extends ArrayAdapter<Integer> {

	long[] myCords = new long[2];
	
	public RutaAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		myCords[0]=myCords[1]=-1;
	}

	public RutaAdapter(Context context, int resource, List<Integer> rutor) {
		super(context, resource, rutor);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;

		if (v == null) {
			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());
			v = vi.inflate(R.layout.ruta_list_row, null);

		}

		final String pi = Integer.toString(getItem(position));
		TextView geo = (TextView) v.findViewById(R.id.geo);
		TextView header = (TextView) v.findViewById(R.id.header);
		header.setText(pi);
		
		geo.setText("Saknar GPS koord. här!");
		
		return v;

	}
	
	public void updateCord(long[] coord) {
		myCords = coord;
	}
}