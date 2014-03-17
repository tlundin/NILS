package com.teraim.nils.bluetooth;

import android.content.Intent;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.log.LoggerI;
import com.teraim.nils.utils.PersistenceHelper;

public abstract class MessageHandler {

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
			gs.setSyncStatus(BluetoothConnectionService.SYNC_RUNNING);
			triggerTransfer();
		}
		else if (message instanceof SyncEntry[]) {
			SyncEntry[] ses = (SyncEntry[])message;
			if (ses.length>0) {
				o.addRow("[Recieving SYNC: "+ses.length+" rows]");
				gs.getDb().synchronise(ses);
				gs.sendMessage(new SyncSuccesful());
			}
			else {
				o.addRow("[SYNC: No changes since last sync]");				
			}
		}
		else if (message instanceof SyncSuccesful)
			gs.getDb().syncDone();
		
		handleSpecialized(message);
		o.draw();
	}
	
	

	public abstract void handleSpecialized(Object message);
	
	
	public void sendEvent(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		gs.getContext().sendBroadcast(intent);
	}
	
	protected void triggerTransfer() {
		SyncEntry[] changes = gs.getDb().getChanges();
		Log.d("nils","Syncrequest received. Sending "+(changes==null?"no changes":changes.toString()));
		if (changes==null)
			o.addRow("[SENDING_SYNC-->0 rows]");
		else
			o.addRow("[SENDING_SYNC-->"+changes.length+" rows]");
		if (changes == null) 
			changes = new SyncEntry[]{};
		gs.sendMessage(changes);
	}
}
