/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/NoProviderFoundException.java,v 1.1 2003/11/21 21:23:49 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:49 $
 */
package com.sun.cli.jmxcmd.support;
 
/**
	Thrown when no JMXConnectorProvider is found that can handle
	the specified parameters.
 */
public class NoProviderFoundException extends Exception
{
		public 
	NoProviderFoundException( String msg )
	{
		super( msg );
	}
}