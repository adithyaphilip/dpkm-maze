package com.dpkabe.maze.mazeutils;

public class MazeConstants {
	public static final int MAZE_ROWS = 16;
	public static final int MAZE_COLS = 10;
	// 3:4 ratio maintained above
	public static final int QUIT_MAZE = 0;
	public static final int EVENT_CRASH = 1;
	public static final int EVENT_WIN = 2;
	public final static int EVENT_LOSS = 3;
	public final static int EVENT_POSITION_UPDATE = 4;


	public static class PositionUpdates {
		public final static String KEY_X_FRACTION = "xfraction";
		public final static String KEY_Y_FRACTION = "yfraction";
	}
}
