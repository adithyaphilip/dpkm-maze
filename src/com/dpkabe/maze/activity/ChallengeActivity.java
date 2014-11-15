package com.dpkabe.maze.activity;

import com.dpkabe.maze.view.ChallengeModeBeta;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;

public class ChallengeActivity extends Activity {
	ChallengeModeBeta drawView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		int x = 16, y = 10;
		float unit = (float) ((height * 0.8) / (y * 5));
		drawView = new ChallengeModeBeta(this, width, height, unit, x, y);
		drawView.setBackgroundColor(Color.WHITE);
		setContentView(drawView);
	}
}
