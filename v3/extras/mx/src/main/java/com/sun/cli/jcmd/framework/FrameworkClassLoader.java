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

package com.sun.cli.jcmd.framework;

import java.lang.ClassLoader;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

import com.sun.cli.jcmd.framework.CmdOutput;


/**
	Classload to support additional jars and folders, set up as the system
	classloader.
 */
public final class FrameworkClassLoader extends URLClassLoader
{
	final List<URL>		mSources;
	CmdOutput			mCmdOutput;
	
	static private FrameworkClassLoader	INSTANCE	= null;
	
		private synchronized static void
	setInstance( FrameworkClassLoader	instance )
	{
		// the first one that's created will be forever the value of 'INSTANCE'
		// we can't create statically, because we must wait for the system to create us
		if ( INSTANCE == null )
		{
			INSTANCE	= instance;
		}
		else
		{
			System.out.println( "WARNING: more than one FrameworkClassLoader is being created." );
		}
	}
	
		public static synchronized FrameworkClassLoader
	getInstance()
	{
		return( INSTANCE );
	}
	
		public
	FrameworkClassLoader( ClassLoader parent )
		throws java.net.MalformedURLException
	{
		super( new URL[0], parent );
		
		mSources		= new ArrayList<URL>();
		mCmdOutput		= new CmdOutputNull();
		
		setInstance( this );
		Thread.currentThread().setContextClassLoader( this );
	}
	
		public void
	setCmdOutput( CmdOutput	cmdOutput )
	{
		if ( cmdOutput == null )
		{
			throw new IllegalArgumentException( "CmdOutput must not be null" );
		}

		mCmdOutput	= cmdOutput;
	}
	
		public void
	addURL( final URL	url )
	{
		if ( mSources.indexOf( url ) < 0 )
		{
			super.addURL( url );
			mSources.add( url );
			mCmdOutput.printDebug( "FrameworkClassLoader.addURL: " + url );
		}
		else
		{
			throw new IllegalArgumentException( "Item \"" + url + "\" already in list" );
		}
	}
}

