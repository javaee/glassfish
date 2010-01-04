/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2009 Sun Microsystems, Inc. All rights reserved.
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

