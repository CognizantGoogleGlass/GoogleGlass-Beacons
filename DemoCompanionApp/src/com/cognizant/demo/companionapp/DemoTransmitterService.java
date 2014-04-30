package com.cognizant.demo.companionapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.cognizant.demo.util.Constants;
import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

//This DemoTransmitterService is responsible for start the ibeacon service,
//bluetooth connection as a service. Also pass the JSON data according to the ibeacon proximity ID
//to the connected device.

@SuppressLint("NewApi")
public class DemoTransmitterService extends Service implements IBeaconConsumer {

	public static final String DEVICE_NAME = null;
	public static final String TOAST = null;
	// private String locationfilecontent = null;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGEDEVICENAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device
	private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
	String paireddevicesname = "";
	private String paireddevicesaddress = "";
	private static BluetoothSocket mmSocket = null;
	BluetoothDevice mmDevice = null;
	private List<UUID> mUuids = new ArrayList<UUID>();
	boolean isConnected = false;
	protected StringBuffer mOutStringBuffer;
	private ConnectedThread mConnectedThread;
	private ArrayList<ConnectedThread> mConnThreads;
	private String iBeaconUUID = null;
	private double proxDistance;

	protected static final String TAG = "DemoTransmitterService";
	private String serviceRunning =Constants.SERVICERUNNINGMSG;
	private String serviceStopped = Constants.SERVICESTOPPEDMSG;
	private IBeaconManager iBeaconManager;
	CountDownTimer timer, cdt;
	private String companionId = Constants.COMPANIONID;
	Handler hand;

	@Override
	public void onCreate() {
		serviceInit();
		Log.i(TAG, "end of oncreate");
	}

	private void serviceInit() {
		// Creating the instance for iBeaconManger
		iBeaconManager = IBeaconManager.getInstanceForApplication(this);
		mOutStringBuffer = new StringBuffer("");
		mUuids.add(UUID.fromString(companionId));
		mConnThreads = new ArrayList<ConnectedThread>();
		SharedPreferences deviceDetails = PreferenceManager
				.getDefaultSharedPreferences(this);
		// These variables contains the device name and device address which you
		// choose from
		// connect device from the settings.
		paireddevicesname = deviceDetails.getString(Constants.DEVICENAME, "");
		paireddevicesaddress = deviceDetails.getString(Constants.DEVICEADDRESS, "");
	}

	/**
	 * This method will called when you click the start sync in companion app,
	 * this is a service method which will run in background even app is closed.
	 * Timer is running for ever 500 millisecond for checking connection between
	 * devices.
	 */

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.i(TAG, "onStartCommand");

