/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package com.sun.cli.jmxcmd.cmd;

import java.util.ArrayDeque;
import java.util.Deque;


/**
	Manages info for {@link NavCmd+}.
 */
public final class NavInfo
{
    public static final String DEFAULT_DIR = "/";
    
    String mCurrentDir;
    Deque<String> mDirStack = new ArrayDeque<String>();
    
		public
	NavInfo()
	{
		mCurrentDir = DEFAULT_DIR;
	}
    
    public String getCurrentDir() { return  mCurrentDir; }
    public void setCurrentDir(String dir) { mCurrentDir = dir; }
    
    public String toString()
    {
        return mCurrentDir;
    }
    
    public void pushd( final String s )
    {
        mDirStack.push(s);
    }
    
    public String popd()
    {
        return mDirStack.pop();
    }
}



