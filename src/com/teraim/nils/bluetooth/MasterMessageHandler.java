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
			Log.d("nils","Received Ping from Slave");
			o.addRow("[--->PING]");			
			gs.triggerTransfer();
			
		}  else if (message instanceof MasterPing) {
			gs.sendEvent(BluetoothConnectionService.SAME_SAME_SYNDROME);
			
		}

	}

}
