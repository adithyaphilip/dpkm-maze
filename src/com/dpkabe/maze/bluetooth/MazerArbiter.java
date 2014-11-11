package com.dpkabe.maze.bluetooth;
/**
 * Defines a uniform, symmetric system (you may interchange maze1 and maze 2 and you will still get the same
 * maze as a result) for determining which maze out of two submitted mazes is to be used.
 * Necessary to allow a distributed arbitration system for selecting a maze when two devices interact over bluetooth
 * @author USER
 *
 */
public class MazerArbiter {
	public static int[][] getMaze(int[][] maze1, int[][] maze2){
		for(int i =0;i<maze1.length;i++){
			for(int j=0;j<maze1[0].length;j++){
				if(maze1[i][j]>maze2[i][j]){
					return maze1;
				}
				else if(maze2[i][j]>maze1[i][j]){
					return maze2;
				}
			}
		}
		return maze1;//both mazes are equal so you may return either one
	}
}
