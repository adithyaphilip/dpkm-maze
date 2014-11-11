package com.dpkabe.maze.bluetooth;

/**
 * Serves as an intermediary class for transferring data between activities which use Bluetooth
 * Existence of static objects once application has been minimized cannot be guaranteed as they may have
 * been garbage collected while the activity was minimized. The activity has techniques to recreate itself via the
 * onCreate and onResume, which are automatically called when the Activity is created, but not these static classes
 * @author USER
 * 
 */
public class BluetoothMediator {
	public static BluetoothChatService mChatService;
}
