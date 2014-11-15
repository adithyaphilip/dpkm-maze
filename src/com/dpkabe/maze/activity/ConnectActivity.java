package com.dpkabe.maze.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dpkabe.maze.bluetooth.BluetoothChatService;
import com.dpkabe.maze.bluetooth.BluetoothEncoderDecoder;
import com.dpkabe.maze.bluetooth.BluetoothMediator;
import com.dpkabe.maze.bluetooth.MazerArbiter;
import com.dpkabe.maze.mazeutils.MazeConstants;
import com.dpkabe.maze.mazeutils.MazeGenerator;
import com.example.maze.R;

public class ConnectActivity extends Activity {
	
	// Debugging
		private static final String TAG = "BluetoothChat";
		private static final boolean D = true;

		// Message types sent from the BluetoothChatService Handler
		public static final int MESSAGE_STATE_CHANGE = 1;
		public static final int MESSAGE_READ = 2;
		public static final int MESSAGE_WRITE = 3;
		public static final int MESSAGE_DEVICE_NAME = 4;
		public static final int MESSAGE_TOAST = 5;

		// Key names received from the BluetoothChatService Handler
			public static final String DEVICE_NAME = "device_name";
		public static final String TOAST = "toast";

		// Intent request codes
		private static final int REQUEST_CONNECT_DEVICE = 2;
		private static final int REQUEST_ENABLE_BT = 3;

		// Name of the connected device
		private String mConnectedDeviceName = null;
		// Local Bluetooth adapter
		private BluetoothAdapter mBluetoothAdapter = null;
		// Member object for the chat services
		private BluetoothChatService mChatService = null;
		
		int[][] mOwnMaze;
		int[][] mOppMaze;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
	}
	public void generateOwnMaze(){
		MazeGenerator mg = new MazeGenerator(MazeConstants.MAZE_ROWS,MazeConstants.MAZE_COLS);
		mOwnMaze = mg.getMaze();		
	}
	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		stopService(new Intent(this, BluetoothChatService.class));
		startService(new Intent(this, BluetoothChatService.class));
		generateOwnMaze();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			mChatService.setHandler(mHandler);//to ensure handler changes in other activities hasn't affected this
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}
	private void setupChat() {
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);
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
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}
	private void sendOwnMaze(){
		sendMessage(BluetoothEncoderDecoder.encodeMaze(mOwnMaze));
	}
	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		
			if (message.length() > 0) {
				// Get the message bytes and tell the BluetoothChatService to write
				mChatService.write(message.getBytes());
			}
	}
	private void startGame(int[][] maze1, int[][] maze2){
		Intent i = new Intent(this, FlexibleMazeActivity.class);
		
		int[][] chosenMaze = MazerArbiter.getMaze(maze1, maze2);
		i.putExtra(FlexibleMazeActivity.KEY_MAZE, BluetoothEncoderDecoder.encodeMaze(chosenMaze));
		
		BluetoothMediator.mChatService=mChatService;
		
		startActivity(i);
	}
	/**
	 * Expected format - delimiter used is ':', first token represents
	 * @param message contains encoded maze as described in BluetoothEncoderDecoder
	 * @return
	 */
	public int[][] getOpponentMaze(String message){
		return BluetoothEncoderDecoder.decodeMaze(message);
	}
	// The Handler that gets information back from the BluetoothChatService
		private final Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MESSAGE_STATE_CHANGE:
					if (D)
						Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
					switch (msg.arg1) {
					case BluetoothChatService.STATE_CONNECTED:
						Toast.makeText(ConnectActivity.this,getString(R.string.title_connected_to,
								mConnectedDeviceName),Toast.LENGTH_SHORT).show();
						sendOwnMaze();
						break;
					case BluetoothChatService.STATE_CONNECTING:
						Toast.makeText(ConnectActivity.this,getString(R.string.title_connecting,
								mConnectedDeviceName),Toast.LENGTH_SHORT).show();
						break;
					case BluetoothChatService.STATE_LISTEN:
					case BluetoothChatService.STATE_NONE:
						Toast.makeText(ConnectActivity.this,getString(R.string.title_not_connected,
								mConnectedDeviceName),Toast.LENGTH_SHORT).show();
						break;
					}
					break;
				case MESSAGE_READ:
					byte[] readBuf = (byte[]) msg.obj;
					// construct a string from the valid bytes in the buffer
					String readMessage = new String(readBuf, 0, msg.arg1);
					Log.d("receivedString",readMessage);
					mOppMaze = getOpponentMaze(readMessage);
					startGame(mOwnMaze, mOppMaze);
					Toast.makeText(ConnectActivity.this, readMessage, Toast.LENGTH_SHORT).show();
					break;
				case MESSAGE_DEVICE_NAME:
					// save the connected device's name
					mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
					Toast.makeText(getApplicationContext(),
							"Connected to " + mConnectedDeviceName,
							Toast.LENGTH_SHORT).show();
					break;
				case MESSAGE_TOAST:
					Log.e("MESSAGE_TOAST","ConnectActivity Handler");
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
			case REQUEST_CONNECT_DEVICE:
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK) {
					connectDevice(data);
				}
				break;
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					setupChat();
				} else {
					// User did not enable Bluetooth or an error occurred
					Log.d(TAG, "BT not enabled");
					Toast.makeText(this, R.string.bt_not_enabled_leaving,
							Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}
		private void connectDevice(Intent data) {
			// Get the device MAC address
			String address = data.getExtras().getString(
					DeviceListActivity.EXTRA_DEVICE_ADDRESS);
			// Get the BluetoothDevice object
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			// Attempt to connect to the device
			mChatService.connect(device);
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
			case R.id.connect_scan:
				// Launch the DeviceListActivity to see devices and do scan
				serverIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				return true;
			case R.id.discoverable:
				// Ensure this device is discoverable by others
				ensureDiscoverable();
				return true;
			}
			return false;
		}
	/**
	 * xml click function to search for devices
	 */
	public void onSearchClick(View v){
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}
}
