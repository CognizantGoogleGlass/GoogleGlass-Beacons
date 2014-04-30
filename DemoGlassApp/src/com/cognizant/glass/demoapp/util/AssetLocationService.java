package com.cognizant.glass.demoapp.util;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AssetLocationService extends Service {

	public static OrientationManager mOrientationManager;

	@Override
	public void onCreate() {
		super.onCreate();
		OrientationManager.getInstance(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
