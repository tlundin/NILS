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
		if (message instanceof SlavePing) {
			Log.d("nils","Sending pong");
			o.addRow("[--->PING]");
			Pong pong = new Pong();
			pong.ruta=vc.getVariableValue(null,"Current_Ruta");
			pong.provyta=vc.getVariableValue(null,"Current_Provyta");
			gs.sendMessage(pong);
			o.addRow("[PONG-->]");
		} else if (message instanceof SyncEntry[]) {			
			gs.setSyncStatus(BluetoothConnectionService.SYNC_DONE);
			sendEvent(BluetoothConnectionService.SYNK_COMPLETE);			
			gs.sendMessage(new SyncComplete());
		} else if (message instanceof MasterPing) {
			sendEvent(BluetoothConnectionService.SAME_SAME_SYNDROME);
			
		}

	}

}
