package com.dpkabe.maze.view;


public class LongestPathFinder {
	/*
	 * MainStack: A stack used to store stacks, where each stack stores a
	 * path from root to leaf, of the maze graph.
	 * 
	 * LinkStack: A stack used to store the branch-points.
	 */
	MainStack ms = new MainStack();
	Stack stk;
	LinkStack ls = new LinkStack();
	int[][] maze;
	int i = 0, j = 0;
	int dir = 0;
	int prevDir = 15;
	Stack endPoints;

	LongestPathFinder(int[][] m, int x, int y) {
		maze = new int[x][y];
		for (int i = 0; i < x; ++i) {
			for (int j = 0; j < y; ++j)
				maze[i][j] = m[i][j];
		}
		stk = new Stack();
		ms.push(stk);
		findAllPaths(0, 0);
		//optimize main-stack for data retrieval
		endPoints = ms.reduce();
	}

	public void findAllPaths(int i,int j){	
		//push (i,j) onto a path
		ms.top().push(i,j);
		// remove the direction-bit of traversing direction
		maze[i][j]&=~getOppDir(dir);
		/*
		 * if there are more than one paths from a point- 1.push the point of
		 * branching onto a link stack. 2.duplicate the path upto the branch
		 * point, traverse along a path. Push the stack onto the main stack.
		 */
		if(count_ones(maze[i][j])>1){
			ls.push(i, j,count_ones(maze[i][j])-1);
			stk = new Stack();
			stk.copy(ms.top());
			ms.push(stk);			
		}
		/*
		 * on reaching a end-point store the path at end of main-stack
		 * load the top location in the link-stack and continue traversal 	
		 */
		if(maze[i][j]==0){
			// if no links remain and no traversable direction remains return
			if(ls.top()==null)
				return;
			else{
				ms.insert(ms.pop());
				//get branch-count at a branch-cell
				int count = ls.top().getCount();
				i=ls.top().getX();
				j=ls.top().getY();
				// if only one traversable direction remaining at a branch, empty link-stack, 
				// else decrement count.
				if(count == 1) 
					ls.pop();
				else 
					ls.top().setCount(count-1);											
			}
					
		}
		// get the highest direction-value at a cell
		dir = findDir(maze[i][j]);
		// remove direction-bit of traversed direction
		maze[i][j]&= ~dir;
		findAllPaths(getAdjX(dir,i),getAdjY(dir,j));						
	}	

	/*
	 * getOppDir returns the opposite direction of traversal.
	 */
	private int getOppDir(int dir2) {
		switch (dir2) {
		case 1:
			return 2;
		case 2:
			return 1;
		case 4:
			return 8;
		case 8:
			return 4;
		}
		return 0;
	}

	public Stack getEndPoints() {
		return endPoints;
	}

	public Stack getLongestPath() {
		return ms.pop();
	}

	/*
	 * count_ones returns the number of one's in the binary number k. this count
	 * denotes the number of directions that are open at any point in the maze.
	 */
	private int count_ones(int k) {
		int i = 0;
		while (k > 0) {
			i += k & 1;
			k >>= 1;
		}
		return i;
	}
	
	//return adjacent x given direction value at a cell
	private int getAdjX(int dir, int i) {
		switch (dir) {
		case 1:
			return i;
		case 2:
			return i;
		case 4:
			return ++i;
		case 8:
			return --i;
		default:
			return 0;
		}
	}

	// return adjacent y given direction value at a cell
	private int getAdjY(int dir, int j) {
		switch (dir) {
		case 1:
			return --j;
		case 2:
			return ++j;
		case 4:
			return j;
		case 8:
			return j;
		default:
			return 0;
		}
	}

	
	//given a value at in a cell, return the highest-order direction 
	private int findDir(int n) {
		int i;
		for (int j = 3; j >= 0; --j) {
			i = n & (int) Math.pow(2, j);
			if (i != 0)
				return (int) Math.pow(2, j);
		}
		return 0;
	}
}


//MainStack is a stack of stacks.

class MainStack {
	private StackNode head;
	private StackNode tail;

	MainStack() {
		head = null;
		tail = null;
	}
	
