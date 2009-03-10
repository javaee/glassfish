/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/DigestMD5ClientCallbackHandler.java,v 1.3 2004/03/09 00:49:01 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/03/09 00:49:01 $
 */
package com.sun.cli.jmxcmd.security.sasl;

import javax.security.auth.callback.Callback;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;


/**
	Client-side callback handler for username/password + Realm, RealmChoice
 */
public class DigestMD5ClientCallbackHandler extends UserPasswordCallbackHandler
{
		public
    DigestMD5ClientCallbackHandler(String user, String password )
    {
    	super( user, password );
    }

		protected boolean
	handleCallback( Callback callback )
	{
		boolean	handled	= true;
		
		if ( callback instanceof RealmCallback )
		{
			/*
				We need to choose the realm to be used...
			 */
			 
			final RealmCallback	rcb = (RealmCallback) callback;
			rcb.setText( rcb.getDefaultText() );
			
			System.out.println("RealmCallback: Default realm = " + rcb.getDefaultText());
		}
		else if ( callback instanceof RealmChoiceCallback )
		{
			/*
				We're allowed to choose the realm...
			 */
			final RealmChoiceCallback	rccb		= (RealmChoiceCallback) callback;
			final String				choices[]	= rccb.getChoices();
			
			if (choices == null || choices.length == 0)
			{
			    System.out.println("RealmChoiceCallback: Zero choices");
			}
			else
			{
			    for (int c = 0; c < choices.length; c++)
			    {
					System.out.println("RealmChoiceCallback: Choice[" + c + "] = " + choices[c] );
			    }
			}
			System.out.println("RealmChoiceCallback: Default choice = " + rccb.getDefaultChoice());
			rccb.setSelectedIndex( rccb.getDefaultChoice() );
		}
		else if ( super.handleCallback( callback ) )
		{
			handled	= true;
		}
		else
		{
			System.out.println( "DigestMD5ClientCallbackHandler: can't handle callback of type: " +
				callback.getClass().getName() );
			handled	= false;
		}
		
		return( handled );
	}
}
