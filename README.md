#XBeeIOIO
========

**Requirements: Android 2.2+ (API 8)**

XBeeIOIO is an open-source library that allows the Android device to communicate with [Xbee](http://www.digi.com/xbee/), a ZigBee module, through [IOIO](https://github.com/ytai/ioio).

> You can also use IOIO-OTG too.

XBeeIOIO is well documented, and the docs are available in the root folder of this repo.

##Setup

Connect the Xbee and the IOIO as follows:

| Xbee        | IOIO          | 
| ----------- |:-------------:| 
| VIN      | 3.3V | 
| GND      | GND  | 
| DIN | UART Pin  |
| DOUT | UART Pin |

Add the IOIO libraries to your workspace. Add then the XbeeIOIO library to your workspace and add it to your project's java build path.

> Don't forget to clean your project.

##Usage

Declare an Xbee object, and in the setup method of the BaseIOIOLooper initialize it:

```java
	xbee = new Xbee(ioio_,45,46,9600);
```

> Where 45 and 46 are UART compatible pins.

To configure your XBee:

```java
    xbee.configure(
        new XbeeConfiguration(xbee).setBaudRate()
        .setDestinationAddress(0x4F)
        .setSourceAddress(0x32)
        .setPanID(0x12).writeToNonVolatileMemory(), null);
```

> Where the baud rate is the set baud rate at declaration (9600), destination address is 0x4F, source address is 0x32 and Personal Area Network (PAN) ID is 0x32. Write to non-volatile memory.

Optionally, set up the onDataReceivedListener, and the onDataSentListener:

```java

	xbee.setOnDataReceivedListener(new OnDataReceivedListener() {
						@Override
						public void onDataReceived(String data) {
							// Use data
						}
					});

	xbee.setOnDataSentListener(new OnDataSentListener() {
						@Override
						public void onDataSent(String data) {
							// Use data
						}
					});
```

To write to the output stream use:

```java
	xbee.writeLine("Writing this to output stream");
```

##More options
- To put your data into a packet format, you can construct packet format using JSON. Example:
```java
	/** Constructs a JSON-formatted packet.
	 * 
	 * @param data Data of the packet.
	 * @return A readable string of the JSON object.
	 */
	private String constructPacket(String data) {

		JSONObject jPacket = new JSONObject();

		try {
			jPacket.put("src", SOURCE_ADDRESS);
			jPacket.put("dest", DESTINATION_ADDRESS);
			jPacket.put("data", data);
			jPacket.put("fcs", getCRC(data));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jPacket.toString();

	}
```

- To enable CRC checking, you may use the following methods:-
```java
	/**
	 * 
	 * @param data The data to get its CRC.
	 * @return A hex string of the CRC value (FCS).
	 */
	public String getCRC(String data){
		CRC32 crc = new CRC32();
		crc.update(data.getBytes());
		String FCS = Long.toHexString(crc.getValue());
		crc.reset();
		return FCS;
	}
```
The returned value (FCS) from this method must be added separately to the data sent in order to check it on the receiver side. On the receiver side, run getCRC on the received data, if the FCS equals the returned value, then the data received is correct.

##Credits

This project is developed by [Zaid Daba'een](https://github.com/zaiddabaeen) and is licensed under [GNU GPLv2](https://github.com/zaiddabaeen/XBeeIOIO/blob/master/LICENSE).


