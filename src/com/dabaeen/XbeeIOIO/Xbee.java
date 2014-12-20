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

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

/** {@link Xbee} class represents an Xbee module. It provides the ability to interact with the UART data streams, and to configure the Xbee
 * module using the method {@link #configure(XbeeConfiguration, OnDataReceivedListener)}. Xbee object should be defined 
 * in an {@link IOIOLooper}, probably in {@link BaseIOIOLooper#setup(IOIO)} to be able to assign it to an IOIO. Note that the Xbee methods
 * block the thread they are running on, so either the methods should be called in the {@link BaseIOIOLooper}'s methods or in a separate thread.
 * Calling its methods on the UI thread will cause interrupted user experience and will cause the application to be unresponsive.
 *
 */
public class Xbee {
	protected String TAG = "XbeeIOIO_lib";

	private IOIO Ioio;
	private Uart Uart;
	private InputStream In;
	private OutputStream Out;

	private Thread ThreadData;
	private Handler mHandler;
	private Runnable mRunnable;
	private OnDataSentListener onDataSentListener;
	private boolean ListenToDataReceived = false;

	int Baud_rate;
	private boolean LogStreams = true;

	public boolean isConfiguring = false;

	/** Creates a new connection to an Xbee device.
	 * 
	 * This must be constructed inside a BaseIOIOLooper constructor. Example:
	 *<pre>{@code {@literal @}Override
protected IOIOLooper createIOIOLooper() {
 return new BaseIOIOLooper() {
  {@literal @}Override
   protected void setup() throws ConnectionLostException {
   Xbee xbee = new Xbee(ioio_, 45, 46, 9600);
  }
 }
	 *}</pre>
	 * @param ioio The IOIO device to connect through.
	 * @param rX_pin The receiver pin. Must be 3.3V tolerant and must support UART/USART connection.
	 * @param tX_pin The transmitter pin. Must be 3.3V tolerant and must support UART/USART connection.
	 * @param baud_rate The baud rate of the connection. Must match the baud rate of the Xbee device. 
	 * The supported standard baud rates are 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200 bps, and any non-standard 
	 * baud rates in the range 0x80 - 0x3D090 bps.
	 * @throws ConnectionLostException Thrown when the connection to the IOIO has been lost or disconnected.
	 * 
	 */
	public Xbee(IOIO ioio, int rX_pin, int tX_pin, int baud_rate) throws ConnectionLostException{

		this.Ioio = ioio;
		this.Baud_rate = baud_rate;

		Uart = ioio.openUart(rX_pin, tX_pin, baud_rate, Parity.NONE, StopBits.ONE);
		In = Uart.getInputStream();
		Out = Uart.getOutputStream();

	}

	/** Gets the InputStream.
	 * 
	 * @return The UART InputStream of the Xbee device.
	 */
	public InputStream getInputStream(){
		return In;
	}

	/** Gets the OutputStream.
	 * 
	 * @return The UART OutputStream of the Xbee device.
	 */
	public OutputStream getOutputStream(){
		return Out;
	}

	/** Closes the UART connection if exists. Does not disconnect the IOIO.
	 * 
	 */
	public void closeConnection(){
		if(Uart!=null) Uart.close();
	}

