/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2010 Sun Microsystems, Inc. All rights reserved.
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


