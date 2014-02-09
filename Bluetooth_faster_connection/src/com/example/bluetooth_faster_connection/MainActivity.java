/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetooth_faster_connection;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.accelerometer.AccelerometerListener;
import com.example.accelerometer.AccelerometerManager;
import com.example.httpasyncpost.JSONUtil;
import com.example.httpasyncpost.ServerRestClientUsage;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current Message session.
 */
public class MainActivity extends Activity implements AccelerometerListener {
	// Debugging
	private static final String TAG = "BluetoothMessage";
	private static final boolean D = true;

	// Message types sent from the BluetoothMessageService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothMessageService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	private static final int REQUEST_DISCOVERABLE = 4;
	private static final int REQUEST_CONNECT_DEVICE = 5;

	public static String Target = "";

	// Layout Views
	private TextView mTitle;
	private TextView mInfo;
	private ListView mConversationView;
	/*
	 * private EditText mOutEditText; private Button mSendButton;
	 */

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the Message services
	private BluetoothMessageService mMessageService = null;

	private int mBluetoothUpdateCounter = 0;
	private Timer timer;
	private TimerTask mTimerTask;
	private int mBluetoothRssi = 0;

	public String s_dns1;
	public String s_dns2;
	public String s_gateway;
	public String s_ipAddress;
	public String s_leaseDuration;
	public String s_netmask;
	public String s_serverAddress;
	TextView info;
	DhcpInfo d;
	public WifiManager wifii;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		mInfo = (TextView) findViewById(R.id.myText);

