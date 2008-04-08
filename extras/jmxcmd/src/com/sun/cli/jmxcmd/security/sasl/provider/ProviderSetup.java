/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/provider/ProviderSetup.java,v 1.3 2004/03/09 00:44:44 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/03/09 00:44:44 $
 */

package com.sun.cli.jmxcmd.security.sasl.provider;

import java.security.Provider;
import java.security.Security;

/**
 */
public final class ProviderSetup 
{
    private ProviderSetup()	{}
    private static boolean	sInited	= false;
    
	/**
		Not needed for JDK 1.5.
	 */
		private static void
	addSaslProvider()
	{
		final Provider	provider	= new com.sun.security.sasl.Provider();
		
		System.out.println( "provider = " + provider.getInfo() );
			
        Security.addProvider( provider );
	}
	
	/**
		JDK 1.5 does not implement a SASL/PLAIN server mechanism.
	 */
		private static void
	addSaslPLAINProvider()
	{
        Security.addProvider( new PLAINServerProvider() );
	}
	
	
    	public static void
    setup()
    {
    	if ( ! sInited )
    	{
    		sInited	= true;
    		addSaslProvider();
    		addSaslPLAINProvider();
    	}
    }
}
