package com.teraim.nils.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class DrawerMenuAdapter extends ArrayAdapter<DrawerMenuItem> {

	private LayoutInflater mInflater;

	public enum RowType {
		LIST_ITEM, HEADER_ITEM
	}

	public DrawerMenuAdapter(Context context, List<DrawerMenuItem> items) {
		super(context, 0, items);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getViewTypeCount() {
		return RowType.values().length;

	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).getViewType();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getItem(position).getView(mInflater, convertView);
	}
}

