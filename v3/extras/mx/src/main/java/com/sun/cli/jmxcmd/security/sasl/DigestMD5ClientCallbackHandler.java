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
