/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.cli.jmxcmd.util;


import javax.management.*;
import org.glassfish.admin.amx.util.DebugState;
import org.glassfish.admin.amx.util.Output;




/**
	This class allows use of MBeanServerConnection methods with ObjectName patterns
	that resolve to a single MBean.  This is useful to avoid hard-coupling to specific
	ObjectNames; instead an ObjectName pattern may be used which resolves to a 
	single MBean.
	
	For example, if you know the 'name' property is unique (at least for your MBean),
	you could use the ObjectName "*:name=myname,*" instead of a possibly much longer
	and complicated name (which potentially could change each time the MBean is registered).
 */
public class MBeanServerConnection_Debug
	extends MBeanServerConnection_Hook
{
	final MBeanServerConnection_Hook.Hook	mHook;
	final DebugState						mDebugState;
	final Output							mOutput;
	
		public
	MBeanServerConnection_Debug(
		MBeanServerConnection	impl,
		DebugState				debugState,
		Output					output )
	{
		super( impl );
		
		mDebugState	= debugState;
		mOutput		= output;
		mHook		= new DebugHook();
	}
	
		Hook
	getHook()
	{
		return( mHook );
	}
	
		final DebugState
	getDebugState()
	{
		return( mDebugState );
	}
	
	private final static Object []	EMPTY_ARRAY	= new Object [ 0 ];
		
	private class DebugHook extends MBeanServerConnection_Hook.HookImpl
	{
			public
		DebugHook()
		{
		}
		
			final void
		printDebug( Object o )
		{
			if ( getDebugState().getDebug() )
			{
				mOutput.printDebug( o );
			}
		}
		
			public long
		preHook( String methodName )
		{
			return( preHook( methodName, EMPTY_ARRAY ) );
		}
		
			public long
		preHook( String methodName, Object [] args )
		{
			final long	id	= getNewID();
			
			printDebug( "pre: " + getInvocationString( id, methodName, args ) );
			
			return( id );
		}
		
			public void
		postHook( long id, String methodName )
		{
			printDebug( "post: " + getInvocationString( id, methodName, null ) );
		}
		
			public void
		postHook( long id, String methodName, Object [] args )
		{
			printDebug( "post: " + getInvocationString( id, methodName, args ) );
		}
		
			public void
		postHook( long id, String methodName, Object [] args, Object result )
		{
			final String resultString	= result == null ?
				"null" : result.getClass().getName() + " => " + result.toString();
				
			printDebug( "post: " +
				getInvocationString( id, methodName, args ) + resultString );
		}
	}
};

