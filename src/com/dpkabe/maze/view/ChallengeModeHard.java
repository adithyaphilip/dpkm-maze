package com.dpkabe.maze.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.dpkabe.maze.mazeutils.MazeGenerator;

public class ChallengeModeHard extends View {
	public final static int STATE_PLAY = 1;
	public final static int STATE_CRASH = 2;
	public final static int STATE_WIN = 3;
	public final static int STATE_LOSS = 4;

	private SparseArray<PointF> mActivePointers;
	Paint paint = new Paint();
	float W, H;
	float ballX, ballY;
	int x, y;
	int draw = STATE_PLAY;
	float mazeX, mazeY, mazeXf, mazeYf;
	float unit;
	int dirX, dirY;
	int delay = 0;
	MazeGenerator mg;
	int[][] maze;
	LongestPathFinder lpf;
	Stack retPath, keys;
	float destX, destY, destfX, destfY;
	float iniX, iniY;
	float rX, rY, retDestX, retDestY;
	int pcSpeed = 10;

	public ChallengeModeHard(Context context) {
		super(context);
	}

	public ChallengeModeHard(Context context, float width, float height,
			float unit, int x, int y) {
		super(context);
		mActivePointers = new SparseArray<PointF>();
		this.unit = unit;
		this.x = x;
		this.y = y;
		this.W = width;
		this.H = height;
		mazeX = (width - (unit * 5 * x + unit)) / 2;
		mazeY = 2 * unit;
		mazeXf = mazeX + unit * 5 * x + unit;
		mazeYf = mazeY + unit * 5 * y + unit;
		ballX = mazeX + 3 * unit;
		ballY = mazeY + 3 * unit;
		iniX = ballX;
		iniY = ballY;
		destfX = mazeX + 5 * unit * destX + 3 * unit;
		destfY = mazeY + 5 * (unit + 2) * destY + 3 * unit;

		mg = new MazeGenerator(x, y);
		maze = mg.getMaze();
		lpf = new LongestPathFinder(maze, x, y);
		retPath = lpf.getLongestPath();
		keys = lpf.getEndPoints();
		destX = retPath.topX();
		destY = retPath.topY();
		rX = retDestX = destX;
		rY = retDestY = destY;
	}

	public void onDraw(Canvas canvas) {
		switch (draw) {
		case STATE_PLAY:
			paintMaze(canvas);
			paintBackgroundColor(canvas);

			paintControlLine(canvas);
			paintPointers(canvas);

			paintPcDestination(canvas);
			paintDestination(canvas);

			paintBall(canvas);
			paintPcBall(canvas);
			break;
		case STATE_CRASH:
			paintCrash(canvas);
			break;
		case STATE_WIN:
			paintWinner(canvas);
			break;
		case STATE_LOSS:
			paintLoss(canvas);
			break;
		}
	}

	public void paintMaze(Canvas canvas) {
		paint.setColor(Color.rgb(0, 162, 232));
		float px = mazeX, py = mazeY;
		paint.setStrokeWidth(unit);
		for (int i = 0; i < y; i++) {
			// print horizontal lines
			for (int j = 0; j < x; j++) {
				if ((maze[j][i] & 1) == 0) {
					if (checkCollision(px, py, px + 5 * unit, py + unit))
						draw = STATE_CRASH;
					canvas.drawRect(px, py, px + 5 * unit, py + unit, paint);
					px += 5 * unit;
				} else {
					if (checkCollision(px, py, px + unit, py + unit))
						draw = STATE_CRASH;
					canvas.drawRect(px, py, px + unit, py + unit, paint);
					px += 5 * unit;
				}
			}
			canvas.drawRect(px, py, px + unit, py + unit, paint);
			px = mazeX;
			// print vertical lines
			for (int j = 0; j < x; j++) {
				if ((maze[j][i] & 8) == 0) {
					if (checkCollision(px, py, px + unit, py + 5 * unit))
						draw = STATE_CRASH;
					canvas.drawRect(px, py, px + unit, py + 5 * unit, paint);
					px += 5 * unit;
				} else {
					px += 5 * unit;
				}
			}
			if (checkCollision(px, py, px + unit, py + 5 * unit))
				draw = STATE_CRASH;
			canvas.drawRect(px, py, px + unit, py + 5 * unit, paint);
			py += 5 * unit;
			px = mazeX;
		}
		// print bottom line
		if (checkCollision(px, py, px + 5 * x * unit + unit, py + unit))
			draw = STATE_CRASH;
		canvas.drawRect(px, py, px + 5 * x * unit + unit, py + unit, paint);
	}

	public boolean checkCollision(float px, float py, float pxf, float pyf) {
		if (ballX > px - unit / 2 && ballX < pxf + unit / 2
				&& ballY > py - unit / 2 && ballY < pyf + unit / 2)
			return true;
		return false;
	}

	private void paintBackgroundColor(Canvas canvas) {
		paint.setColor(Color.rgb(0, 162, 232));
		canvas.drawRect(0, 0, mazeX, H, paint);
		canvas.drawRect(0, 0, W, mazeY, paint);
		canvas.drawRect(mazeXf, mazeY, W, H, paint);
		canvas.drawRect(mazeX, mazeYf, W, H, paint);
	}

