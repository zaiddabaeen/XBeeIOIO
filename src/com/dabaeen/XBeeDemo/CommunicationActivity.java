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

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.IOException;
import java.util.Calendar;

import android.R.raw;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.dabaeen.XBeeDemo.Constants.Direction;
import com.dabaeen.XbeeIOIO.Xbee;
import com.dabaeen.XbeeIOIO.Xbee.OnDataReceivedListener;
import com.dabaeen.XbeeIOIO.Xbee.OnDataSentListener;
import com.dabaeen.XbeeIOIO.XbeeConfiguration;

public class CommunicationActivity extends IOIOActivity {

	final static String TAG = "XBeeDemo";

	RelativeLayout progressLayout;
	Context context;
	TextView tVlog;
	ScrollView sVlog;
	CheckBox cBcr;
	Button bSend;
	LinearLayout llLog, indicatorsLayout;

	private ImageView iLight, iTemp;
	private TextView tLight, tCO, tTemp, tStatus, iCO;
	private Button bRetry;

	ArrayAdapter<View> lvAdapter;
	private ToggleButton button_, tLED;
	EditText eTmessage;
	private String mOut = "", mLog="";
	boolean isConfigured = false, TerminalMode = false, isConfiguring = false;
	private Handler mHandler;
	private boolean DiscoverNodes = false;

	/** @param SrcAddress Source address in decimal form (Default: 1)
	 * @param DestAddress Dest
	 * ination address in decimal form (Default: 1)
	 * @param BaudRate Baud rate in decimal form (Default: 9600)
	 * @param PanId PAN Id address in decimal form (Default: 1)
	 * */
	private String SrcAddress, DestAddress, BaudRate, PanId;
	private int ioioBaudRate;

