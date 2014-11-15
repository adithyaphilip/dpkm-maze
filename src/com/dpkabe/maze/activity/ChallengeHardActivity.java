package com.dpkabe.maze.activity;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

import com.dpkabe.maze.view.ChallengeModeHard;

public class ChallengeHardActivity extends Activity{
	ChallengeModeHard drawView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		int x = 16, y = 10;
		float unit = (float) ((height * 0.8) / (y * 5));
		drawView = new ChallengeModeHard
				(this, width, height, unit, x, y);
		drawView.setBackgroundColor(Color.WHITE);
		setContentView(drawView);
	}
}
