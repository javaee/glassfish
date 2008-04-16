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
package com.sun.appserv.management.helper;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.BulkAccess;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.Util;

import javax.management.ObjectName;
import java.util.HashSet;
import java.util.Set;

/**
	Base class for Helpers, useable alone as well.
 */
public class Helper
{
	protected final DomainRoot	mDomainRoot;
	protected final QueryMgr		mQueryMgr;
	protected final BulkAccess	mBulkAccess;
	
		public
	Helper( final AMX	proxy )
	{
		mDomainRoot	= proxy.getDomainRoot();
		mQueryMgr		= mDomainRoot.getQueryMgr();
		mBulkAccess	= mDomainRoot.getBulkAccess();
	}
	
		public DomainRoot
	getDomainRoot()
	{
		return( mDomainRoot );
	}
	
		protected <T extends AMX> Set<T>
	propsQuery( final String	props )
	{
		final Set<T>		results	= mQueryMgr.queryPropsSet( props );
		return( results );
	}
	
		protected  <T extends AMX> Set<T>
	propsQuery(
		final String	props1,
		final String	props2 )
	{
		final String	props	= Util.concatenateProps( props1, props2 );
		
		return( propsQuery( props ) );
	}
	
	/**
		Filter ObjectNames based on the value of a particular Attribute.  The value
		may be null or anything else.  This is essentially a crude form of using
		the QueryMgr. A value which is a Class object succeeds if the result is
		an object whose class is assignable to the specfied class. Typically this
		is used to detect a thrown Exception.
		<p>
		For example, to select all MBeans which have a [bB]oolean Attribute named "Enabled",
		which is set to true, call:
		<pre>filterByAttributeValue( objectNameSet, "Enabled", Boolean.TRUE)</pre>
		<p>
		The query for the Attribute value is performed as a bulk operation; thus this
		routine may be used with confidence that it is fast.
		
		
		@param objectNameSet	Set of ObjectName
		@param attributeName	
		@param valueToMatch	an Object whose value must be null, or equals() to the result
		@return Set of ObjectName which have Enabled flag matching
	 */
		public Set<ObjectName>
	filterByAttributeValue(
		final Set<ObjectName>	objectNameSet,
		final String	        attributeName,
		final Object	        valueToMatch )
	{
		final ObjectName[]	objectNames	= new ObjectName[ objectNameSet.size() ];
		objectNameSet.toArray( objectNames );
		
		final Object[]	values	= mBulkAccess.bulkGetAttribute( objectNames, attributeName );
		
		final Set<ObjectName>	filtered	= new HashSet<ObjectName>();
		for( int i = 0; i < values.length; ++i )
		{
			final Object	idxValue	= values[ i ];
			
			boolean	matches	= false;
			
			if ( valueToMatch == null && idxValue == null )
			{
				matches	= true;
			}
			else if ( valueToMatch instanceof Class &&
					((Class<?>)valueToMatch).isAssignableFrom( idxValue.getClass() ) )
			{
				matches	= true;
			}
			else if ( valueToMatch != null && valueToMatch.equals( idxValue ) )
			{
				matches	= true;
			}
			else
			{
				// no match
			}
			
			if ( matches )
			{
				filtered.add( objectNames[ i ] );
			}
		}
		
		return( filtered );
	}
	
}


