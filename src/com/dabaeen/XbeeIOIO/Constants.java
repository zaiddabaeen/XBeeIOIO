/**
XbeeIOIO: Send/Receive over Xbee using your Android device.
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

package com.dabaeen.XbeeIOIO;

public class Constants {

	public static final String PAN_ID = "pan";
	public static final String BAUD = "baud";
	public static final String SOURCE_ADDRESS = "source_address";
	public static final String DESTINATION_ADDRESS = "destination_address";
	public static final String DESTINATION_TYPE = "destination_type";
	public static final String INFO_BUNDLE = "info_bundle";
	
	public static final String SOURCE_ADDRESS_LOW = "source_address_low";
	public static final String SOURCE_ADDRESS_HIGH = "source_address_high";
	public static final String DESTINATION_ADDRESS_LOW = "destination_address_low";
	public static final String DESTINATION_ADDRESS_HIGH = "destination_address_high";
	public static final String TERMINAL_MODE = "terminal_mode";
	
	public static final int[][] BAUD_RATES = new int[][]{
		{0,1200},{1,2400},{2,4800},{3,9600},{4,19200},{5,38400},{6,57600},{7,115200}
	};
		
}
