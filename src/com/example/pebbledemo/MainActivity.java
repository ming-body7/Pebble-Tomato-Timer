package com.example.pebbledemo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleAckReceiver;
import com.getpebble.android.kit.PebbleKit.PebbleNackReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.support.v7.app.ActionBarActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
	
	private static final String TAG = "Pebble";
	private TextView introduction_text;
	private Button push_notification;
	private Button send_message;
	private Button open_app;
	private Button close_app;
	private boolean connected = false;
	private final static UUID PEBBLE_APP_UUID = UUID.fromString("5570367d-0195-4d39-9bae-c369346005e5");
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        introduction_text = (TextView)findViewById(R.id.introduce);
        push_notification = (Button)findViewById(R.id.push_notification);
        send_message = (Button)findViewById(R.id.send_message);
        open_app = (Button)findViewById(R.id.openApp);
        close_app = (Button)findViewById(R.id.closeApp);
        initView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void initView(){
    	push_notification.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendAlertToPebble();
			}
		});
    	
    	send_message.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendMessage();
				
			}
		});
    	open_app.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				openMyApp();
				
			}
		});
    	
    	close_app.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				closeMyApp();
				
			}
		});
    	boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
    	Log.i(getLocalClassName(), "Pebble is " + (connected ? "connected" : "not connected"));
    }
    
    
    public void sendAlertToPebble() {
    	  final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

    	  final Map data = new HashMap();
    	  data.put("title", "Test Message");
    	  data.put("body", "Whoever said nothing was impossible never tried to slam a revolving door.");
    	  final JSONObject jsonData = new JSONObject(data);
    	  final String notificationData = new JSONArray().put(jsonData).toString();

    	  i.putExtra("messageType", "PEBBLE_ALERT");
    	  i.putExtra("sender", "MyAndroidApp");
    	  i.putExtra("notificationData", notificationData);

    	  Log.d(TAG, "About to send a modal alert to Pebble: " + notificationData);
    	  sendBroadcast(i);
    	}
    
    public void detectingConnection(){
    	PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), new BroadcastReceiver() {

    		  @Override
    		  public void onReceive(Context context, Intent intent) {
    		    Log.i(getLocalClassName(), "Pebble connected!");
    		  }

    		});

    		PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), new BroadcastReceiver() {

    		  @Override
    		  public void onReceive(Context context, Intent intent) {
    		    Log.i(getLocalClassName(), "Pebble disconnected!");
    		  }

    		});
    }
    
    public void openMyApp(){
    	//open my app identified by UUID
    	PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
    }
    
    public void closeMyApp(){
    	// Closing my app
    	PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
    }
    
    public void checkCompatibilityWithAppMessage(){
    	
    	//firmware 2.0+ is support
    	if (PebbleKit.areAppMessagesSupported(getApplicationContext())) {
    		  Log.i(getLocalClassName(), "App Message is supported!");
    		} else {
    		  Log.i(getLocalClassName(), "App Message is not supported");
    		}
    }
    
    public void sendMessage(){
    	PebbleDictionary data = new PebbleDictionary();

    	// Add a key of 0, and a uint8_t (byte) of value 42.
    	data.addUint8(0, (byte) 42);

    	// Add a key of 1, and a string value.
    	data.addString(1, "A string");
    	
    	PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);
    }
    
    public void registerMessage(){
    	PebbleKit.registerReceivedAckHandler(getApplicationContext(), new PebbleAckReceiver(PEBBLE_APP_UUID) {

    		  @Override
    		  public void receiveAck(Context context, int transactionId) {
    		    Log.i(getLocalClassName(), "Received ack for transaction " + transactionId);
    		  }

    		});

    		PebbleKit.registerReceivedNackHandler(getApplicationContext(), new PebbleNackReceiver(PEBBLE_APP_UUID) {

    		  @Override
    		  public void receiveNack(Context context, int transactionId) {
    		    Log.i(getLocalClassName(), "Received nack for transaction " + transactionId);
    		  }

    		});
    		
    		PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {

    			  @Override
    			  public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
    			    Log.i(getLocalClassName(), "Received value=" + data.getUnsignedIntegerAsLong(0) + " for key: 0");

    			    PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
    			  }

    			});
    }
}


