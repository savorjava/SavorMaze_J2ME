/*
 * Created on 2005-1-28
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.savorjava.game.savormaze;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import com.savorjava.game.Action;
import com.savorjava.game.CoreCanvas;

/**
 * @author Colin
 *
 * The second phase: support a menu to gamer, gamer could choose single player or vs
 * with robot, they could config a game level for different difficulty, help information
 * and exit item are ready too.
 */
public class MazeStartAction extends Action {

	/**
	 * there are two menus in this game.
	 */
	private String[][] menu = null;
	private String helpInfo = null;
	/**
	 * record which menu is showing at the time.
	 */
	private short currentMenu = 0;
	/**
	 * record what's the choices in two menus from gamer.
	 */
	private short currentChoice[] = null;
	private int hitColor = 0x0000FF;
	private int unHitColor = 0x9999FF;
	private int xSize = 0;
	private int ySize = 0;
	private int y1 = 0;
	private int y2 = 0;
	private int y = 0;
	private short tieHeight = 60;
	private boolean drawTie = true;
	private Font font = null;
	private Command selectCommand = new Command("Select", Command.SCREEN, 1);
	
	public MazeStartAction(CoreCanvas cc)
	{
		super(cc);
		menu = new String[][]{{"Human", "Human vs Robot", "Config", "Help", "Exit"},
								{"Easy", "Normal", "Impossible"}};
		helpInfo = "Win: Move to green point asap.\nDirection: 2, 8, 4, 6.\nNext Level: 5\nDesigner: Shajin(2005.2.22)";
		currentChoice = new short[2];
		font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
		tieHeight = (short)((font.getHeight() + 2) * menu[0].length);
		cc.setAttribute(GameRunner.ROBOTRUN, new Boolean(false));
		cc.setAttribute(GameRunner.LEVEL, new Short((short)0));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		canvas.addCommand(selectCommand);
		canvas.setCommandListener(this);
		
		Graphics g = canvas.getBufferedGraphics();
		xSize = g.getClipWidth();
		ySize = g.getClipHeight();
		y1 = (ySize - tieHeight) / 2;
		y2 = y1 + tieHeight;
		y = 0;
		while(acting)
		{
			perform(g);
			canvas.repaint();
			try{
			  Thread.sleep(canvas.getSleepTime() / 2);
			}catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}
		// set these flag if you want to reuse this action in thread.
		acting = true;
		drawTie = true;

	}

	/**
	 * draw menu: menu items show in a horizontal tie.
	 */
	public void perform(Graphics g) {
		
		if(drawTie)
		{
			g.setColor(0xFFFFFF);
			g.fillRect(0, y1, xSize, tieHeight);
		    g.setColor(0xCCCCFF);
		    for(int i = y1; i <= y2; i += 2)
		    {
		    	g.drawLine(0, i, xSize, i);
		    }
		    for(int i = 0; i <= xSize; i += 2)
		    {
		    	g.drawLine(i, y1, i, y2);
		    }
			drawTie = false;
		}else
		{
			int choice = 0;
			int color = unHitColor;
			choice = currentChoice[currentMenu];
			for(int i = 0; i < menu[currentMenu].length; i++)
			{
				if(choice == i)
				{
					color = hitColor;
				}else
				{
					color = unHitColor;
				}
				g.setColor(color);
				g.setFont(font);
				g.drawString(menu[currentMenu][i], font.getHeight(), y1 + (font.getHeight() + 1) * i  + 2, 0);
			}
		}
		// this is a change to release memory, gamer response slowly the CPU.
		System.gc();
	}
		
	/**
	 * handle key pressed event: 
	 * 2 and UP -> UP
	 * 8 and DOWN -> DOWN
	 * 5 and FIRE -> Hit the menu item.
	 */
	public void interrupt(int key) {
		int action2key = canvas.getGameAction(key);
		if(action2key == Canvas.UP || key == Canvas.KEY_NUM2)
		{
			if(currentChoice[currentMenu] == 0)
			{
				currentChoice[currentMenu] = (short)(menu[currentMenu].length - 1);
			}else
			{
				currentChoice[currentMenu] --;
			}
		}else if(action2key == Canvas.DOWN || key == Canvas.KEY_NUM8)
		{
			if(currentChoice[currentMenu] == menu[currentMenu].length - 1)
			{
				currentChoice[currentMenu] = 0;
			}else
			{
				currentChoice[currentMenu] ++;
			}
		}else if(action2key == Canvas.FIRE || key == Canvas.KEY_NUM5)
		{
			if(currentMenu == 0)
			{
				switch(currentChoice[currentMenu])
				{
					case 0:
					{
						canvas.removeCommand(selectCommand);
						canvas.setCommandListener(null);
						// no robot in this game.
						canvas.setAttribute(GameRunner.ROBOTRUN, new Boolean(false));
						// re-use this action for next time.
						canvas.regStartAction(this);
						// go forward running game.
						canvas.setGameStatus(CoreCanvas.GS_RUN);
						break;
					}
					case 1:
					{
						canvas.removeCommand(selectCommand);
						canvas.setCommandListener(null);
						// robot in this game.
						canvas.setAttribute(GameRunner.ROBOTRUN, new Boolean(true));
						canvas.regStartAction(this);
						canvas.setGameStatus(CoreCanvas.GS_RUN);
						break;
					}
					case 2:
					{
						currentMenu = 1;
						drawTie = true;
						break;
					}
					case 3:
					{
						// show help information in a alert dialog.
						Alert help = new Alert("Help", helpInfo, null, AlertType.INFO);
						Display.getDisplay(GameRunner.midlet).setCurrent(help);
						break;
					}
					case 4:
					{
						GameRunner.midlet.notifyDestroyed();
					}
				}
			}else
			{
				// record the game level which gamer choosed.
				canvas.setAttribute(GameRunner.LEVEL, new Short(currentChoice[currentMenu]));
				currentMenu = 0;
				drawTie = true;
			}
		}
	}
	
	/**
	 * handle command "Select" as same as pressing 5 or FIRE.
	 */
	public void commandAction(Command arg0, Displayable arg1) {

		if(arg0.getLabel().equals("Select"))
		{
			interrupt(canvas.getKeyCode(Canvas.FIRE));
		}

	}

}
