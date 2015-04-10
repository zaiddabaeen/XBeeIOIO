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

import java.util.ArrayList;

import android.util.Log;

/** XbeeConfiguration class is the builder for an Xbee configuration. Passed as the parameter for
 *  {@link Xbee#configure(XbeeConfiguration, Xbee.OnDataReceivedListener)}.
 * Each method concats AT commands to build a string batch command. If multiple get commands were sent, one response will only be received which
 * would probably be the response for the first command.
 *
 */
public class XbeeConfiguration {
	protected static String TAG = "XbeeIOIO_lib";

	protected String configurationString;
	/** The time required for the command to be received by the Xbee module. Default is 500msec. */
	protected int timeRequired = 500;
	protected boolean replyAfterOK = false;
    protected boolean keepOpen = false;

	private Xbee xbee;

	public XbeeConfiguration(Xbee xbee){
		configurationString = "AT";
		this.xbee = xbee;
	}

	/** Sets the source address (My address) of the Xbee device.</br>
	 * Parameter Range: 0 - 0xFFFF</br>
	 * Default Parameter Value: 0
	 * 
	 * @param source_address The source address.
	 * 
	 * @return The XbeeConfiguration instance.
	 */
	public XbeeConfiguration setSourceAddress(int source_address){

		configurationString += "MY";
		configurationString += getHex(source_address);
		configurationString += ",";

		return this;
	}

	/** Sets the destination address of the Xbee device.</br>
	 * Parameter Range: 0 - 0xFFFF</br>
	 * Default Parameter Value: 0
	 * @param destination_address The destination address.
	 * 
	 * @return The XbeeConfiguration instance.
	 */
	public XbeeConfiguration setDestinationAddress(int destination_address){

		configurationString += "DL";
		configurationString += getHex(destination_address);
		configurationString += ",";

		return this;
	}

	/** Sets the Personal Area Network ID of the Xbee device.</br>
	 * Parameter Range: 0 - 0xFFFF</br>
	 * Default Parameter Value: 0
	 * @param pan_id The Personal Area Network ID.
	 * 
	 * @return The XbeeConfiguration instance.
	 */
	public XbeeConfiguration setPanID(int pan_id){

		configurationString += "ID";
		configurationString += getHex(pan_id);
		configurationString += ",";

		return this;
	}

	/** Sets the baud rate of the Xbee device to the baud rate of the UART.
	 * 
	 * @return The XbeeConfiguration instance.
	 */
	public XbeeConfiguration setBaudRate(){

		configurationString += "BD";
		configurationString += getHex(getCorrespondingBaud());
		configurationString += ",";

		return this;

	}

	/** Sets the node identifier (NI) of the Xbee module. The node identifier appears in the node description when
	 *  {@link #getNodeDiscovery()}
	 * is called.
	 * @param nodeIdentifier ASCII string of a maximum of 20 characters, it will be substringed otherwise.
	 * @return The XbeeConfiguration instance.
	 */
	public XbeeConfiguration setNodeIdentifier(String nodeIdentifier){

		String parsedNI = nodeIdentifier;
		parsedNI = parsedNI.length()>20?parsedNI.substring(0,19):parsedNI;

		configurationString += "NI";
		configurationString += " " + parsedNI;
		configurationString += ",";

		return this;

	}

    /** Sets the Xbee device as a coordinator or end-device.</br>
     * Default Parameter Value: Disable
     *
     * @param enable True to enable coordinator mode.
     *
     * @return The XbeeConfiguration instance.
     */
    public XbeeConfiguration setCoordinatorEnable(boolean enable){

        configurationString += "CE";
        configurationString += enable?"1":"0";
        configurationString += ",";

        return this;
    }

	/** Writes the current configuration the non-volatile memory. Parameter values remain
	 * in the module's memory until overwritten by subsequent use of this command. </br></br>
	 * <strong> This must be the last method to call before configuring.</strong>
	 * 
	 * @return The XbeeConfiguration instance.
	 */
	public XbeeConfiguration writeToNonVolatileMemory(){

		configurationString += "WR";
		configurationString += ",";

		return this;
	}

	/** Gets the firmware version of the Xbee module.
	 * 
	 * @return The XbeeConfiguration instance.
	 */
	public XbeeConfiguration getFirmwareVersion(){

		configurationString += "VR,";

		return this;

	}