		if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
			mBtAdapter.enable();
		}
		final BluetoothDevice device = mBtAdapter
				.getRemoteDevice(paireddevicesaddress);
		// Attempt to connect to the device
		timer = new CountDownTimer(500, 500) {
			@Override
			public void onTick(final long millisUntilFinished) {
				// TODO Auto-generated method stub
			}

			/**
			 * Method invoke when timer reached.
			 */

			@Override
			public void onFinish() {
				Log.i(TAG, "Timer Called");
				// TODO Auto-generated method stub
				logToDisplay(serviceRunning);
				// Bluetooth connection is called in service.
				connect(device);
				timer.start();
			}
		}.start();
		// This line will bind the ibeacon service
		iBeaconManager.bind(this);

		// Timer for Web Service, Webservice will called every 10 seconds
		// cdt = new CountDownTimer(10000, 1000) {
		//
		// @Override
		// public void onTick(long millisUntilFinished) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onFinish() {
		// // TODO Auto-generated method stub
		// hand = new Handler();
		// hand.postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// if (isNetworkAvailable()) {
		// Here _details will contains the JSON response from
		// webservice and this can be used for data population
		// _details = webServiceCall();
		// }
		// }
		// }, 1000);
		// cdt.start();
		// }
		// }.start();

		return START_FLAG_REDELIVERY;
	}

	// This method will connect the bluetooth device using socket

	@SuppressLint("NewApi")
	public synchronized void connect(BluetoothDevice device) {
		mmDevice = device;
		BluetoothSocket tmp = null;
		try {
			tmp = device.createRfcommSocketToServiceRecord(mUuids.get(0));
			Log.i(TAG, "connected");
			// break;
		} catch (IOException e) {
			Log.e(TAG, "create() failed", e);
		}
		if (mmSocket==tmp) {
			try {
				mmSocket.close();
				mmSocket = null;
			} catch (IOException e) {
				Log.d("IOException", e.toString());
			}
		}
		mmSocket = tmp;
		if (mmSocket != null && mmSocket == tmp) {
			try {
				mmSocket.connect();
				mConnectedThread = new ConnectedThread(mmSocket);
				mConnectedThread.start();
				// Add each connected thread to an array
				mConnThreads.add(mConnectedThread);
				if (mmSocket.isConnected()) {
					isConnected = true;
				}
				isConnected = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d("IOException", e.toString());
			}
		}
	}

	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				Log.i("TAG", "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case STATE_CONNECTED:
					break;
				case STATE_CONNECTING:
					break;
				case STATE_LISTEN:
				case STATE_NONE:
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				final String writeMessage = new String(writeBuf);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				final String readMessage = new String(readBuf, 0, msg.arg1);
				if (readMessage.length() > 0) {
					Toast.makeText(getApplicationContext(), readMessage,
							Toast.LENGTH_SHORT).show();
				}
				break;
			case MESSAGEDEVICENAME:
				// save the connected device's name
				Toast.makeText(getApplicationContext(), "Connected to ",
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	// This method will send the data to the open socket in wearable device
	private void sendMessage(final String message) {
		if (message.length() > 0) {
			final String toSend = message;
			byte[] send = toSend.getBytes();
			write(send);
		}
	}

	// This method will write the data in buffer and send using open socket
	public void write(byte[] outStream) {
		// When writing, try to write out to all connected threads
		for (int i = 0; i < mConnThreads.size(); i++) {
			try {
				// Create temporary object
				ConnectedThread cThread;
				// Synchronize a copy of the ConnectedThread
				synchronized (this) {
					cThread = mConnThreads.get(i);
				}
				// Perform the write unsynchronized
				cThread.write(outStream);
			} catch (Exception e) {
				try {
					mmSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					Log.d(TAG, e1.toString());
				}
				mmSocket = null;
			}
		}
	}

	/**
	 * Class that handles the socket connection
	 * 
	 * @author 409870
	 * 
	 */

	private class ConnectedThread extends Thread {
		// protected final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			// Log.d(TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.d(TAG, e.toString());
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			// Log.i(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes = 0;

			// Keep listening to the InputStream while connected for future
			// enhancement
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);

				} catch (IOException e) {
					// Log.e(TAG, "disconnected", e);
					// connectionLost();
					break;
				}
			}
		}

		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
				// Share the sent message back to the UI Activity
				mHandler.obtainMessage(DemoTransmitterService.MESSAGE_WRITE,
						-1, -1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}
	}

	/**
	 * This method is responsible for get the ibeacon. This will run in
	 * background and get the available ibeacon and send the available
	 * proximityUUID to loadLocationData method and get the corresponding data
	 * from the mapped JSON file and push the data to wearable that connected.
	 * 
	 * This services is started in onStartCommand - iBeaconManager.bind();
	 */

	@Override
	public void onIBeaconServiceConnect() {
		iBeaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons,
					Region region) {
				if (!iBeacons.isEmpty()) {
					Log.d(TAG, "inside didRangeBeaconsInRegion");
					// Getting proximity UUID of the ibeacon object that
					// received
					iBeaconUUID = iBeacons.iterator().next().getProximityUuid();
					proxDistance = iBeacons.iterator().next().getAccuracy();
					Log.i(TAG, iBeaconUUID + "!!!" + proxDistance);
					final String data = loadLocationData(iBeaconUUID);
					if (data.contains(Constants.FILE_NOT_FOUND)
							|| data.contains(Constants.TAG_FILE_ERROR)) {
						logToDisplay(Constants.FILE_NOT_FOUND);
						Log.d(TAG, Constants.FILE_NOT_FOUND);
					} else {
						Log.d(TAG, "Beacon Details Send::::  "
								+ loadLocationData(iBeaconUUID));
						sendMessage(loadLocationData(iBeaconUUID));
					}

				}
			}

		});

		try {
			iBeaconManager.startRangingBeaconsInRegion(new Region(
					"myRangingUniqueId", null, null, null));
			Log.d(TAG,
					"inside try of iBeaconManager.startRangingBeaconsInRegion");
		} catch (RemoteException e) {
			Log.d(TAG,
					"inside catch of iBeaconManager.startRangingBeaconsInRegion");
		}
	}

	/**
	 * This method is used to get the data from the JSON file and send the data
	 * to wearable device.
	 * 
	 * @param proximityId
	 * @return
	 */

	public String loadLocationData(String proximityId) {
		Log.d(TAG, "inside loadLocationData ");
		final JSONObject location = new JSONObject();
		final JSONArray jsonArray = new JSONArray();
		final String locationfilecontent = readStringFromAssetsFile(Constants.FILE_NAME);
		Log.d(TAG, "inside loadLocationData ; value of location_file_content"
				+ locationfilecontent);
		try {
			Log.d(TAG, "inside loadLocationData try");
			JSONObject locationJson = new JSONObject(
					locationfilecontent.toString());

			final JSONArray locationArray = locationJson.getJSONArray(Constants.TAG_ASSETS);
			JSONObject loc = null;

			String pid = proximityId;
			for (int i = 0; i < locationArray.length(); i++) {
				loc = locationArray.getJSONObject(i);
				String locationid = loc.getString(Constants.TAG_BEACONID);
				// JSON object
				Log.d(TAG, "inside loadLocationData for loop");
				if (locationid.equalsIgnoreCase(pid)) {
					Log.d(TAG, "inside 	if (location_id.equalsIgnoreCase(pid))");
					location.put(Constants.TAG_BEACONID, locationid);
					String direction = loc.getString(Constants.TAG_DIRECTION);
					location.put(Constants.TAG_DIRECTION, direction);
					String demoId = loc.getString(Constants.TAG_ID);
					location.put(Constants.TAG_ID, demoId);

					final JSONArray caseArray = loc.getJSONArray(Constants.TAG_CASEINFO);
					for (int j = 0; j < caseArray.length(); j++) {
						final JSONObject caseinfo = caseArray.getJSONObject(j);
						jsonArray.put(caseinfo);
					}
					location.put(Constants.TAG_CASEINFO, jsonArray);
				}

			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			Log.d(TAG, "inside loadLocationData catch");
			e1.printStackTrace();
		}
		Log.d(TAG, "return loadLocationData");
		return location.toString();
	}

	/**
	 * This method for read the data from the JSON file
	 * 
	 * @param fileName
	 * @return
	 */

	public String readStringFromAssetsFile(final String fileName) {
		String data = "";
		Log.i(TAG, "called read string from assets file");
		try {
			final InputStream inputStream = getAssets().open(fileName);
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);

			String line = null;
			StringBuffer stringBuffer = new StringBuffer();
			// check if file is not empty
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);
			}
			data = stringBuffer.toString();
		} catch (Exception e) {
			Log.d("Exception",e.toString());
			data = "";
		}
		// System.out.println(data);
		return data;
	}

	public void logToDisplay(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	// Webservice calling method
	public String webServiceCall() {
		final String serviceURL = "https://testservice.com";
		final String deviceToken = "TBhRAykA/Rthas2h5RPkQkZhikmMyRzfeIxQo2NfCUrv6mSPs/OaC61qdIJ9cURqD3avYW0VdQzpWUKTxB44";
		final String appVersion = "5.7.0";

		HttpClient cliente = new DefaultHttpClient();
		final JSONObject reqData = new JSONObject();
		String respText = null;
		try {
			final HttpPost post = new HttpPost(serviceURL);
			reqData.put("deviceToken", deviceToken);
			post.setHeader("X-Client-Platform", "Android");
			post.setHeader("X-Application-Version", appVersion);
			final StringEntity entityData = new StringEntity(reqData.toString());
			entityData.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			post.setEntity(entityData);
			final HttpResponse respuesta = cliente.execute(post);
			final HttpEntity entity = respuesta.getEntity();
			respText = getASCIIContentFromEntity(entity);
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), ex.toString(),
					Toast.LENGTH_SHORT).show();
		}
		return respText;
	}

	protected String getASCIIContentFromEntity(HttpEntity entity)
			throws IllegalStateException, IOException {
		Log.d(TAG, "INSIDE getASCIIContentFromEntity");
		final InputStream in = entity.getContent();
		final StringBuffer out = new StringBuffer();
		int num = 1;
		while (num > 0) {
			final byte[] b = new byte[4096];
			num = in.read(b);
			if (num > 0)
				out.append(new String(b, 0, num));
		}
		Log.d(TAG, "RETURN FROM getASCIIContentFromEntity");
		return out.toString();
	}

	// On Kill the service, the following method will called and close the
	// socket
	// connection, ibeacon service and timer
	@Override
	public void onDestroy() {
		try {
			logToDisplay(serviceStopped);
			timer.cancel();
			mmSocket.close();
			iBeaconManager.unBind(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i(TAG, "ON DESTROY IN SERVICE CALLED");
	}
}
