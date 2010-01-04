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


