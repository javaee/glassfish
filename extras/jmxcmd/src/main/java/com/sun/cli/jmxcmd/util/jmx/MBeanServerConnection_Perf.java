/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/MBeanServerConnection_Perf.java,v 1.3 2005/11/15 20:21:48 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/15 20:21:48 $
 */
package com.sun.cli.jmxcmd.util.jmx;

import javax.management.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.sun.cli.jcmd.util.misc.Output;



/**
	This class allows use of MBeanServerConnection methods with ObjectName patterns
	that resolve to a single MBean.  This is useful to avoid hard-coupling to specific
	ObjectNames; instead an ObjectName pattern may be used which resolves to a 
	single MBean.
	
	For example, if you know the 'name' property is unique (at least for your MBean),
	you could use the ObjectName "*:name=myname,*" instead of a possibly much longer
	and complicated name (which potentially could change each time the MBean is registered).
 */
public class MBeanServerConnection_Perf
	extends MBeanServerConnection_Hook
{
	private final PerfHook	mHook;
	private final Output	mOutput;
	private  boolean		mPerfEnabled;
	
		public
	MBeanServerConnection_Perf(
		MBeanServerConnection	impl,
		Output					output )
	{
		super( impl );
		
		mOutput			= output;
		mHook			= new PerfHook();
		mPerfEnabled	= true;
	}
	
		public final boolean
	getPerf()
	{
		return( mPerfEnabled );
	}
	
		public final void
	setPerf( final boolean perfEnabled)
	{
		mPerfEnabled	= perfEnabled;
	}
	
		protected final Hook
	getHook()
	{
		return( mHook );
	}
	
	private final static Object []	EMPTY_ARRAY	= new Object [ 0 ];
		
	private final class PerfHook extends MBeanServerConnection_Hook.HookImpl
	{
		private final Map<Long,Long>	mTimers;
		
			public
		PerfHook()
		{
			mTimers	= Collections.synchronizedMap( new HashMap<Long,Long>() );
		}
		
			private final void
		print( Object o )
		{
			if ( getPerf() )
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
			final long	id	= super.preHook( methodName, args);

			final Long	start	= new Long( System.currentTimeMillis() );
			mTimers.put( new Long( id ), start );
			
			return( id );
		}
		
			private void
		printTime( final long id, final String methodName, final Object[] args )
		{
			final long	curTime	= System.currentTimeMillis();
			final Long	start	= mTimers.remove( new Long( id ) );
			if ( start != null )
			{
				mOutput.println( getInvocationString( methodName, args ) + ": " +
					(curTime - start.longValue()) );
			}
		}
		
			public void
		postHook( long id, String methodName )
		{
			printTime( id, methodName, null);
		}
		
			public void
		postHook( long id, String methodName, Object [] args )
		{
			printTime( id, methodName, args);
		}
		
			public void
		postHook( long id, String methodName, Object [] args, Object result )
		{
			printTime( id, methodName, args);
		}
	}
};

