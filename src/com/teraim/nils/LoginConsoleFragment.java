package com.teraim.nils;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class LoginConsoleFragment extends Fragment {

	TextView log;

	 @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fragment_login_console,
	        container, false);
	    
	    
		log = (TextView)view.findViewById(R.id.logger);
		
		Typeface type=Typeface.createFromAsset(getActivity().getAssets(),
		        "clacon.ttf");
		log.setTypeface(type);
		
	    return view;
	  }

	public TextView getTextWindow() {
		return log;
	}

	 


}
