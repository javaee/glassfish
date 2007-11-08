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
 
/*
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/util/MBeanServerConnection_Debug.java,v 1.3 2005/12/25 03:45:55 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:55 $
 */
package com.sun.cli.jmx.util;

import javax.management.*;
import java.io.IOException;
import java.util.Set;

import com.sun.cli.util.DebugState;



/*
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
	
		public
	MBeanServerConnection_Debug( MBeanServerConnection impl, DebugState debugState )
	{
		super( impl );
		
		mDebugState	= debugState;
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
	
	/*
		Hook which resolves a name pattern to a single name
	*/
	private final static Object []	EMPTY_ARRAY	= new Object [ 0 ];
		
	class DebugHook extends MBeanServerConnection_Hook.HookImpl
	{
			public
		DebugHook( )
		{
			printDebug( "MBeanServerConnection_Debug: instantiated" );
		}
		
			final void
		printDebug( Object o )
		{
			if ( getDebugState().getDebug() )
			{
				getDebugState().printDebug( o );
			}
		}
		
			
			String
		getInvocationString( long id, String methodName, Object [] args )
		{
			String	msg	= id + ": " + methodName + "(";
			
			for( int i = 0; i < args.length; ++i )
			{
				msg	= msg + args[ i ].getClass().getName();
				if ( i != args.length - 1 )
				{
					msg	= msg + ",";
				}
			}
			msg	= msg + ")";
			
			return( msg );
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
			printDebug( "post: " +
				getInvocationString( id, methodName, args ) + 
				result.getClass().getName() + " => " + result.toString()
				);
		}
	}
};

