/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/spi/JMXConnectorProviderInfo.java,v 1.2 2005/05/19 19:34:08 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/05/19 19:34:08 $
 */
 
package com.sun.cli.jmxcmd.spi;

import javax.management.remote.JMXConnector;

import java.lang.reflect.Method;

/**
 	Info as supplied from a JMXConnectorProvider along with a routine
 	to get it via expected name of getInfo().
 */
public interface JMXConnectorProviderInfo
{
	/**
		Return a usage String
		
		@return	a String describing the usage of the Provider (with example)
	 */
	public String	getUsage();
	
	/**
		Return a description String for the Provider.
		
		@return	a free-format String
	 */
	public String	getDescription();
	
	
	
	/**
		Used to get a JMXConnectorProviderInfo from an Object. 
	 */
	public final class InfoGetter
	{
		private InfoGetter()	{}
		
		/**
			Call getInfo() on an Object to see if it has a JMXConnectorProviderInfo
			available.  Return null if not present.
		 */
			public static JMXConnectorProviderInfo
		getInfo( Class<?> theClass )
		{
			JMXConnectorProviderInfo	info	= null;
			
			try
			{
				Method	m	= theClass.getDeclaredMethod( "getInfo", (Class[])null );
			
				info	= (JMXConnectorProviderInfo)m.invoke( theClass, (Object[])null );
			}
			catch( Exception e )
			{
			}
			
			return( info );
		}
	}
	
}




