package com.teraim.nils;

import android.util.Log;

import com.teraim.nils.exceptions.BluetoothNotSupportedException;

/**
 * 
 * @author Terje
 *
 *This class hides the communication and implementation details of the bluethooth/or other tech used to communicate.
 */
public class CommManager implements BTListener {

	
	static CommManager me=null;
	CommListener mCaller;
	private BluetoothRemoteDevice rd;
	private boolean deviceActive = false;
	
	private CommManager(CommListener caller) {
		me = this; 
		mCaller = caller;
		try {
			rd = BluetoothRemoteDevice.create(CommonVars.cv().getDeviceColor(), this);
		} catch (BluetoothNotSupportedException e) {
			Log.e("NILS","Bluetooth not supported");
		}
		if(rd.activate())
			deviceIsReady();
	}

	
	
	public static CommManager getCommManager(CommListener caller) {
		if (me==null)
				return new CommManager(caller);	
		else
			return me;
	}
	
	

	public void getParameter(String parKey) {
		if (rd !=null && rd.isActive()) {
			rd.getParameter(parKey);
			
		}
	}
	
	public void getParameter(String parKey, int timeOutInMs) {
		
	}

	public void onParameterReceived(String key, String value) {
		mCaller.onValueRecievedCb(value);
	}



	public void deviceIsReady() {
		Log.e("NILS","Device is ready!");
		onParameterReceived(null,"test");
	}


}