	/** Prompts the Xbee to get its node identifier. The node identifier is the return value of
	 *  {@link Xbee#configure(XbeeConfiguration, Xbee.OnDataReceivedListener)} for this command.
	 * @return The XbeeConfiguration instance.
	 */
	public XbeeConfiguration getNodeIdentifier(){

		configurationString += "ND,";

		return this;

	}

	/** Prompts the Xbee to discover the nodes. Use {@link #parseNodeDiscovery(String)} on
	 *  {@link Xbee#configure(XbeeConfiguration, Xbee.OnDataReceivedListener)}'s return value of this command.
	 * 
	 * @return The XbeeConfiguration instance.
	 */
	public XbeeConfiguration getNodeDiscovery(){

		configurationString += "ND,";

		replyAfterOK = true;
		setTimeRequired(1000);

		return this;
	}

    /** Prompts the Xbee to read the coordinator enable command.
     *
     * @return The XbeeConfiguration instance.
     */
    public XbeeConfiguration getCoordinatorEnable(){

        configurationString += "CE,";

        return this;
    }

    public XbeeConfiguration keepConfigurationOpen(){

        keepOpen = true;

        return this;

    }

    /** Writes any command.
     *
     * @param command The command to be written without AT letters
     * @param value The value to be written for the command or null
     * @return The XbeeConfiguration instance.
     */
    public XbeeConfiguration writeCommand(String command, String value){

        if(value==null) value = "";
        configurationString += command + value + ",";

        return this;
    }

	/** Parses the returned value from {@link Xbee#configure(XbeeConfiguration, com.dabaeen.XbeeIOIO.Xbee.OnDataReceivedListener)} which called {@link #getNodeDiscovery()}.
	 * 
	 * @param xbeeReply The returned value from {@link Xbee#configure(XbeeConfiguration, com.dabaeen.XbeeIOIO.Xbee.OnDataReceivedListener))}.
	 * @return An ArrayList of {@link Node nodes} discovered.
	 */
	public static ArrayList<Node> parseNodeDiscovery(String xbeeReply){
		ArrayList<Node> nodes = new ArrayList<Node>();

		String[] nodeArr = xbeeReply.split('\r' + "");

		int nodeArrLength = nodeArr.length;

		try{
			for(int i = 0; i < nodeArrLength ; i+= 6){
				if(i + 5 > nodeArrLength) {
					Log.w(TAG, "parseNodeDiscovery: Remaining fields are not enough. Break.");
					break; // Ensures that there are 5 entries to read. The 5th is empty.
				}

				Node node = new Node(getIntFromHex(nodeArr[i]), getIntFromHex(nodeArr[i+1]), getIntFromHex(nodeArr[i+2]),
                        getIntFromHex(nodeArr[i+3]), nodeArr[i+4]);
				Log.i(TAG, "parseNodeDiscovery: " + node.toString());
				nodes.add(node);
			}
		} catch(Exception e){
			Log.e(TAG, "parseNodeDiscovery: Error parsing nodes.");
			e.printStackTrace();
		}

		/*MY (Source Address) value<CR>
		SH (Serial Number High) value<CR>
		SL (Serial Number Low) value<CR>
		DB (Received Signal Strength) value<CR>
		NI (Node Identifier) value<CR>
		<CR> (This is part of the response and not the end of command indicator.)*/

		return nodes;
	}

	/** Sets this time as the time required if it was greater than the predefined time required.
	 * 
	 * @param timeRequired The time required for the command to be processed in milliseconds.
	 */
	private void setTimeRequired(int timeRequired){

		if(this.timeRequired<timeRequired) this.timeRequired = timeRequired;

	}

	/** Returns a hex string of the integer value without "0x".
	 * 
	 * @param value Integer value to be converted to a hex string.
	 * @return A hex string of the integer value.
	 */
	private String getHex(int value){
		return Integer.toHexString(value);
	}
    private static int getIntFromHex(String hex) { return Integer.parseInt(hex, 16); }

	/** Returns the corresponding baud of the Xbee module as specified in Xbee's datasheet.
	 * 
	 * @return The corresponding baud of the Xbee module.
	 */
	private int getCorrespondingBaud(){
		int baud_int = xbee.Baud_rate;
		for(int i = 0; i< Constants.BAUD_RATES.length; i++){
			if(Constants.BAUD_RATES[i][1]==baud_int) return Constants.BAUD_RATES[i][0];
		}

		return xbee.Baud_rate; //9600
	}

}
