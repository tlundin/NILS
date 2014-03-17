package com.teraim.nils.bluetooth;

import com.teraim.nils.GlobalState;

import android.content.Context;
import android.content.Intent;

public abstract class MessageHandler {

	protected GlobalState gs;

	public MessageHandler(GlobalState gs) {
		this.gs=gs;
		
	}
	
	public void handleMessage(Object message) {
		handleSpecialized(message);
		sendEvent(BluetoothConnectionService.SYNK_SERVICE_MESSAGE_RECEIVED);
	}
	
	public abstract void handleSpecialized(Object message);
	
	
	public void sendEvent(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		gs.getContext().sendBroadcast(intent);
	}
}
