package com.teraim.nils.ui;

import com.teraim.nils.R;
import com.teraim.nils.ui.DrawerMenuAdapter.RowType;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


public class DrawerMenuHeader implements DrawerMenuItem {

	private final String name;

	public DrawerMenuHeader(String name) {
		this.name = name;
	}

	@Override
	public int getViewType() {
		return RowType.HEADER_ITEM.ordinal();
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view;
		if (convertView == null) {
			view = (View) inflater.inflate(R.layout.drawer_menu_header, null);
			// Do some initialization
		} else {
			view = convertView;
		}

		TextView text = (TextView) view.findViewById(R.id.separator);
		text.setText(name);

		return view;
	}

}