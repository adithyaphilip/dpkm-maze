package com.dpkabe.maze.activity;

import com.example.maze.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ChallengeActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge_launcher);
    }
	//xml onClick functions
    public void onEasyClick(View v){
    	Intent i = new Intent(this, ChallengeEasyActivity.class);
    	startActivity(i);
    }
    public void onNormalClick(View v){
    	Intent i = new Intent(this, ChallengeNormalActivity.class);
    	startActivity(i);
    }
    public void onHardClick(View v){
    	Intent i = new Intent(this, ChallengeHardActivity.class);
    	startActivity(i);
    }
}