	/** Configures the Xbee device with the {@link com.dabaeen.Xbee.XbeeConfiuration XbeeConfiguration} xbeeConfig.
	 * 
	 * Example:
	 * <pre>{@code
		XbeeConfiguration xbeeConfig = new XbeeConfiguration();
		xbeeConfig.setPanID(0xFF)
		.setSourceAddress(0x12) // Using hex form
		.setDestinationAddress(50); // Using dec form
		xbee.configure(xbeeConfig);}</pre>
	 * 
	 * @param xbeeConfig The configuration parameters.
	 * @param onDataReceivedListener The {@link OnDataReceivedListener} of which {@link OnDataReceivedListener#onDataReceived(String)} 
	 * will be triggered. Could be null.
	 * @return The reply from the Xbee.
	 * @throws IOException 
	 */
	public String configure(XbeeConfiguration xbeeConfig, OnDataReceivedListener onDataReceivedListener) throws IOException{

		isConfiguring = true;

		String config = xbeeConfig.configurationString;
		String replyCommand;
		String readBuffer = "";
		int commandTime = xbeeConfig.timeRequired;
		boolean replyAfterOK = xbeeConfig.replyAfterOK;

		config = config.substring(0, config.length()-1); // Removes the last comma
		config += "\r"; // Adds carriage return

		int plusRetry = 3;

		do{
			writeToBuffer("+++"); // Begin setup
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			plusRetry--;
			if(plusRetry==-1){
				Log.e(TAG, "configure: No reply was received from the Xbee. Aborting.");
				isConfiguring = false;
				return null;
			}
			readBuffer = safeReadBuffer(5);
			if(readBuffer.length()>0&&onDataReceivedListener!=null) onDataReceivedListener.onDataReceived(readBuffer);
		}
		while(!readBuffer.contains("OK"));

		writeToBuffer(config);
		replyCommand = safeReadBuffer(commandTime/50);
		if(replyCommand.length()>0&&onDataReceivedListener!=null) onDataReceivedListener.onDataReceived(replyCommand);
		if(replyCommand.contains("ERROR")){
			Log.e(TAG, "configure: Error occured while configuring. Check the configuration values.");
		}
		
		/*if(replyAfterOK){
			replyCommand = safeReadBuffer(commandTime/50);
			if(replyCommand.length()>0&&onDataReceivedListener!=null) onDataReceivedListener.onDataReceived(replyCommand);
			if(replyCommand.contains("ERROR")){
				Log.e(TAG, "configure: Error occured while configuring. Check the configuration values.");
			}
		}*/

		writeToBuffer("ATCN\r");
		if(readBuffer.length()>0&&onDataReceivedListener!=null) onDataReceivedListener.onDataReceived(readBuffer);
		readBuffer = safeReadBuffer(5);
		if(!readBuffer.contains("OK")){
			Log.e(TAG, "configure: Unable to close configuration. Aborting.");
			isConfiguring = false;
			return null;
		}

		Log.i(TAG, "configure: Configuration completed successfully");
		isConfiguring = false;

		return replyCommand;

	}

	/** Sends data to the Xbee device.
	 * 
	 * @param data The data to be sent. Discards if data is null.
	 * @throws IOException
	 */
	public void write(String data) throws IOException{

		if(data!=null) writeToBuffer(data);

	}

	/** Reads the data. Waits a maximum of 250 milliseconds for the data to start being received.
	 * 
	 * @return The data read. Null if no data was read.
	 * @throws IOException
	 */
	public String read() throws IOException{

		return readBuffer();

	}
	
	/** Reads the data that is expected in a defined maximum interval.
	 * 
	 * @param interval Approximate maximum interval to wait for the data to start being received in milliseconds. Default is 250msec.
	 * @return The data read. Null if no data was read.
	 * @throws IOException
	 */
	public String read(int interval) throws IOException{

		return readBuffer(interval/50);

	}

	/** Enables logging of the input and output streams to LogCat. Logs displayed do not ensure successful transmission.</br></br>
	 * Set True by default.
	 * 
	 * @param enabled True if the streams are to be logged.
	 */
	public void setlogStreamsEnabled(boolean enabled){

		LogStreams = enabled;

	}

