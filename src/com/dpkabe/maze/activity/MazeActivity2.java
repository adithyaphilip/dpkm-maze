package com.dpkabe.maze.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.dpkabe.maze.bluetooth.BluetoothChatService;
import com.dpkabe.maze.mazeutils.MazeConstants;
import com.dpkabe.maze.mazeutils.MazeGenerator;
import com.dpkabe.maze.view.DrawView2;
import com.example.maze.R;
public class MazeActivity2 extends Activity {
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
	
	DrawView2 mDrawView;
	private OnClickListener endActivityOnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v){
			MazeActivity2.this.finish();
		}
	};
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			mDrawView.setOnClickListener(endActivityOnClickListener);
			switch(msg.what){
			case MazeConstants.EVENT_CRASH:
				onCrash();
				break;
			case MazeConstants.EVENT_LOSS:
				onLoss();
				break;
			case MazeConstants.EVENT_WIN:
				onWin();
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        MazeGenerator mg = new MazeGenerator(MazeConstants.MAZE_ROWS,MazeConstants.MAZE_COLS);
        int maze[][] = mg.getMaze();
        mDrawView = new DrawView2(this,width,height,maze,mHandler);
        mDrawView.setBackgroundColor(Color.WHITE);
        setContentView(mDrawView);
    }
  	public void onCrash(){
  		
  	}
  	public void onLoss(){
  	}
  	public void onWin(){
  		//TODO
  	}
} 
