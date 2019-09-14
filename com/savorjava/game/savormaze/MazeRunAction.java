/*
 * Created on 2005-1-20
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.savorjava.game.savormaze;

import java.util.Random;
import java.util.Stack;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

import com.savorjava.game.Action;
import com.savorjava.game.CoreCanvas;
import com.savorjava.game.Utility;

/**
 * @author Colin
 *
 * The third phase: this action include all the game content, generating maze,
 * controlling the robot, responsing the gamer and level checking.
 */
public class MazeRunAction extends Action {
	
	public final byte INITIAL = 0;
	public final byte RUN = 1;
	public final byte PAUSE = 2;
	public final byte STOP = 3;
	public final byte PASS = 4;
	
	public final byte EAST = 0x01;
	public final byte SOUTH = 0x02;
	public final byte WEST = 0x04;
	public final byte NORTH = 0x08;
	
	private byte status = INITIAL;
	private short grid = 11;
	private short maxGrid = 11;
	private byte gridSize = 4;
	// seed of random number.
	private Random random = new Random(System.currentTimeMillis());
	
	// this flag decide if the robot is running.
	private boolean runingFlag = false;
	
	// this flag decide if the robot should run in this game.
	private boolean robotRun = false;
	private short level = 1;
	
	private Command nextCommand = new Command("Next", Command.SCREEN, 1);
	private Command exitCommand = new Command("Exit", Command.EXIT, 1);
	
	/**
	 * in java, a integer have 32 bits.
	 * 4 bits: robot solutions, 
	 * 2 bits: keep, 1 bit: robot visited, 1 bit: visited, 
	 * 4 bits: solutions, 
	 * 4 bits: borders, 
	 * 4 bits: walls 
	 */
	private int[][] rooms = new int[0][0];
	
	/**
	 * current location of room which gamer step to.
	 */
	private short[] hacker = {0, 0};
	
	/**
	 * robot footprint: x, y, go to direction, come from direction.
	 */
	private short[] robot = {0, 0, EAST, WEST};
	private Stack robotMemory = new Stack();
	
	/**
	 * record the rooms which be drawn after gamer click direction button.
	 */
	private Stack roomsNeedDraw = new Stack();
	
	public MazeRunAction(CoreCanvas cc)
	{
		super(cc);
	}
	
	/**
	 * initial all rooms in the maze according to size of maze,  
	 * walls and borders will be up which present by 1.
	 */
	private void initialRooms()
	{
		int xSize = canvas.getWidth();
		int ySize = canvas.getHeight();
		short minSize = (short)(Utility.min(xSize, ySize));
		maxGrid = (short)((minSize - 1) / gridSize);
		rooms = new int[grid][grid];
		// initial all the rooms: no visited and all the wall up.
		for(int i = 0; i < grid; i++)
			for(int j = 0; j < grid; j++)
			{
				rooms[i][j] = 0x000F;
			}
		// add border for the rooms which on the four sides.
		for(int i = 0; i < grid; i++)
		{
			rooms[0][i] |= 0x0040;
			rooms[grid - 1][i] |= 0x0010;
			rooms[i][0] |= 0x0080;
			rooms[i][grid - 1] |= 0x0020;
		}
		hacker[0] = 0;
		hacker[1] = 0;
		roomsNeedDraw.removeAllElements();
		robot[0] = 0;
		robot[1] = 0;
		robot[2] = EAST;
		robot[3] = WEST;
		robotMemory.removeAllElements();

		System.gc();
	}
	
