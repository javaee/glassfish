/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdOutputN.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 
package com.sun.cli.jcmd.framework;

/**
	Directs output to to other CmdOutputs
 */

public class CmdOutputN implements CmdOutput
{
	private final CmdOutput[]		mOutputs;
	
		public
	CmdOutputN( CmdOutput[]	outputs )
		throws java.io.IOException
	{
		mOutputs	= outputs;
	}
	
		public void
	print( Object o )
	{
		for( int i = 0; i < mOutputs.length; ++i )
		{
			mOutputs[ i ].print( o );
		}
	}
	
		public void
	println( Object o )
	{
		for( int i = 0; i < mOutputs.length; ++i )
		{
			mOutputs[ i ].print( o + "\n" );
		}
	}
	
		public void
	printError( Object o )
	{
		for( int i = 0; i < mOutputs.length; ++i )
		{
			mOutputs[ i ].printError( o );
		}
	}
	
		public boolean
	getDebug(  )
	{
		boolean	debug	= false;
		
		for( int i = 0; i < mOutputs.length; ++i )
		{
			if ( mOutputs[ i ].getDebug() )
			{
				debug	= true;
				break;
			}
		}
		return( debug );
	}
	
		public void
	printDebug( Object o )
	{
		for( int i = 0; i < mOutputs.length; ++i )
		{
			mOutputs[ i ].printDebug( o );
		}
	}
	
	
		public void
	close( )
	{
		for( int i = 0; i < mOutputs.length; ++i )
		{
			mOutputs[ i ].close();
		}
	}
}
	