	private void paintControlLine(Canvas canvas) {
		paint.setColor(Color.rgb(153, 217, 234));
		paint.setStrokeWidth(unit);
		// left control line
		canvas.drawLine(mazeX - 5 * unit, mazeY + unit, mazeX - 5 * unit,
				mazeYf - unit, paint);
		// bottom control line
		canvas.drawLine(mazeX + unit, mazeYf + 5 * unit, mazeXf - unit, mazeYf
				+ 5 * unit, paint);
	}

	private void paintPointers(Canvas canvas) {
		paint.setColor(Color.GRAY);
		// left control-line pointer
		canvas.drawCircle(mazeX - 5 * unit, ballY, unit / 2, paint);
		// bottom control-line pointer
		canvas.drawCircle(ballX, mazeYf + 5 * unit, unit / 2, paint);
	}

	private void paintPcDestination(Canvas canvas) {
		paint.setColor(Color.rgb(255, 168, 111));
		canvas.drawCircle(iniX, iniY, unit, paint);
	}

	private void paintDestination(Canvas canvas) {
		paint.setColor(Color.rgb(200, 200, 200));
		canvas.drawCircle(mazeX + 5 * unit * destX + 3 * unit, mazeY + 5 * unit
				* destY + 3 * unit, unit, paint);
	}

	private void paintBall(Canvas canvas) {
		if (ballX < mazeX + 5 * unit * destX + 4 * unit
				&& ballX > mazeX + 5 * unit * destX + 2 * unit
				&& ballY < mazeY + 5 * unit * destY + 4 * unit
				&& ballY > mazeY + 5 * unit * destY + 2 * unit) {
			draw = STATE_WIN;
		}
		paint.setColor(Color.GRAY);
		canvas.drawCircle(ballX, ballY, unit, paint);
	}

	private void paintPcBall(Canvas canvas) {
		if (rX == retDestX && rY == retDestY) {
			retPath.pop();
			if (retPath.isEmpty())
				draw = STATE_LOSS;
			else {
				retDestX = retPath.topX();
				retDestY = retPath.topY();
			}
		}
		delay++;
		if (delay == pcSpeed) {
			rX = retDestX;
			rY = retDestY;
			delay = 0;
		}
		paint.setColor(Color.rgb(255, 127, 39));
		canvas.drawCircle(mazeX + 5 * unit * rX + 3 * unit, mazeY + 5 * unit
				* rY + 3 * unit, unit, paint);
		invalidate();
	}

	private void paintCrash(Canvas canvas) {
		paint.setColor(Color.rgb(255, 145, 70));
		canvas.drawRect(0, 0, W, H, paint);
		paint.setColor(Color.WHITE);
		paint.setTextSize(6 * unit);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.drawText("Nasty bump!", (W - 11 * 3 * unit) / 2, H / 2, paint);
	}

	private void paintWinner(Canvas canvas) {
		paint.setColor(Color.rgb(189, 233, 59));
		canvas.drawRect(0, 0, W, H, paint);
		paint.setColor(Color.WHITE);
		paint.setTextSize(6 * unit);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.drawText("You Won!", (W - 8 * 3 * unit) / 2, H / 2, paint);
	}

	private void paintLoss(Canvas canvas) {
		paint.setColor(Color.rgb(154, 137, 211));
		canvas.drawRect(0, 0, W, H, paint);
		paint.setColor(Color.WHITE);
		paint.setTextSize(6 * unit);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.drawText("You Lost!", (W - 9 * 3 * unit) / 2, H / 2, paint);
	}

	public boolean onTouchEvent(MotionEvent event) {
		int pointerIndex = event.getActionIndex();
		int pointerId = event.getPointerId(pointerIndex);
		int maskedAction = event.getActionMasked();
		switch (maskedAction) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {
			if (event.getX() > (ballX - 3 * unit)
					&& event.getX() < (ballX + 3 * unit)
					&& event.getY() > (ballY - 3 * unit)
					&& event.getY() < (ballY + 3 * unit))
				break;

			if (event.getX() > ballX - unit && event.getX() < ballX + unit
					|| event.getY() > ballY - unit
					&& event.getY() < ballY + unit) {
				if (event.getY() < mazeY
						&& (event.getX() > mazeX && event.getX() < mazeXf))
					ballX = event.getX();
				if (event.getX() < mazeY
						&& (event.getY() > mazeY && event.getY() < mazeYf))
					ballY = event.getY();
				PointF f = new PointF();
				f.x = event.getX(pointerIndex);
				f.y = event.getY(pointerIndex);
				mActivePointers.put(pointerId, f);
			}
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			for (int size = event.getPointerCount(), i = 0; i < size; i++) {
				PointF point = mActivePointers.get(event.getPointerId(i));
				if (point != null) {
					if (event.getX(i) > ballX - 2 * unit
							&& event.getX(i) < ballX + 2 * unit)
						ballX = event.getX(i);
					if (event.getY(i) > ballY - 2 * unit
							&& event.getY(i) < ballY + 2 * unit)
						ballY = event.getY(i);
				}
			}
			break;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL: {
			mActivePointers.remove(pointerId);
			break;
		}
		}
		invalidate();
		return true;
	}
}
