/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdEventListener.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 

package com.sun.cli.jcmd.framework;

/**
	Events may be issued by commands and/or the framework.  Any object
	wishing to listen to such events must implement this interface.
 */
public interface CmdEventListener
{
	/**
		An event has happened...the listener may do something or ignore the event.
	 */
	public void		acceptCmdEvent( CmdEvent event );
	
	/**
		Return the name of this listener.
	 */
	public String	getListenerName();
};



