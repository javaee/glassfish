/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package com.sun.cli.jmxcmd.cmd;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;


/**
	Manages info for {@link NavCmd+}.
 */
public final class NavResolver
{
    private final MBeanServerConnection mConn;
    
		public
	NavResolver( final MBeanServerConnection conn)
	{
        mConn = conn;
	}
    
    /** return an ObjectName pattern for the current dir */
    public ObjectName resolve( final String wd )
    {
        return null;
    }
}



