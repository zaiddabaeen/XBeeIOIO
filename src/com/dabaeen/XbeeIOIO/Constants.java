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
