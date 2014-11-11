package com.dpkabe.maze.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.dpkabe.maze.mazeutils.MazeGenerator;


public class DrawView extends View {
	private SparseArray<PointF> mActivePointers;
	Paint paint = new Paint();
	int W=getWidth(), H=getHeight();
	float ballX, ballY;
	int x,y;
	int draw = 1;
	int mazeX, mazeY, mazeXf, mazeYf;
	int path;
	int dirX, dirY;
	int delay = 0;
	MazeGenerator mg;
	int[][] maze;
	LongestPathFinder lpf;
	Stack retPath;
	int destX, destY, destfX, destfY;
	float iniX, iniY;
	int rX, rY, retDestX, retDestY;
	/*int x=10,y=18;
	MazeGenerator mg = new MazeGenerator(x, y);
	int[][] maze = mg.getMaze();
	LongestPathFinder lpf = new LongestPathFinder(maze, x, y);
	Stack retPath = lpf.getLongestPath();
	int destX = retPath.topX();
	int destY = retPath.topY();
	int destfX, destfY;
	float iniX, iniY;
	int rX = destX, rY = destY;
	int retDestX = destX, retDestY = destY;*/
	
	
	public DrawView(Context context) {
		super(context);
	}

	/*
	public DrawView(Context context, int width, int height) {
		super(context);
		Log.d("w&h",Integer.toString(width)+" "+Integer.toString(height));
		mActivePointers = new SparseArray<PointF>();
		W = width;
		H = height;		
		path = 10;
		mazeX = W * 20 / 100;
		mazeY = W * 20 / 100;
		mazeXf = mazeX + path * 5 * x + path;
		mazeYf = mazeY + path * 5 * y + path;	
		ballX = mazeX + 3 * path;
		ballY = mazeY + 3 * path;
		iniX = ballX;
		iniY = ballY;
		destfX = mazeX + 5 * path * destX + 3 * path;
		destfY = mazeY + 5 * (path + 2) * destY + 3 * path;
	}
	*/
	
	public DrawView(Context context, int width, int height, int path, int x, int y) {
		super(context);
		Log.d("w&h",Integer.toString(width)+" "+Integer.toString(height));
		mActivePointers = new SparseArray<PointF>();
		W = width;
		H = height;		
		this.path = path;
		this.x=x;
		this.y=y;
		mazeX = W * 20 / 100;
		mazeY = W * 20 / 100;
		mazeXf = mazeX + path * 5 * x + path;
		mazeYf = mazeY + path * 5 * y + path;	
		ballX = mazeX + 3 * path;
		ballY = mazeY + 3 * path;
		iniX = ballX;
		iniY = ballY;
		destfX = mazeX + 5 * path * destX + 3 * path;
		destfY = mazeY + 5 * (path + 2) * destY + 3 * path;
		
		mg = new MazeGenerator(x, y);
		maze = mg.getMaze();
		lpf = new LongestPathFinder(maze, x, y);
		retPath = lpf.getLongestPath();
		destX = retPath.topX();
		destY = retPath.topY();		
		rX = retDestX = destX;
		rY = retDestY = destY;
	}

	public void onDraw(Canvas canvas) {
		switch (draw) {
		case 1:
			paintPcDestination(canvas);
			paintMaze(canvas);
			paintBall(canvas);			
			paintDestination(canvas);
			paintBackgroundColor(canvas);
			paintControlLine(canvas);
			paintPointers(canvas);
			paintPcBall(canvas);
			break;
		case 2:
			paintCrash(canvas);
			break;
		case 3:
			paintPcWinner(canvas);
			break;
		case 4:
			paintWinner(canvas);
			break;
		}
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

	private void paintPcWinner(Canvas canvas) {
		paint.setColor(Color.rgb(154, 137, 211));
		canvas.drawRect(0, 0, W, H, paint);
		paint.setColor(Color.rgb(243, 251, 221));
		paint.setTextSize(80);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.save();
		canvas.rotate((float) 90, W / 2, H / 4);
		canvas.drawText("Yo! I won!", W / 2, H / 4, paint);
		canvas.restore();
	}

	private void paintWinner(Canvas canvas) {
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

	private void paintPcDestination(Canvas canvas) {
		paint.setColor(Color.rgb(255, 168, 111));
		paint.setStrokeWidth(2f);
		canvas.drawCircle(iniX, iniY, 10f, paint);
	}

	private void paintDestination(Canvas canvas) {
		paint.setColor(Color.rgb(200, 200, 200));
		paint.setStrokeWidth(2f);
		canvas.drawCircle(mazeX + 5 * path * destX + 3 * path, mazeY + 5 * path
				* destY + 3 * path, 10f, paint);
	}

	private void paintPcBall(Canvas canvas) {
		if (rX == retDestX && rY == retDestY) {
			retPath.pop();
			if (retPath.isEmpty())
				draw = 3;
			if (retPath.top() != null) {
				retDestX = retPath.topX();
				retDestY = retPath.topY();
			}
			dirX = retDestX - rX;
			dirY = retDestY - rY;
		}
		delay++;
		if (delay == 20) {
			rX += dirX * 1;
			rY += dirY * 1;
			delay = 0;
		}
		paint.setColor(Color.rgb(255, 127, 39));
		paint.setStrokeWidth(2f);
		canvas.drawCircle(mazeX + 5 * path * rX + 3 * path, mazeY + 5 * path
				* rY + 3 * path, 10f, paint);
		invalidate();
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
				&& ballY > mazeY + 5 * path * destY + 3 * path - 10) {
			draw = 4;
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
				if ((maze[j][i] & 1) == 0) {
					if (checkCollision(px, py, px + 5 * path, py + path))
						draw = 2;
					canvas.drawRect(px, py, px + 5 * path, py + path, paint);
					px += 5 * path;
				} else {
					if (checkCollision(px, py, px + path, py + path))
						draw = 2;
					canvas.drawRect(px, py, px + path, py + path, paint);
					px += 5 * path;
				}
			}
			canvas.drawRect(px, py, px + path, py + path, paint);
			px = mazeX;
			for (int j = 0; j < x; j++) {
				if ((maze[j][i] & 8) == 0) {
					if (checkCollision(px, py, px + path, py + 5 * path))
						draw = 2;
					canvas.drawRect(px, py, px + path, py + 5 * path, paint);
					px += 5 * path;
				} else {
					px += 5 * path;
				}
			}
			if (checkCollision(px, py, px + path, py + 5 * path))
				draw = 2;
			canvas.drawRect(px, py, px + path, py + 5 * path, paint);
			py += 5 * path;
			px = mazeX;
		}
		for (int j = 0; j < x; j++) {
			if (checkCollision(px, py, px + 5 * path, py + path))
				draw = 2;
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
			if(event.getX()>(ballX-30) && event.getX()<(ballX+30)
					&& event.getY()>(ballY-30) && event.getY()<(ballY+30))
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
