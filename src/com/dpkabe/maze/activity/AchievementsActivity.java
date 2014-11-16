package com.dpkabe.maze.activity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.maze.R;

public class AchievementsActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.achievements);
		TextView tv = (TextView) findViewById(R.id.tv);
		String scores = "";
		scores+= readFromFile("practice_scores.txt");		
		tv.setText(scores);
	}

	void setPracticeScores(int n) throws FileNotFoundException {
		String ps = readFromFile("practice_scores.txt");
		List<String> pslist = Arrays.asList(ps.split(","));
		int[] temp = new int[4];
		for (int i = 0; i < 3; ++i) {
			temp[i] = Integer.parseInt(pslist.get(0));
		}
		temp[3] = n;
		Arrays.sort(temp);
		String new_pscore = "";
		for (int i = 0; i < 3; ++i) {
			new_pscore += Integer.toString(temp[i]) + ",";
		}
		writeToPracticeFile(new_pscore);
	}

	void setChallengeScores(int n, int mode) {
		String cs = readFromFile("challenge_scores.txt");
		List<String> pslist = Arrays.asList(cs.split(","));
		int[] temp = new int[4];
		for (int i = 0; i < 3; ++i) {
			temp[i] = Integer.parseInt(pslist.get(0));
		}
		if (temp[mode - 1] < n) {
			temp[mode - 1] = n;
			String new_pscore = "";
			for (int i = 0; i < 3; ++i) {
				new_pscore += Integer.toString(temp[i]) + ",";
			}
			writeToChallengeFile(new_pscore);
		}
	}

	private void writeToPracticeFile(String data) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
					openFileOutput("practice_scores.txt",
							getBaseContext().MODE_PRIVATE));
			outputStreamWriter.write(data);
			outputStreamWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeToChallengeFile(String data) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
					openFileOutput("challenge_scores.txt",
							getBaseContext().MODE_PRIVATE));
			outputStreamWriter.write(data);
			outputStreamWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String readFromFile(String file_name) {

		String ret = "";

		try {
			InputStream inputStream = openFileInput(file_name);

			if (inputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();

				while ((receiveString = bufferedReader.readLine()) != null) {
					stringBuilder.append(receiveString);
				}

				inputStream.close();
				ret = stringBuilder.toString();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}
}