	/** Sets an {@link OnDataReceivedListener} for the Xbee device. Triggers onDataReceived when data is received.
	 * <strong> Do not use {@link Xbee#read read()} when a listener is set. Could cause loss of data.</strong> The listener will be disabled
	 * while {@link #configure(XbeeConfiguration)} is running.
	 *  Refresh rate is 500 msec. 
	 * 
	 * @param onDataReceivedListener The listener in which OnDataReceived will be triggered.
	 * @param handler Any handler that has its looper prepared. Could be the handler of the activity thread's looper.</br></br>
	 * A {@link android.os.Handler handler} of the activity's thread could be initialized as follows in your onCreate of the Activity:
	 *  <pre>
	Handler mHandler; 
	&#064;Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mHandler = new Handler();
	}</pre>
	 */
	public void setOnDataReceivedListener(final OnDataReceivedListener onDataReceivedListener, final Handler handler){

		ListenToDataReceived = true;

		mHandler = handler;
		//if(ThreadData!=null) ThreadData.

		mRunnable = new Runnable() {

			@Override
			public void run() {

				if(!isConfiguring){ // Skip reading if currently configuring to avoid data loss of configuration
					if(checkBuffer())
						try {
							dataReceived(readBuffer(), onDataReceivedListener);
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
				
				if(ListenToDataReceived) mHandler.postDelayed(mRunnable, 500);
			}
		};

		ThreadData = new Thread(new Runnable() {

			@Override
			public void run() {

				mHandler.post(mRunnable);
			}
		});

		ThreadData.start();

	}
	
	/** Allows the listener to be disabled then enabled temporarily without the need to remove it.
	 * 
	 * @param enable Enables or disables the {@link OnDataReceivedListener}
	 */
	public void enableOnDataReceiverListener(boolean enable){
		
		ListenToDataReceived = enable;
		
	}

	/** Removes the {@link OnDataReceivedListener} assigned.
	 * 
	 */
	public void removeOnDataReceiverListener(){

		ListenToDataReceived = false;

	}

	/** Sets an {@link OnDataSentListener OnDataSentListener} for the Xbee device. Triggers onDataSent when data is sent.
	 * 
	 * @param onDataSentListener The listener in which OnDataSent will be triggered.
	 */
	public void setOnDataSentListener(OnDataSentListener onDataSentListener){

		this.onDataSentListener = onDataSentListener;

	}

	/** Removes the {@link OnDataSentListener} assigned.
	 * 
	 */
	public void removeOnDataSentListener(){

		this.onDataSentListener = null;

	}

	/** Interface {@link OnDataReceivedListener}. {@link #onDataReceived(String)} is triggered when data is received.
	 * 
	 */
	public interface OnDataReceivedListener{

		/** This method is called when data is received.
		 * 
		 * @param data The received data.
		 */
		void onDataReceived(String data);

	}

	/** Interface {@link OnDataSentListener}. {@link #onDataSent(String)} is triggered when data is sent.
	 * 
	 */
	public interface OnDataSentListener{

		/** This method is called when data is sent.
		 * 
		 * @param data The data sent.
		 */
		void onDataSent(String data);

	}

	// Private API -----------------------------------------------------------------------------------------------------
	// Private API -----------------------------------------------------------------------------------------------------
	// Private API -----------------------------------------------------------------------------------------------------

	private void dataReceived(final String data, final OnDataReceivedListener onDataReceivedListener){

		mHandler.post(new Runnable() {

			@Override
			public void run() {

				onDataReceivedListener.onDataReceived(data);

			}
		});

	}

	/** Waits for retries * 50msec */
	private boolean waitForInput(int retries) throws IOException{

		while(retries!=0){

			if(In.available() == 0){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				retries--;
			} else return true;

		}

		return false;

	}

	/** Writes message to the buffer 200 bytes at a time.
	 * 
	 * @param message The string data to be written
	 * @throws IOException Thread was not able to sleep
	 */
	private void writeToBuffer(String message) throws IOException {

		int len = message.length();
		if(len==0) return;
		
		byte[] bytes = message.getBytes();

		for(int i=0; i < len ; i++){
			Out.write(bytes[i]);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(len % 200 == 0){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if(LogStreams) Log.v(TAG, "Sent: " + message);

		if(onDataSentListener!=null) onDataSentListener.onDataSent(message);

	}

	private boolean checkBuffer(){
		try {
			return In.available()>0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/** Reads the buffer safely by returning an empty string instead of null.
	 * 
	 * @param retries Parameter to be passed to {@link #readBuffer(int)}.
	 * @return A string that cannot be null.
	 * @throws IOException
	 */
	private String safeReadBuffer(int retries) throws IOException{

		String buffer = readBuffer(retries);

		return buffer==null?"":buffer;

	}

	/** Reads the buffer and formats it to readable ASCII, waiting for the input in an interval of 250 msec.
	 * 
	 * @return The read string.
	 * @throws IOException
	 */
	private String readBuffer() throws IOException{

		String msg = "";

		while(waitForInput(5)){ // Default is 5
			byte[] buffer = new byte[1];
			In.read(buffer);
			msg += formatString(buffer);
		}

		if(msg.length()==0) msg = null;

		if(LogStreams && msg != null) Log.v(TAG, "Received: " + msg);

		return msg;

	}
	
	/** Reads the buffer waiting for the input in an interval of retries * 50 msec.
	 * 
	 * @param retries Number of retries. Time interval would be retries * 50 msec.
	 * @return The read string.
	 * @throws IOException
	 */
	private String readBuffer(int retries) throws IOException{

		String msg = "";

		while(waitForInput(retries)){
			byte[] buffer = new byte[1];
			In.read(buffer);
			msg += formatString(buffer);
		}

		if(msg.length()==0) msg = null;

		if(LogStreams && msg != null) Log.v(TAG, "Received: " + msg);

		return msg;

	}

	/** Returns a formatted byte in ascii format */
	private String formatString(byte[] str){
		String msg = "";
		if(str==null) return "";

		for (int i = 0; i < str.length; i++)
			msg += getAsciiFromHex(String.format("%1$02X", str[i])) + "";


		return msg;
	}

	private String getAsciiFromHex(String hex){
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < hex.length(); i+=2) {
			String str = hex.substring(i, i+2);
			output.append((char)Integer.parseInt(str, 16));
		}

		return output.toString();
	}

}
