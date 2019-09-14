/*
 * Created on 2005-1-20
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.savorjava.game;

/**
 * @author Colin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Utility {

	public static long min(long left, long right)
	{
		if(left >= right)
		{
			return right;
		}else
		{
			return left;
		}
	}
	
	public static long max(long left, long right)
	{
		if(left >= right)
		{
			return left;
		}else
		{
			return right;
		}
	}
}
