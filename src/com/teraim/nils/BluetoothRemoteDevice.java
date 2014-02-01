package com.teraim.nils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.teraim.nils.exceptions.BluetoothDeviceExtra;
import com.teraim.nils.exceptions.BluetoothDevicesNotPaired;

public class BluetoothRemoteDevice extends Service implements RemoteDevice {



	private static BluetoothRemoteDevice me =null;
	private static BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
	//final Activity mActivity;

	public final static String SYNK_SERVICE_STOPPED = "com.teraim.nils.synkstopped";
	public final static String SYNK_SERVICE_STARTED = "com.teraim.nils.synkstarted";
	public final static String SYNK_SERVICE_MESSAGE_RECEIVED = "com.teraim.nils.message";
	public final static String SYNK_SERVICE_CONNECTED = "com.teraim.nils.synk_connected";
	public final static String SYNK_SERVICE_CLIENT_CONNECT_FAIL = "com.teraim.nils.synk_no_connect";
	public final static String SYNK_SERVICE_SERVER_CONNECT_FAIL = "com.teraim.nils.synk_lost_connect";
	public final static String SYNK_PING_MESSAGE_RECEIVED = "com.teraim.nils.ping";
	public final static String SYNK_NO_BONDED_DEVICE = "com.teraim.nils.binderror";

	public final static int SYNK_SEARCHING = 0;
	public final static int SYNK_RUNNING = 1;
	public final static int SYNK_STOPPED = 2;
	

	//Ping_delay
	protected static final long PING_DELAY = 5000;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.local_service_started;



	@Override
	public IBinder onBind(Intent arg0) {

		return iBinder;
	}

