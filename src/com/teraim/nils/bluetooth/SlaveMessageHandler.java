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
		if (message instanceof MasterPing) {
			MasterPing p = (MasterPing)message;		
			Variable ruta = gs.getArtLista().getVariableInstance("Current_Ruta");
			Variable provyta = gs.getArtLista().getVariableInstance("Current_Provyta");
			ruta.setValue(p.getRuta());
			provyta.setValue(p.getProvyta());
			Log.d("nils","Got MasterPong");			
			gs.triggerTransfer();
			
		} 
		
		 else if (message instanceof SlavePing) {
			gs.sendEvent(BluetoothConnectionService.SAME_SAME_SYNDROME);
		}
	}

}
