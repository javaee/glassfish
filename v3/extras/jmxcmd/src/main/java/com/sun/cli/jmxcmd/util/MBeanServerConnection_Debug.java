/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
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

