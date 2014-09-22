/*
 * Credits: with help from android open source project, Google
 * 
 */
package com.example.bluedex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.BluePower.R;

public class MainActivity extends Activity {
	private Button onBtn, offBtn, listBtn, findBtn, leftBtn, rightBtn, upBtn,
			downBtn, autoBtn, ctrlBtn;

	private ListView lv;
	private TextView text;
	private ArrayAdapter<String> BTArrayAdapter;
	private BluetoothAdapter bluetooth; // represents the local bluetooth
										// adapter
	private BroadcastReceiver bReceiver;
	private Set<BluetoothDevice> pairedDevices;
	ArrayList<String> ALPairedDevice;
	ArrayList<BluetoothDevice> blueArray;
	public static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	protected static final int MESSAGE_UP = 2;
	protected static final int MESSAGE_DOWN = 3;
	protected static final int MESSAGE_LEFT = 4;
	protected static final int MESSAGE_RIGHT = 5;
	protected static final int MESSAGE_AUTOMATIC = 6;
	protected static final int MESSAGE_CONTROLLER = 7;

	protected static final int MESSAGE_CLEAR = 8;

	ConnectedThread connectedThread;
	ConnectThread connect;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SUCCESS_CONNECT:
				// DO something
				connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
				Toast.makeText(getApplicationContext(), "CONNECTED", 0).show();
				text.setText("Status: Connected");
				// String s = "successfully connected";
				// connectedThread.write(s.getBytes());
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				String string = new String(readBuf);
				Toast.makeText(getApplicationContext(), string, 0).show();
				break;
			case MESSAGE_UP:

				connectedThread.write(new byte[] { 0x38 });
				break;
			case MESSAGE_DOWN:

				connectedThread.write(new byte[] { 0x32 });
				break;
			case MESSAGE_LEFT:

				connectedThread.write(new byte[] { 0x34 });
				break;
			case MESSAGE_RIGHT:

				connectedThread.write(new byte[] { 0x36 });
				break;
			case MESSAGE_AUTOMATIC:

				connectedThread.write(new byte[] { 0x43 });
				break;
			case MESSAGE_CONTROLLER:

				connectedThread.write(new byte[] { 0x44 });
				break;
			case MESSAGE_CLEAR:

