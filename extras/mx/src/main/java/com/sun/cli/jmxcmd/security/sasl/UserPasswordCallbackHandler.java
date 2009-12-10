/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/UserPasswordCallbackHandler.java,v 1.3 2004/03/09 00:49:01 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/03/09 00:49:01 $
 */
package com.sun.cli.jmxcmd.security.sasl;

import java.io.IOException;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import javax.security.sasl.AuthorizeCallback;

/**
	Client-side callback handler for username/password.
 */
public class UserPasswordCallbackHandler implements CallbackHandler
{
    private final String	mUser;
    private char[]		mPasswordChars;
    
		public
    UserPasswordCallbackHandler( final String user, final String password)
    {
    	if ( user == null || password == null )
    	{
    		assert( false );
    		throw new IllegalArgumentException();
    	}
    	
		mUser		= user;
		mPasswordChars	= password.toCharArray();
    }
	
		protected boolean
	handleCallback( Callback callback )
	{
		boolean	handled	= true;
		
		System.out.println( "UserPasswordCallbackHandler.handleCallback: " +
			callback.getClass().getName() );
		
		if (callback instanceof NameCallback)
		{
			final NameCallback ncb = (NameCallback) callback;
			ncb.setName( mUser );
		}
		else if (callback instanceof PasswordCallback)
		{
			final PasswordCallback pcb = (PasswordCallback) callback;
			pcb.setPassword( mPasswordChars );
		}
		else
		{
			System.out.println( "UserPasswordCallbackHandler: can't handle callback of type: " +
				callback.getClass().getName() );
			handled	= false;
		}
		
		return( handled );
	}
	
		public void
	handle(Callback[] callbacks)
		throws IOException, UnsupportedCallbackException
	{
		for (int i = 0; i < callbacks.length; i++)
		{
			if ( ! handleCallback( callbacks[ i ] ) )
			{
				throw new UnsupportedCallbackException(callbacks[i]);
			}
		}
	}

		private void
    clearPassword()
    {
		if (mPasswordChars != null)
		{
			for (int i = 0; i < mPasswordChars.length ; i++)
			{
				mPasswordChars[i] = 0;
			}
		    mPasswordChars = null;
		}
    }

		protected void
    finalize()
    {
        clearPassword();
    }
}


