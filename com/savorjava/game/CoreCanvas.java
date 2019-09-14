/*
 * Created on 2005-1-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.savorjava.game;



import java.util.Hashtable;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;


import com.savorjava.game.savormaze.GameRunner;


/**
 * @author Colin
 *
 * Core allocation: all the drawing actions is push on the buffered graphics
 * and this Canvas thread is just in charge of deciding which action should
 * be invoked and repaint buffered image on own graphics.
 * this canvas support four actions:
 * INITIAL ACTION
 * START ACTION
 * RUN ACTION
 * END ACTION
 */
public class CoreCanvas extends Canvas implements Runnable{

	/**
	 * Game status: INITIAL...
	 */
	public static final byte GS_INITIAL = 0;
	public static final byte GS_START = 1; 
	public static final byte GS_RUN = 2;
	public static final byte GS_END = 3;
	
	// actions could be re-used.
	private Action initialAction = null;
	private Action startAction = null;
	private Action runAction = null;
	private Action endAction = null;
	// just one action is acting at one time.
	private Action currentAction = null;
	
	// every action is driving by thread.
	private Thread initialThread = null;
	private Thread startThread = null;
	private Thread runThread = null;
	private Thread endThread = null;
	
	private int gameStatus = GS_INITIAL;
	private int sleepTime = 200;
	private boolean statusChanged = true;
	
	// translate values with key between several actions.
	private Hashtable attributes = null;
	// buffered tip.
	private Image bufferedImage = null;
	private Graphics bufferedGraphics = null;
	
	public CoreCanvas()
	{
		attributes = new Hashtable();
		bufferedImage = Image.createImage(this.getWidth(), this.getHeight());
		bufferedGraphics = bufferedImage.getGraphics();
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.Displayable#paint(javax.microedition.lcdui.Graphics)
	 */
	protected void paint(Graphics g) {
		if(currentAction != null)
		{
			g.drawImage(bufferedImage, 0, 0, 0);
		}else
		{
			System.out.println("Action is null!");
		}
	}

	/**
	 * core allocation: switch actions according to game status while status is changed.
	 */
	public void run() {
		while(true)
		{
			if(statusChanged)
			{
				switch(gameStatus)
				{
					case GS_INITIAL :
					{
						if(initialAction != null)
						{
							statusChanged = false;
							currentAction = initialAction;
							initialThread.start();
						}else
						{
							gameStatus = GS_START;
						}
						break;
					}
					case GS_START :
					{
						if(startAction != null)
						{
							statusChanged = false;
							currentAction = startAction;
							startThread.start();
						}else
						{
							gameStatus = GS_RUN;
						}
						break;
					}
					case GS_RUN :
					{
						if(runAction != null)
						{
							statusChanged = false;
							currentAction = runAction;
							runThread.start();
						}else
						{
							gameStatus = GS_END;
						}
						break;
					}
					case GS_END :
					{
						if(runAction != null)
						{
							statusChanged = false;
							currentAction = endAction;
							endThread.start();
						}
						break;
					}
				}
			}
			try{
			    Thread.sleep(sleepTime);
			}catch(InterruptedException ie)
			{
				
			}
		}
		
	}
	
	/**
	 * register the action as Initial action, and prepare thread.
	 * @param action
	 */
	public void regInitialAction(Action action)
	{
		initialAction = action;
		initialThread = new Thread(action);
	}
	
	public void regStartAction(Action action)
	{
		startAction = action;
		startThread = new Thread(action);
	}
	
	public void regRunAction(Action action)
	{
		runAction = action;
		runThread = new Thread(action);
	}
	
	public void regEndAction(Action action)
	{
		endAction = action;
		endThread = new Thread(action);
	}

	/**
	 * @return Returns the sleepTime.
	 */
	public int getSleepTime() {
		return sleepTime;
	}
	/**
	 * @param sleepTime The sleepTime to set.
	 */
	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
	/**
	 * @return Returns the bufferedGraphics.
	 */
	public Graphics getBufferedGraphics() {
		return bufferedGraphics;
	}
	/**
	 * @param bufferedGraphics The bufferedGraphics to set.
	 */
	public void setBufferedGraphics(Graphics bufferedGraphics) {
		this.bufferedGraphics = bufferedGraphics;
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.Displayable#keyPressed(int)
	 */
	protected void keyPressed(int arg0) {
		currentAction.interrupt(arg0);
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.Displayable#keyPressed(int)
	 */
	protected void keyRepeated(int arg0) {
		currentAction.interrupt(arg0);
	}
	/**
	 * @return Returns the gameStatus.
	 */
	public int getGameStatus() {
		return gameStatus;
	}
	/**
	 * @param gameStatus The gameStatus to set.
	 */
	public void setGameStatus(int gameStatus) {
		// stop current action.
		currentAction.setActing(false);
		this.gameStatus = gameStatus;
		// time to garbage collect.
		System.gc();
		// notice core allcation cavas: status have been changed.
		statusChanged = true;
	}
	
	public void setAttribute(String name, Object value)
	{
		attributes.put(name, value);
	}
	
	public void removeAttribute(String name)
	{
		attributes.remove(name);
	}
	
	public Object getAttribute(String name)
	{
		return attributes.get(name);
	}

	/**
	 *  
	 * @uml.property name="gameRunner"
	 * @uml.associationEnd inverse="coreCanvas:com.savorjava.game.savormaze.GameRunner" multiplicity="(1 1)"
	 */
	private GameRunner runner;

	/**
	 *  
	 * @uml.property name="gameRunner"
	 */
	public GameRunner getGameRunner() {
		return runner;
	}

	/**
	 *  
	 * @uml.property name="gameRunner"
	 */
	public void setGameRunner(GameRunner runner) {
		this.runner = runner;
	}


}
