package com.teraim.nils.bluetooth;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;

public class MasterMessageHandler extends MessageHandler {

	VariableConfiguration vc;
	public MasterMessageHandler(GlobalState gs) {
		super(gs);
		vc = gs.getArtLista();
	}

	

	@Override
	public void handleSpecialized(Object message) {
		if (message instanceof Ping) {
			Log.d("nils","Sending pong");
			Pong pong = new Pong();
			pong.ruta=vc.getVariableValue(null,"Current_Ruta");
			pong.provyta=vc.getVariableValue(null,"Current_Provyta");
			gs.sendMessage(pong);
		} else if (message instanceof SyncRequest) {
			gs.setSyncStatus(BluetoothConnectionService.SYNC_RUNNING);
			SyncEntry[] changes = gs.getDb().getChanges();			
			Log.d("nils","Syncrequest received in Master. Sending "+changes.toString());
			gs.sendMessage(changes);
		}
		
	}

}
