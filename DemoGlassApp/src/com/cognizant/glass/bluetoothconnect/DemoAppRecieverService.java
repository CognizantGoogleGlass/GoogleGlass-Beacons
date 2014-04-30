/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cognizant.glass.bluetoothconnect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cognizant.glass.demoapp.MainLauncherActivity;
import com.cognizant.glass.demoapp.R;
import com.cognizant.glass.demoapp.util.Constants;
import com.google.android.glass.timeline.LiveCard;

/**
 * @author 409870 the background service class that handles the connection.
 * 
 */
@SuppressLint("NewApi")
public class DemoAppRecieverService extends Service {
	private static final String NAME = "DemoAppRecieverService";
	private BluetoothAdapter mAdapter;

	/**
	 * the connection class objects.
	 */
	private AcceptThread mAcceptThread;
	private ConnectedThread mConnectedThread;

	private int mState;
	/**
	 * the connection state change variable declarations.
	 */
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final String TOAST = "toast";
	private static final String TAG = "DemoApp Reciever Service";
	private static final boolean D = true;

	private ArrayList<String> mDeviceAddresses;
	private ArrayList<ConnectedThread> mConnThreads;
	private ArrayList<BluetoothSocket> mSockets;

	/**
	 * the live card objects declaration.
	 */
	private LiveCard mLiveCard;
	private RemoteViews livecardView;
	private static final String LIVE_CARD_TAG = "my_card";
	public final static String MY_ACTION = "MY_ACTION";

	JSONObject displayJson;
	ArrayList<String> beaconList;

	private ArrayList<UUID> mUuids;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	// the Bluetooth UUID that is shared with the companion app.
	private String clientId = Constants.CLIENTID ;

	@Override
	public void onCreate() {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mDeviceAddresses = new ArrayList<String>();
		mConnThreads = new ArrayList<ConnectedThread>();
		mSockets = new ArrayList<BluetoothSocket>();
		mUuids = new ArrayList<UUID>();
		mUuids.add(UUID.fromString(clientId));
		beaconList = new ArrayList<String>();
		start();
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		// the incoming connection listener is started.
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
		return START_STICKY;
	}

