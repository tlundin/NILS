package com.teraim.nils.bluetooth;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.Variable;

public class SlaveMessageHandler extends MessageHandler {

	
	public SlaveMessageHandler(GlobalState gs) {
		super(gs);
	}

	@Override
	public void handleSpecialized(Object message) {
		if (message instanceof Pong) {
			Pong p = (Pong)message;		
			Variable ruta = gs.getArtLista().getVariableInstance("Current_Ruta");
			Variable provyta = gs.getArtLista().getVariableInstance("Current_Provyta");
			ruta.setValue(p.ruta);
			provyta.setValue(p.provyta);
			Log.d("nils","Got PONG message!!");			
			gs.setSyncStatus(BluetoothConnectionService.SYNC_RUNNING);
			sendEvent(BluetoothConnectionService.SYNK_INITIATE);			
			gs.sendMessage(new SyncRequest());
			
		} else if (message instanceof SyncEntry[]) {
			triggerTransfer();
		}
		//SYNC_COMPLETE
		else if (message instanceof SyncComplete) {
			gs.setSyncStatus(BluetoothConnectionService.SYNC_DONE);
			sendEvent(BluetoothConnectionService.SYNK_COMPLETE);
			
		} else if (message instanceof SlavePing) {
			sendEvent(BluetoothConnectionService.SAME_SAME_SYNDROME);
		}
	}

}
