/*
 * Created on 2005-1-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.savorjava.game.savormaze;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.savorjava.game.Action;
import com.savorjava.game.CoreCanvas;

/**
 * @author Colin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GameRunner extends MIDlet {

	/**
	 * static instance of This MIDlet.
	 */
	public static MIDlet midlet = null;
	
	private CoreCanvas canvas = null;
	private Thread core = null;
	
	/** 
	 * key of the config item: ROBOTRUN
	 * values: true, false
	 */
	public static String ROBOTRUN = "ROBOTRUN";
	/**
	 * key of the config item: LEVEL
	 * values: Easy, Normal, Impossible
	 */
	public static String LEVEL = "LEVEL";
	
	/**
	 * Prepare Canvas and actions for this midlet.
	 */
	public GameRunner() {
		super();
		// initial allocation canvas.
		canvas = new CoreCanvas();
		// maybe order of creating actions is very important: some data initial order.
		Action initialAction = new MazeInitialAction(canvas);
		Action startAction = new MazeStartAction(canvas);
		Action runAction = new MazeRunAction(canvas);
		canvas.regInitialAction(initialAction);
		canvas.regStartAction(startAction);
		canvas.regRunAction(runAction);
		// start allocation thread.
		core = new Thread(canvas);
		core.start();
	}

	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	protected void startApp() throws MIDletStateChangeException {
		midlet = this;
		Display.getDisplay(this).setCurrent(canvas);
	}

	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	protected void pauseApp() {

	}

	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
	 */
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {

	}

}
