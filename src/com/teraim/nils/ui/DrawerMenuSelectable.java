package com.teraim.nils.ui;

import com.teraim.nils.R;
import com.teraim.nils.ui.DrawerMenuAdapter.RowType;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class DrawerMenuSelectable implements DrawerMenuItem {

	    private final String         str1;

	    public DrawerMenuSelectable(String text1) {
	        this.str1 = text1;
	    }

	    @Override
	    public int getViewType() {
	        return RowType.LIST_ITEM.ordinal();
	    }

	    @Override
	    public View getView(LayoutInflater inflater, View convertView) {
	        View view;
	        if (convertView == null) {
	            view = (View) inflater.inflate(R.layout.drawer_menu_selectable, null);
	            // Do some initialization
	        } else {
	            view = convertView;
	        }

	        TextView text1 = (TextView) view.findViewById(R.id.list_content1);
	        text1.setText(str1);

	        return view;
	    }

	}
