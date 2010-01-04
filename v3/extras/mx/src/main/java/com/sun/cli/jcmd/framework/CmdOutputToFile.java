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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdOutputToFile.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 

package com.sun.cli.jcmd.framework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.glassfish.admin.amx.util.DebugState;

/**
	Sends all output to a file.
 */
public class CmdOutputToFile implements CmdOutput
{
	final PrintStream	mOutput;
	final DebugState	mDebugState;
	
		public
	CmdOutputToFile( final File theFile )
		throws java.io.IOException
	{
		this( theFile, null );
	}
	
		public
	CmdOutputToFile( final File theFile, final DebugState debugState )
		throws java.io.IOException
	{
		theFile.createNewFile( );
		mOutput	= new PrintStream( new FileOutputStream( theFile, true ) );
		
		mDebugState	= debugState;
	}
	
		public void
	print( Object o )
	{
		mOutput.print( o );
	}
	
		public void
	println( Object o )
	{
		mOutput.println( o );
	}
	
		public void
	printError( Object o )
	{
		mOutput.println( o );
	}
	
		public boolean
	getDebug( )
	{
		return( mDebugState != null && mDebugState.getDebug() );
	}
	
		public void
	printDebug( Object o )
	{
		if ( getDebug() )
		{
			mOutput.println( o );
		}
	}
	
		public void
	close()
	{
		mOutput.close();
	}
};


