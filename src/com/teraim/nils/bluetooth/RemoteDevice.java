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

	void sendParameter(String key, String value, int MsgType);
		
	
}
