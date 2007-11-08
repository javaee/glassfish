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
 * $Header: /cvs/glassfish/admin/mbeans/src/java/com/sun/enterprise/admin/dottedname/DottedNameServerInfoCache.java,v 1.3 2005/12/25 03:42:04 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:42:04 $
 */
package com.sun.enterprise.admin.dottedname;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;

import javax.management.ObjectName;

/*
	This class maintains a cache of the current server info as an optimization.
	
	The user should call refresh() prior to first use, and when wishing to get
	the current state.
	
 */
public class DottedNameServerInfoCache implements DottedNameServerInfo
{
	final DottedNameServerInfo	mSrc;
	
	Set			mConfigNames;
	Set			mServerNames;
	HashMap		mServerToConfig;
	HashMap		mConfigToServers;
	
		public
	DottedNameServerInfoCache( final DottedNameServerInfo src )
	{
		mSrc	= src;
		
		// underlying source may or may not be ready yet, so it's up
		// to the caller to call refresh() before first use.
		mConfigNames	= Collections.EMPTY_SET;
		mServerNames	= Collections.EMPTY_SET;
		mServerToConfig	= mConfigToServers	= new HashMap();
	}
	
		public synchronized Set
	getConfigNames()
		throws DottedNameServerInfo.UnavailableException
	{
		return( mConfigNames );
	}
	
		public synchronized Set
	getServerNames()
		throws DottedNameServerInfo.UnavailableException
	{
		return( mServerNames );
	}
	
		public synchronized String
	getConfigNameForServer( String serverName )
		throws DottedNameServerInfo.UnavailableException
	{
		return( (String)mServerToConfig.get( serverName ) );
	}
	
		public synchronized String []
	getServerNamesForConfig( String configName )
		throws DottedNameServerInfo.UnavailableException
	{
		return( (String [])mConfigToServers.get( configName ) );
	}
	
		void
	_refresh()
		throws DottedNameServerInfo.UnavailableException
	{
		mConfigNames	= mSrc.getConfigNames();
		mServerNames	= mSrc.getServerNames();
		
		// create mapping from server to config
		Iterator	iter	= mServerNames.iterator();
		while ( iter.hasNext() )
		{
			final String	serverName	= (String)iter.next();
			
			final String	configName	= mSrc.getConfigNameForServer( serverName );
			
			if ( configName != null )
			{
				mServerToConfig.put( serverName, configName );
			}
		}
		
		// create mapping from config to servers
		iter	= mConfigNames.iterator();
		while ( iter.hasNext() )
		{
			final String	configName	= (String)iter.next();
			
			final String [] serverNames	= mSrc.getServerNamesForConfig( configName );
			
			if ( serverNames != null )
			{
				mConfigToServers.put( configName, serverNames );
			}
		}
	}
	
		public synchronized void
	refresh()
	{
		try
		{
			_refresh();
		}
		catch( DottedNameServerInfo.UnavailableException e )
		{
			DottedNameLogger.logException( e );
		}
	}
}









