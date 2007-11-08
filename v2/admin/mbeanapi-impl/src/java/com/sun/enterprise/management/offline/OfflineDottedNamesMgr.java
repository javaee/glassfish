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
package com.sun.enterprise.management.offline;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.MBeanServer;

import com.sun.appserv.management.base.DottedNames;
import com.sun.appserv.management.base.AMXDebug;


/**
 */
public final class OfflineDottedNamesMgr
{
    private final OfflineDottedNamesRegistry   mRegistry;
    private final OfflineDottedNamePrefixes    mPrefixes;
    private final MBeanServer                  mServer;
    
		public
	OfflineDottedNamesMgr( final MBeanServer server )
	{
	    mServer = server;
	    
	    mRegistry   = new OfflineDottedNamesRegistry();
	    mPrefixes   = OfflineDottedNamePrefixes.getInstance();
	}
	
	    private void
	debug( final Object o )
	{
	    AMXDebug.getInstance().getOutput( "OfflineDottedNamesMgr" ).println( o );
	}
	
	    private String
	getPrefix( final String dottedName )
	{
	    final int   idx = dottedName.lastIndexOf( dottedName );
	    if ( idx <= 0 )
	    {
	        throw new IllegalArgumentException( dottedName );
	    }
	    return dottedName.substring( 0, idx - 1 );
	}
	
	    private String
	getAttrName( final String dottedName )
	{
	    final String    prefix  = getPrefix( dottedName );
	    
	    return dottedName.substring( prefix.length() + 1, dottedName.length() );
	}
	
	    public void
	refresh()
	{
	    // nothing to do--we're always "live"
	}
	
		public Object[]
	dottedNameGet( final String[] names )
	{
	    final Object[]  results = new Object[ names.length ];
	    
	    for( int i = 0; i < names.length; ++i )
	    {
	        try
	        {
	            results[ i ]    = dottedNameGet( names[ i ] );
	        }
	        catch( Exception e )
	        {
	            results[ i ]    = e;
	        }
	    }
        
        return results;
	}
	
	private final String WILD_ALL   = "*";
	
		public Object
	dottedNameGet( final String dottedName )
	{
	    final String        prefix      = getPrefix( dottedName );
	    final ObjectName    objectName  = mRegistry.getObjectName( prefix );
	    
	    if ( objectName == null )
	    {
	        throw new IllegalArgumentException( dottedName );
	    }
	    
	    final String attrName   = getAttrName( dottedName );
	    
	    debug( "dottedNameGet: " + dottedName + ", prefix = " + prefix +
	        "attrName = " + attrName );
	    
	    Object value    = null;
	    try
	    {
	        value   = mServer.getAttribute( objectName, attrName );
	    }
	    catch( Exception e )
	    {
	        value   = e;
	    }
	    debug( "dottedNameGet: " + dottedName + " = " + value );
		return value;
	}
	
		public Object[]
	dottedNameList( final String[] names )
	{
		return new String[0];
	}
	
		public Object[]
	dottedNameSet( final String[] nameValuePairs )
	{
	    for( final String pair : nameValuePairs )
	    {
	    }
	    throw new UnsupportedOperationException( "dottedNameSet" );
	}
	
}
































