package com.teraim.nils.bluetooth;

import java.io.Serializable;

import android.util.Log;

public class SyncEntry implements Serializable {

	private static final long serialVersionUID = 862826293136691823L;
	boolean isInsert = false;
	private String changes;
	private String timeStamp;
	public SyncEntry(String a,String changes,String timeStamp) {
		if (a.equals("I"))
			isInsert = true;
		else if (a.equals("D"))
			isInsert = false;
		else {
			Log.e("nils","Unknown type of Sync action!: "+a);
			isInsert = true;
		}
		this.changes=changes;
		this.timeStamp=timeStamp;
	}

	public boolean isInsert() {
		return isInsert;
	}

	public String getChanges() {
		return changes;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

}
