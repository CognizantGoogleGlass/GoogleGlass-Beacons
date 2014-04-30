package com.cognizant.glass.demoapp.util;

import java.io.Serializable;
import java.util.ArrayList;


public class TicketData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String title;
	private String maintenanceText1;
	private String maintenanceText2;
	private String ticketNo;
	private ArrayList<String> ticketDesc;
	private String date;
	private String assetID;
	private String cost;
	private int imageID;
	private String direction;
	
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public int getImageID() {
		return imageID;
	}
	public void setImageID(int imageID) {
		this.imageID = imageID;
	}
	public TicketData() {
		// TODO Auto-generated constructor stub
		ticketDesc = new ArrayList<String>();
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMaint1Text() {
		return maintenanceText1;
	}

	public void setMaint1Text(String maint1Text) {
		this.maintenanceText1 = maint1Text;
	}

	public String getMaint2Text() {
		return maintenanceText2;
	}

	public void setMaint2Text(String maint2Text) {
		this.maintenanceText2 = maint2Text;
	}

	public String getTicketNo() {
		return ticketNo;
	}

	public void setTicketNo(String ticketNo) {
		this.ticketNo = ticketNo;
	}

	public ArrayList<String> getTicketDesc() {
		return ticketDesc;
	}

	public void setTicketDesc(String ticketDescText) {
		ticketDesc.add(ticketDescText);
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public String getAssetID() {
		return assetID;
	}

	public void setAssetID(String id) {
		this.assetID = id;
	}
	
	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

}
