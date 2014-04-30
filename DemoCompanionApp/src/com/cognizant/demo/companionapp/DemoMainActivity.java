package com.cognizant.demo.companionapp;

import com.cognizant.demo.util.Constants;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * This DemoTransmit file contains the UI in which user will interact with the
 * companion app to sync the data and connection between the devices.
 * 
 */

public class DemoMainActivity extends Activity {
	private static final int REQUESTCONNECTDEVICE = 1;
	private static final String TAG = "DemoMainActivity";
	private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
			.getDefaultAdapter();
	private boolean isDeviceSelected = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	/** Method to initialize the mandatory things required
	 */
	public void init() {
		// Check for bluetooth turned on
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		}
		// Use this check to determine whether BLE is supported on the device.
		// Then
		// you can selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this,
					Constants.COMPATIBLEWARNING,
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	/**
	 * This method will called once you click the Start sync from the companion
	 * app, and called service in DemoTransmitterService.java
	 * 
	 * @param view
	 */
	public void onClickStartService(View view) {
		// start the service from here //MyService is your service class name
		final SharedPreferences devicePref = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String paireddevicesname = devicePref.getString(Constants.DEVICENAME, "");

		if (paireddevicesname != null && !(paireddevicesname.equals(""))) {
			isDeviceSelected = true;
		}

		if (isDeviceSelected) {
			startService(new Intent(getApplicationContext(),
					DemoTransmitterService.class));
		} else {

			Toast.makeText(getApplicationContext(),
					Constants.SELECTWARNING,
					Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Default onDestory event of activity
	 */

	@Override
	public void onDestroy() {		
		finish();		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.connect_menu, menu);
		return true;
	}

	/**
	 * Method to handle menu item in settings
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.connect_device:
			final Intent serverIntent = new Intent(getApplicationContext(),
					DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUESTCONNECTDEVICE);
			break;
		case R.id.stopservice:
			stopService(new Intent(getApplicationContext(),
					DemoTransmitterService.class));
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Method handles the result of the Devicelistactivity
	 */
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUESTCONNECTDEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				final String address = data.getExtras().getString(
						DeviceListActivity.deviceAddress);
				// Get the BLuetoothDevice object
				final BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				final SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(this);
				final SharedPreferences.Editor editor = preferences.edit();
				editor.putString(Constants.DEVICEADDRESS, device.getAddress());
				editor.putString(Constants.DEVICENAME, device.getName());
				editor.commit();
			}
			break;

		}
	}
}
