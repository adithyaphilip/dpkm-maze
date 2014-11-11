package com.dpkabe.maze.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.dpkabe.maze.mazeutils.MazeConstants;
import com.dpkabe.maze.mazeutils.MazeGenerator;

public class PracticeModeView extends View {
	public final static int STATE_PLAY = 1;
	public final static int STATE_CRASH = 2;
	public final static int STATE_WIN = 3;
	public final static int STATE_LOSS = 4;

	private SparseArray<PointF> mActivePointers;
	Paint paint = new Paint();
	float W, H;
	float ballX, ballY;
	int mCols, mRows;
	int draw = 1;
	float mazeX, mazeY, mazeXf, mazeYf;
	float unit;
	int dirX, dirY;
	int delay = 0;
	MazeGenerator mg;
	int[][] mMaze;
	LongestPathFinder lpf;
	Stack retPath, keys;
	float destX, destY, destfX, destfY;
	float iniX, iniY;
	float rX, rY, retDestX, retDestY;
	int key_count = 0;
	int key_score = 0;

	Handler mHandler;

	public PracticeModeView(Context context, float width, float height, int maze[][], Handler h) {
		super(context);
		mHandler = h;

		this.mCols = maze.length;
		this.mRows = maze[0].length;
		
		this.unit = getUnitSize(height,mRows);
		
		mActivePointers = new SparseArray<PointF>();
		this.W = width;
		this.H = height;
		mazeX = (width - (unit * 5 * mCols + unit)) / 2;
		mazeY = 2 * unit;
		mazeXf = mazeX + unit * 5 * mCols + unit;
		mazeYf = mazeY + unit * 5 * mRows + unit;
		ballX = mazeX + 3 * unit;
		ballY = mazeY + 3 * unit;
		iniX = ballX;
		iniY = ballY;
		destfX = mazeX + 5 * unit * destX + 3 * unit;
		destfY = mazeY + 5 * (unit + 2) * destY + 3 * unit;

		this.mMaze = maze;

		lpf = new LongestPathFinder(maze, mCols, mRows);
		retPath = lpf.getLongestPath();
		keys = lpf.getEndPoints();
		key_count = keys.getSize();
		destX = retPath.topX();
		destY = retPath.topY();
		rX = retDestX = destX;
		rY = retDestY = destY;
	}
	private float getUnitSize(float height, int rows){
		float unit = (float) ((height * 0.8) /(rows * 6));
		return unit;
	}
	// super class method called when invalidate(), it renders the graphics
	public void onDraw(Canvas canvas) {
		switch (draw) {
		case STATE_PLAY:
			paintMaze(canvas);
			paintDestination(canvas);
			paintBall(canvas);
			paintKeys(canvas);
			paintBackgroundColor(canvas);
			paintControlLine(canvas);
			paintPointers(canvas);
			break;
		case STATE_CRASH:
			paintCrash(canvas);
			postCrashMessage(mHandler);
			break;
		case STATE_WIN:
			paintWinner(canvas);
			postWinMessage(mHandler);
			break;
		case STATE_LOSS:
			paintLoss(canvas);
			// postLossMessage(mHandler); no need as your loss is detected by
			// outlying activity from opponent win only. Hence activity already
			// knows about it
			break;
		}
	}

	public void setDrawState(int state) {
		draw = state;
		invalidate();
	}

	private void paintLoss(Canvas canvas) {
		paint.setColor(Color.rgb(255, 145, 70));
		canvas.drawRect(0, 0, W, H, paint);
		paint.setColor(Color.rgb(255, 198, 159));
		paint.setTextSize(80);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.save();
		canvas.rotate((float) 90, W / 2, H / 4);
		canvas.drawText("You lost!", W / 2, H / 4, paint);
		canvas.restore();
	}

	private void postCrashMessage(Handler h) {
		Message msg = h.obtainMessage();
		msg.what = MazeConstants.EVENT_CRASH;
		h.sendMessage(msg);
	}

	private void postWinMessage(Handler h) {
		Message msg = h.obtainMessage();
		msg.what = MazeConstants.EVENT_WIN;
		h.sendMessage(msg);
	}

	// method to paint the remaining keys at end-points
	private void paintKeys(Canvas canvas) {
		keys = checkKeyStatus(keys);
		Node key = keys.top();
		paint.setColor(Color.rgb(255, 208, 47));
		while (key != null) {
			canvas.drawCircle(mazeX + 5 * unit * key.getX() + 3 * unit, mazeY
					+ 5 * unit * key.getY() + 3 * unit, unit, paint);
			key = key.getNext();
		}
	}

	// checks if the ball collides with any of the remaining-keys
	private Stack checkKeyStatus(Stack keys) {
		Node key = keys.top();
		while (key != null) {
			if (ballX < mazeX + 5 * unit * key.getX() + 4 * unit
					&& ballX > mazeX + 5 * unit * key.getX() + 2 * unit
					&& ballY < mazeY + 5 * unit * key.getY() + 4 * unit
					&& ballY > mazeY + 5 * unit * key.getY() + 2 * unit) {
				++key_score;
				if (key.getNext() == null) {
					keys.removeLastNode();
				} else
					key.removeCurrentNode();
			}
			key = key.getNext();
		}
		return keys;
	}

