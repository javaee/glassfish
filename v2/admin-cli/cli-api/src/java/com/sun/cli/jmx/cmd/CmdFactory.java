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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/CmdFactory.java,v 1.3 2005/12/25 03:45:29 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:29 $
 */
 
package com.sun.cli.jmx.cmd;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.cli.jmx.support.CLISupportMBeanProxy;
import com.sun.cli.util.ClassUtil;
import com.sun.cli.util.stringifier.SmartStringifier;

public class CmdFactory
{
	private final HashMap	mCmds;
	
	public static final String	DEFAULT_CMD_NAME	= "DEFAULT_CMD";
	
		private static void
	p( Object o )
	{
		System.out.println( SmartStringifier.toString( o ) );
	}
	
		public
	CmdFactory()
	{
		mCmds	= new HashMap();
	}
	
		public void
	addCmdMapping( String name, Class theClass )
	{
		if ( ! Cmd.class.isAssignableFrom( theClass ) )
		{
			throw new IllegalArgumentException( "Command " + theClass.getName() +
				" must implement " + Cmd.class.getName() );
		}
		
		mCmds.put( name, theClass );
	}
	
		public void
	removeCmdMapping( String name )
	{
		mCmds.remove( name );
	}
	
		public Cmd
	createCmd( String name, CmdEnv env )
		throws Exception
	{
		final String	cmdString	= name;
		
		final Cmd	cmd	= instantiateCmd( cmdString, env );
		
		return( cmd );
	}
	
		public Class
	getClass( String cmdName  )
		throws Exception
	{
		final Class	cmdClass	= (Class)mCmds.get( cmdName );
		
		return( cmdClass );
	}
	
		public String []
	getNames( Class theClass )
	{
		final ArrayList	list	= new ArrayList();
		
		final Iterator keys	= mCmds.keySet().iterator();
		
		while( keys.hasNext() )
		{
			final String	key	= (String)keys.next();
			
			final Class	thisClass	= (Class)mCmds.get( key );
			if ( thisClass == theClass ) 
			{
				list.add( key );
			}
		}
		
		final String []	names	= new String [ list.size() ];
		list.toArray( names );
		
		return( names );
	}
	
		private Cmd
	instantiateCmd( String cmdName, CmdEnv env )
		throws Exception
	{
		Class	cmdClass	= (Class)mCmds.get( cmdName );
		
		if ( cmdClass == null )
		{
			// not found; see if there is a default registered
			cmdClass	= (Class)mCmds.get( DEFAULT_CMD_NAME );
		}
		
		if ( cmdClass == null )
		{
			return( null );
		}

		final Object []	args	= new Object [] { env };

		Cmd cmd	= null;
		try
		{
			cmd	= (CmdBase)ClassUtil.InstantiateObject( cmdClass, args );
		}
		catch( Exception e )
		{
			p( e.getMessage() );
			e.printStackTrace();
			throw e;
		}
		
		return( cmd );
	}
}

