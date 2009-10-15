/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/SupportUtil.java,v 1.1 2004/04/26 07:29:39 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/04/26 07:29:39 $
 */
package com.sun.cli.jmxcmd.support;

import javax.management.ObjectName;

/**
 */
public final class SupportUtil
{
	private	 SupportUtil()	{}
	
		public static String
	getObjectNameDisplay( final ObjectName	objectName )
	{
		return( "---" + objectName + "---" );
	}
}