/*
 * Created on 2005-1-28
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.savorjava.game.savormaze;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.savorjava.game.Action;
import com.savorjava.game.CoreCanvas;

/**
 * @author Colin
 *
 * The first phase: show game face image, maybe loading some data we need.
 */
public class MazeInitialAction extends Action {

	public MazeInitialAction(CoreCanvas cc)
	{
		super(cc);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		perform(canvas.getBufferedGraphics());
		canvas.repaint();
		try{
		    // Gamer will cannot leave until after 3 second.
		    Thread.sleep(3000);
		}catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}
		// re-register this Action for re-use while gamer exit the third phase.
		canvas.regInitialAction(this);
		// notice allocation cavas: next phase should be StartAction.
		canvas.setGameStatus(CoreCanvas.GS_START);
	}

	public void perform(Graphics g)
	{
		Image face = null;
		try
		{
		    face = Image.createImage("/face.png");
		}catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		if(face != null)
		{
			int x = g.getClipWidth();
			int y = g.getClipHeight();
			g.setColor(0xFFFFFF);
			// clear the full screen first.
			g.fillRect(0, 0, x, y);
			// then draw the game face image.
			g.drawImage(face, (x - face.getWidth()) / 2, (y - face.getHeight()) / 2, 0);
		}
	}
	


}