	private IBinder iBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		BluetoothRemoteDevice getBinder() {
			return BluetoothRemoteDevice.this;
		}
	}

	private NotificationManager mNM;
	private BroadcastReceiver brr=null;
	//Try pinging five times. Before giving up.
	private int pingC = 5;
	private GlobalState global;
	@Override
	public void onCreate() {
		
		global = GlobalState.getInstance(this);
		Log.d("NILS","Service on create");
		me = this;
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		global.setSyncStatus(SYNK_SEARCHING);
		//showNotification();
		brr = new BroadcastReceiver() {
			@Override
			public void onReceive(Context ctx, Intent intent) {
				final String action = intent.getAction();
				//If message fails, try to ping until sister replies.
				if (action.equals(BluetoothRemoteDevice.SYNK_SERVICE_SERVER_CONNECT_FAIL)) {
					Toast.makeText(me, "Förlorade kontakten med andra dosan", Toast.LENGTH_LONG).show();
					stop();
				}
				else if (action.equals(BluetoothRemoteDevice.SYNK_SERVICE_CLIENT_CONNECT_FAIL)) {
					//Try to ping again in a while if still running.
					new Handler().postDelayed(new Runnable() {
						public void run() {
							if (pingC>0) {
								pingC--;
								Toast.makeText(me, "Försök kontakta andra dosan. Försök kvar ("+pingC+")", Toast.LENGTH_LONG).show();
								ping();
							} else
								//after five attempts, stop the sync, and shutdown bluetooth.
								me.stop();
						}
					}, PING_DELAY);
					
				}

				
				else if (action.equals(BluetoothRemoteDevice.SYNK_SERVICE_MESSAGE_RECEIVED))
					Toast.makeText(me, intent.getStringExtra("MSG"), Toast.LENGTH_LONG).show();
				else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
					final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
							BluetoothAdapter.ERROR);
					switch (state) {
					case BluetoothAdapter.STATE_OFF:
						Log.d("BT","Bluetooth off");
						stop();
						break;
					case BluetoothAdapter.STATE_TURNING_OFF:
						Log.d("BT","Bluetooth turning off");
						break;
					case BluetoothAdapter.STATE_ON:
						Log.d("BT","Bluetooth on...starting server");
						startServer();
						ping();
						break;
					case BluetoothAdapter.STATE_TURNING_ON:
						Log.d("BT","Bluetooth turning on");
						break;
					}
				}

			}};
			//Listen for the bluetooth to start if not started.
			//When bluetooth runs, call start server. Then try to ping the server.
			IntentFilter ifi = new IntentFilter();
			ifi.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			ifi.addAction(BluetoothRemoteDevice.SYNK_SERVICE_CLIENT_CONNECT_FAIL);
			ifi.addAction(BluetoothRemoteDevice.SYNK_SERVICE_SERVER_CONNECT_FAIL);
			ifi.addAction(BluetoothRemoteDevice.SYNK_SERVICE_STOPPED);
			ifi.addAction(BluetoothRemoteDevice.SYNK_SERVICE_MESSAGE_RECEIVED);
			
			this.registerReceiver(brr, ifi);
			
			
			BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
			if (ba==null) {
				Log.e("NILS","bt adaptor null in bluetoothremotedevice service CREATE");
				return;
			} 
			if (!ba.isEnabled())	
				ba.enable();
			else {
				startServer();
				ping();
			}
			
	
	}


	private void ping() {
		//Send a ping to see if we can connect straight away.
		Log.d("NILS","Sending ping");
		send(new Ping());
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);
		// Tell the user we stopped.
		Toast.makeText(this, "Synkservice avstängd", Toast.LENGTH_SHORT).show();
		if (client!=null)
			client.cancel();
		if (server!=null)
			server.cancel();
		if (connected_T !=null)
			connected_T.cancel();
		connected_T=null;
		if(BluetoothAdapter.getDefaultAdapter().isEnabled())
			BluetoothAdapter.getDefaultAdapter().disable();
		global.setSyncStatus(SYNK_STOPPED);
		Intent intent = new Intent();
		intent.setAction(SYNK_SERVICE_STOPPED);
		this.sendBroadcast(intent);
		this.unregisterReceiver(brr);
	}


	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the expanded notification
		CharSequence text = getText(R.string.local_service_started);

		// Set the icon, scrolling text and timestamp

		// Notification noti = new 

		Notification noti = new Notification.Builder(getApplicationContext())
		.setContentTitle("Not sure what this is ")
		.setContentText("or this one?")
		.setSmallIcon(R.drawable.glaciar)
		.setLargeIcon(drawableToBitmap(getResources().getDrawable(R.drawable.glaciar_d))).
		getNotification();
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, BluetoothRemoteDevice.class), 0);

		// Set the info for the views that show in the notification panel.
		noti.setLatestEventInfo(this, "absolutely no idea what this should be",
				text, contentIntent);

		// Send the notification.
		mNM.notify(NOTIFICATION, noti);
	}

	public static Bitmap drawableToBitmap (Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable)drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap); 
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public void stop() {
		this.stopSelf();
	}

	public void restart() {
		startServer();
	}
	/*
	private void init() throws BluetoothNotSupportedException {
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
	 */

	ClientConnectThread client=null;
	AcceptThread server=null;
	ConnectedThread connected_T = null;


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
			Log.d("NILS","Device type major class: "+pair.getBluetoothClass().getMajorDeviceClass());
			Log.d("NILS","Device type majorminor class: "+pair.getBluetoothClass().getDeviceClass());
			client = new ClientConnectThread(this,pair);
			client.start();
			if (pairedDevices.size()>1)
				Log.e("NILS","Error: More than one bonded device");
			//TODO: TURN  THIS ON
			//throw new BluetoothDeviceExtra();
		}
		//caller need to wait for socket.

	}


	private void startServer() {
		//Create server thread
		Log.d("NILS","Trying to start server.");
		server = new AcceptThread();
		server.start();
		//Tell the world.
		Intent startI = new Intent();
		startI.setAction(SYNK_SERVICE_STARTED);
		me.sendBroadcast(startI);

	}

	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client code
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("NILS", Constants.getmyUUID());
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
					e.printStackTrace();
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
		connected_T = new ConnectedThread(getApplicationContext(),socket);
		connected_T.start();
		Log.d("NILS","connected thread started.");
		//if any delayed messages - send them.
		Iterator<Object> it = msgBuf.iterator();
		//Send any buffered messages.
		Object msg;
		while(it.hasNext()) {
			msg = it.next();
			connected_T.write(msg);
			Log.d("NILS","Wrote "+msg+" to socket");
		}
		//Send a message that service is now connected.
		global.setSyncStatus(SYNK_RUNNING);
		Intent intent = new Intent();
		intent.setAction(SYNK_SERVICE_CONNECTED);
		this.sendBroadcast(intent);

	}

	//This Handler will recieve a byte array and decide what to do with it.



	/*
   	Handler mHandler =  new Handler() {
		@Override
	    public void handleMessage(Message msg) {
	  		Intent intent = new Intent(getApplicationContext(), null);
			intent.setAction("com.teraim.nils.bluetooth");

	    	if (msg.obj instanceof Parameter) {
	    		Parameter par = (Parameter)msg.obj;
	    		Log.d("Parameter","Key "+par.mkey+" Val: "+par.mvalue);
	    		intent.putExtra("MSG","Received parameter: "+par.mkey+" value: "+par.mvalue);
	    	}
	    	else if (msg.obj instanceof String) {
	    		intent.putExtra("MSG",(String)msg.obj);
	    	}
	    	me.sendBroadcast(intent);
	    }
	 */

	/*String rec = new String((byte[])msg.obj).substring(0, msg.arg1);
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
	 */

	final static int MESSAGE_READ = 1;


	private class ClientConnectThread extends Thread {

		private final BluetoothSocket mmSocket;
		private Context mContext;
		public ClientConnectThread(Context ctx,BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server code
				tmp = device.createRfcommSocketToServiceRecord(Constants.getmyUUID());
			} catch (IOException e) { }
			mmSocket = tmp;
			mContext = ctx;
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
				Log.d("NILS","Failed to connect to sister");
				connectException.printStackTrace();
				//Tell the world.
				Intent intent = new Intent();
				intent.setAction(SYNK_SERVICE_CLIENT_CONNECT_FAIL);
				mContext.sendBroadcast(intent);
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
		//private final InputStream mmInStream;
		//private final OutputStream mmOutStream;
		private final ObjectOutputStream obj_out;
		private final ObjectInputStream obj_in;
		private Context mContext;
		public ConnectedThread(Context ctx, BluetoothSocket socket) {
			mContext = ctx;
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			ObjectOutputStream tmp_obj_out=null;
			ObjectInputStream tmp_obj_in=null;


			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
				//stream for sending objects.
				tmp_obj_out = new
						ObjectOutputStream (tmpOut);
				tmp_obj_in = 
						new ObjectInputStream (tmpIn);

			} catch (IOException e) { }

			obj_out = tmp_obj_out;
			obj_in = tmp_obj_in;
			//mmInStream = tmpIn;
			//mmOutStream = tmpOut;

		}

		public void run() {
			//byte[] buffer = new byte[1024];  // buffer store for the stream
			//int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			Object o=null;
			while (true) {
				try {
					// Read from the InputStream
					try {
						o = obj_in.readObject();
					} catch (ClassNotFoundException e) {
						Log.e("NILS","CLASS NOT FOUND IN Stream");
						e.printStackTrace();
					}
					// Send the obtained bytes to the UI activity
					//mHandler.obtainMessage(MESSAGE_READ, -1, -1, o).sendToTarget();
					sendToUI(o);

				} catch (IOException e) {
					Intent intent = new Intent();
					intent.setAction(SYNK_SERVICE_SERVER_CONNECT_FAIL);
					mContext.sendBroadcast(intent);
					break;
				}

			}
		}

		// Write object with ObjectOutputStream
		public void write(Object o) {
			try {
				obj_out.writeObject(o);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void sendToUI(Object o) {
			Intent intent = new Intent();

			if (o instanceof Parameter) {
				intent.setAction(SYNK_SERVICE_MESSAGE_RECEIVED);
				Parameter par = (Parameter)o;
				Log.d("Parameter","Key "+par.mkey+" Val: "+par.mvalue);
				intent.putExtra("MSG","Received parameter: "+par.mkey+" value: "+par.mvalue);
			}
			else if (o instanceof String) {
				intent.setAction(SYNK_SERVICE_MESSAGE_RECEIVED);
				intent.putExtra("MSG",(String)o);

			}
			else if (o instanceof Ping) {
				intent.setAction(SYNK_PING_MESSAGE_RECEIVED);
			}
			mContext.sendBroadcast(intent);

		}

		/* Call this from the main activity to shutdown the connection */

		public void cancel() {
			try {
				obj_out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				obj_in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				mmSocket.close();
			} catch (IOException e) { e.printStackTrace();}
		}
	}


	Queue<Object>msgBuf = new LinkedList<Object>();



	@Override
	public void sendParameter(String key, String value, int Scope) {
		send(new Parameter(key,value));
	}


	public void push(Object obj) {

	}

	/*
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
	 */
	@Override
	public void sendMessage(String msg) {
		send(msg);
	}	


	public void send(Object o) {
		//If we have a socket - fine. 
		//Else this device need to act client to generate socket.
		//Sending will be delayed. Push message to stack while waiting.
		Log.d("NILS","Trying to send: "+o.toString());
		if(connected_T!=null)
			//connected_T.write(msg.getBytes());
			connected_T.write(o);
		else {
			Log.d("NILS","No socket...trying to aquire..");
			try {
				this.prepare();
				msgBuf.add(o);

			} catch (BluetoothDevicesNotPaired e) {
				Toast.makeText(getBaseContext(),"No bounded (paired) device found",Toast.LENGTH_LONG).show();
				Intent intent = new Intent();
				intent.setAction(BluetoothRemoteDevice.SYNK_NO_BONDED_DEVICE);
				sendBroadcast(intent);
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