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