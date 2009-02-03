/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/provider/PLAINServerFactory.java,v 1.1 2004/03/09 00:44:44 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/03/09 00:44:44 $
 */
package com.sun.cli.jmxcmd.security.sasl.provider;


import java.util.Map;
import javax.security.auth.callback.*;
import javax.security.sasl.*;

/**
 * Plain SASL server factory. Used with PlainServer.java.
 */
public class PLAINServerFactory implements SaslServerFactory
{
    	public SaslServer
    createSaslServer(
    	final String			mechs,
		final String			protocol,
		final String			serverName,
		final Map				props,
		final CallbackHandler	cbh)
		throws SaslException
	{
		SaslServer	result	= null;
		
		if ( mechs.equals( PLAIN ) )
		{
		    result	=  new PLAINServer(cbh);
		}
		return result;
    }

	static private final String		TRUE	= "true";
	static public final String		PLAIN	= "PLAIN";
	static private final String[]	EMPTY	= new String[ 0 ];
	static private final String[]	PLAIN_ARRAY	= new String[] { PLAIN };
	
    /**
     * Simple-minded implementation that ignores props
     */
    	public String[]
    getMechanismNames(Map props)
    {
    	String[]	result	= null;
    	
		if ( props == null )
		{
			result	= PLAIN_ARRAY;
		}
		else if (
			TRUE.equals( props.get( Sasl.POLICY_NOPLAINTEXT ) ) ||
			TRUE.equals( props.get( Sasl.POLICY_NOACTIVE ) ) ||
			TRUE.equals( props.get( Sasl.POLICY_NODICTIONARY ) ) ||
			TRUE.equals( props.get( Sasl.POLICY_FORWARD_SECRECY ) )
			 )
		{
		System.out.println( "PLAINServerFactory: policy prohibits use" );
			result	= EMPTY;
		}
		else
		{
			result	= PLAIN_ARRAY;
		}
		
		return result;
    }
}
