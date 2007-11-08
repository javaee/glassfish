/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.util;

/**
	Provides wrapper to direct logging messages to some kind of output.
	
 */
public class Logger
{
	private final static int 	LOG_BASE	= 13;
	// caution: this are assumed to be in order
	public final static int		LOG_OFF		= LOG_BASE;
	public final static int		LOG_ERRORS	= LOG_BASE + 1;
	public final static int		LOG_DEBUG	= LOG_BASE + 2;
	
	static private Logger		 mInstance	= new Logger();
	
	
	static protected int	 sLogLevel	= LOG_DEBUG;
	
	
	Logger()
	{
	}
	
		protected void
	_log( String msg )
	{
		System.out.println( msg );
	}
	
	
	//--------------------------------------------------------
	
	/**
		set the log level; return the old log level
	 */
		public static int
	setLogLevel( int logLevel )
	{
		int	oldLevel	= getLogLevel();
		
		Assert.assertit(	logLevel != LOG_OFF &&
						logLevel != LOG_ERRORS &&
						logLevel != LOG_DEBUG, "illegal log level" );
						
		sLogLevel	= logLevel;
			
		return( oldLevel );
	}
	
		Logger
	getInstance()
	{
		return( mInstance );
	}
	
		public static int
	getLogLevel()
	{
		return( sLogLevel );
	}
	
	
	
	/**
		OK, but log( object ) preferred if it avoids
		generating a String from an object prior to the call.
	*/
		public static void
	log( String msg )
	{
		if ( sLogLevel >= LOG_DEBUG )
		{
			System.out.println( msg );
		}
	}
	
	/**
		optimization for the caller; caller can pass an object
		directly so that if logging is off, we won't ever call
		toString()
	*/
		public static void
	log( Object object )
	{
		if ( sLogLevel >= LOG_DEBUG && object != null )
		{
			log( object.toString() );
		}
	}
	
		public static void
	logError( String msg )
	{
		if ( sLogLevel >= LOG_ERRORS && msg != null )
		{
			log( msg );
		}
	}
	
		public static void
	logFatal( String msg )
	{
		// always log
		log( msg );
	}
};

