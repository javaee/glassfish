/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/SaslServerSetup.java,v 1.2 2004/10/14 19:06:28 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2004/10/14 19:06:28 $
 */
package com.sun.cli.jmxcmd.security.sasl;

import java.util.Map;
import java.io.File;
import java.io.IOException;
import org.glassfish.admin.amx.util.StringUtil;



public class SaslServerSetup extends SaslSetup
{
		public
	SaslServerSetup( final Map<String,Object> env, final boolean	useTLS )
	{
		super( env, useTLS );
	}
	
	/**
		the access level file used by the connector server to
		perform user authorization. The access level file is a properties
		based text file specifying username/access level pairs where
		access level is either "readonly" or "readwrite" access to the
		MBeanServer operations. This properties based access control
		checker has been implemented using the MBeanServerForwarder
		interface which wraps the real MBean server inside an access
		controller MBean server which performs the access control checks
		before forwarding the requests to the real MBean server.

		This property is implementation-dependent and might not be
		supported by all implementations of the JMX Remote API.
	*/
		public void
	setupAuthorization( File authorizationFile)
	{
		if ( authorizationFile != null )
		{
			put("jmx.remote.x.access.file", authorizationFile );
			printDebug( "authorization enabled using file " + StringUtil.quote( authorizationFile ));
		}
		else
		{
			printDebug( "authorization disabled" );
		}
	}
	
		public void
	setupAuthentication( File authenticationFile )
		throws IOException
	{
		if ( authenticationFile != null )
		{
			final PasswordFileCallbackHandler	handler	= new PasswordFileCallbackHandler( authenticationFile.toString() );
			put("jmx.remote.sasl.callback.handler", handler );
			printDebug( "authentication enabled using file " + StringUtil.quote( authenticationFile ));
		}
		else
		{
			printDebug( "authentication disabled" );
		}
	}
}


