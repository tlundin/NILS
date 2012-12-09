package com.teraim.nils;

public interface CommListener {

	static int CONN_LOST = -1;
	static int TIME_OUT = -2;
	
	public void onValueRecievedCb(String value);
	public void onError(int errCode);
}
