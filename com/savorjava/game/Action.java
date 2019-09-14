/*
 * Created on 2005-1-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.savorjava.game;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

/**
 * @author Colin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class Action implements Runnable, CommandListener{
	
	/**
	 * maybe every action need draw something on the Canvas.
	 */
	protected CoreCanvas canvas = null;
	
	/**
	 * this flag decide if the action should run.
	 */
	protected boolean acting = true;
	
	/**
	 * this method should be invoked in run() of thread, it's core of this action.
	 * @param g
	 */
	public void perform(Graphics g)
	{
		
	}
	
	/**
	 * handle the key pressed event while user pressed keyboard.
	 * @param key
	 */
	public void interrupt(int key)
	{
		
	}
	
	/**
	 * Constructor: every sub classes of Action should invoke super(CoreCanvas).
	 * @param cc
	 */
	public Action(CoreCanvas cc)
	{
		canvas = cc;
	}

	/**
	 * @return Returns the acting.
	 */
	public boolean isActing() {
		return acting;
	}
	
	/**
	 * @param acting The acting to set.
	 */
	public void setActing(boolean acting) {
		this.acting = acting;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command arg0, Displayable arg1) {
		// TODO Auto-generated method stub

	}
}