	/** @param SrcLow Low source address in hex form
	 * @param SrcHigh High source address in hex form
	 * @param DestLow Low destination address in hex form
	 * @param DestHigh High destination address in hex form
	 * */
	private String SrcLow, SrcHigh, DestLow, DestHigh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_communication);

		//getActionBar().setHomeButtonEnabled(true);

		context = this;
		mHandler = new Handler();

		//tVlog = (TextView) findViewById(R.id.tVlog);
		llLog = (LinearLayout) findViewById(R.id.llLog);
		indicatorsLayout = (LinearLayout) findViewById(R.id.indicatorLayout);
		progressLayout = (RelativeLayout) findViewById(R.id.progressLayout);
		eTmessage = (EditText) findViewById(R.id.eTmessage);
		bSend = (Button) findViewById(R.id.bSend);
		sVlog = (ScrollView) findViewById(R.id.scrollView1);
		button_ = (ToggleButton) findViewById(R.id.tYellowB);
		tLED = (ToggleButton) findViewById(R.id.tLED);
		cBcr = (CheckBox) findViewById(R.id.cBcr);
		tLight = (TextView) findViewById(R.id.tLight);
		tCO = (TextView) findViewById(R.id.tCO);
		tTemp = (TextView) findViewById(R.id.tTemp);
		tStatus = (TextView) findViewById(R.id.tStatus);
		iLight = (ImageView) findViewById(R.id.iLight);
		iTemp = (ImageView) findViewById(R.id.iTemp);
		iCO = (TextView) findViewById(R.id.iCO);
		bRetry = (Button) findViewById(R.id.bRetry);

		eTmessage.setOnEditorActionListener(editorActionListener);

		setStatus(Constants.WAITING_CONNECTION);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_configure:
			Intent i = new Intent(this, PrefsActivity.class);
			startActivity(i);
			break;
		case R.id.action_indicators:
			if(indicatorsLayout.isShown()) setLayoutVisibility(false);
			else setLayoutVisibility(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setLayoutVisibility(boolean visible){
		
		if(visible){
			indicatorsLayout.setVisibility(View.VISIBLE);
			indicatorsLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right_in));
		} else {
			indicatorsLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right_out));
			indicatorsLayout.setVisibility(View.GONE);
		}
		
	}

	/** Checks the validity of the values entered. Currently only checks if the values are of zero-size*/
	private void checkValues(){
		if(SrcAddress.length()==0) SrcAddress = "1";
		if(DestAddress.length()==0) DestAddress = "2";
		if(BaudRate.length()==0) BaudRate = "9600";
		if(PanId.length()==0) PanId = "1";
	}

	/** Splits SrcAddress and DestAddress into highs and lows */
	private void getAddresses(){
		String srcHex = getHex(SrcAddress);
		SrcAddress = srcHex;
		int length = srcHex.length();

		/*SrcLow = getCharacterSafely(srcHex, length-2) + getCharacterSafely(srcHex, length-1);
		SrcHigh = getCharacterSafely(srcHex, length-4) + getCharacterSafely(srcHex, length-3);*/

		String destHex = getHex(DestAddress);
		DestAddress = destHex;
		length = destHex.length();

		DestLow = getCharacterSafely(destHex, length-2) + getCharacterSafely(destHex, length-1);
		DestHigh = getCharacterSafely(destHex, length-4) + getCharacterSafely(destHex, length-3);

		DestLow = DestHigh + DestLow; // For 16-bit addressing
		DestHigh = "0000";
	}

	/** Returns the character in string form from the string array of position pos safely. Replaces errors with zeros */
	private String getCharacterSafely(String array, int pos){
		try{
			return array.charAt(pos) + "";
		} catch(Exception e){

		}

		return "0";
	}

	@Override
	protected void onResume() {

		App.loadSettings();

		SrcAddress = App.SrcAddress;
		DestAddress = App.DestAddress;
		BaudRate = App.BaudRate;
		PanId = App.PanId;
		isConfigured = App.isConfigured;

		getAddresses();

		logConfig();

		super.onResume();
	}

	private void logConfig(){
		String str = "";
		str += "SrcAddress: ";
		str += SrcAddress;
		str += "; ";
		str += "DstAddress: ";
		str += DestHigh + "," + DestLow;
		str += "; ";
		str += "PAN ID: ";
		str += PanId;
		str += "; ";
		str += "Baud Rate: ";
		str += BaudRate;
		str += "; ";

		log(str, Direction.Center);
	}

	/** Returns the hex value of the integer contained in the String value 
	 * @param value String that contains the decimal which is to be converted to hex*/
	private String getHex(String value){
		return Integer.toHexString(Integer.valueOf(value));
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {

			private DigitalOutput led_;
			private Xbee xbee;

			@Override
			public void disconnected() {

				runOnUiThread(new Runnable() {
					public void run() {
						setStatus(Constants.WAITING_CONNECTION);
						progressLayout.setVisibility(View.VISIBLE);
					}
				});

				log("IOIO is disconnected", Direction.Center);
				xbee.closeConnection();
				xbee.removeOnDataReceiverListener();
				xbee.removeOnDataSentListener();
				xbee = null;
			}

			/**
			 * Called every time a connection with IOIO has been established.
			 * Typically used to open pins.
			 * 
			 * @throws ConnectionLostException
			 *             When IOIO connection is lost.
			 * 
			 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
			 */
			@Override
			protected void setup() throws ConnectionLostException {

				log("IOIO is connected", Direction.Center);

				setStatus(Constants.CONFIGURING);

				//Toast.makeText(context, "IOIO is connected", Toast.LENGTH_LONG).show();

				try{
					led_ = ioio_.openDigitalOutput(0, true);
					xbee = new Xbee(ioio_,45,46,Integer.valueOf(BaudRate));/*
					in = xbee.getInputStream();
					out = xbee.getOutputStream();*/

					xbee.setOnDataSentListener(new OnDataSentListener() {

						@Override
						public void onDataSent(String data) {
							log(data, Direction.Out);
						}
					});

					xbee.setOnDataReceivedListener(onDataReceivedListener, mHandler);

				} catch (Exception e){
					e.printStackTrace();
					log("Error setting up ports: " + e.getMessage(), Direction.Error);
				}

				if(TerminalMode){
					log("Starting terminal mode. Baud Rate = " + BaudRate, Direction.Center);
					return;
				}

				try{
					isConfiguring = true;
					if(!isConfigured) xbee.configure(
							new XbeeConfiguration(xbee).setBaudRate()
							.setDestinationAddress(Integer.decode("0x" + DestAddress))
							.setSourceAddress(Integer.decode("0x" + SrcAddress))
							.setPanID(Integer.valueOf(PanId)).writeToNonVolatileMemory()
							,onDataReceivedListener);//xbeeSetup();
					isConfiguring = false;

					runOnUiThread(new Runnable() {
						public void run() {
							setStatus(Constants.READY);
							progressLayout.setVisibility(View.INVISIBLE);
						}
					});
				} catch (IOException e){
					e.printStackTrace();
					log("IO error in xbeeSetup(). Check log", Direction.Error);
					log(e.getMessage(), Direction.Error);
					isConfiguring = false;

					runOnUiThread(new Runnable() {
						public void run() {
							setStatus(Constants.XBEE_ERROR);
							bRetry.setVisibility(View.VISIBLE);
						}
					});
				}
			}

			private OnDataReceivedListener onDataReceivedListener = new OnDataReceivedListener() {

				@Override
				public void onDataReceived(String data) {
					log(data, Direction.In);
					parseData(data);
				}
			};

			/**
			 * Called repetitively while the IOIO is connected.
			 * 
			 * @throws ConnectionLostException
			 *             When IOIO connection is lost.
			 * 
			 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
			 */
			@Override
			public void loop() throws ConnectionLostException {
				try {
					Thread.sleep(100);

					try {
						led_.write(!button_.isChecked());
						/*if(checkBuffer()){
							readBuffer();
						}*/

						if(mOut.length()>0){
							xbee.write(mOut);
							mOut = "";
						}

						if(DiscoverNodes){
							String reply = xbee.configure(new XbeeConfiguration(xbee).getNodeDiscovery().getFirmwareVersion(), onDataReceivedListener);
							XbeeConfiguration.parseNodeDiscovery(reply);
							log("Reply: " + reply, Direction.Center);
							DiscoverNodes = false;
						}

					} catch (Exception e) {
						e.printStackTrace();
						log("Error in inner loop" + e.getMessage(), Direction.Error);
					}
				} catch (Exception e) {
					log("Error in looping" + e.getMessage(), Direction.Error);
					e.printStackTrace();
				}
			}


		};
	}

	private void parseData(String data){

		String d = data;
		d = d.toLowerCase();
		try{
			if(d.contains("light=") || d.contains("temp=") || d.contains("co=")) {

				String reading = d.split("=")[1];
				reading = reading.trim();
				if(d.contains("light")){
					int light = Integer.valueOf(reading);
					String p = "";
					if(light == 0) p = "High";
					if(light > 0 && light <= 50) p = "Medium";
					if(light > 50) p = "Low";

					tLight.setAlpha(0f);
					tLight.setText("Light: " + p);
					tLight.animate().alpha(1f).setDuration(100).start();
				} else if(d.contains("temp")){
					int temp = Integer.valueOf(reading);
					tTemp.setAlpha(0f);
					tTemp.setText("Temperature: " + temp + "°C");
					tTemp.animate().alpha(1f).setDuration(100).start();
				} else if(d.contains("co")){
					int co = Integer.valueOf(reading);
					
					String p = "";
					if(co <= 300) p = "Low";
					if(co > 300 && co <= 400) p = "Medium";
					if(co > 400) p = "High";
					
					tCO.setAlpha(0f);
					tCO.setText("CO: " + p);
					tCO.animate().alpha(1f).setDuration(100).start();
				} 

			}
		}catch(Exception e){
			Log.w(TAG, "Too fast");
		}

	}

	public void setStatus(final String Status){

		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				tStatus.setAlpha(0f);
				tStatus.setText(Status);
				tStatus.animate().alpha(1f).setDuration(100).start();

			}
		});

	}

	private void animateView(View v){
		TranslateAnimation rAnimation = new TranslateAnimation(0, 10f, 0f, 0f);
		rAnimation.setRepeatMode(Animation.REVERSE);
		rAnimation.setRepeatCount(1);
		rAnimation.setDuration(100);
		rAnimation.setInterpolator(new LinearInterpolator());
		
		v.startAnimation(rAnimation);
	}

	public void GetLight(View v){
		mOut += 'L';
		animateView(iLight);
	}
	public void GetTemp(View v){
		mOut += 'T';
		animateView(iTemp);
	}
	public void GetCO(View v){
		mOut += 'C';
		animateView(iCO);
	}
	
	public void ControlLight(View v){
		if(((ToggleButton) v).isChecked()) {
			mOut += 'E';
		} else 
			mOut += 'E';
	}

	public void RetryConnection(View v){

		super.onStop();
		super.onStart();
		bRetry.setVisibility(View.GONE);

	}

	public void ToggleLED(View v){

		if(tLED.isChecked()){
			mOut += 'Y';
		} else {
			mOut += 'N';
		}

	}

	public void DiscoverNodes(View v){
		DiscoverNodes = true;
	}

	public void SendMessage(View v){

		String message = eTmessage.getText().toString();

		eTmessage.setText("");

		mOut += message;
		if(cBcr.isChecked()) mOut += '\r';

	}

	public void log(final String message){

		mLog += message +"\n";

		log(message, Direction.In);

	}

	public void log(final String message, boolean newLine){

		if(newLine)	mLog += message +"\n";
		else mLog += message;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				tVlog.setText(mLog);
				sVlog.post(new Runnable() {

					@Override
					public void run() {
						sVlog.fullScroll(View.FOCUS_DOWN);
					}
				});
			}
		});

	}

	public void log(final String message, final int direction){

		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				addLogItem(message, direction);

			}
		});

	}

	public void addLogItem(String message, int direction){

		if(isConfiguring) direction = Direction.Center;

		View logView = LayoutInflater.from(this).inflate(R.layout.view_logitem, llLog, false);

		ViewFlipper vF = (ViewFlipper) logView.findViewById(R.id.vF);
		TextView tvTime = null, tvText = null;

		vF.setDisplayedChild(direction);

		if(direction == Direction.Out){
			tvTime = (TextView) logView.findViewById(R.id.tvTimeR);
			tvText = (TextView) logView.findViewById(R.id.tvTextR);
		} else if(direction == Direction.In){
			tvTime = (TextView) logView.findViewById(R.id.tvTimeL);
			tvText = (TextView) logView.findViewById(R.id.tvTextL);
		} else if(direction == Direction.Center || direction == Direction.Error){
			tvTime = (TextView) logView.findViewById(R.id.tvTimeC);
			tvText = (TextView) logView.findViewById(R.id.tvTextC);
			tvText.setTextColor(Color.parseColor("#111111"));
		}

		if(direction == Direction.Error){
			tvText.setTextColor(Color.parseColor("#A10000"));
		}

		Calendar ci = Calendar.getInstance();

		tvTime.setText(String.format("%02d", ci.get(Calendar.HOUR)) + ":" + String.format("%02d", ci.get(Calendar.MINUTE)) + ":" + String.format("%02d", ci.get(Calendar.SECOND)));

		tvText.setText(message);

		llLog.addView(logView,0);

		logView.setAlpha(0);
		logView.animate().setDuration(400).alpha(1).start();

	}

	public void toastMessage(final String message, final int length){

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(context, message, length).show();

			}
		});

	}

	OnEditorActionListener editorActionListener = new OnEditorActionListener(){

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

			if ((actionId == EditorInfo.IME_NULL  || actionId == EditorInfo.IME_ACTION_DONE) && event == null
					/*|| event.getAction() == KeyEvent.ACTION_DOWN*/) { 
				SendMessage(null);
			}

			return false;
		}

	};

	@Override
	public void onBackPressed(){
		super.onBackPressed();

	}

}