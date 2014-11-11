package com.dpkabe.maze.bluetooth;
/**
 * Used for encoding and decoding data structures while passing between devices
 * Centralised here for:
 * a) Code correction: In the case the format is changed, it should be reflected in both encoding and decoding functions.
 * 	Hence makes sense to have them both in the same place
 * b) Encoding and decoding is independent of the Activity in which it takes place. Hence in the interest of abstraction,
 * 	modularity, re-usability and associated high level language features, it is deemed best to place them in a separate class
 * @author USER
 *
 */
public class BluetoothEncoderDecoder {
	/**
	 * Expected format: String consists of tokens delimited by ":"
	 * 1st token is rows
	 * 2nd token is columns
	 * remaining tokens are the int matrix represented in row-after-row format
	 * @param message The string to decode into a maze
	 * @return the decoded maze
	 */
	public static int[][] decodeMaze(String message){
		String parts[] = message.split(":");
		int rows = Integer.parseInt(parts[0]);
		int cols = Integer.parseInt(parts[1]);

		int start = 2;//index to start from in parts array while decoding maze in loop
		
		int maze[][] = new int[rows][cols];
		for(int i=0;i<rows;i++){
			for(int j=0;j<cols;j++){
				maze[i][j] = Integer.parseInt(parts[start+i*cols+j]);
			}
		}
		return maze;
	}
	/**
	 * Expected format: String consists of tokens delimited by ":"
	 * 1st token is rows
	 * 2nd token is columns
	 * remaining tokens are the int matrix represented in row-after-row format
	 * @param maze the maze to encode as a String
	 * @return the encoded maze
	 */
	public static String encodeMaze(int[][] maze){
		int rows = maze.length;
		int cols = maze[0].length;
		
		StringBuilder message = new StringBuilder(rows*cols*2+3);
		message.append(rows+":"+cols);//colon after cols is added by first iteration of maze encoding loop
		
		for(int i =0;i<rows;i++){
			for(int j=0;j<cols;j++){
				message.append(":"+maze[i][j]);
			}
		}
		return message.toString();
	}
}
