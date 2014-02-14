package com.teraim.nils;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class Logger {

	 CharSequence myTxt = new SpannableString("");
	 TextView log = null;
	 Context myContext;
	 boolean dev=false;
	 	
	public Logger(Context c,boolean state) {
		myContext = c;
		dev = state;
	}
	 
	public void setDev(boolean state) {
		dev = state;
	}
	 public void setOutputView(TextView txt) {
		 log = txt;
	 }
	 
	 public void addRow(String text) {
		 if (dev)
			 myTxt = TextUtils.concat(myTxt,"\n"+text);
		
	 }
	 public void addRedText(String text) {
		 if (!dev)
			 return;
		 SpannableString s = new SpannableString(text);
		 s.setSpan(new TextAppearanceSpan(myContext, R.style.RedStyle),0,s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		 myTxt = TextUtils.concat(myTxt, s);	 
		 //if (log!=null) log.setText(myTxt);
	 }	 
	 public void addGreenText(String text) {
		 if (!dev)
			 return;
		 SpannableString s = new SpannableString(text);
		 s.setSpan(new TextAppearanceSpan(myContext, R.style.GreenStyle),0,s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		 myTxt = TextUtils.concat(myTxt, s);	
		 //if (log!=null) log.setText(myTxt);
	 }
	 public void addYellowText(String text) {
		 if (!dev)
			 return;
		 SpannableString s = new SpannableString(text);
		 s.setSpan(new TextAppearanceSpan(myContext, R.style.YellowStyle),0,s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		 myTxt = TextUtils.concat(myTxt, s);	
		 //if (log!=null) log.setText(myTxt);
	 }
	 public void addText(String text) {
		 if (!dev)
			 return;
		 myTxt = TextUtils.concat(myTxt, text);
		 //if (log!=null) log.setText(myTxt);
	 }
	 
	 
	 
	 public CharSequence getLogText() {
		 return myTxt;
	 }

	 public void draw() {
		 if (!dev)
			 return;
		 if (log!=null) 
			 log.setText(myTxt);
	 }


	public void clear() {
		myTxt = "";
		if (log!=null) log.setText(myTxt);
	}
}
