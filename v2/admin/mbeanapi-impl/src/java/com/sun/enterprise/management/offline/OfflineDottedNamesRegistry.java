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


/**
 */
public final class OfflineDottedNamesRegistry
{
	/**
	    Maps from dotted name prefxi to ObjectName
	 */
	private Map<String,ObjectName>  mPrefixToObjectName;
	
	/**
	    Maps from ObjectName to all dotted name prefixes.
	 */
	private Map<ObjectName,String>  mObjectNameToPrefix;
	
	
	/**
	    The legal attribute names
	 */
	private Map<ObjectName,Set<String>>  mLegalAttributes;
	
		public
	OfflineDottedNamesRegistry()
	{
	    mPrefixToObjectName   = new HashMap<String,ObjectName>();
	    mObjectNameToPrefix   = new HashMap<ObjectName,String>();
	    mLegalAttributes      = new HashMap<ObjectName,Set<String>>();
	}
	
	/**
	    Add a mapping from an ObjectName to its dotted name prefixes.
	    @param objectName   the ObjectName in question
	    @param prefix     one or more prefixes associated with this ObjectName
	    @param legalAttributes  the legal attributes, as found in the MBeanInfo of the ObjectName
	 */
	    public synchronized void
	addMapping(
	    final ObjectName   objectName,
	    final String       prefix,
	    final Set<String>  legalAttributes )
	{
	    if ( objectName == null || prefix == null )
	    {
	        throw new IllegalArgumentException( "null" );
	    }
	    
	    if ( mObjectNameToPrefix.containsKey( objectName ) )
	    {
	        throw new IllegalArgumentException( "Already registered: " + objectName );
	    }
	    
	    mObjectNameToPrefix.put( objectName, prefix );
	    mPrefixToObjectName.put( prefix, objectName );
	    mLegalAttributes.put( objectName, Collections.unmodifiableSet( legalAttributes ) );
	}
	
	    public synchronized void
	removeMapping( final ObjectName objectName )
	{
	    final String  prefix    = mObjectNameToPrefix.get( objectName );
	    if ( prefix != null )
	    {
	        mObjectNameToPrefix.remove( objectName );
	        mLegalAttributes.remove( objectName );
            mPrefixToObjectName.remove( prefix );
	    }
	}
	
	    public String
	getPrefix( final ObjectName objectName )
	{
	    return mObjectNameToPrefix.get( objectName );
	}
	
	    public ObjectName
	getObjectName( final String prefix )
	{
	    return mPrefixToObjectName.get( prefix );
	}
	
	    public Set<String>
	getLegalAttributes( final ObjectName objectName )
	{
	    return mLegalAttributes.get( objectName );
	}
	
	    public Set<ObjectName>
	getObjectNames()
	{
	    final Set<ObjectName>   result  = new HashSet<ObjectName>();
	    
	    result.addAll( mObjectNameToPrefix.keySet() );
	    return result;
	}
	
	    public Set<String>
	getPrefixes()
	{
	    final Set<String>   result  = new HashSet<String>();
	    
	    result.addAll( mObjectNameToPrefix.values() );
	    return result;
	}
}
































