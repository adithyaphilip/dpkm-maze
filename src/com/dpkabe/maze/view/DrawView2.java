package com.dpkabe.maze.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.dpkabe.maze.mazeutils.MazeConstants;
import com.dpkabe.maze.mazeutils.MazeGenerator;

public class DrawView2 extends View {
	/*
	 * below constants are used to set draw values assigning them constant names
	 * helps in: a) Understanding what each change to draw means without having
	 * to check the switch case in onDraw b) Ensuring right int value is set for
	 * draw when corresponding event occurs c) When a new draw int value is
	 * added, it is easy to make sure we are not using an existing value d) Easy
	 * refactoring, if for some reason values of draw states need to be changed
	 * e) Much more readable code
	 */

	public final static int STATE_PLAY = 1;
	public final static int STATE_CRASH = 2;
	public final static int STATE_WIN = 3;
	public final static int STATE_LOSS = 4;

	private Canvas mCanvas;

	private SparseArray<PointF> mActivePointers;
	Paint paint = new Paint();
	int W,H;
	float ballX, ballY;
	float oppX, oppY;
	int x, y;// x is columns (x-axis), y is rows (y-axis)
	int draw = STATE_PLAY;
	float mazeX, mazeY, mazeXf, mazeYf;
	float path;
	int delay = 0;
	MazeGenerator mg;
	int[][] mMazeInt;
	LongestPathFinder lpf;
	Stack retPath, keys;
	float destX, destY, destfX, destfY;
	float iniX, iniY;
	float rX, rY, retDestX, retDestY;

	int key_count = 0;
	int key_score = 0;

	Handler mHandler;

	/**
	 * NOTE:- This class uses the maze such that it appears to be TRANSPOSED
	 * while drawing
	 * 
	 * @param context
	 * @param width
	 *            width of area for DrawView
	 * @param height
	 *            height of area for DrawView
	 * @param maze
	 *            maze to be drawn
	 * @param mHandler
	 *            handler to use to inform calling activity about certain
	 *            actions like winning or losing
	 */
	public DrawView2(Context context, int width, int height, int[][] maze,
			Handler h) {
		super(context);

		mHandler = h;

		mActivePointers = new SparseArray<PointF>();
		W = width;
		H = height;

		this.x = maze.length;
		this.y = maze[0].length;
		this.path = getPathSize(width, height);
		
		mazeX = (width - (path * 5 * x + path)) / 2;
		mazeY = 2 * path;		

		mazeXf = mazeX + path * 5 * x + path;
		mazeYf = mazeY + path * 5 * y + path;
		Log.d("DrawView2", "mazeXf" + mazeXf + "mazeYf" + mazeYf);
		ballX = mazeX + 3 * path;
		ballY = mazeY + 3 * path;
		iniX = ballX;
		iniY = ballY;
		destfX = mazeX + 5 * path * destX + 3 * path;
		destfY = mazeY + 5 * (path + 2) * destY + 3 * path;

		mMazeInt = maze;

		lpf = new LongestPathFinder(mMazeInt, mMazeInt.length,
				mMazeInt[0].length);
		retPath = lpf.getLongestPath();
		keys = lpf.getEndPoints();
		key_count = keys.getSize();
		destX = retPath.topX();
		destY = retPath.topY();
		rX = retDestX = destX;
		rY = retDestY = destY;
	}

	/**
	 * sets opponent x and y co-ordinates from outside
	 * 
	 * @param x
	 * @param y
	 */
	public void setOpponentXY(int x, int y) {
		oppX = x;
		oppY = y;
	}

	/**
	 * Used to determine required path size by taking minimum of maximum
	 * possible size of path considering width, and height
	 * 
	 * @param width
	 *            width LEFT FOR THE MAZE
	 * @param height
	 *            height LEFT FOR THE MAZE
	 * @param mMaze
	 *            maze to be drawn
	 * @return
	 */
	public float getPathSize(int width, int height) {
		return (float) ((height * 0.8) / (y * 5));
	}

	public void setDrawState(int state) {
		draw = state;
		invalidate();
	}

