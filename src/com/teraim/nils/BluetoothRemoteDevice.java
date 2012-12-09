package com.teraim.nils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;

import com.teraim.nils.exceptions.BluetoothNotSupportedException;

public class BluetoothRemoteDevice extends RemoteDevice {

	private static BluetoothRemoteDevice me =null;
	private static BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
	final BTListener mCaller;
	boolean isActive = false;
	//Callback ID for BT
	private final int REQUEST_ENABLE_BT = 1;

	private BluetoothRemoteDevice(String expectedColor, BTListener caller) {
		super(expectedColor);	

		mCaller = caller;
		mActivity = new Activity() {
			 @Override
			    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			        Log.d("NILS", "Got activity result " + resultCode);
			        mCaller.deviceIsReady();
			        isActive = true;
			    }
		};
	}


	public static BluetoothRemoteDevice create(String expectedColor, BTListener caller) throws BluetoothNotSupportedException {
		if (mBluetoothAdapter==null) {
			Log.e("NILS","This device does not support bluetooth!!");
			throw new BluetoothNotSupportedException();
		}	
		if (me==null)
			me = new BluetoothRemoteDevice(expectedColor,caller);	
		return me;
	}


	//Check that the remote device has the correct color. Establish a comm. channel.
	//NOTE: DEVICES MUST BE PAIRED BEFOREHAND.

	public boolean activate() {
		//turn on bluetooth if not done.
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			return false;
		} else
			return true;

	}
	
	public void getParameter(String key) {
		
	}
	
	
	public boolean isActive() {
		return isActive;
	}

}