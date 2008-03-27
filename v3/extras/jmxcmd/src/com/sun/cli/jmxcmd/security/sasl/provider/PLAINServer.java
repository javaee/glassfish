/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/provider/PLAINServer.java,v 1.3 2004/03/09 00:44:44 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/03/09 00:44:44 $
 */
package com.sun.cli.jmxcmd.security.sasl.provider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;


import javax.security.sasl.Sasl;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslException;

/**
 * Implements the PLAIN SASL mechanism.
 */
public class PLAINServer implements SaslServer
{
    private boolean					mCompleted;
    private final CallbackHandler	mCallbackHandler;
    private String					mAuthorizationID;
    private static final byte		SEPARATOR = 0;

    	public
    PLAINServer(CallbackHandler cbh)
    {
		mCallbackHandler 	= cbh;
		mAuthorizationID	= null;
		mCompleted			= false;
    }

    	public String
    getMechanismName()
    {
		return "PLAIN";
    }

		public byte[]
    evaluateResponse(byte[] response)
    	throws SaslException
    {
		if ( mCompleted )
		{
		    throw new IllegalStateException("PLAIN: authentication already completed");
		}
		mCompleted = true;

		// Extract authorization_id/authentication_id/password from response
		//
		int indexSeparator1 = 0;
		int indexSeparator2 = 0;
		boolean foundSeparator1 = false;
		boolean foundSeparator2 = false;
		for (int i = 0; i < response.length; i++)
		{
			if (response[i] != SEPARATOR)
			{
				continue;
			}
			else
			{
				if (!foundSeparator1)
				{
				    indexSeparator1 = i;
				    foundSeparator1 = true;
				}
				else if (!foundSeparator2)
				{
				    indexSeparator2 = i;
				    foundSeparator2 = true;
				}
			}
		}
		
		int authzidSize = indexSeparator1;
		int authnidSize = indexSeparator2 - indexSeparator1 - 1;
		int passwdSize = response.length - indexSeparator2 - 1;
		byte authzid[] = new byte[authzidSize];
		byte authnid[] = new byte[authnidSize];
		byte passwd[] = new byte[passwdSize];
		
		System.arraycopy(response, 0, authzid, 0, authzidSize);
		System.arraycopy(response, indexSeparator1+1, authnid, 0, authnidSize);
		System.arraycopy(response, indexSeparator2+1, passwd, 0, passwdSize);
		
		String authenticationID;
		String password;
		try
		{
		    authenticationID = new String(authnid, "UTF-8");
		    password = new String(passwd, "UTF-8");
		    for (int i = 0; i < passwd.length; i++)
		    {
				passwd[i] = 0;
		    }
		    passwd = null;
		    
		    if (authzid.length == 0)
		    {
				mAuthorizationID = authenticationID;
		    }
		    else
		    {
				mAuthorizationID = new String(authzid, "UTF-8");
		    }
		}
		catch (UnsupportedEncodingException e)
		{
		    throw new SaslException("PLAIN: Cannot get UTF-8 encoding of ids", e);
		}

		// Compare the remote authentication_id/password with the one supplied
		// locally by the server callback
		//
		final String userPassword = getUserPassword(authenticationID);
		if ( ! userPassword.equals( password ) )
		{
		    throw new SaslException("PLAIN: invalid username/password.");
		}

		return null;
    }

		public String
    getAuthorizationID()
    {
		return mAuthorizationID;
    }

    	public boolean
    isComplete()
    {
		return mCompleted;
    }

		public byte[]
    unwrap(byte[] incoming, int offset, int len)
		throws SaslException
	{
		if ( mCompleted )
		{
		    throw new IllegalStateException("PLAIN: this mechanism supports " +
						    "neither integrity nor privacy");
		}
		else
		{
		    throw new IllegalStateException("PLAIN: authentication not " +
						    "completed");
		}
    }

    	public byte[]
    wrap(byte[] outgoing, int offset, int len)
        throws SaslException
	{
		if ( mCompleted )
		{
		    throw new IllegalStateException("PLAIN: this mechanism supports " +
						    "neither integrity nor privacy");
		}
		else
		{
		    throw new IllegalStateException("PLAIN: authentication not " +
						    "completed");
		}
	}

    	public Object
    getNegotiatedProperty(String propName)
    {
        if ( mCompleted )
        {
            if (propName.equals(Sasl.QOP))
            {
                return "auth";
            }
            else
            {
                return null;
            }
        }
        else
        {
            throw new IllegalStateException("PLAIN: authentication not completed");
        }
    }

    	public void
    dispose()
   	 throws SaslException
    {
    }

		private String
    getUserPassword(String user)
		throws SaslException
	{
		try
		{
			final String userPrompt = "PLAIN authentication id: ";
			final String pwPrompt = "PLAIN password: ";
			
			NameCallback nameCb = new NameCallback(userPrompt, user);
			PasswordCallback passwordCb = new PasswordCallback(pwPrompt, false);
			
			mCallbackHandler.handle(new Callback[] { nameCb, passwordCb });
			
			final char	pwchars[] = passwordCb.getPassword();
			String pw = null;
			if (pwchars != null)
			{
				pw = new String(pwchars);
				passwordCb.clearPassword();
			}
			return pw;
		}
		catch (IOException e)
		{
			throw new SaslException("Cannot get password", e);
		}
		catch (UnsupportedCallbackException e)
		{
			throw new SaslException("Cannot get userid/password", e);
		}
	}
}
