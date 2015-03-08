/**
XBeeDemo: A demo for XBeeIOIO library.
Copyright (C) 2014 Zaid Dabain

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */


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