	//store all end-points other that longest-path into a stack
	//remove all paths except the longest-path form main-stack
	public Stack reduce() {
		Stack endPoints = new Stack();
		while (head.getNext() != null) {
			if (head.getData().getSize() > head.getNext().getData().getSize()) {
				endPoints.insert(head.getNext().getData().top().getX(), head
						.getNext().getData().top().getY());
				head.setNext(head.getNext().getNext());
			} else {
				endPoints.insert(head.getData().top().getX(), head.getData()
						.top().getY());
				head = head.getNext();
			}
		}
		return endPoints;
	}

	public Stack top() {
		return head.getData();
	}

	public boolean isEmpty() {
		return head == null;
	}

	public void push(Stack k) {
		if (head == null) {
			head = new StackNode(k, null);
			tail = head;
		} else
			head = new StackNode(k, head);
	}

	public Stack pop() {
		Stack res = head.getData();
		head = head.getNext();
		return res;
	}
	//insert a node at end
	public void insert(Stack i) {
		if (head == null) {
			head = new StackNode(i, null);
			tail = head;
		} else {
			tail.setNext(new StackNode(i, null));
			tail = tail.getNext();
		}
	}
}


//Node of MainStack 
class StackNode {
	private StackNode next;
	private Stack data;

	StackNode(Stack s, StackNode n) {
		data = s;
		next = n;
	}

	public StackNode getNext() {
		return next;
	}

	public void setNext(StackNode n) {
		next = n;
	}

	public Stack getData() {
		return data;
	}
}

//Stack of return values
class LinkStack {
	private LinkNode head;
	private int size;

	public LinkStack() {
		head = null;
		size = 0;
	}

	public LinkNode top() {
		return head;
	}

	public boolean isEmpty() {
		return head == null;
	}

	public int getSize() {
		return size;
	}

	public void push(int i, int j, int k) {
		head = new LinkNode(i, j, k, head);
		size++;
	}

	public void pop() {
		size--;
		head = head.getNext();
	}
}

//Node of LinkStack, contains coordinates, and branch-count
class LinkNode {
	private LinkNode next;
	private int x, y, z;

	public LinkNode(int i, int j, int k, LinkNode n) {
		x = i;
		y = j;
		z = k;
		next = n;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getCount() {
		return z;
	}

	public void setCount(int k) {
		z = k;
	}

	public void setData(int i, int j, int k) {
		x = i;
		y = j;
		z = k;
	}

	public void setNext(LinkNode n) {
		next = n;
	}

	public LinkNode getNext() {
		return next;
	}
}

//Stack storing the path coordinates
class Stack {
	private Node head;
	private Node tail;
	private int size;

	public Stack() {
		head = null;
		tail = null;
		size = 0;
	}

	public Node top() {
		return head;
	}

	public boolean isEmpty() {
		return head == null;
	}

	public int getSize() {
		return size;
	}

	public void push(int i, int j) {
		if (head == null)
			head = new Node(i, j, tail);
		else
			head = new Node(i, j, head);
		size++;
	}

	public int[] pop() {
		size--;
		int[] res = { head.getX(), head.getY() };
		head = head.getNext();
		return res;
	}

	public int topX() {
		return head.getX();
	}

	public int topY() {
		return head.getY();
	}

	public void insert(int i, int j) {
		if (size == 0) {
			head = new Node(i, j, null);
			tail = head;
		} else {
			tail.setNext(new Node(i, j, null));
			tail = tail.getNext();
		}
		size++;
	}

	public void copy(Stack s1) {
		Node p = s1.head;
		while (p != null) {
			this.insert(p.getX(), p.getY());
			p = p.getNext();
		}
	}

	public void removeLastNode() {
		Node p = this.head;
		if (p.getNext() == null)
			head = null;
		else {
			while (p.getNext().getNext() != null)
				p = p.getNext();
			p.setNext(null);
		}
	}
}

//Node of Stack
class Node {
	private Node next;
	private int x, y;

	public Node(int i, int j, Node n) {
		x = i;
		y = j;
		next = n;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setData(int i, int j) {
		x = i;
		y = j;
	}

	public void setNext(Node n) {
		next = n;
	}

	public Node getNext() {
		return next;
	}

	public void removeCurrentNode() {
		this.x = this.next.x;
		this.y = this.next.y;
		this.next = this.next.next;
	}
}