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
			sendEvent(BluetoothConnectionService.SYNK_INITIATE);
		} else if (message instanceof SyncEntry[]) {
			Log.d("nils","SYNCDATA RECEIVED!!");
			SyncEntry[] ses = (SyncEntry[])message;
			gs.getDb().synchronise(ses);
		}
	}

}