	// super class method called when invalidate(), it renders the graphics
	public void onDraw(Canvas canvas) {
		mCanvas = canvas;
		switch (draw) {
		case STATE_PLAY:
			paintMaze(canvas);
			paintDestination(canvas);
			paintKeys(canvas);
			paintOpponent(canvas);
			paintBall(canvas);
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

	private void paintOpponent(Canvas canvas) {
		paint.setColor(Color.rgb(255, 127, 39));

		paint.setStrokeWidth(2f);
		canvas.drawCircle(ballX, ballY, path, paint);
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
			canvas.drawCircle(mazeX + 5 * path * key.getX() + 3 * path, mazeY
					+ 5 * path * key.getY() + 3 * path, path, paint);
			key = key.getNext();
		}
	}

	// checks if the ball collides with any of the remaining-keys
	private Stack checkKeyStatus(Stack keys) {
		Node key = keys.top();
		while (key != null) {
			if (ballX < mazeX + 5 * path * key.getX() + 3 * path + 10
					&& ballX > mazeX + 5 * path * key.getX() + 3 * path - 10
					&& ballY < mazeY + 5 * path * key.getY() + 3 * path + 10
					&& ballY > mazeY + 5 * path * key.getY() + 3 * path - 10) {
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

	// paints the pointers which show position of player
	private void paintPointers(Canvas canvas) {
		paint.setColor(Color.GRAY);
		// left control-line pointer
		canvas.drawCircle(mazeX - 3 * path, ballY, path / 2, paint);
		// bottom control-line pointer
		canvas.drawCircle(ballX, mazeYf + 3 * path, path / 2, paint);
	}

	private void paintCrash(Canvas canvas) {
		paint.setColor(Color.rgb(255, 145, 70));
		canvas.drawRect(0, 0, W, H, paint);
		paint.setColor(Color.rgb(255, 198, 159));
		paint.setTextSize(80);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.drawText("Nasty bump!", W / 2, H / 4, paint);		
	}

	private void paintLoss(Canvas canvas) {
		paint.setColor(Color.rgb(255, 145, 70));
		canvas.drawRect(0, 0, W, H, paint);
		paint.setColor(Color.rgb(255, 198, 159));
		paint.setTextSize(80);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.drawText("You lost!", W / 2, H / 4, paint);
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
		canvas.drawCircle(mazeX + 5 * path * destX + 3 * path, mazeY + 5 * path
				* destY + 3 * path, path, paint);
	}

	// paints line on which pointer is placed
	private void paintControlLine(Canvas canvas) {
		paint.setColor(Color.rgb(153, 217, 234));
		paint.setStrokeWidth(10f);

		// left control line
		canvas.drawLine(mazeX - 3 * path, mazeY + path, mazeX - 3 * path,
				mazeYf - path, paint);
		// bottom control line
		canvas.drawLine(mazeX + path, mazeYf + 3 * path, mazeXf - path, mazeYf
				+ 3 * path, paint);
	}

	private void paintBackgroundColor(Canvas canvas) {
		paint.setColor(Color.rgb(0, 162, 232));
		canvas.drawRect(0, 0, W, mazeY, paint);
		canvas.drawRect(0, 0, mazeX, H, paint);
		canvas.drawRect(mazeXf, 0, W, H, paint);
		canvas.drawRect(0, mazeYf, W, H, paint);
	}

	private void paintBall(Canvas canvas) {
		if (ballX < mazeX + 5 * path * destX + 3 * path + 10
				&& ballX > mazeX + 5 * path * destX + 3 * path - 10
				&& ballY < mazeY + 5 * path * destY + 3 * path + 10
				&& ballY > mazeY + 5 * path * destY + 3 * path - 10
				&& key_score == key_count) {
			draw = 3;
		}
		paint.setColor(Color.GRAY);
		canvas.drawCircle(ballX, ballY, path, paint);
	}

	public void paintMaze(Canvas canvas) {
		paint.setColor(Color.rgb(0, 162, 232));
		paint.setStrokeWidth(5);
		float px = mazeX, py = mazeY;
		for (int i = 0; i < y; i++) {
			// print horizontal lines
			for (int j = 0; j < x; j++) {
				if ((mMazeInt[j][i] & 1) == 0) {
					if (checkCollision(px, py, px + 5 * path, py + path))
						draw = STATE_CRASH;
					canvas.drawRect(px, py, px + 5 * path, py + path, paint);
					px += 5 * path;
				} else {
					if (checkCollision(px, py, px + path, py + path))
						draw = STATE_CRASH;
					canvas.drawRect(px, py, px + path, py + path, paint);
					px += 5 * path;
				}
			}
			canvas.drawRect(px, py, px + path, py + path, paint);
			px = mazeX;
			// print vertical lines
			for (int j = 0; j < x; j++) {
				if ((mMazeInt[j][i] & 8) == 0) {
					if (checkCollision(px, py, px + path, py + 5 * path))
						draw = STATE_CRASH;
					canvas.drawRect(px, py, px + path, py + 5 * path, paint);
					px += 5 * path;
				} else {
					px += 5 * path;
				}
			}
			if (checkCollision(px, py, px + path, py + 5 * path))
				draw = STATE_CRASH;
			canvas.drawRect(px, py, px + path, py + 5 * path, paint);
			py += 5 * path;
			px = mazeX;
		}
		// print bottom line
		if (checkCollision(px, py, px + 5 * x * path + path, py + path))
			draw = STATE_CRASH;
		canvas.drawRect(px, py, px + 5 * x * path + path, py + path, paint);
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
		case MotionEvent.ACTION_MOVE: { // a pointer was moved
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
