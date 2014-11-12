package com.dpkabe.maze.activity;

import com.dpkabe.maze.mazeutils.MazeConstants;
import com.dpkabe.maze.mazeutils.MazeGenerator;
import com.dpkabe.maze.view.DrawView;
import com.example.maze.R;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
public class MazeActivity extends Activity {

	DrawView drawView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        
        MazeGenerator mg = new MazeGenerator(MazeConstants.MAZE_ROWS,MazeConstants.MAZE_COLS);
        int[][] maze = mg.getMaze();
        drawView = new DrawView(this,width,height,maze);
        drawView.setBackgroundColor(Color.WHITE);
        setContentView(drawView);
    }
     

      @Override
      public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
      }
} 
