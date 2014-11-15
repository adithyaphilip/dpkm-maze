package com.dpkabe.maze.activity;

import com.dpkabe.maze.view.PracticeMode;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class PracticeActivity extends Activity {
	PracticeMode drawView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		float width = size.x;
		float height = size.y;
		int x = 16, y = 10;
		float unit = (float) ((height * 0.8) / (y * 5));
		drawView = new PracticeMode(this, width, height, unit, x, y);
		drawView.setBackgroundColor(Color.WHITE);
		setContentView(drawView);
	}
}
