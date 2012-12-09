package com.teraim.nils;

import android.app.Activity;

/**
 * 
 * @author Terje
 *
 *This implements a remote device of a certain color. It should be tech independent.
 */
public class RemoteDevice  {

	private String mColor=null;
	protected Activity mActivity=null;
	
	protected RemoteDevice(String expectedColor) {
		mColor = expectedColor;	
			
	}
	
	
}
