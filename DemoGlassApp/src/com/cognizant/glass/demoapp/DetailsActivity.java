package com.cognizant.glass.demoapp;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cognizant.glass.demoapp.util.Constants;
import com.cognizant.glass.demoapp.util.HeadListView;
import com.cognizant.glass.demoapp.util.TicketData;
import com.google.android.glass.app.Card;
import com.google.android.glass.touchpad.GestureDetector;

public class DetailsActivity extends Activity {
	public final String[] engineerNames = { "Robert Davis", "Glenn Maxwell" };

	private TextView maint1Text;
	private TextView maint2Text;
	private TextView assetName;
	private TextView ticketNo;
	private TextView requiredEngineersText;
	private ImageView rideImage;
	private TicketData data;
	private GestureDetector mGestureDetector;
	private Intent activityIntent;

	private ListView mListView;
	private HeadListView mHeadScroll;
	private boolean isTicketAvailable = false;
	private LinearLayout topLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (getIntent().getSerializableExtra("data") != null) {
			isTicketAvailable = true;
			data = (TicketData) getIntent().getSerializableExtra("data");

			setContentView(R.layout.product_desc);
			topLayout = (LinearLayout) findViewById(R.id.parent);
			ArrayList<String> receivedData = data.getTicketDesc();
			for (int i = 0; i < receivedData.size(); i++) {
//				System.out.println("ReceivedData :: " + receivedData.get(i));
			}

			mListView = (ListView) findViewById(R.id.listView);
			CustomListAdapter listAdapter = new CustomListAdapter(
					DetailsActivity.this, receivedData);
			mListView.setAdapter(listAdapter);
			mHeadScroll = (HeadListView) findViewById(R.id.listView);

			assetName = (TextView) findViewById(R.id.assetName);
			assetName.setText(data.getTitle());
			maint1Text = (TextView) findViewById(R.id.maintenance1);
			maint1Text.setText(data.getMaint1Text());
			maint2Text = (TextView) findViewById(R.id.maintenance2);
			maint2Text.setText(data.getMaint2Text());
			ticketNo = (TextView) findViewById(R.id.ticket_no);
			ticketNo.setText(data.getTicketNo());
			ticketNo.append(" ticket(s)");
			requiredEngineersText = (TextView) findViewById(R.id.required_engnrs);
			if (Integer.parseInt(data.getTicketNo()) == 1) {

				requiredEngineersText.setText("(1) " + engineerNames[0]);
			} else if (Integer.parseInt(data.getTicketNo()) > 1) {

				requiredEngineersText.setText("(2) " + engineerNames[0] + ", "
						+ engineerNames[1]);
			}

			rideImage = (ImageView) findViewById(R.id.ride_image);

			if (data.getTitle().equals("Silver Bullet")) {
				rideImage.setImageResource(R.drawable.silver_bullet);

			} else if (data.getTitle().equals("Voyager")) {
				rideImage.setImageResource(R.drawable.voyager);

			} else if (data.getTitle().equals("Vertical Loop")) {
				rideImage.setImageResource(R.drawable.vertical_loop);

			} else if (data.getTitle().equals("Animation Academy")) {
				rideImage.setImageResource(R.drawable.animation_academy);

			} else if (data.getTitle().equals("Buzz Light Year")) {
				rideImage.setImageResource(R.drawable.buzz_light_year);

			}

		} else {
			Card card = new Card(this);
			card.setText(Constants.NOWORKORDER);

			Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (isTicketAvailable) {
			mHeadScroll.activate();
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mHeadScroll.deactivate();
		isTicketAvailable = false;
	}

	private class CustomListAdapter extends ArrayAdapter<String> {
		private Context cxt;
		private ArrayList<String> items;

		public CustomListAdapter(Context context, ArrayList<String> tickDesc) {
			super(context, 0, tickDesc);
			this.cxt = context;
			this.items = tickDesc;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			LinearLayout mainLayout = new LinearLayout(cxt);
			mainLayout.setLayoutParams(new ListView.LayoutParams(640,
					ListView.LayoutParams.WRAP_CONTENT));
			mainLayout.setOrientation(LinearLayout.VERTICAL);

			TextView textView = new TextView(cxt);
			textView.setLayoutParams(new ListView.LayoutParams(
					ListView.LayoutParams.WRAP_CONTENT,
					ListView.LayoutParams.WRAP_CONTENT));
			textView.setText(String.valueOf(position + 1) + ". ");
			textView.append(items.get(position));
			textView.setTextSize(20);

			View view = new View(cxt);
			view.setLayoutParams(new ListView.LayoutParams(
					ListView.LayoutParams.MATCH_PARENT, 1));
			view.setPadding(0, 20, 0, 0);
			view.setBackgroundColor(Color.BLUE);

			return textView;
		}

	}

	/*
	 * @Override public boolean onGenericMotionEvent(MotionEvent event) { if
	 * (mGestureDetector != null) { return
	 * mGestureDetector.onMotionEvent(event); } return
	 * super.onGenericMotionEvent(event); }
	 * 
	 * private GestureDetector createGestureDetector(Context context) {
	 * GestureDetector gestureDetector = new GestureDetector(context); // Create
	 * a base listener for generic gestures gestureDetector.setBaseListener(new
	 * GestureDetector.BaseListener() {
	 * 
	 * @Override public boolean onGesture(Gesture gesture) {
	 * 
	 * if (gesture == Gesture.SWIPE_RIGHT) { activityIntent = new Intent(
	 * WorkLogActivity.this, DefectDetailActivity.class);
	 * //workLogIntent.putExtra("data", data.get(0));
	 * startActivity(activityIntent); return true; } else if (gesture ==
	 * Gesture.SWIPE_LEFT) { activityIntent = new Intent( WorkLogActivity.this,
	 * DefectDetailActivity.class); //workLogIntent.putExtra("data",
	 * data.get(1)); startActivity(activityIntent); return true; } else if
	 * (gesture == Gesture.SWIPE_DOWN) { WorkLogActivity.this.finish(); }
	 * 
	 * return false; } }); return gestureDetector; }
	 */
}
