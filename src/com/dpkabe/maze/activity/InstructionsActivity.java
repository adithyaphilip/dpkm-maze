package com.dpkabe.maze.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.example.maze.R;

public class InstructionsActivity extends Activity{
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);
        TextView tv = (TextView) findViewById(R.id.instructions);
        tv.setMovementMethod(new ScrollingMovementMethod());
    } 
}
