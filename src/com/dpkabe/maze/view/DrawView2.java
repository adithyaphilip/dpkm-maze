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
	/* below constants are used to set draw values
	 * assigning them constant names helps in:
	 * a) Understanding what each change to draw means without having to check the switch case in onDraw
	 * b) Ensuring right int value is set for draw when corresponding event occurs
	 * c) When a new draw int value is added, it is easy to make sure we are not using an existing value
	 * d) Easy refactoring, if for some reason values of draw states need to be changed
	 * e) Much more readable code
	 **/
	
	public final static int STATE_PLAY = 1;
	public final static int STATE_CRASH = 2;
	public final static int STATE_WIN = 3;
	public final static int STATE_LOSS = 4;
	
	private Canvas mCanvas;
	
	private SparseArray<PointF> mActivePointers;
	Paint paint = new Paint();
	int W = getWidth(), H = getHeight();//wrong because a View does not know it's height and width until it is drawn, and drawing occurs after object is created
	float ballX, ballY;
	int x, y;//x is columns (x-axis), y is rows (y-axis)
	int draw = STATE_PLAY;
	int mazeX, mazeY, mazeXf, mazeYf;
	int path;
	int dirX, dirY;
	int delay = 0;
	MazeGenerator mg;
	int[][] mMazeInt;
	LongestPathFinder lpf;
	Stack retPath, keys;
	int destX, destY, destfX, destfY;
	float iniX, iniY;
	int rX, rY, retDestX, retDestY;

	int key_count = 0;
	int key_score = 0;
	
	Handler mHandler;

	/*
	 * public DrawView(Context context, int width, int height) { super(context);
	 * Log.d("w&h",Integer.toString(width)+" "+Integer.toString(height));
	 * mActivePointers = new SparseArray<PointF>(); W = width; H = height; path
	 * = 10; mazeX = W * 20 / 100; mazeY = W * 20 / 100; mazeXf = mazeX + path *
	 * 5 * x + path; mazeYf = mazeY + path * 5 * y + path; ballX = mazeX + 3 *
	 * path; ballY = mazeY + 3 * path; iniX = ballX; iniY = ballY; destfX =
	 * mazeX + 5 * path * destX + 3 * path; destfY = mazeY + 5 * (path + 2) *
	 * destY + 3 * path; }
	 */
	/**
	 * NOTE:- This class uses the maze such that it appears to be TRANSPOSED while drawing
	 * @param context
	 * @param width width of area for DrawView
	 * @param height height of area for DrawView
	 * @param maze maze to be drawn
	 * @param mHandler handler to use to inform calling activity about certain actions like winning or losing
	 */
	public DrawView2(Context context, int width, int height, int[][] maze, Handler h) {
		super(context);
		
		mHandler = h;
		
		Log.d("w&h", Integer.toString(width) + " " + Integer.toString(height));
		mActivePointers = new SparseArray<PointF>();
		W = width;
		H = height;
		
		
		this.x = maze.length;
		this.y = maze[0].length;
		
		mazeX = W * 20 / 100;
		mazeY = W * 20 / 100;
		
		this.path = getPathSize(width-mazeX, height-mazeY);
		
		mazeXf = mazeX + path * 5 * x + path;
		mazeYf = mazeY + path * 5 * y + path;
		Log.d("DrawView2","mazeXf"+mazeXf+"mazeYf"+mazeYf);
		ballX = mazeX + 3 * path;
		ballY = mazeY + 3 * path;
		iniX = ballX;
		iniY = ballY;
		destfX = mazeX + 5 * path * destX + 3 * path;
		destfY = mazeY + 5 * (path + 2) * destY + 3 * path;

		mMazeInt=maze;
		
		lpf = new LongestPathFinder(mMazeInt, mMazeInt.length, mMazeInt[0].length);
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
	 * @param x
	 * @param y
	 */
	public void setOpponentXY(int x, int y){
		//Deepu
		//assume x and y are the co-ordinates of opponent move. This should set the co-ords such that in the next onDraw opponent will be drawn at right place
	}
	/**
	 * Used to determine required path size by taking minimum of maximum possible size of path considering width, and height
	 * @param width width LEFT FOR THE MAZE
	 * @param height height LEFT FOR THE MAZE
	 * @param mMaze maze to be drawn
	 * @return
	 */
	public int getPathSize(int width, int height){
		/*
		 * using: 
		 * mazeXf = mazeX + path * 5 * x + path; =>width = path(5x+1), path = width/5x+1
		 * mazeYf = mazeY + path * 5 * y + path;
		 */
		int cols = x;
		int rows = y;
		Log.d("DrawView2","width="+width+"height="+height+"rows"+rows+"cols"+cols);
		
		int maxPathFromWidth = width/(cols+1);
		int maxPathFromHeight = height/(rows+1);
		return Math.min(maxPathFromWidth, maxPathFromHeight)/5;
	}
	public void setDrawState(int state){
		draw = state;
		invalidate();
	}
	public void onDraw(Canvas canvas) {
		mCanvas = canvas;
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
			//postLossMessage(mHandler); no need as your loss is detected by outlying activity from opponent win only. Hence activity already knows about it
			break;
		}
	}
	private void postCrashMessage(Handler h){
		Message msg = h.obtainMessage();
		msg.what=MazeConstants.EVENT_CRASH;
		h.sendMessage(msg);
	}
	private void postWinMessage(Handler h){
		Message msg = h.obtainMessage();
		msg.what=MazeConstants.EVENT_WIN;
		h.sendMessage(msg);
	}
	private void paintKeys(Canvas canvas) {
		keys = checkKeyStatus(keys);
		Node key = keys.top();
		paint.setColor(Color.rgb(255, 208, 47));
		paint.setStrokeWidth(2f);

		while (key != null) {
			canvas.drawCircle(mazeX + 5 * path * key.getX() + 3 * path, mazeY
					+ 5 * path * key.getY() + 3 * path, 10f, paint);
			key = key.getNext();
		}

	}
	private Stack checkKeyStatus(Stack keys) {
		Node key = keys.top();
		while (key != null) {
			if (ballX < mazeX + 5 * path * key.getX() + 3 * path + 10
					&& ballX > mazeX + 5 * path * key.getX() + 3 * path - 10
					&& ballY < mazeY + 5 * path * key.getY() + 3 * path + 10
					&& ballY > mazeY + 5 * path * key.getY() + 3 * path - 10) {
				++key_score;
				if (key.getNext() == null){
					keys.removeLastNode();
				}					
				else
					key.removeCurrentNode();
			}
			key = key.getNext();
		}
		return keys;
	}
	
	private void paintPointers(Canvas canvas) {
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(2f);
		canvas.drawCircle(ballX, mazeY - 5 * path, 5f, paint);
		canvas.drawCircle(mazeX - 5 * path, ballY, 5f, paint);
	}

	private void paintCrash(Canvas canvas) {
		paint.setColor(Color.rgb(255, 145, 70));
		canvas.drawRect(0, 0, W, H, paint);
		paint.setColor(Color.rgb(255, 198, 159));
		paint.setTextSize(80);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.save();
		canvas.rotate((float) 90, W / 2, H / 4);
		canvas.drawText("Nasty bump!", W / 2, H / 4, paint);
		canvas.restore();
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
	public void paintWinner(Canvas canvas) {
		paint.setColor(Color.rgb(189, 233, 59));
		canvas.drawRect(0, 0, W, H, paint);
		paint.setColor(Color.rgb(235, 249, 193));
		paint.setTextSize(80);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.save();
		canvas.rotate((float) 90, W / 2, H / 4);
		canvas.drawText("You Won!", W / 2, H / 4, paint);
		canvas.restore();
	}

	private void paintDestination(Canvas canvas) {
		paint.setColor(Color.rgb(200, 200, 200));
		paint.setStrokeWidth(2f);
		canvas.drawCircle(mazeX + 5 * path * destX + 3 * path, mazeY + 5 * path
				* destY + 3 * path, 10f, paint);
	}

	private void paintControlLine(Canvas canvas) {
		paint.setColor(Color.rgb(153, 217, 234));
		paint.setStrokeWidth(10f);
		canvas.drawLine(mazeX + path, mazeY - 5 * path, mazeXf - path, mazeY
				- 5 * path, paint);
		canvas.drawLine(mazeX - 5 * path, mazeY + path, mazeX - 5 * path,
				mazeYf - path, paint);
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
			draw = STATE_WIN;
		}
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(2f);
		canvas.drawCircle(ballX, ballY, 10f, paint);
	}

	public void paintMaze(Canvas canvas) {
		paint.setColor(Color.rgb(0, 162, 232));
		paint.setStrokeWidth(5);
		int px = mazeX, py = mazeY;
		for (int i = 0; i < y; i++) {
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
		for (int j = 0; j < x; j++) {
			if (checkCollision(px, py, px + 5 * path, py + path))
				draw = STATE_CRASH;
			canvas.drawRect(px, py, px + 5 * path, py + path, paint);
			px += 5 * path;
		}
		canvas.drawRect(px, py, px + path, py + path, paint);
	}

	public boolean checkCollision(int px, int py, int pxf, int pyf) {
		float ballR = ballX + 2, ballL = ballX - 2, ballT = ballY - 2, ballB = ballY + 2;
		if (ballR > px && ballL < pxf && ballB > py && ballT < pyf)
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