	// paints the non-maze part of screen
	private void paintBackgroundColor(Canvas canvas) {
		paint.setColor(Color.rgb(0, 162, 232));
		canvas.drawRect(0, 0, mazeX, H, paint);
		canvas.drawRect(0, 0, W, mazeY, paint);
		canvas.drawRect(mazeXf, mazeY, W, H, paint);
		canvas.drawRect(mazeX, mazeYf, W, H, paint);
	}

	// paints the pointers which show position of player
	private void paintPointers(Canvas canvas) {
		paint.setColor(Color.GRAY);
		// left control-line pointer
		canvas.drawCircle(mazeX - 3 * unit, ballY, unit / 2, paint);
		// bottom control-line pointer
		canvas.drawCircle(ballX, mazeYf + 3 * unit, unit / 2, paint);
	}

	// paints line on which pointer is placed
	private void paintControlLine(Canvas canvas) {
		paint.setColor(Color.rgb(153, 217, 234));
		paint.setStrokeWidth(10f);

		// left control line
		canvas.drawLine(mazeX - 3 * unit, mazeY + unit, mazeX - 3 * unit,
				mazeYf - unit, paint);
		// bottom control line
		canvas.drawLine(mazeX + unit, mazeYf + 3 * unit, mazeXf - unit, mazeYf
				+ 3 * unit, paint);
	}

	private void paintCrash(Canvas canvas) {
		paint.setColor(Color.rgb(255, 145, 70));
		canvas.drawRect(0, 0, W, H, paint);
		paint.setColor(Color.rgb(255, 198, 159));
		paint.setTextSize(80);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.drawText("Nasty bump!", W / 2, H / 4, paint);
	}

	private void paintWinner(Canvas canvas) {
		paint.setColor(Color.rgb(189, 233, 59));
		canvas.drawRect(0, 0, W, H, paint);
		paint.setColor(Color.rgb(235, 249, 193));
		paint.setTextSize(80);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.drawText("You Won!", W / 2, H / 4, paint);
	}

	private void paintDestination(Canvas canvas) {
		paint.setColor(Color.rgb(200, 200, 200));
		paint.setStrokeWidth(2f);
		canvas.drawCircle(mazeX + 5 * unit * destX + 3 * unit, mazeY + 5 * unit
				* destY + 3 * unit, unit, paint);
	}

	private void paintBall(Canvas canvas) {
		if (ballX < mazeX + 5 * unit * destX + 3 * unit + 10
				&& ballX > mazeX + 5 * unit * destX + 3 * unit - 10
				&& ballY < mazeY + 5 * unit * destY + 3 * unit + 10
				&& ballY > mazeY + 5 * unit * destY + 3 * unit - 10
				&& key_score == key_count) {
			draw = 3;
		}
		paint.setColor(Color.GRAY);
		canvas.drawCircle(ballX, ballY, unit, paint);
	}

	public void paintMaze(Canvas canvas) {
		paint.setColor(Color.rgb(0, 162, 232));
		paint.setStrokeWidth(5);
		float px = mazeX, py = mazeY;
		for (int i = 0; i < mRows; i++) {
			// print horizontal lines
			for (int j = 0; j < mCols; j++) {
				if ((mMaze[j][i] & 1) == 0) {
					if (checkCollision(px, py, px + 5 * unit, py + unit))
						draw = 2;
					canvas.drawRect(px, py, px + 5 * unit, py + unit, paint);
					px += 5 * unit;
				} else {
					if (checkCollision(px, py, px + unit, py + unit))
						draw = 2;
					canvas.drawRect(px, py, px + unit, py + unit, paint);
					px += 5 * unit;
				}
			}
			canvas.drawRect(px, py, px + unit, py + unit, paint);
			px = mazeX;
			// print vertical lines
			for (int j = 0; j < mCols; j++) {
				if ((mMaze[j][i] & 8) == 0) {
					if (checkCollision(px, py, px + unit, py + 5 * unit))
						draw = 2;
					canvas.drawRect(px, py, px + unit, py + 5 * unit, paint);
					px += 5 * unit;
				} else {
					px += 5 * unit;
				}
			}
			if (checkCollision(px, py, px + unit, py + 5 * unit))
				draw = 2;
			canvas.drawRect(px, py, px + unit, py + 5 * unit, paint);
			py += 5 * unit;
			px = mazeX;
		}
		// print bottom line
		if (checkCollision(px, py, px + 5 * mCols * unit + unit, py + unit))
			draw = 2;
		canvas.drawRect(px, py, px + 5 * mCols * unit + unit, py + unit, paint);
	}

	public boolean checkCollision(float px, float py, float f, float g) {
		float ballR = ballX + 2, ballL = ballX - 2, ballT = ballY - 2, ballB = ballY + 2;
		if (ballR > px && ballL < f && ballB > py && ballT < g)
			return true;
		return false;
	}

	public boolean onTouchEvent(MotionEvent event) {
		int pointerIndex = event.getActionIndex();
		int pointerId = event.getPointerId(pointerIndex);
		int maskedAction = event.getActionMasked();
		switch (maskedAction) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {
			if (event.getX() > (ballX - 30) && event.getX() < (ballX + 30)
					&& event.getY() > (ballY - 30)
					&& event.getY() < (ballY + 30))
				break;
			if (event.getX() > ballX - 20 && event.getX() < ballX + 20
					|| event.getY() > ballY - 20 && event.getY() < ballY + 20) {
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
					if (event.getX(i) > ballX - 20
							&& event.getX(i) < ballX + 20)
						ballX = event.getX(i);
					if (event.getY(i) > ballY - 20
							&& event.getY(i) < ballY + 20)
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