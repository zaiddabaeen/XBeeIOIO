package com.dabaeen.XbeeIOIO;

public class utils {

	/** Parses an RSS value from the Xbee to absolute power in dBm.
	 * 
	 * @param rss The RSS value to parse.
	 * @return Absolute power in dBm.
	 */
	public static int parseRSS(int rss){
		
		return -rss;
		
	}
	
}
