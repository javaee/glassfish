/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/CmdInfos.java,v 1.4 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:18 $
 */
 
package com.sun.cli.jcmd.util.cmd;

import java.util.List;
import java.util.ArrayList;



/**
	Interface for info about a particular subcommand.  There should be one
	of these for every command that a Cmd class implements.
 */
public final class CmdInfos
{
	private final List<CmdInfo>	mInfos;
	
		public
	CmdInfos()
	{
		mInfos	= new ArrayList<CmdInfo>();
	}
	
	/**
		The order of the CmdInfo within the array is significant and is retained;
		the first item is by default the overall name for the entire group.
		
		@param infos	array of CmdInfo
	 */
		public
	CmdInfos( CmdInfo[] infos )
	{
		this();
		
		for( int i = 0; i < infos.length; ++i )
		{
			add( infos[ i ] );
		}
	}
	
		public
	CmdInfos( CmdInfo info )
	{
		this( new CmdInfo[] { info } );
	}
	
		public
	CmdInfos( CmdInfo info1, CmdInfo info2)
	{
		this( new CmdInfo[] { info1, info2 } );
	}
	
		public
	CmdInfos( CmdInfo info1, CmdInfo info2, CmdInfo info3 )
	{
		this( new CmdInfo[] { info1, info2, info3 } );
	}
	
		public
	CmdInfos( CmdInfo info1, CmdInfo info2, CmdInfo info3, CmdInfo info4 )
	{
		this( new CmdInfo[] { info1, info2, info3, info4} );
	}
	
		public
	CmdInfos( CmdInfo info1, CmdInfo info2, CmdInfo info3, CmdInfo info4, CmdInfo info5 )
	{
		this( new CmdInfo[] { info1, info2, info3, info4, info5} );
	}
	
		public void
	add( CmdInfo info )
	{
		mInfos.add( info );
	}
	
		public int
	size(  )
	{
		return( mInfos.size() );
	}
	
		public CmdInfo
	get( int i )
	{
		return( (CmdInfo)mInfos.get( i ) );
	}
	
		public CmdInfo
	get( String name )
	{
		CmdInfo	item	= null;
		
		for( int i = 0; i < mInfos.size(); ++i )
		{
			final CmdInfo	info	= get( i );
			
			if ( name.equals( info.getName() ) )
			{
				item	= info;
				break;
			}
		}
			
		return( item );
	}
	
	
	/**
		Return a list of all the command names.  The order is significant; the first name
		is the overall name for the group.
	 */
		public List<String>
	getNamesList( )
	{
		final List<String>	names	= new ArrayList<String>();
		
		for( int i = 0; i < mInfos.size(); ++i )
		{
			names.add( get( i ).getName() );
		}
		
		return( names );
	}
	
	
	/**
		Return a list of all the command names.  The order is significant; the first name
		is the overall name for the group.
	 */
		public String[]
	getNames( )
	{
		final List<String>	namesList	= getNamesList();
		
		return( (String[])namesList.toArray( new String[ namesList.size() ] ) );
	}
	
		public CmdInfo[]
	toArray()
	{
		return( (CmdInfo[])mInfos.toArray( new CmdInfo[ mInfos.size() ] ) );
	}
	
		public String
	toString()
	{
		final StringBuffer	buf	= new StringBuffer();
		
		final String[]	names	= getNames();
		for( int i = 0; i < names.length; ++i )
		{
			buf.append( get( names[ i ] ).toString() + "\n" );
		}
		
		return( buf.toString() );
	}
}





