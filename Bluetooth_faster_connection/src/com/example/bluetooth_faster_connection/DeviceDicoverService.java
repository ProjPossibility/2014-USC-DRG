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

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * This Service appears as background program. It continues to scan for proper bluetooth device
 * and connect to it then start our communication.
 */
public class DeviceDicoverService extends Service {
    // Debugging
    private static final String TAG = "DeviceDicoverService";
    private static final boolean D = true;
    
    private static  boolean DEVICESELECTED = false;
    
    public static String  DEVICE_CONNECTION_ADDRESS = "com.example.bluetooth_faster_connection.device_connection_address";
    public static String  DEVICE_CONNECTION_INFO = "com.example.bluetooth_faster_connection.device_connection_info";

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_DEVICE_NAME = "device_name";
    public static String EXTRA_DEVICE_RSSI = "device_rssi";

    // Member fields
    private BluetoothAdapter mBtAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        if (D) Log.d(TAG, "Service started");
        
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Notification notification = new Notification(android.R.drawable.arrow_up_float,
                "my_service_name",
                 System.currentTimeMillis());
        PendingIntent p_intent = PendingIntent.getActivity(this, 0,
                 new Intent(this, MainActivity.class), 0);
        notification.setLatestEventInfo(this, "MyServiceNotification", "MyServiceNotification is Running!",   p_intent);
        Log.d(TAG, String.format("notification = %s", notification));
        startForeground(0x1982, notification);
         
        //start scanning
        doDiscovery();
        
    }

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            	if (D) Log.d(TAG, "Service bluetooth device founded.");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);
                try{
                	if (device != null){
                		System.out.println(device.getName());
                		System.out.println(device.getAddress());
                		System.out.println("rssi = "+ rssi);
                		SendDeviceInfo(device.getName(),device.getAddress(),rssi);
                		//if the device is what we want to connect to then.
                		/*if(device.getName().equals("qs-Ubuntu") ){ 
                			
                			//broadcast the selected address 
                			DeviceSelected(device.getAddress());
                			//set flag to true;
                			DEVICESELECTED=true;
                		}*/
                		if (device.getName().equals(MainActivity.Target)){
                			Log.v(TAG,"Get target phone");
                			if (mBtAdapter.isDiscovering()) {
                	            mBtAdapter.cancelDiscovery();
                	        }
                			//mBtAdapter.startDiscovery();
                		}
                	}
                } catch (Exception e){
                	e.printStackTrace();
                }

            }   
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                Log.v(TAG,"Entered the Finished ");
                
                if(!DEVICESELECTED){
                	Log.v(TAG,"Restart scanning");
                	 //Toast.makeText(context, "restart scanning", Toast.LENGTH_SHORT).show();
                    mBtAdapter.startDiscovery(); 	
                }
            }
        }
    };
    
    
    public void DeviceSelected( String address){
    	 mBtAdapter.cancelDiscovery();
    	 
    	 if (D) Log.d(TAG, "DeviceSelected()");
         

         // Create the result Intent and include the MAC address
         Intent intent = new Intent();
         intent.setAction(DEVICE_CONNECTION_ADDRESS);
         intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

         // Set result and finish this Activity
         //this.setResult(Activity.RESULT_OK, intent);
         sendBroadcast(intent);
         if (D) Log.d(TAG, "Service device address broadcast sent.");
    }

    public void SendDeviceInfo(String name, String address, int rssi  ){
   	 
   	 if (D) Log.d(TAG, "SendDeviceInfo()");

        // Create the result Intent and include the MAC address
        Intent intent = new Intent();
        intent.setAction(DEVICE_CONNECTION_INFO);
        intent.putExtra(EXTRA_DEVICE_NAME, name);
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
        intent.putExtra(EXTRA_DEVICE_RSSI, rssi);

        // Set result and finish this Activity
        //this.setResult(Activity.RESULT_OK, intent);
        sendBroadcast(intent);
        if (D) Log.d(TAG, "SendDeviceInfo broadcast sent.");
        
   }

    @Override
	public void onDestroy() {
        super.onDestroy();
        if (D) Log.d(TAG, "Service Destroy()");
        

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
        
        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

  
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}