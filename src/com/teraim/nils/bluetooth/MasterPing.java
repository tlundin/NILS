package com.teraim.nils.bluetooth;

import java.io.Serializable;
/**
 * Ping message.
 * @author Terje
 *
 */
public class MasterPing implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String ruta,provyta;
	
	public MasterPing(String ruta, String provyta) {
		this.ruta=ruta;
		this.provyta=provyta;
	}

	/**
	 * @return the ruta
	 */
	public String getRuta() {
		return ruta;
	}

	/**
	 * @return the provyta
	 */
	public String getProvyta() {
		return provyta;
	}
	
}

