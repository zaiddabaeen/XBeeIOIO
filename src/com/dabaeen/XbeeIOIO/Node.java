package com.dabaeen.XbeeIOIO;

/** Class representing an Xbee module. Contains the address, serial number, RSS (Received Signal Strength) and NI (Node Identifier) 
 * of the module.
 * 
 */
public class Node{
	public int Address;
	public int SerialHigh;
	public int SerialLow;
	/** Receiver Signal Strength (RSS) in dBm.*/
	public int RSS; 
	/** Node Identifier (NI).*/
	public String NI;
	
	public Node(int address, int serialHigh, int serialLow, int rss, String ni){
		
		Address = address;
		SerialHigh = serialHigh;
		SerialLow = serialLow;
		RSS = utils.parseRSS(rss);
		NI = ni;
		
	}
	
	@Override
	public String toString(){
		return "MY:"+Address+"|SH:"+SerialHigh+"|SL:"+SerialLow+"|RSS:"+RSS+"|NI:"+NI+"|";
	}
}