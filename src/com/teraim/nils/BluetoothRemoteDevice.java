package com.teraim.nils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.teraim.nils.exceptions.BluetoothDeviceExtra;
import com.teraim.nils.exceptions.BluetoothDevicesNotPaired;
import com.teraim.nils.exceptions.BluetoothNotSupportedException;

public class BluetoothRemoteDevice extends Activity implements RemoteDevice {

	private static BluetoothRemoteDevice me =null;
	private static BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
	//final Activity mActivity;
	private static boolean hasSocket = false;
	//Callback ID for BT
	private final static int REQUEST_ENABLE_BT = 11;	
	private final static Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	//Start the Server thread to listen for incoming calls.
	//Also turn Bluetooth on if off.
	
	public static void init() throws BluetoothNotSupportedException {
		if (mBluetoothAdapter==null) {
			Log.e("NILS","This device does not support bluetooth!!");
			throw new BluetoothNotSupportedException();
		}
		if (me==null)
			me = new BluetoothRemoteDevice();	
		if (!mBluetoothAdapter.isEnabled()) {
		    me.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else
			me.startServer();	

		
	}
	
	
	//Start a Client thread for communication.
	//Create an instance of this singleton if not already existing.
	
	public void prepare() throws BluetoothDevicesNotPaired, BluetoothDeviceExtra {
		
		//check if there is a bonded device.
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		BluetoothDevice pair;
		if (pairedDevices.isEmpty()) {
			Log.e("NILS","DIDNT FIND ANY PAIRED DEVICE");
			throw new BluetoothDevicesNotPaired();
		}
		else {
			pair = pairedDevices.iterator().next();
			Log.d("NILS","FIRST PAIRED DEVICE IS: "+pair.getName());
		
		(new ClientConnectThread(pair)).start();
		if (pairedDevices.size()>1)
			Log.e("NILS","Error: More than one bonded device");
		//TODO: TURN  THIS ON
			//throw new BluetoothDeviceExtra();
		}
		//caller need to wait for socket.
		
	}
	


	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        Log.d("NILS", "Bluetooth should be turned on now. " + resultCode);
	        startServer();
	    }	

	
	private void startServer() {
		//Create server thread
		new AcceptThread().start();
	}
	
	private class AcceptThread extends Thread {
	    private final BluetoothServerSocket mmServerSocket;
	 
	    public AcceptThread() {
	        // Use a temporary object that is later assigned to mmServerSocket,
	        // because mmServerSocket is final
	        BluetoothServerSocket tmp = null;
	        try {
	            // MY_UUID is the app's UUID string, also used by the client code
	            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("NILS", CommonVars.cv().getmyUUID());
	        } catch (IOException e) { }
	        mmServerSocket = tmp;
	    }
	 
	    public void run() {
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        Log.d("NILS","Server is started");
	        while (true) {
	            try {
	                socket = mmServerSocket.accept();
	                Log.d("NILS","SERVER got a SOCKET");
	                
	            } catch (IOException e) {
	            	 Log.e("NILS","Exception in AcceptThread");
	                break;
	            }
	            // If a connection was accepted
	            if (socket != null) {
	                // Do work to manage the connection (in a separate thread)
	            	Log.d("NILS","calling managesocket");
	                manageConnectedSocket(socket);
	                try {
						mmServerSocket.close();
					} catch (IOException e) {
						
					}
	                break;
	            }
	        }
	    }
	 


		/** Will cancel the listening socket, and cause the thread to finish */
	    public void cancel() {
	        try {
	            mmServerSocket.close();
	        } catch (IOException e) { }
	    }
	}




	
    private void manageConnectedSocket(BluetoothSocket socket) {  	
		connected_T = new ConnectedThread(socket);
		connected_T.start();
		hasSocket = true;
		Log.d("NILS","connected thread started.");
		//if any delayed messages - send them.
		Iterator<String> it = msgBuf.iterator();
		//Send any buffered messages.
		String msg;
		while(it.hasNext()) {
			msg = it.next();
			connected_T.write(msg.getBytes());
			Log.d("NILS","Wrote "+msg+" to socket");
		}
	
	}
	
    ConnectedThread connected_T = null;
    
    //This Handler will recieve a byte array and decide what to do with it.
    
   	Handler mHandler =  new Handler () {
	    @Override
	    public void handleMessage(Message msg) {
	    	String rec = new String((byte[])msg.obj).substring(0, msg.arg1);
	    	Log.d("NILS","Got Message "+rec);
	    	String msgType = rec.substring(0, 1);
    		rec = rec.substring(1,rec.length());
	    	if (msgType.equals("M")) 
	    		//Not sure what to do with normal messages...
	    		Log.d("NILS","Got message: "+msg);
	    	else {
	    		String[] tmp = rec.split("zzz");
	    		Log.d("NILS","Found key value");
	    		if (tmp!=null&&tmp.length>0) {
	    			Log.d("NILS","Length: "+tmp.length);
	    			Log.d("NILS","Key: "+tmp[0]);
	    			Log.d("NILS","Value: "+tmp[1]);
	    		}
	    		//Depending on message type, save in different files.
	    		
	    		if (msgType.equals("@")) 
	    			CommonVars.cv().putG(tmp[0], tmp[1]);
	    		else if (msgType.equals("D"))
	    			CommonVars.cv().putD(tmp[0], tmp[1]);
	    		else if (msgType.equals("P"))
	    			CommonVars.cv().putP(tmp[0], tmp[1]);	    			
	    		else if (msgType.equals("R"))
	    			CommonVars.cv().putR(tmp[0], tmp[1]);	    	
	    	}
	    }
   	};
   	
   	final static int MESSAGE_READ = 1;
   	
   	
	private class ClientConnectThread extends Thread {
		
	    private final BluetoothSocket mmSocket;
	 
	    public ClientConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(CommonVars.cv().getmyUUID());
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	        // Cancel discovery because it will slow down the connection
	        mBluetoothAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            Log.d("NILS","Trying to connect to sister device..");
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
            Log.d("NILS","connected to sister device..");

	        manageConnectedSocket(mmSocket);
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}	
	
	
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
	            } catch (IOException e) {
	                break;
	            }
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	

	Queue<String>msgBuf = new LinkedList<String>();
 	
	

	@Override
	public void sendParameter(String key, String value, int Scope) {
		String header;
		switch (Scope) {
		case SCOPE_DELYTA:
			header = "D";
			break;
		case SCOPE_PROVYTA:
			header = "P";
			break;
		case SCOPE_RUTA:
			header = "R";
			break;
		default:
			header = "@";
		}
		send(header+key+"zzz"+value);
	}

	@Override
	public void sendMessage(String msg) {
		send("M"+msg);
	}	

	
	public void send(String msg) {
		//If we have a socket - fine. 
		//Else this device need to act client to generate socket.
		//Sending will be delayed. Push message to stack while waiting.
		Log.d("NILS","Trying to send: "+msg);
		if(hasSocket)
			connected_T.write(msg.getBytes());
		else {
			Log.d("NILS","No socket...trying to aquire..");
			try {
				this.prepare();
				msgBuf.add(msg);
				
			} catch (BluetoothDevicesNotPaired e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BluetoothDeviceExtra e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
	}

	@Override
	public void getParameter(String key) {
		
	}


	//this cannot be called before openChannel.
	
	public static RemoteDevice getSingleton() {
		return me;
	}



}