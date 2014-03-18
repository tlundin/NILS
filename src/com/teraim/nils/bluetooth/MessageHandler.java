package com.teraim.nils.bluetooth;

import android.content.Intent;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.workflow_realizations.WF_Event_OnSave;
import com.teraim.nils.log.LoggerI;
import com.teraim.nils.utils.PersistenceHelper;

public abstract class MessageHandler {

	private static final String WhatEver = null;
	private static final String SYNC_SOURCE = "SYNC_SOURCE";
	protected GlobalState gs;
	protected LoggerI o;

	public MessageHandler(GlobalState gs) {
		this.gs=gs;
		o = gs.getLogger();
	}
	
	public void handleMessage(Object message) {
		//SYNC_REQUEST
		if (message instanceof SyncRequest) {
			o.addRow("[--->SYNC_REQUEST]");
			gs.triggerTransfer();
		}
		else if (message instanceof SyncEntry[]) {
			SyncEntry[] ses = (SyncEntry[])message;
			if (ses.length>0) {
				o.addRow("[Recieving SYNC: "+ses.length+" rows]");
				gs.getDb().synchronise(ses);
				gs.sendMessage(new SyncSuccesful());
				gs.setSyncStatus(BluetoothConnectionService.SYNC_READY_TO_ROCK);
				gs.sendEvent(BluetoothConnectionService.SYNK_DATA_RECEIVED);
			}
			else {
				o.addRow("[SYNC: No changes since last sync]");				
			}
		}
		else if (message instanceof SyncSuccesful) {
			gs.getDb().syncDone();
			gs.setSyncStatus(BluetoothConnectionService.SYNC_READY_TO_ROCK);
			gs.sendEvent(BluetoothConnectionService.SYNK_DATA_TRANSFER_DONE);
		}
		
		handleSpecialized(message);
		o.draw();
	}
	
	

	public abstract void handleSpecialized(Object message);
	
	
	
	
	
}