				connectedThread.write(new byte[] { 0x30 });
				break;

			}
		}
	};

	private ActionBar actionBar;
	public static Context context;
	public static String DEVICE_MAC_ADDRESS = "MAC_ADDRESS";
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 5;
	private static final int REQUEST_ENABLE_BT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		onStartUp();

		if (bluetooth == null) {
			onBtn.setEnabled(false);
			offBtn.setEnabled(false);
			listBtn.setEnabled(false);
			findBtn.setEnabled(false);

			Toast.makeText(getApplicationContext(),
					"device does not support Bluetooth", Toast.LENGTH_LONG)
					.show();
			finish();
		} else {

		}

	}

	private void onStartUp() {
		// TODO Auto-generated method stub
		// get and hide action bar
		actionBar = getActionBar();
		actionBar.hide();
		bluetooth = BluetoothAdapter.getDefaultAdapter();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		lv = (ListView) findViewById(R.id.listView1);
		// create the arrayAdapter that contains the BTDevices, and set it to
		// the ListView
		BTArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		lv.setAdapter(BTArrayAdapter);
		bluetooth = BluetoothAdapter.getDefaultAdapter();
		blueArray = new ArrayList<BluetoothDevice>();

		text = (TextView) findViewById(R.id.text);
		text.setText("Status: ");
		onBtn = (Button) findViewById(R.id.button1);
		onBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				on(v);
			}
		});

		offBtn = (Button) findViewById(R.id.button4);
		offBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				off(v);
			}
		});

		listBtn = (Button) findViewById(R.id.button3);
		listBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				list(v);
			}
		});

		findBtn = (Button) findViewById(R.id.search);
		findBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				find(v);
			}
		});

		upBtn = (Button) findViewById(R.id.button5);
		upBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					sendMessage(MESSAGE_UP);

					break;
				case MotionEvent.ACTION_UP:
					sendMessage(MESSAGE_CLEAR);

					break;

				}
				return true;
			}

		});

		downBtn = (Button) findViewById(R.id.button8);
		downBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					sendMessage(MESSAGE_DOWN);

					break;
				case MotionEvent.ACTION_UP:
					sendMessage(MESSAGE_CLEAR);
					break;

				}
				return true;
			}
		});

		leftBtn = (Button) findViewById(R.id.button6);
		leftBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					sendMessage(MESSAGE_LEFT);

					break;
				case MotionEvent.ACTION_UP:
					sendMessage(MESSAGE_CLEAR);
					break;
				}
				return true;
			}
		});

		rightBtn = (Button) findViewById(R.id.button7);
		rightBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					sendMessage(MESSAGE_RIGHT);
					break;
				case MotionEvent.ACTION_UP:
					sendMessage(MESSAGE_CLEAR);
					break;
				}
				return true;
			}
		});

		autoBtn = (Button) findViewById(R.id.buttonA);
		autoBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					sendMessage(MESSAGE_AUTOMATIC);

					break;
				case MotionEvent.ACTION_MOVE:
					sendMessage(MESSAGE_AUTOMATIC);
					break;
				case MotionEvent.ACTION_UP:
					sendMessage(MESSAGE_CLEAR);
					break;
				default:
					break;
				}
				return true;
			}
		});

		// autoBtn = (Button) findViewById(R.id.buttonA);
		// autoBtn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// Toast.makeText(getApplicationContext(), "Automated",
		// Toast.LENGTH_SHORT).show();
		// sendMessage(MESSAGE_UP);
		//
		// }
		// });

		ctrlBtn = (Button) findViewById(R.id.buttonC);
		ctrlBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "Phone Control",
						Toast.LENGTH_SHORT).show();
				sendMessage(MESSAGE_CONTROLLER);

			}
		});

		ALPairedDevice = new ArrayList<String>();

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				int btState = bluetooth.getState();

				// TODO Auto-generated method stub
				int itemPosition = position;
				// ListView Clicked item value
				String itemValue = (String) lv.getItemAtPosition(position);
				String MAC_address = itemValue.substring(itemValue.length() - 17);

				// create an intent to connect
				Intent connectIntent = new Intent();
				connectIntent.putExtra(DEVICE_MAC_ADDRESS, MAC_address);
				if (connect != null && connect.isConnected()) {
					connectedThread.cancel();
					Toast.makeText(getApplicationContext(),
							"Device disconnected.", Toast.LENGTH_SHORT).show();
					text.setText("Disconnected");

				}
				if (bluetooth.isDiscovering()) {
					// discovery is costly so we cancel it
					bluetooth.cancelDiscovery();
				}
				if (btState == BluetoothAdapter.STATE_ON) {

					BluetoothDevice selectedDevice = blueArray.get(position);
					connect = new ConnectThread(selectedDevice);
					connect.start();
					// BTArrayAdapter.clear();

				} else {
					Toast.makeText(getApplicationContext(),
							"Turn on Bluetooth before pairing.", 0).show();

				}

			}
		});
		// Register for broadcasts on BluetoothAdapter state change
		IntentFilter filter = new IntentFilter(
				BluetoothAdapter.ACTION_STATE_CHANGED);
		// register the broadcast reciever
		bReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				// check if action state changes
				if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
					final int state = intent.getIntExtra(
							BluetoothAdapter.EXTRA_STATE,
							BluetoothAdapter.ERROR);
					switch (state) {
					case BluetoothAdapter.STATE_OFF:
						text.setText("Status: Bluetooth off");
						break;
					case BluetoothAdapter.STATE_TURNING_OFF:
						text.setText("Status: Turning Bluetooth off...");
						break;
					case BluetoothAdapter.STATE_ON:
						text.setText("Status: Bluetooth on");
						break;
					case BluetoothAdapter.STATE_TURNING_ON:
						text.setText("Status: Turning Bluetooth on...");
						break;

					}
				}

				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					blueArray.add(device);
					String s = "";
					for (int a = 0; a < ALPairedDevice.size(); a++) {
						if (device.getName().equals(ALPairedDevice.get(a))) {
							// append
							s = "(Paired)";
							break;
						}
					}

					// add the name and the MAC address of the object to the
					// arrayAdapter
					BTArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
					BTArrayAdapter.notifyDataSetChanged();
					text.setText("Status: Devices Found");

				}

				else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
						.equals(action)) {
					text.setText("Status: Searching...");
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
						.equals(action) && BTArrayAdapter.getCount() == 0) {
					text.setText("Status: No Devices Found");
				}

			}
		};

		this.registerReceiver(bReceiver, filter);

	}

	private void sendMessage(int messageUp) {
		// TODO Auto-generated method stub
		if (connect != null) {
			if (connect.isConnected()) {
				mHandler.obtainMessage(messageUp).sendToTarget();
			}
		}
	}

	public void on(View view) {
		if (!bluetooth.isEnabled()) {
			Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(turnOn, REQUEST_ENABLE_BT); // returned and
																// done when
																// activity
																// exits

		} else {
			Toast.makeText(getApplicationContext(), "Already On",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void list(View view) {
		pairedDevices = bluetooth.getBondedDevices();
		// ArrayList list = new ArrayList();
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// ListView
				ALPairedDevice.add(device.getName());
			}
		}
	}

	public void off(View view) {
		bluetooth.disable();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// searches for bluetoothdevices
	public void find(View view) {
		if (connect != null && connect.isConnected()) {
			Toast.makeText(getApplicationContext(), "Already Connected",
					Toast.LENGTH_SHORT).show();
		} else if (bluetooth.isDiscovering()) {
			// the button is pressed when it discovers, so cancel the discovery
			bluetooth.cancelDiscovery();
			text.setText("Status: Canceled search");
		} else {
			BTArrayAdapter.clear();
			bluetooth.startDiscovery();
			registerReceiver(bReceiver, new IntentFilter(
					BluetoothDevice.ACTION_FOUND));
			registerReceiver(bReceiver, new IntentFilter(
					BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
			registerReceiver(bReceiver, new IntentFilter(
					BluetoothAdapter.ACTION_DISCOVERY_STARTED));
		}
	}

	@Override
	protected void onDestroy() {

		// toast.cancel();
		super.onDestroy();
		unregisterReceiver(bReceiver);
	}

	private void pairBTdevice(Intent data, boolean secure) {

		String MAC_address = data.getExtras().getString(DEVICE_MAC_ADDRESS);
		// get bluetoothDevice
		BluetoothDevice connectDevice = bluetooth.getRemoteDevice(MAC_address);

		// BTChat.connect(connectDevice, secure);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(), "Bluetooth is not enabled",
					Toast.LENGTH_SHORT).show();
			finish();
		}

	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			bluetooth.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)
			// manageConnectedSocket(mmSocket);
			mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();

		}

		public boolean isConnected() {
			return mmSocket.isConnected();
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					// overwrite previous bytes
					buffer = new byte[1024];
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI activity
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();

				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

}
