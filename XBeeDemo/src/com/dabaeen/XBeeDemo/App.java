package com.dabaeen.XBeeDemo;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class App extends Application{

	public static String SrcAddress, DestAddress, PanId, BaudRate;
	public static boolean isConfigured = false;

	public static SharedPreferences prefs;

	@Override
	public void onCreate() {

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		loadSettings();
		
		super.onCreate();
	}
	
	public static void saveSettings() {
		
		prefs.edit().putString(Constants.SOURCE_ADDRESS, SrcAddress)
		.putString(Constants.DESTINATION_ADDRESS, DestAddress)
		.putString(Constants.BAUD, BaudRate)
		.putString(Constants.PAN_ID, PanId)
		.putBoolean(Constants.IS_CONFIGURED, isConfigured).apply();
		
	}
	
	public static void loadSettings() {

		SrcAddress = prefs.getString(Constants.SOURCE_ADDRESS, "1");
		DestAddress = prefs.getString(Constants.DESTINATION_ADDRESS, "2");
		BaudRate = prefs.getString(Constants.BAUD, "9600");
		PanId = prefs.getString(Constants.PAN_ID, "1");
		
	}
}