		Button mButton = (Button) findViewById(R.id.myButton);
		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openOptionsMenu();
			}
		});

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// register for background service broadcast.
		IntentFilter ifilt = new IntentFilter(
				DeviceDicoverService.DEVICE_CONNECTION_ADDRESS);
		ifilt.addAction(DeviceDicoverService.DEVICE_CONNECTION_INFO);
		registerReceiver(mReceiver, ifilt);

		// just for test purpose, should be deleted if not in test procedure.
		Intent service = new Intent(this, DeviceDicoverService.class);
		startService(service);

		wifii = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wifii.getConnectionInfo();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupMessage() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableDiscoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			enableDiscoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			startActivity(enableDiscoverableIntent);
			// startActivityForResult(enableDiscoverableIntent,
			// REQUEST_ENABLE_BT);
			// Otherwise, setup the Message session
		} else {
			if (mMessageService == null)
				setupMessage();
		}

	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mMessageService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mMessageService.getState() == BluetoothMessageService.STATE_NONE) {
				// Start the Bluetooth Message services
				mMessageService.start();
			}
		}

		Toast.makeText(getBaseContext(), "onResume Accelerometer Started",
				Toast.LENGTH_SHORT).show();

		// Check device supported Accelerometer senssor or not
		if (AccelerometerManager.isSupported(this)) {

			// Start Accelerometer Listening
			AccelerometerManager.startListening(this);
		}
	}

	private void setupMessage() {
		Log.d(TAG, "setupMessage()");

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);
		mConversationView = (ListView) findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);

		/*
		 * // Initialize the compose field with a listener for the return key
		 * mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		 * mOutEditText.setOnEditorActionListener(mWriteListener);
		 * 
		 * // Initialize the send button with a listener that for click events
		 * mSendButton = (Button) findViewById(R.id.button_send);
		 * mSendButton.setOnClickListener(new OnClickListener() { public void
		 * onClick(View v) { // Send a message using content of the edit text
		 * widget TextView view = (TextView) findViewById(R.id.edit_text_out);
		 * String message = view.getText().toString(); sendMessage(message); }
		 * });
		 */

		// Initialize the BluetoothMessageService to perform bluetooth
		// connections
		mMessageService = new BluetoothMessageService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");

		// Check device supported Accelerometer senssor or not
		if (AccelerometerManager.isListening()) {

			// Start Accelerometer Listening
			AccelerometerManager.stopListening();

			Toast.makeText(getBaseContext(), "onStop Accelerometer Stoped",
					Toast.LENGTH_SHORT).show();
			// v.cancel();

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Check device supported Accelerometer senssor or not
		if (AccelerometerManager.isListening()) {

			// Start Accelerometer Listening
			AccelerometerManager.stopListening();

			Toast.makeText(getBaseContext(), "onDestroy Accelerometer Stoped",
					Toast.LENGTH_SHORT).show();
		}

		// Stop the Bluetooth Message services
		if (mMessageService != null)
			mMessageService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");

		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);

		// service stop;
		Intent service = new Intent(this, DeviceDicoverService.class);
		stopService(service);
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mMessageService.getState() != BluetoothMessageService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothMessageService to
			// write
			byte[] send = message.getBytes();
			mMessageService.write(send);

			/*
			 * // Reset out string buffer to zero and clear the edit text field
			 * mOutStringBuffer.setLength(0);
			 * mOutEditText.setText(mOutStringBuffer);
			 */
		}
	}

	/*
	 * // The action listener for the EditText widget, to listen for the return
	 * key private TextView.OnEditorActionListener mWriteListener = new
	 * TextView.OnEditorActionListener() { public boolean
	 * onEditorAction(TextView view, int actionId, KeyEvent event) { // If the
	 * action is a key-up event on the return key, send the message if (actionId
	 * == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
	 * String message = view.getText().toString(); sendMessage(message); } if(D)
	 * Log.i(TAG, "END onEditorAction"); return true; } };
	 */

	// The Handler that gets information back from the BluetoothMessageService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothMessageService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					mConversationArrayAdapter.clear();
					break;
				case BluetoothMessageService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothMessageService.STATE_LISTEN:
				case BluetoothMessageService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
						+ readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;

		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;

		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a Message session
				setupMessage();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceDicoverService.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mMessageService.connect(device);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		/*
		 * case R.id.secure_connect_scan: // Launch the DeviceListActivity to
		 * see devices and do scan serverIntent = new Intent(this,
		 * DeviceListActivity.class); startActivityForResult(serverIntent,
		 * REQUEST_CONNECT_DEVICE_SECURE); return true;
		 */
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			/*
			 * serverIntent = new Intent(this, DeviceListActivity.class);
			 * startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			 */

			Intent service = new Intent(this, DeviceDicoverService.class);
			startService(service);

			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			mMessageService.write(BluetoothMessageService.VOL_UP);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			mMessageService.write(BluetoothMessageService.VOL_DOWN);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (D)
				Log.d(TAG, "Service broadcast received.");

			if (action.equals(DeviceDicoverService.DEVICE_CONNECTION_ADDRESS)) {
				// establish insecure connection.
				connectDevice(intent, false);
			}
			if (action.equals(DeviceDicoverService.DEVICE_CONNECTION_INFO)) {
				mBluetoothUpdateCounter++;
				// establish insecure connection.
				String name = intent
						.getStringExtra(DeviceDicoverService.EXTRA_DEVICE_NAME);
				String address = intent
						.getStringExtra(DeviceDicoverService.EXTRA_DEVICE_ADDRESS);
				int rssi = intent.getIntExtra(
						DeviceDicoverService.EXTRA_DEVICE_RSSI, 0);
				if (Target.equals(address)) {
					if (mBluetoothUpdateCounter > 100
							| mBluetoothUpdateCounter < 0) {
						mBluetoothUpdateCounter = 0;
					}

					mInfo.setText("Name:" + name + "\nAddress:" + address
							+ "\nRSSI:" + rssi + "\nCounter:"
							+ mBluetoothUpdateCounter);
					setRssi(rssi);
					if (timer == null) {
						int seconds = 1;
						timer = new Timer();
						timer.schedule(new UpdateTask(), 1, seconds * 1000);
					}
				}
			}
		}
	};

	class UpdateTask extends TimerTask {
		public void run() {
			int rssi = getRssi();
			System.out.println("Timer updated rssi is :" + rssi);
		}
	}

	private void setRssi(int in) {
		mBluetoothRssi = in;
	}

	private int getRssi() {
		return mBluetoothRssi;
	}
	
	public static String getBluetoothMacAddress() {
	    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	 
	    // if device does not support Bluetooth
	    if(mBluetoothAdapter==null){
	        Log.d(TAG,"device does not support bluetooth");
	        return null;
	    }
	     
	    return mBluetoothAdapter.getAddress();
	}

	@Override
	public void onAccelerationChanged(float x, float y, float z) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onShake(float force) {
		// TODO Auto-generated method stub
		HashMap<String, String> map = sendmessage();
		ServerRestClientUsage server = new ServerRestClientUsage();
		server.getPairedPhone(map, this);
	}

	public void vibrate(int duration) {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(duration);
	}

	public HashMap<String, String> sendmessage() {
		d = wifii.getDhcpInfo();
		s_dns1 = String.valueOf(d.dns1);
		s_dns2 = String.valueOf(d.dns2);
		s_gateway = String.valueOf(d.gateway);
		s_ipAddress = String.valueOf(d.ipAddress);
		s_leaseDuration = String.valueOf(d.leaseDuration);
		s_netmask = String.valueOf(d.netmask);
		s_serverAddress = String.valueOf(d.serverAddress);
		HashMap<String, String> hashmap = new HashMap<String, String>();
		hashmap.put("DNS 1", s_dns1);
		hashmap.put("DNS 2", s_dns2);
		hashmap.put("Default Gateway", s_gateway);
		hashmap.put("IP Address", s_ipAddress);
		hashmap.put("lease Time", s_leaseDuration);
		hashmap.put("Subnet Mask", s_netmask);
		hashmap.put("Server IP:", s_serverAddress);
		hashmap.put("mac_address", "94:DB:C9:13:EB:FD");
		hashmap.put("shake_date", "00000");
		return hashmap;
	}

	public void getResponseMessage(String response) {
		// TODO Auto-generated method stub
		Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
		JSONObject obj = JSONUtil.convertFromStrToJson(response);
		try {
			this.Target = obj.get("macAddress").toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}