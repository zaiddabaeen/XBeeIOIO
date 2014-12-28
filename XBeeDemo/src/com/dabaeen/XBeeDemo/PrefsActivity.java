package com.dabaeen.XBeeDemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.dabaeen.XBeeDemo.R;
import com.dabaeen.XbeeIOIO.XbeeConfiguration;

public class PrefsActivity extends Activity{

	EditText eTpan, eTsource, eTdestination;
	Spinner spBaud, spDestination;
	SharedPreferences sharedPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sharedPref = getPreferences(Context.MODE_PRIVATE);

		eTpan = (EditText) findViewById(R.id.eTpan);
		spBaud = (Spinner) findViewById(R.id.spBaud);
		spDestination = (Spinner) findViewById(R.id.spDestination);
		eTsource = (EditText) findViewById(R.id.eTsource);
		eTdestination = (EditText) findViewById(R.id.eTdestination);

		eTpan.setOnLongClickListener(longClickListener);
		spBaud.setOnLongClickListener(longClickListener);
		eTsource.setOnLongClickListener(longClickListener);
		eTdestination.setOnLongClickListener(longClickListener);

		spDestination.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if(arg2==0){
					eTdestination.setVisibility(View.VISIBLE);
				} else if (arg2 == 1){
					eTdestination.setVisibility(View.GONE);
					eTdestination.setText(0xFFFF + "");
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});

		loadPreferences();
	}

	OnLongClickListener longClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {

			if(v==eTpan) HelpPAN();
			if(v==spBaud) HelpBaud();
			if(v==eTsource) HelpSource();
			if(v==eTdestination) HelpDestination();

			return false;
		}
	};

	public void Setup(View v){

		if(!checkValues()) return;

//		Intent i = new Intent(this, CommunicationActivity.class);
//		Bundle b = new Bundle();
//		b.putString(Constants.PAN_ID, eTpan.getText().toString());
//		b.putString(Constants.BAUD, spBaud.getSelectedItem().toString().replace(" bps", ""));
//		b.putString(Constants.SOURCE_ADDRESS, eTsource.getText().toString());
//		b.putString(Constants.DESTINATION_ADDRESS, eTdestination.getText().toString());
//
//		i.putExtra(Constants.INFO_BUNDLE, b);

		savePreferences();

//		startActivity(i);
		
		Toast.makeText(this, "XBee will be configured upon restart", Toast.LENGTH_SHORT).show();

		finish();

	}

	private boolean checkValues(){

		boolean error = false;
		try{
			int src = eTsource.getText().toString().toLowerCase().contains("0x")?
					Integer.valueOf(eTsource.getText().toString().toLowerCase().replace("0x", ""), 16):
						Integer.valueOf(eTsource.getText().toString());

					int dest = eTdestination.getText().toString().toLowerCase().contains("0x")?
							Integer.valueOf(eTdestination.getText().toString().toLowerCase().replace("0x", ""), 16):
								Integer.valueOf(eTdestination.getText().toString());

							int pan = eTpan.getText().toString().toLowerCase().contains("0x")?
									Integer.valueOf(eTpan.getText().toString().toLowerCase().replace("0x", ""), 16):
										Integer.valueOf(eTpan.getText().toString());

									eTdestination.setText(dest+"");
									eTsource.setText(src+"");
									eTpan.setText(pan+"");

									if(src > 0xFFFF) {
										eTsource.setError("Source address should be less than 65535");
										error = true;
									}
									if(src < 0) {
										eTsource.setError("Source address should be greater or equal to 0");
										error = true;
									}
									if(dest > 0xFFFF ) {
										eTdestination.setError("Destination address should be less than 65535");
										error = true;
									}
									if(dest < 0) {
										eTdestination.setError("Destination address should be greater or equal to 0");
										error = true;
									}
									if(pan > 0xFFFF ) {
										eTpan.setError("PAN ID should be less than 65535");
										error = true;
									}
									if(pan < 0) {
										eTpan.setError("PAN ID should be greater or equal to 0");
										error = true;
									}
		} catch (Exception e){
			Toast.makeText(this, "An error occured in the values", Toast.LENGTH_SHORT).show();
		}

		if(error) return false;
		return true;

	}

	private void loadPreferences(){

		for(int i = 0 ; i < spBaud.getCount(); i++){
			if(spBaud.getItemAtPosition(i).toString().equals(App.BaudRate + " bps")){
				spBaud.setSelection(i);
				break;
			}
		}
		
		eTpan.setText(App.PanId);
		eTsource.setText(App.SrcAddress);
		spDestination.setSelection(App.DestAddress.equals("65535") ? 1 : 0);
		eTdestination.setText(App.DestAddress);
		
//		spBaud.setSelection(sharedPref.getInt(Constants.BAUD, 3));
//		eTpan.setText(sharedPref.getString(Constants.PAN_ID, "1"));
//		eTsource.setText(sharedPref.getString(Constants.SOURCE_ADDRESS, "1"));
//		spDestination.setSelection(sharedPref.getInt(Constants.DESTINATION_TYPE, 0));
//		eTdestination.setText(sharedPref.getString(Constants.DESTINATION_ADDRESS, "2"));

	}

	private void savePreferences(){

		//Editor editor = sharedPref.edit();
//		editor.putInt(Constants.BAUD, spBaud.getSelectedItemPosition());
//		editor.putString(Constants.PAN_ID, eTpan.getText().toString());
//		editor.putString(Constants.SOURCE_ADDRESS, eTsource.getText().toString());
//		editor.putInt(Constants.DESTINATION_TYPE, spDestination.getSelectedItemPosition());
//		editor.putString(Constants.DESTINATION_ADDRESS, eTdestination.getText().toString());

		//editor.commit();
		
		App.PanId = eTpan.getText().toString();
		App.SrcAddress = eTsource.getText().toString();
		App.DestAddress = eTdestination.getText().toString();
		App.BaudRate = spBaud.getSelectedItem().toString().replace(" bps", "");
		App.isConfigured = false;
		App.saveSettings();

	}

	public void HelpBaud(){
		alert("Sets the baud rate. Default is 9600 baud/sec. The baud rates of both Zigbee devices have to match.");
	}

	public void HelpPAN(){
		alert("Sets the PAN ID in decimal or hex form. Default is 1. The Zigbee devices have to have the same PAN to be able to communicate.");
	}

	public void HelpSource(){
		alert("Sets the source address for this Zigbee device in decimal or hex form.");
	}

	public void HelpDestination(){
		alert("Sets the destination address for this Zigbee device in decimal or hex form.");
	}

	private void alert(String message){
		new AlertDialog.Builder(this)
		.setMessage(message)
		.setPositiveButton("Ok", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create().show();
	}

}