	/**
	 * Game's status processor, creating random maze when status is INITIAL;
	 * drawing room which need be drawn after gamer click direction button as status
	 * is RUN; cleaning the maze and go to INITIAL when the status is PASS; if gamer
	 * arrived destination, PASS status will be set.
	 */
	public void perform(Graphics g)
	{
		if(status == INITIAL)
		{
			Stack stack = new Stack();
			int totalRooms = grid * grid;
			int visitedRooms = 1;
			// room location: 1:x 2:y
			int[] roomL = new int[2];
			roomL[0] = random.nextInt() % grid;
			roomL[1] = random.nextInt() % grid;
			if(roomL[0] < 0)
			{
				roomL[0] = - roomL[0];
			}
			if(roomL[1] < 0)
			{
				roomL[1] = - roomL[1];
			}
			// set room to be visited by gamer.
			rooms[roomL[0]][roomL[1]] |= 0x1000;

			while(visitedRooms < totalRooms)
			{
				int[] neighbor = getRandomNeighbor(rooms, roomL[0], roomL[1]);
				if(neighbor == null)
				{
					roomL = (int[])stack.pop();
				}else
				{
					stack.push(neighbor);
					rooms[neighbor[0]][neighbor[1]] |= 0x1000;
					visitedRooms ++;
					roomL = neighbor;
				}
			}
			g.setColor(0xFFFFFF);
			g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());			
			drawMaze(g);
			status = RUN;
		}else if(status == RUN)
		{
			drawRoomsNeedDraw(g);
		}else if(status == PASS)
		{
			grid += 2;
			if(grid > maxGrid)
			{
				if(level == 0)
				{
					grid = 5;
				}else if(level == 1)
				{
					grid = 11;
				}else if(level == 2)
				{
					grid = 25;
				}
			}
			initialRooms();
			runingFlag = false;
			status = INITIAL;
		}

