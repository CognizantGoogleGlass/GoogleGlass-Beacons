package com.cognizant.glass.demoapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cognizant.glass.bluetoothconnect.DemoAppRecieverService;
import com.cognizant.glass.demoapp.util.Constants;
import com.cognizant.glass.demoapp.util.TicketData;
import com.cognizant.glass.demoapp.util.OrientationManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * @author 368222 the MainLauncherActivity class is responsible for handling the
 *         UI updates based on the message passed by the DemoAppReceiverService
 *         and also the direction changes.
 * 
 */
public class MainLauncherActivity extends Activity {
	private static boolean isRefreshing = false;
	private static int prevLength;
	private GestureDetector mGestureDetector;
	public final static String MY_ACTION = "MY_ACTION";

	private Intent serviceIntent;
	private DemoAppRecieverService demoAppRecieverService;
	private static String prevLocation = Constants.DIRECTION_NORTH;
	private static String currentLocation = "";

	private static final boolean D = true;
	private static final String TAG = "MainLauncherActivity";
	private static boolean isDirectionChanged = false;
	public static final String DEVICE_NAME = Constants.DEVICENAME;

	private TextView directionText;
	private TextView degreeText;
	private TextView helpText;
	private TextView noWorkOrderText;
	private TextView assetCount;


	private Intent workLogIntent;

	private List<TicketData> data;
	private LinearLayout blackOutLayout;
	private LinearLayout mainLayout;
	private LinearLayout assetListLayout;

	private ListView rideList;

	DataReceiver dataReceiver;

	TicketAdapter adapter;
	private static boolean isDeviceConnected = false;
	int[] itemTextIds = new int[] { R.id.tag_item1, R.id.tag_item2,
			R.id.tag_item3, R.id.tag_item4 };
	int[] hypenTextIds = new int[] { R.id.tag_hyphen_item1,
			R.id.tag_hyphen_item2, R.id.tag_hyphen_item3, R.id.tag_hyphen_item4 };
	int[] directionTextIds = new int[] { R.id.tag_direction_item1,
			R.id.tag_direction_item2, R.id.tag_direction_item3,
			R.id.tag_direction_item4 };
	int[] itemTicketNosIds = new int[] { R.id.tag_no_of_tickets_item1,
			R.id.tag_no_of_tickets_item2, R.id.tag_no_of_tickets_item3,
			R.id.tag_no_of_tickets_item4 };
	int[] layoutIds = new int[] { R.id.firstrow, R.id.secondrow, R.id.thirdrow,
			R.id.fourthrow };
	int[] rideImageIds = new int[] { R.id.ride1_image, R.id.ride2_image,
			R.id.ride3_image, R.id.ride4_image };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initialising the views
		blackOutLayout = (LinearLayout) findViewById(R.id.view);
		mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
		assetListLayout = (LinearLayout) findViewById(R.id.assetListLayout);
		rideList = (ListView) findViewById(R.id.assetList);
		mGestureDetector = createGestureDetector(this);
		// initialising service
		initiateService();

		data = new ArrayList<TicketData>();

