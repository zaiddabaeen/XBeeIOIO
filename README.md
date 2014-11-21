#XBeeIOIO
========

**Requirements: Android 2.3.3+ (API 10)**

XBeeIOIO is an open-source library that allows the Android device to communicate with [Xbee](http://www.digi.com/xbee/), a ZigBee module, through [IOIO](https://github.com/ytai/ioio).

XBeeIOIO is well documented, and the docs are available in the root folder of this repo.

##Setup

Connect the Xbee and the IOIO as follows:

| Xbee        | IOIO           | 
| ------------- |:-------------:| 
| VIN      | 3.3V | 
| GND      | GND      | 
| DIN | UART Pin      |
| DOUT | UART Pin      |

Add the IOIO libraries to your workspace. Add then the XbeeIOIO library to your workspace and add it to your project's java build path.

##Usage

Declare an Xbee object, and in the setup method of the BaseIOIOLooper initialize it:

```java
	xbee = new Xbee(ioio_,45,46,9600);
```

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
	xbee.write("Writing this to output stream");
```