		System.gc();
		if(hacker[0] == grid - 1 && hacker[1] == grid - 1)
		{
			status = PASS;
		}
	}
	
	/**
	 * just drawing the room which need be drawn after gamer click the buttons.
	 * @param g
	 */
	private void drawRoomsNeedDraw(Graphics g)
	{
		int increaseX = (canvas.getWidth() - grid * gridSize) / 2;
		int increaseY = (canvas.getHeight() - grid * gridSize) / 2;
		while(!roomsNeedDraw.empty())
		{
			int[] location = (int[])roomsNeedDraw.pop();
			int dx = increaseX + location[0] * gridSize;
			int dy = increaseY + location[1] * gridSize;
			// repaint the room with withe exception four corner point.
			g.setColor(0xFFFFFF);
			g.fillRect(dx + 1, dy, gridSize - 1, gridSize);
			g.fillRect(dx, dy + 1, gridSize, gridSize - 1);
			drawRoom(g, rooms[location[0]][location[1]], dx, dy);
		}
		g.setColor(0x00FF00);
		g.fillRect((grid - 1) * gridSize + 1 + increaseX, 
				(grid - 1) * gridSize + 1 + increaseY, 
				gridSize - 1, 
				gridSize - 1);
		g.setColor(0x6666FF);
		g.fillRect(robot[0] * gridSize + 1 + increaseX, 
				robot[1] * gridSize + 1 + increaseY, 
				gridSize - 1, 
				gridSize - 1);
		g.setColor(0xFF0000);
		g.fillRect(hacker[0] * gridSize + 1 + increaseX, 
				hacker[1] * gridSize + 1 + increaseY, 
				gridSize - 1, 
				gridSize - 1);
	}
	
	/**
	 * drawing whole initial random maze, including every room.
	 * @param g
	 */
	private void drawMaze(Graphics g)
	{
		// draw rooms.
		int increaseX = (canvas.getWidth() - grid * gridSize) / 2;
		int increaseY = (canvas.getHeight() - grid * gridSize) / 2;
		for(int i = 0; i < grid; i++)
		{
			int dx = increaseX + i * gridSize;
			for(int j = 0; j < grid; j++)
			{
				int dy = increaseY + j * gridSize;
				drawRoom(g, rooms[i][j], dx, dy);
			}
		}
		g.setColor(0x00FF00);
		g.fillRect((grid - 1) * gridSize + 1 + increaseX, 
				(grid - 1) * gridSize + 1 + increaseY, 
				gridSize - 1, 
				gridSize - 1);
		g.setColor(0xFF0000);
		g.fillRect(hacker[0] * gridSize + 1 + increaseX, 
				hacker[1] * gridSize + 1 + increaseY, 
				gridSize - 1, 
				gridSize - 1);
	}
	
	/**
	 * just drawing a room, this method be invoked by other methods.
	 * @param g
	 * @param room
	 * @param dx
	 * @param dy
	 */
	private void drawRoom(Graphics g, int room, int dx, int dy)
	{
		// draw walls of rooms.
		g.setColor(0x000000);
		if((room & EAST) != 0)
		{
			g.drawLine(dx + gridSize, dy, dx + gridSize, dy + gridSize);
		}
		if((room & SOUTH) != 0)
		{
			g.drawLine(dx, dy + gridSize, dx + gridSize, dy + gridSize);
		}
		if((room & WEST) != 0)
		{
			g.drawLine(dx, dy, dx, dy + gridSize);
		}
		if((room & NORTH) != 0)
		{
			g.drawLine(dx, dy, dx + gridSize, dy);
		}
		// draw robot solution rooms.
		g.setColor(0x6666FF);
		if((room & 0x10000) != 0)
		{
			g.drawLine(dx + gridSize / 2, dy + gridSize / 2, dx + gridSize, dy + gridSize / 2);
		}
		if((room & 0x20000) != 0)
		{
			g.drawLine(dx + gridSize / 2, dy + gridSize / 2, dx + gridSize / 2, dy + gridSize);
		}
		if((room & 0x40000) != 0)
		{
			g.drawLine(dx + gridSize / 2, dy + gridSize / 2, dx, dy + gridSize / 2);
		}
		if((room & 0x80000) != 0)
		{
			g.drawLine(dx + gridSize / 2, dy + gridSize / 2, dx + gridSize / 2, dy);
		}
		// draw gamer solution rooms.
		g.setColor(0xFF0000);
		if((room & 0x0100) != 0)
		{
			g.drawLine(dx + gridSize / 2, dy + gridSize / 2, dx + gridSize, dy + gridSize / 2);
		}
		if((room & 0x0200) != 0)
		{
			g.drawLine(dx + gridSize / 2, dy + gridSize / 2, dx + gridSize / 2, dy + gridSize);
		}
		if((room & 0x0400) != 0)
		{
			g.drawLine(dx + gridSize / 2, dy + gridSize / 2, dx, dy + gridSize / 2);
		}
		if((room & 0x0800) != 0)
		{
			g.drawLine(dx + gridSize / 2, dy + gridSize / 2, dx + gridSize / 2, dy);
		}

	}
	
	/**
	 * getting a random neighbor room according to indicated room.
	 * @param rooms
	 * @param x
	 * @param y
	 * @return
	 */
	private int[] getRandomNeighbor(int[][] rooms, int x, int y)
	{
		int direction = random.nextInt() % 4;
		if(direction < 0)
		{
			direction = -direction;
		}
		int[] neighbor = null;
		for(int i = 0; i < 4; i++)
		{
			switch(direction)
			{
				case 0: // EAST
				{
					if((x + 1 < grid) && 
							(rooms[x + 1][y] & 0x1000) == 0 && 
							(rooms[x][y] & 0x0010) == 0)
					{
						neighbor = new int[2];
						neighbor[0] = x + 1;
						neighbor[1] = y;
						rooms[x][y] &= ~EAST;
						rooms[x + 1][y] &= ~WEST;
					}
					break;
				}
				case 1: // SOUTH
				{
					if((y + 1 < grid) && 
							(rooms[x][y + 1] & 0x1000) == 0 && 
							(rooms[x][y] & 0x0020) == 0)
					{
						neighbor = new int[2];
						neighbor[0] = x;
						neighbor[1] = y + 1;
						rooms[x][y] &= ~SOUTH;
						rooms[x][y + 1] &= ~NORTH;
					}
					break;
				}
				case 2: // WEST
				{
					if((x - 1 >= 0) && 
							(rooms[x - 1][y] & 0x1000) == 0 && 
							(rooms[x][y] & 0x0040) == 0)
					{
						neighbor = new int[2];
						neighbor[0] = x - 1;
						neighbor[1] = y;
						rooms[x][y] &= ~WEST;
						rooms[x - 1][y] &= ~EAST;
					}
					break;
				}
				case 3: // NORTH
				{
					if((y - 1 >= 0) && 
							(rooms[x][y - 1] & 0x1000) == 0 && 
							(rooms[x][y] & 0x0080) == 0)
					{
						neighbor = new int[2];
						neighbor[0] = x;
						neighbor[1] = y - 1;
						rooms[x][y] &= ~NORTH;
						rooms[x][y - 1] &= ~SOUTH;
					}
					break;
				}
			}
			if(neighbor != null)
			{
				break;
			}
			if(++ direction >= 4)
			{
				direction = 0;
			}
		}
		return neighbor;
	}
	
	public void run()
	{
		robotRun = ((Boolean)canvas.getAttribute(GameRunner.ROBOTRUN)).booleanValue();
		level = ((Short)canvas.getAttribute(GameRunner.LEVEL)).shortValue();
		if(level == 0)
		{
			gridSize = 6;
			grid = 5;
			maxGrid = 5;
		}else if(level == 1)
		{
			gridSize = 4;
			grid = 11;
			maxGrid = 11;
		}else if(level == 2)
		{
			gridSize = 2;
			grid = 25;
			maxGrid = 25;
		}
		initialRooms();
		
		canvas.addCommand(nextCommand);
		canvas.addCommand(exitCommand);
		canvas.setCommandListener(this);
		
		while(acting)
		{
			perform(canvas.getBufferedGraphics());
			if(runingFlag && robotRun)
			{
				robotRun();
			}
			canvas.repaint();
			try{
				Thread.sleep(canvas.getSleepTime() / 3);
			}catch(InterruptedException ie)
			{
				// do nothing!
			}
		}
		acting = true;
		status = INITIAL;
		runingFlag = false;
	}
	
	private void robotRun()
	{
		// robot stop running while arrived terminal point.
		if(robot[0] == grid - 1 && robot[1] == grid - 1)
		{
			runingFlag = false;
			robotMemory.removeAllElements();
			System.gc();
			return;
		}
		// go to easy neighbor if possible.
		if((rooms[robot[0]][robot[1]] & EAST) == 0 &&
				robot[2] == EAST &&
				robot[3] != EAST)
		{
			// change room information.
			rooms[robot[0]][robot[1]] |= 0x10000;
			rooms[robot[0] + 1][robot[1]] |= 0x40000;
			// record path which robot walk through.
			robotMemory.push(new short[]{robot[0], robot[1], robot[2], robot[3]});
			// record room which need be re-drawn
			roomsNeedDraw.push(new int[]{robot[0], robot[1]});
			robot[0] ++;
			robot[2] = EAST;
			robot[3] = WEST;
		}else if((rooms[robot[0]][robot[1]] & SOUTH) == 0 &&
				robot[2] == SOUTH &&
				robot[3] != SOUTH)
		{
			rooms[robot[0]][robot[1]] |= 0x20000;
			rooms[robot[0]][robot[1] + 1] |= 0x80000;
			robotMemory.push(new short[]{robot[0], robot[1], robot[2], robot[3]});
			roomsNeedDraw.push(new int[]{robot[0], robot[1]});
			robot[1] ++;
			robot[2] = EAST;
			robot[3] = NORTH;
		}else if((rooms[robot[0]][robot[1]] & WEST) == 0 &&
				robot[2] == WEST &&
				robot[3] != WEST)
		{
			rooms[robot[0]][robot[1]] |= 0x40000;
			rooms[robot[0] - 1][robot[1]] |= 0x10000;
			robotMemory.push(new short[]{robot[0], robot[1], robot[2], robot[3]});
			roomsNeedDraw.push(new int[]{robot[0], robot[1]});
			robot[0] --;
			robot[2] = EAST;
			robot[3] = EAST;
		}else if((rooms[robot[0]][robot[1]] & NORTH) == 0 &&
				robot[2] == NORTH &&
				robot[3] != NORTH)
		{
			rooms[robot[0]][robot[1]] |= 0x80000;
			rooms[robot[0]][robot[1] - 1] |= 0x20000;
			robotMemory.push(new short[]{robot[0], robot[1], robot[2], robot[3]});
			roomsNeedDraw.push(new int[]{robot[0], robot[1]});
			robot[1] --;
			robot[2] = EAST;
			robot[3] = SOUTH;
		}else
		{
			// go to next direction if current direction is available;
			// go back to last room if no direction is available in this room.
			robot[2] <<= 1;
			while(robot[2] > NORTH)
			{
				// clean current room's solution by robot.
				rooms[robot[0]][robot[1]] &= ~0xF0000;
				roomsNeedDraw.push(new int[]{robot[0], robot[1]});
				// get last room location of robot and clean the solution with current room.
				robot = (short[])robotMemory.pop();
				if((robot[2] & EAST) == EAST)
				{
					rooms[robot[0]][robot[1]] &= ~0x10000;
				}else if((robot[2] & SOUTH) == SOUTH)
				{
					rooms[robot[0]][robot[1]] &= ~0x20000;
				}else if((robot[2] & WEST) == WEST)
				{
					rooms[robot[0]][robot[1]] &= ~0x40000;
				}else if((robot[2] & NORTH) == NORTH)
				{
					rooms[robot[0]][robot[1]] &= ~0x80000;
				}
				robot[2] <<= 1;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.savorjava.game.Action#interrupt()
	 */
	public void interrupt(int key) {

		switch(status)
		{
			case RUN:
			{
				runingFlag = true;
				int action2key = canvas.getGameAction(key);
				if(action2key == Canvas.UP || key == Canvas.KEY_NUM2)
				{
					if((rooms[hacker[0]][hacker[1]] & NORTH) == 0)
					{
						roomsNeedDraw.push(new int[]{hacker[0], hacker[1]});
						if((rooms[hacker[0]][hacker[1]] & 0x0800) == 0)
						{
							rooms[hacker[0]][hacker[1]] |= 0x0800;
							rooms[hacker[0]][hacker[1] - 1] |= 0x0200;
						}else
						{
							rooms[hacker[0]][hacker[1]] &= ~0x0800;
							rooms[hacker[0]][hacker[1] - 1] &= ~0x0200;
						}
						hacker[1] --; 
					}
				}else if(action2key == Canvas.DOWN || key == Canvas.KEY_NUM8)
				{
					if((rooms[hacker[0]][hacker[1]] & SOUTH) == 0)
					{
						roomsNeedDraw.push(new int[]{hacker[0], hacker[1]});
						if((rooms[hacker[0]][hacker[1]] & 0x0200) == 0)
						{
							rooms[hacker[0]][hacker[1]] |= 0x0200;
							rooms[hacker[0]][hacker[1] + 1] |= 0x0800;
						}else
						{
							rooms[hacker[0]][hacker[1]] &= ~0x0200;
							rooms[hacker[0]][hacker[1] + 1] &= ~0x0800;
						}
						hacker[1] ++; 
					}
				}else if(action2key == Canvas.LEFT || key == Canvas.KEY_NUM4)
				{
					if((rooms[hacker[0]][hacker[1]] & WEST) == 0)
					{
						roomsNeedDraw.push(new int[]{hacker[0], hacker[1]});
						if((rooms[hacker[0]][hacker[1]] & 0x0400) == 0)
						{
							rooms[hacker[0]][hacker[1]] |= 0x0400;
							rooms[hacker[0] - 1][hacker[1]] |= 0x0100;
						}else
						{
							rooms[hacker[0]][hacker[1]] &= ~0x0400;
							rooms[hacker[0] - 1][hacker[1]] &= ~0x0100;
						}
						hacker[0] --; 
					}
				}else if(action2key == Canvas.RIGHT || key == Canvas.KEY_NUM6)
				{
					if((rooms[hacker[0]][hacker[1]] & EAST) == 0)
					{
						roomsNeedDraw.push(new int[]{hacker[0], hacker[1]});
						if((rooms[hacker[0]][hacker[1]] & 0x0100) == 0)
						{
							rooms[hacker[0]][hacker[1]] |= 0x0100;
							rooms[hacker[0] + 1][hacker[1]] |= 0x0400;
						}else
						{
							rooms[hacker[0]][hacker[1]] &= ~0x0100;
							rooms[hacker[0] + 1][hacker[1]] &= ~0x0400;
						}
						hacker[0] ++; 
					}
				}else if(action2key == Canvas.FIRE || key == Canvas.KEY_NUM5)
				{
					status = PASS;
				}
				break;
			}
			case INITIAL:
			{
				break;
			}
			case STOP:
			{
				break;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.Displayable#keyPressed(int)
	 */
	protected void keyPressed(int arg0) {
		interrupt(arg0);
	}
	
	
	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command arg0, Displayable arg1) {
		if(arg0.getLabel().equals("Next"))
		{
			interrupt(canvas.getKeyCode(Canvas.FIRE));
		}else if(arg0.getLabel().equals("Exit"))
		{
			// clean the commands in Canvas.
			canvas.removeCommand(nextCommand);
			canvas.removeCommand(exitCommand);
			canvas.setCommandListener(null);
			// re-use this action.
			canvas.regRunAction(this);
			// notice allocation canvas: return to initial action.
			canvas.setGameStatus(CoreCanvas.GS_INITIAL);
		}
	}
}
