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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/MBeanServerConnection_Pattern.java,v 1.3 2005/11/15 20:21:48 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/15 20:21:48 $
 */
package com.sun.cli.jmxcmd.util.jmx;

import javax.management.*;
import java.io.IOException;
import java.util.Set;

import com.sun.cli.jcmd.util.misc.SetUtil;


/**
	This class allows use of MBeanServerConnection methods with ObjectName patterns
	that resolve to a single MBean.  This is useful to avoid hard-coupling to specific
	ObjectNames; instead an ObjectName pattern may be used which resolves to a 
	single MBean.
	
	For example, if you know the 'name' property is unique (at least for your MBean),
	you could use the ObjectName "*:name=myname,*" instead of a possibly much longer
	and complicated name (which potentially could change each time the MBean is registered).
 */
public class MBeanServerConnection_Pattern extends MBeanServerConnection_Hook
{
	final MBeanServerConnection_Hook.Hook	mHook;
	
		public
	MBeanServerConnection_Pattern( MBeanServerConnection impl )
	{
		super( impl );
		
		mHook	= new NameResolverHook();
	}
	
		Hook
	getHook()
	{
		return( mHook );
	}
	
	/**
		Hook which resolves a name pattern to a single name
	*/
	class NameResolverHook extends MBeanServerConnection_Hook.HookImpl
	{
			public
		NameResolverHook(   )
		{
		}
		
			public ObjectName
		nameHook( long id, ObjectName name )
			throws java.io.IOException
		{
			try
			{
				ObjectName	newName	= resolve( name );
				
				return( newName );
			}
			catch( InstanceNotFoundException e )
			{
				// we can't let InstanceNotFoundException propogate because
				// the MBeanServerConnection interface does not declare it as a legal
				// exception for some of the calls the nameHook() is called in.
				throw new IllegalArgumentException();
			}
		}
	}

	
		public ObjectName
	resolve( ObjectName input )
		throws java.io.IOException, InstanceNotFoundException
	{
		ObjectName	resolvedName	= input;
		
		if ( input.isPattern() )
		{
			final Set	resolvedNames	= getConn().queryNames( input, null );
			final int	numNames	= resolvedNames.size();
			
			if ( numNames == 1 )
			{
				resolvedName	= (ObjectName)SetUtil.getSingleton( resolvedNames );
			}
			else if ( numNames > 1 )
			{
				throw new InstanceNotFoundException( input.toString() );
			}
			else
			{
				throw new InstanceNotFoundException( input.toString() );
			}
		}
		return( resolvedName );
	}
	


};

