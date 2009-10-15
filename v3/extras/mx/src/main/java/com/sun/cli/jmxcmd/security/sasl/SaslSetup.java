/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/SaslSetup.java,v 1.4 2004/10/14 19:06:28 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:06:28 $
 */
package com.sun.cli.jmxcmd.security.sasl;

import java.util.Map;

import javax.security.sasl.Sasl;




public class SaslSetup
{
	protected final Map<String,Object>		mEnv;
	protected final boolean	mUseTLS;
	
		public
	SaslSetup( final Map<String,Object> env, boolean useTLS )
	{
		mEnv	= env;
		mUseTLS	= useTLS;
		
		com.sun.cli.jmxcmd.security.sasl.provider.ProviderSetup.setup();
		
		if ( mUseTLS )
		{
		}
	}
	
	
		static protected void
	printDebug( Object o )
	{
		System.out.println( "#DEBUG: " + o );
	}
	
		protected void
	put( String key, Object value )
	{
		mEnv.put( key, value );
	}
	
	

	/**
		Get the quality of protection (QOP) setting for DIGEST-MD5
	 */
		public String
	getQOP( )
	{
		String	result	= null;
		
		if ( mUseTLS )
		{
			// login, but no TLS takes care of integrity, privacy
			result	= "auth";
		}
		else
		{
			result	= "auth-conf";
		}
	
		return( result );
	}
	
	
	
		private void
	setupQOP( String saslAlgorithm )
	{
		if ( saslAlgorithm != null &&
				saslAlgorithm.equals( "DIGEST-MD5" ) )
		{
			put( Sasl.QOP, getQOP( ) );
			printDebug( "Sasl.QOP = " + mEnv.get( Sasl.QOP ) );
			
			if ( ! mUseTLS )
			{
				put( Sasl.STRENGTH, "high" );
			}
		}
	}
	
	
		public void
	setupProfiles( String saslAlgorithm )
	{
		String	profile	= null;
		
		if ( saslAlgorithm == null )
		{
			profile	= mUseTLS ? "TLS" : "";
		}
		
		if ( mUseTLS )
		{
			profile	= "TLS SASL/" + saslAlgorithm;
		}
		else if ( saslAlgorithm != null )
		{
			profile	= "SASL/" + saslAlgorithm;
		}
		
		put("jmx.remote.profiles", profile );
		printDebug( "jmx.remote.profiles = " + mEnv.get( "jmx.remote.profiles" ) );
		
		setupQOP( saslAlgorithm );
	}
	
	
		public void
	setupSensible()
	{
		put( Sasl.MAX_BUFFER, "" + 32768 );	// allow a large buffer
		put( Sasl.REUSE, "true" );
		
		// JDK doesn't support ANONYMOUS anyway
		put( Sasl.POLICY_NOANONYMOUS, "true" );
	}
	

}


