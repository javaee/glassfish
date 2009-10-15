/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/PasswordFileCallbackHandler.java,v 1.1 2004/03/08 23:28:39 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/03/08 23:28:39 $
 */
package com.sun.cli.jmxcmd.security.sasl;

import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import javax.security.sasl.RealmCallback;
import javax.security.sasl.AuthorizeCallback;


/**
	Server Callback handler invoked during SASL processing.
	
	Utilizes a properties file containing usernamd password pairs
	or a Properties object containing same.
 */
	public class
PasswordFileCallbackHandler implements CallbackHandler
{
	private final Properties	mUserPasswordPairs;
	
		static Properties
	initProps( String pwFile )
		throws FileNotFoundException, IOException
	{
		final FileInputStream	input	= new FileInputStream( pwFile );
		final Properties		props	= new Properties();
		
		try
		{
			props.load( input );
		}
		finally
		{
			input.close();
		}

		return( props );
	}
	
	/**
	* Contents of files are in the Properties file format.
	*
	* @param pwFile name of file containing name/password pairs
	*/
		public
	PasswordFileCallbackHandler( String pwFile ) throws IOException
	{
		this( initProps( pwFile ) );
	}
	
	
	/**
	* Contents of files are in the Properties file format.
	*
	* @param pwFile name of file containing name/password pairs
	*/
		public
	PasswordFileCallbackHandler( final Properties pairs ) throws IOException
	{
		mUserPasswordPairs = pairs;
	}

		public void
	handle( final Callback[] callbacks)
		throws UnsupportedCallbackException
	{
		NameCallback		nameCallback	= null;
		PasswordCallback	passwordCallback = null;
		
		for (int i = 0; i < callbacks.length; i++)
		{
			final Callback	callback	= callbacks[ i ];
			
			if ( callback instanceof NameCallback)
			{
				nameCallback = (NameCallback) callback;
			}
			else if (callback instanceof PasswordCallback)
			{
				passwordCallback = (PasswordCallback) callback;
			}
			else if ( callback instanceof RealmCallback )
			{
				final RealmCallback	rcb = (RealmCallback) callback;
				rcb.setText( "file" );
			}
			else if ( callback instanceof AuthorizeCallback )
			{
				final AuthorizeCallback	acb = (AuthorizeCallback) callback;
				
				final String	authenticatedID	= acb.getAuthenticationID();
				final String	authorizationID	= acb.getAuthorizationID();
				
				acb.setAuthorized( authorizationID.equals( authenticatedID ) );
			}
			else
			{
				throw new UnsupportedCallbackException( callback );
			}
		}
		
		// Process retrieval of password; can get password iff
		// username is available in NameCallback
		//
		if ( nameCallback != null && passwordCallback != null)
		{
			final String		username	= nameCallback.getDefaultName();
			final String		pw			= mUserPasswordPairs.getProperty( username );
			if (pw != null)
			{
				final char[] pwChars = pw.toCharArray();
				passwordCallback.setPassword( pwChars );

				java.util.Arrays.fill( pwChars, (char)0 );
			}
		}
	}

}

