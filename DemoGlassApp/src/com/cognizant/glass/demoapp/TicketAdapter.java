package com.cognizant.glass.demoapp;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cognizant.glass.demoapp.util.TicketData;

public class TicketAdapter extends ArrayAdapter<TicketData>{
	List<TicketData> data;
	LayoutInflater inflater;
	Context context;

	public TicketAdapter(Context context, int resource,
			List<TicketData> ridesinfo) {
		super(context, resource, ridesinfo);
		// TODO Auto-generated constructor stub
	data=ridesinfo;
	this.context=context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public TicketData getItem(int position) {
		// TODO Auto-generated method stub
		return ((TicketAdapter) data).getItem(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return super.getItemId(position);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View vitems = convertView;
		ImageView rideImage;
		TextView rideName;
		TextView rideDistance;
		TicketData currentData=data.get(position);
		
		if (convertView == null) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			vitems = inflater.inflate(R.layout.row_layout, parent, false);
		}
			
		rideImage=(ImageView) vitems.findViewById(R.id.list_ride1_image);
		rideName=(TextView) vitems.findViewById(R.id.list_tag_item1);
		rideDistance=(TextView) vitems.findViewById(R.id.list_tag_direction_item1);
		

		rideName.setText(currentData.getTitle());
		rideImage.setImageResource(currentData.getImageID());
		rideDistance.setText(currentData.getDirection());
		
		
		return vitems;
	}
	
}
