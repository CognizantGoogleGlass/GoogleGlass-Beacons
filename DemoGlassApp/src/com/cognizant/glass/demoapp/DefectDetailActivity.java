package com.cognizant.glass.demoapp;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cognizant.glass.demoapp.util.HeadListView;
import com.cognizant.glass.demoapp.util.TicketData;
import com.google.android.glass.app.Card;

public class DefectDetailActivity extends Activity {

	private TextView date;
	private TextView assetID;
	private TextView cost;
	private TextView status;
	private TextView assetName;
	private TicketData modelData;
	private ListView listView;
	private HeadListView headScroll;
	private boolean isTicketAvailable = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		if (getIntent().getSerializableExtra("data") != null) {

			modelData = (TicketData) getIntent().getSerializableExtra("data");

			setContentView(R.layout.worklog);

			listView = (ListView) findViewById(R.id.listView1);
			CustomAdapter listAdapter = new CustomAdapter(
					DefectDetailActivity.this, modelData.getTicketDesc());
			listView.setAdapter(listAdapter);
			headScroll = (HeadListView) findViewById(R.id.listView1);

			date = (TextView) findViewById(R.id.date);
			assetID = (TextView) findViewById(R.id.assetID);
			cost = (TextView) findViewById(R.id.cost);
			status = (TextView) findViewById(R.id.status);
			assetName = (TextView) findViewById(R.id.name);

			date.setText(modelData.getDate());
			assetID.append(modelData.getAssetID());
			cost.setText(modelData.getCost());
			status.setText("Pending");
			assetName.setText(modelData.getTitle());

		} else {
			Card card = new Card(this);
			card.setText("No Work order details found");

			Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (isTicketAvailable) {
			headScroll.activate();
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		headScroll.deactivate();
		isTicketAvailable = false;
	}

	private class CustomAdapter extends ArrayAdapter<String> {
		private Context cxt;
		private ArrayList<String> items;

		public CustomAdapter(Context context, ArrayList<String> tickDesc) {
			super(context, 0, tickDesc);
			this.cxt = context;
			this.items = tickDesc;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final View view = View.inflate(cxt, R.layout.defect_item, null);
			final TextView textView = (TextView) view
					.findViewById(R.id.textViews);
			textView.setText(items.get(position));

			return view;
		}

	}

}
