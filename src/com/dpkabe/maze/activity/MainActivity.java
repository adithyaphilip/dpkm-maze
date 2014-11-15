package com.dpkabe.maze.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dpkabe.maze.view.DrawView;
import com.example.maze.R;
public class MainActivity extends Activity {

	DrawView drawView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
     
    //xml onClick functions
    
    public void onChallengeClick(View v){
    	Intent i = new Intent(this, ChallengeActivity.class);
    	startActivity(i);
    }
    
    public void onPracticeClick(View v){
    	Intent i = new Intent(this, PracticeActivity.class);
    	startActivity(i);
    }
    public void on2PlayerClick(View v){
    	Intent i = new Intent(this, ConnectActivity.class);
    	startActivity(i);
    }
    public void onAchievementsClick(View v){
    	//when we get achievements up
    }
    public void onExitClick(View v){
    	super.onBackPressed();
    }
} 
