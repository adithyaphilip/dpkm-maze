package com.dpkabe.maze.activity;

import com.dpkabe.maze.view.DrawView;
import com.example.maze.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
public class MainActivity extends Activity {

	DrawView drawView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
     
    public void onPlayClick(View v){
    	Intent i = new Intent(this, MazeActivity2.class);
    	startActivity(i);
    }
    
    public void onExitClick(View v){
    	super.onBackPressed();
    }
    public void on2PlayerClick(View v){
    	Intent i = new Intent(this, ConnectActivity.class);
    	startActivity(i);
    }
      @Override
      public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
      }
} 