	public synchronized void start() {
		if (D) {
			Log.d(TAG, "start");
		}
		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(final BluetoothSocket socket,
			BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connected");

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
		// Add each connected thread to an array
		mConnThreads.add(mConnectedThread);
		// Send the name of the connected device back to the UI Activity
		final Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(Constants.DEVICENAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D) {
			Log.d(TAG, "stop");
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		// Send a failure message back to the Activity
		final Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, "Device connection was lost");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted (or
	 * until cancelled).
	 */
	private class AcceptThread extends Thread {
		BluetoothServerSocket serverSocket = null;

		public void run() {
			if (D)
				Log.d(TAG, "BEGIN mAcceptThread" + this);
			setName("AcceptThread");
			BluetoothSocket socket = null;
			try {
				// listen for connection with the respective UUID from the
				// companion app and creates a communication socket
				serverSocket = mAdapter.listenUsingRfcommWithServiceRecord(
						NAME, mUuids.get(0));
				socket = serverSocket.accept();
				if (socket != null) {
					// the remote device address is recieved and saved.
					String address = socket.getRemoteDevice().getAddress();
					mSockets.add(socket);
					mDeviceAddresses.add(address);
					connected(socket, socket.getRemoteDevice());
				}
			} catch (IOException e) {
				Log.e(TAG, "accept() failed", e);
			}
			if (D) {
				Log.i(TAG, "END mAcceptThread");
			}
		}

		/**
		 * the socket is closed.
		 */
		public void cancel() {
			if (D)
				Log.d(TAG, "cancel " + this);
			try {
				serverSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	/**
	 * @author 409870
	 * 
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;

		public ConnectedThread(BluetoothSocket socket) {
			Log.d(TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;

		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI Activity
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		/**
		 * whenever connection gets lost or canceled the server socket is
		 * closed.
		 */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
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
				break;
			case MESSAGE_READ:

				byte[] readBuf = (byte[]) msg.obj;

				final String recieved_message = new String(readBuf, 0, msg.arg1);

				if (recieved_message.length() > 0
						&& !isForeground(getPackageName())) {
					try {
						// the received message is passed to
						// MainLauncherActivity for making the UI changes.
						Intent intent = new Intent();
						intent.setAction(MY_ACTION);
						intent.putExtra(Constants.SENDDATA, recieved_message);
						sendBroadcast(intent);

					} catch (Exception e) {
						e.printStackTrace();
					}

					// on receiving new tickets the live card notification is
					// triggered / updated the data in live card.
					try {
						displayJson = new JSONObject(recieved_message);
						publishCard(getApplicationContext(),
								displayJson.getJSONArray(Constants.TAG_CASEINFO));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				showToast("Device Connected");
				break;
			case MESSAGE_TOAST:
				// connection lost scenario handled...
				showToast(msg.getData().get(TOAST).toString());
				break;
			}
		}
	};

	public void showToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * @param packageName
	 * @return checks if a particular application is in the foreground
	 */
	public boolean isForeground(final String packageName) {
		// Get the Activity Manager
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		// Get a list of running tasks, we are only interested in the last one,
		// the top most so we give a 1 as parameter so we only get the topmost.
		final List<ActivityManager.RunningTaskInfo> task = manager
				.getRunningTasks(1);

		// Get the info we need for comparison.
		final ComponentName componentInfo = task.get(0).topActivity;

		// Check if it matches our package name.
		if (componentInfo.getPackageName().equals(packageName)
				&& componentInfo.getClassName().contains("SecondActivity"))
			return true;

		// If not then our app is not on the foreground.
		return false;
	}

	/**
	 * method handles the Live card notifications
	 * 
	 * @param context
	 * @param jsonArray
	 * 
	 *            whenever the user is in background and new message is received
	 *            a live card is created and if already present the values are
	 *            updated with the current message contents.
	 */
	private void publishCard(final Context context, final JSONArray jsonArray) {

		if (mLiveCard == null) {
			mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
		}
		livecardView = new RemoteViews(context.getPackageName(), R.layout.activity_second);
		try {
			if (jsonArray.length() > 1) {
				String imageName1 = jsonArray.getJSONObject(0)
						.getString(Constants.TAG_CASETITLE).toLowerCase();
				imageName1 = imageName1.replace(" ", "_");
				String imageName2 = jsonArray.getJSONObject(1)
						.getString(Constants.TAG_CASETITLE).toLowerCase();
				imageName2 = imageName2.replace(" ", "_");
				livecardView.setTextViewText(R.id.text, jsonArray.getJSONObject(0)
						.getString(Constants.TAG_CASETITLE));
				livecardView.setTextViewText(R.id.livecard_ticketNo, jsonArray
						.getJSONObject(0).getString(Constants.TAG_TICKETCOUNT)
						+ " ticket(s)");
				livecardView.setTextViewText(R.id.text2, jsonArray.getJSONObject(1)
						.getString(Constants.TAG_CASETITLE));
				livecardView.setTextViewText(R.id.livecard_ticketNo2, jsonArray
						.getJSONObject(1).getString(Constants.TAG_TICKETCOUNT)
						+ " ticket(s)");
				try {
					String fnm = imageName1; // this is image
												// file name
					final String image2 = imageName2;
					String packageName = getApplicationContext()
							.getPackageName();
					final int imgId = getResources().getIdentifier(
							packageName + ":drawable/" + fnm, null, null);
					final int imgId2 = getResources().getIdentifier(
							packageName + ":drawable/" + image2, null, null);
					livecardView.setImageViewBitmap(R.id.image1_livecard,
							BitmapFactory.decodeResource(getResources(), imgId));
					livecardView.setImageViewBitmap(R.id.image2_livecard, BitmapFactory
							.decodeResource(getResources(), imgId2));
				} catch (Exception e) {
					e.printStackTrace();
				}
				mLiveCard.setViews(livecardView);

			} else {
				livecardView.setTextViewText(R.id.text, jsonArray.getJSONObject(0)
						.getString(Constants.TAG_CASETITLE));
				livecardView.setTextViewText(R.id.livecard_ticketNo, jsonArray
						.getJSONObject(0).getString(Constants.TAG_TICKETCOUNT)
						+ " ticket(s)");
				livecardView.setTextViewText(R.id.text2, "");
				livecardView.setTextViewText(R.id.livecard_ticketNo2, "");
				mLiveCard.setViews(livecardView);

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// pending intent so that when user clicks the live card the
		// corresponding application is opened.
		Intent intent = new Intent(context, MainLauncherActivity.class);
		mLiveCard.setAction(PendingIntent.getActivity(context, 0, intent, 0));
		if (!mLiveCard.isPublished()) {
			mLiveCard.publish(LiveCard.PublishMode.SILENT);
		} else {
			mLiveCard.setViews(livecardView);
		}
	}
}
