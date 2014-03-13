package com.teraim.nils.bluetooth;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.Variable;

public class SlaveMessageHandler implements MessageHandlerI {

	private GlobalState gs;

	@Override
	public void handleMessage(Object message) {
		
		if (message instanceof Pong) {
			Pong p = (Pong)message;
			
			Variable ruta = gs.getArtLista().getVariableInstance("Current_Ruta");
			Variable provyta = gs.getArtLista().getVariableInstance("Current_Provyta");
			ruta.setValue(p.ruta);
			provyta.setValue(p.provyta);
			
		}
		
	}
	
	public SlaveMessageHandler(GlobalState gs) {
		this.gs=gs;
	}

}
