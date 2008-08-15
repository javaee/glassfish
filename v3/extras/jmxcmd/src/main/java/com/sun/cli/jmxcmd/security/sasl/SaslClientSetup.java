/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/SaslClientSetup.java,v 1.2 2004/03/09 00:45:06 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2004/03/09 00:45:06 $
 */
 
package com.sun.cli.jmxcmd.security.sasl;

import java.util.Map;


public class SaslClientSetup extends SaslSetup
{
		public
	SaslClientSetup( final Map<String,Object> env, final boolean useTLS )
	{
		super( env, useTLS );
	}
	
		public void
	setupClientCallback( final String user, final String password )
	{
		if ( user != null )
		{
            // DigestMD5ClientCallbackHandler is compatible with PLAIN also
	   		put("jmx.remote.sasl.callback.handler",
	   			new DigestMD5ClientCallbackHandler( user, password ) );
	   			
			put( "jmx.remote.sasl.authorization.id", user );
		}
	}
}