		adapter = new TicketAdapter(getApplicationContext(),
				R.layout.activity_main, data);
		rideList.setAdapter(adapter);
		rideList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				workLogIntent = new Intent(MainLauncherActivity.this,
						DetailsActivity.class);
				workLogIntent.putExtra("data", data.get(arg2));
				startActivity(workLogIntent);
			}
		});
		directionText = (TextView) findViewById(R.id.compass_screen1);
		degreeText = (TextView) findViewById(R.id.compass_degree_screen1);
		helpText = (TextView) findViewById(R.id.helpTxt);
		noWorkOrderText = (TextView) findViewById(R.id.noWorkOrderText);
		assetCount = (TextView) findViewById(R.id.tag_assets_count);
		mainLayout.setVisibility(LinearLayout.VISIBLE);
	}

	/**
	 * to register the broadcast receiver which is responsible to populate the
	 * views based on the message received from the service and to start the
	 * background service which handles the connection
	 */

	protected void initiateService() {
		dataReceiver = new DataReceiver();
		IntentFilter intentFilter = new IntentFilter();
		// receiver registered with DemoAppRecieverService
		intentFilter.addAction(DemoAppRecieverService.MY_ACTION);
		registerReceiver(dataReceiver, intentFilter);
		// Start our own service
		Intent intent = new Intent(getApplicationContext(),
				DemoAppRecieverService.class);
		startService(intent);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub		
		super.onStop();
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return super.onGenericMotionEvent(event);
	}

	/**
	 * methos to capture right and left swipe gestures and implement list scroll
	 * to next element as the 4.4.2 version does not support the default list
	 * item traverse.
	 * 
	 * @param context
	 * @return
	 */
	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		// Create a base listener for generic gestures

		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.SWIPE_RIGHT) {
					// if gesture is swipe right move to next item.
					int currPosition = rideList.getSelectedItemPosition();
					rideList.setSelection(currPosition + 1);
					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {
					// if gesture is swipe left move to previous item.
					int currPosition = rideList.getSelectedItemPosition();
					rideList.setSelection(currPosition - 1);
					return true;
				} else if (gesture == Gesture.SWIPE_DOWN) {
					// if gesture is swipe down finish the activity.
					MainLauncherActivity.this.finish();
				}
				return false;
			}
		});
		return gestureDetector;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		OrientationManager.getInstance(this).removeOnChangedListener(mListener);
		OrientationManager.getInstance(this).stop();

		stopService(serviceIntent);
		isDirectionChanged = false;
		isRefreshing = false;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		// if device connected then the list view is made visible while the
		// device pairing is made hidden.
		if (isDeviceConnected) {
			blackOutLayout.setVisibility(LinearLayout.GONE);
			mainLayout.setVisibility(LinearLayout.VISIBLE);
		}

		serviceIntent = new Intent(this,
				com.cognizant.glass.demoapp.util.AssetLocationService.class);
		startService(serviceIntent);

		OrientationManager.getInstance(this).addOnChangedListener(mListener);
		OrientationManager.getInstance(this).start();
		// the direction changed is made true while resuming the activity
		// so that the broadcast receiver can handle the view population
		if (!isDirectionChanged) {
			isDirectionChanged = true;
		}
	}

	private final OrientationManager.OnChangedListener mListener = new OrientationManager.OnChangedListener() {

		@Override
		public void onOrientationChanged(OrientationManager orientationManager) {
			int value = (int) OrientationManager.getInstance(
					MainLauncherActivity.this).getHeading();
			String degText = Integer.toString(value);
			// based on the degree the direction is identified.
			if ((value >= 0 && value <= 45) || (value >= 315 && value <= 360)) {
				// North
				currentLocation = Constants.DIRECTION_NORTH;
			} else if (value > 45 && value <= 135) {
				// East
				currentLocation = Constants.DIRECTION_EAST;
			} else if (value > 135 && value <= 225) {
				// South
				currentLocation = Constants.DIRECTION_SOUTH;
			} else {
				// West
				currentLocation = Constants.DIRECTION_WEST;
			}

			degreeText.setText(degText);

			// if the current and previous locations differ it signifies a
			// direction change and hence
			// isDirectionChanged is set to true.
			if (!prevLocation.equalsIgnoreCase(currentLocation)) {
				directionText.setText(currentLocation);
				prevLocation = currentLocation;
				isDirectionChanged = true;
			}

		}

		@Override
		public void onLocationChanged(OrientationManager orientationManager) {
		}

		@Override
		public void onAccuracyChanged(OrientationManager orientationManager) {
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		isDeviceConnected = false;
		isRefreshing = false;
		isDirectionChanged = false;
		unregisterReceiver(dataReceiver);
		// Stop the Bluetooth services
		if (demoAppRecieverService != null)
			demoAppRecieverService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	/**
	 * @author 368222 this class handles the callback from the
	 *         DemoAppRecieverService
	 * 
	 *         whenever the DemoAppRecieverService receives a message, it pushes
	 *         the message to this class the class then parses the message and
	 *         uses the data to populate the UI
	 * 
	 */
	private class DataReceiver extends BroadcastReceiver {
		// Default method overridden to handle message receive ang make
		// corresponding UI changes.
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// the message passed from the service is received and parsed
			String datapassed = arg1.getStringExtra(Constants.SENDDATA);
			isDeviceConnected = true;
			// if connection is established then device pairing screen is amde
			// hidden.
			if (isDeviceConnected) {
				blackOutLayout.setVisibility(LinearLayout.GONE);
			}
			// if isDirectionChanged the corresponding UI change is invoked.
			if (isDirectionChanged) {
				try {
					// parsing the json string to json objects.
					JSONObject displayJson = new JSONObject(datapassed);
					// if the direction you are looking into is same as the
					// location of the beacon signal recieved then
					// the corresponding beacon details are populated in the
					// view.
					if ((isRefreshing && displayJson.getString(Constants.DIRECTION)
							.equalsIgnoreCase(currentLocation))
							|| displayJson.getString(Constants.DIRECTION)
									.equalsIgnoreCase(currentLocation)) {
						isRefreshing = false;
						isDirectionChanged = false;
						// If data founded, show the asset layout
						noWorkOrderText.setVisibility(View.GONE);
						rideList.setVisibility(LinearLayout.VISIBLE);
						assetListLayout.setVisibility(LinearLayout.VISIBLE);
						for (int k = 0; k < prevLength; k++) {
							((RelativeLayout) findViewById(layoutIds[k]))
									.setVisibility(RelativeLayout.INVISIBLE);
						}

						int length = displayJson.getJSONArray(Constants.TAG_CASEINFO)
								.length();
						if (!data.isEmpty()) {
							data.clear();
						}
						prevLength = length;
						// Putting data in model class
						for (int i = 0; i < length; i++) {
							TicketData modelData = new TicketData();
							modelData.setTitle(displayJson
									.getJSONArray(Constants.TAG_CASEINFO).getJSONObject(i)
									.getString(Constants.TAG_CASETITLE));
							modelData.setMaint1Text(displayJson
									.getJSONArray(Constants.TAG_CASEINFO).getJSONObject(i)
									.getString(Constants.TAG_LASTMAINTENANCE));
							modelData.setMaint2Text(displayJson
									.getJSONArray(Constants.TAG_CASEINFO).getJSONObject(i)
									.getString(Constants.TAG_NEXTMAINTENANCE));
							modelData.setTicketNo(displayJson
									.getJSONArray(Constants.TAG_CASEINFO).getJSONObject(i)
									.getString(Constants.TAG_TICKETCOUNT));
							modelData.setDate(displayJson
									.getJSONArray(Constants.TAG_CASEINFO).getJSONObject(i)
									.getString(Constants.TAG_DATE));
							modelData.setAssetID(displayJson.getString(Constants.TAG_ID));
							modelData.setCost(displayJson
									.getJSONArray(Constants.TAG_CASEINFO).getJSONObject(i)
									.getString(Constants.TAG_COST));
							modelData.setDirection(displayJson
									.getJSONArray(Constants.TAG_CASEINFO).getJSONObject(i)
									.getString(Constants.TAG_DEGREE));

							int ticketCount = Integer.parseInt(displayJson
									.getJSONArray(Constants.TAG_CASEINFO).getJSONObject(i)
									.getString(Constants.TAG_TICKETCOUNT));
							// depends on the ticket number
							for (int j = 1; j <= ticketCount; j++) {
								modelData.setTicketDesc(displayJson
										.getJSONArray(Constants.TAG_CASEINFO)
										.getJSONObject(i)
										.getString(Constants.TAG_TICKETDES + (j)));
							}

							String imageName = displayJson
									.getJSONArray(Constants.TAG_CASEINFO).getJSONObject(i)
									.getString(Constants.TAG_CASETITLE).toLowerCase();
							imageName = imageName.replace(" ", "_");

							// populating view
							((RelativeLayout) findViewById(layoutIds[i]))
									.setVisibility(RelativeLayout.VISIBLE);
							((TextView) findViewById(itemTextIds[i]))
									.setText(displayJson
											.getJSONArray(Constants.TAG_CASEINFO)
											.getJSONObject(i)
											.getString(Constants.TAG_CASETITLE));
							((TextView) findViewById(hypenTextIds[i]))
									.setText("-");
							((TextView) findViewById(directionTextIds[i]))
									.setText(displayJson
											.getJSONArray(Constants.TAG_CASEINFO)
											.getJSONObject(i)
											.getString(Constants.TAG_DEGREE));
							((TextView) findViewById(itemTicketNosIds[i]))
									.setText(displayJson
											.getJSONArray(Constants.TAG_CASEINFO)
											.getJSONObject(i)
											.getString(Constants.TAG_TICKETCOUNT)
											+ "ticket(s)");

							// parsing the image resource id based on the ticket
							// info and setting the id in the model class
							// object.
							try {
								String fnm = imageName; // this is image
															// file name
								String packageName = getApplicationContext()
										.getPackageName();
								int imgId = getResources().getIdentifier(
										packageName + ":drawable/" + fnm,
										null, null);
								((ImageView) findViewById(rideImageIds[i]))
										.setImageBitmap(BitmapFactory
												.decodeResource(getResources(),
														imgId));
								modelData.setImageID(imgId);

							} catch (Exception e) {

							}

							// the model data object is added to the dataset.
							data.add(modelData);
							// the adapter is notified of the data set content
							// change hence the view gets updated.
							adapter.notifyDataSetChanged();
							// based on the size of the dataset the count is
							// updated.
							assetCount.setText(data.size()
									+ " attractions nearby");
						}

					} else {
						// else no data received in the current direction
						rideList.setVisibility(LinearLayout.GONE);
						assetListLayout.setVisibility(LinearLayout.GONE);
						noWorkOrderText.setVisibility(View.VISIBLE);
						helpText.setVisibility(View.INVISIBLE);
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// Direction not changed
			}
		}
	}

}