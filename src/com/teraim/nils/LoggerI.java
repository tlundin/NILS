package com.teraim.nils;

import android.widget.TextView;

public interface LoggerI {
	
	 public void setOutputView(TextView txt);	 
	 public void addRow(String text);
	 public void addRedText(String text);
	 public void addGreenText(String text);
	 public void addYellowText(String text);
	 public void addText(String text);
	 public CharSequence getLogText();
	 public void draw();
	 public void clear();
}
