/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
