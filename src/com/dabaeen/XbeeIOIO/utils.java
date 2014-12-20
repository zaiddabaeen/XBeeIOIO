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
