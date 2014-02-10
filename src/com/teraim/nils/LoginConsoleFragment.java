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
	 CharSequence myTxt = new SpannableString("");

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

	 

	 public void addRow(String text) {
		 myTxt = TextUtils.concat(myTxt,"\n"+text);
		 log.setText(myTxt);
	 }
	 public void addRedText(String text) {
		 SpannableString s = new SpannableString(text);
		 s.setSpan(new TextAppearanceSpan(getActivity(), R.style.RedStyle),0,s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		 myTxt = TextUtils.concat(myTxt, s);	 
		 log.setText(myTxt);
	 }	 
	 public void addGreenText(String text) {
		 SpannableString s = new SpannableString(text);
		 s.setSpan(new TextAppearanceSpan(getActivity(), R.style.GreenStyle),0,s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		 myTxt = TextUtils.concat(myTxt, s);	
		 log.setText(myTxt);
	 }
	 public void addText(String text) {
		 myTxt = TextUtils.concat(myTxt, text);
		 log.setText(myTxt);
	 }



	public void clear() {
		myTxt = "";
		log.setText(myTxt);
	}
}
