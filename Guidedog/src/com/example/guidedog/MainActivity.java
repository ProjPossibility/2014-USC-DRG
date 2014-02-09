package com.example.guidedog;

 
import java.util.HashMap;

import com.example.guidedog.R;
import com.example.guidedog.R.layout;
import com.example.httpasyncpost.ServerRestClientUsage;

import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
 
public class MainActivity extends Activity implements AccelerometerListener{
	 public String   s_dns1 ;
	    public String   s_dns2;     
	    public String   s_gateway;  
	    public String   s_ipAddress;    
	    public String   s_leaseDuration;    
	    public String   s_netmask;  
	    public String   s_serverAddress;
	    TextView info;
	    DhcpInfo d;
	    public WifiManager wifii;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifii= (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wifii.getConnectionInfo();
	       
         
        // Check onResume Method to start accelerometer listener
    }
    
    public void onAccelerationChanged(float x, float y, float z) {
        // TODO Auto-generated method stub
    	
         
    }
    public HashMap<String, String> sendmessage(){
    	 d=wifii.getDhcpInfo();
	        s_dns1=String.valueOf(d.dns1);
	        s_dns2=String.valueOf(d.dns2);    
	        s_gateway=String.valueOf(d.gateway);    
	        s_ipAddress=String.valueOf(d.ipAddress); 
	        s_leaseDuration=String.valueOf(d.leaseDuration);     
	        s_netmask=String.valueOf(d.netmask);    
         s_serverAddress=String.valueOf(d.serverAddress);
    	 HashMap<String, String> hashmap = new HashMap<String,String>();
         hashmap.put("DNS 1", s_dns1);
         hashmap.put("DNS 2", s_dns2);
         hashmap.put("Default Gateway", s_gateway);
         hashmap.put("IP Address", s_ipAddress);
         hashmap.put("lease Time", s_leaseDuration);
         hashmap.put("Subnet Mask", s_netmask);
         hashmap.put("Server IP:", s_serverAddress);
         hashmap.put("mac_address", "1111111");
         hashmap.put("shake_date", "00000");
         return hashmap;
    }
    
    public void vibrate(int duration)
    {
       Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
       v.vibrate(duration);    
    }
 
    public void onShake(float force) {
         
        // Do your stuff here
    	
      
    	//vibrate(1000);

        // vibrate(10);
        // Called when Motion Detected
      //  Toast.makeText(getBaseContext(), "Motion detected", 
      //          Toast.LENGTH_SHORT).show();
      HashMap<String, String> map =  sendmessage();
      ServerRestClientUsage server = new ServerRestClientUsage();
      server.getPairedPhone(map, this);     
//      String response = server.responseMessage;
      
//      Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show();
    }
 
    

	@Override
    public void onResume() {
            super.onResume();
            Toast.makeText(getBaseContext(), "onResume Accelerometer Started", 
                    Toast.LENGTH_SHORT).show();
             
            //Check device supported Accelerometer senssor or not
            if (AccelerometerManager.isSupported(this)) {
                 
                //Start Accelerometer Listening
                AccelerometerManager.startListening(this);
            }
    }
     
    @Override
    public void onStop() {
            super.onStop();
             
            //Check device supported Accelerometer senssor or not
            if (AccelerometerManager.isListening()) {
                 
                //Start Accelerometer Listening
                AccelerometerManager.stopListening();
                 
                Toast.makeText(getBaseContext(), "onStop Accelerometer Stoped", 
                         Toast.LENGTH_SHORT).show();
                //v.cancel();

            }
            
    }
     
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Sensor", "Service  distroy");
         
        //Check device supported Accelerometer senssor or not
        if (AccelerometerManager.isListening()) {
             
            //Start Accelerometer Listening
            AccelerometerManager.stopListening();
             
            Toast.makeText(getBaseContext(), "onDestroy Accelerometer Stoped", 
                   Toast.LENGTH_SHORT).show();
        }
             
    }

	public void getResponseMessage(String response) {
		// TODO Auto-generated method stub
		Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
	}
 
}