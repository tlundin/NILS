package com.teraim.nils.bluetooth;


/**
 * 
 * @author Terje
 *
 *This implements a remote device of a certain color. It should be tech independent.
 */
public interface RemoteDevice  {

	
	public void sendMessage(String msg) ;
		
	public void getParameter(String key) ;

	//Sends a parameter with a given scope (namespace). Scope is either RUTA or 
	public static final int SCOPE_PROVYTA=0;
	public static final int SCOPE_RUTA=1;
	public static final int SCOPE_GLOBAL=2;
	public static final int SCOPE_DELYTA = 3;

	void sendParameter(String key, String value, int Scope);
		
	
}
