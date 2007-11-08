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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
 
/*
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/support/OldTypesBase.java,v 1.5 2006/03/09 20:30:48 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2006/03/09 20:30:48 $
 */

package com.sun.enterprise.management.support;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.ObjectName;

import com.sun.appserv.management.base.XTypes;


 
/**
	Maps an AMX j2eeType to/from and "old" (8.0) type.
	
	See {@link com.sun.appserv.management.base.XTypes}
 */
abstract class OldTypesBase implements OldTypeToJ2EETypeMapper
{	
	final Map<String,String>	mOldTypeToJ2EETypeMap;
	final Map<String,String>	mJ2EETypeToOldTypeMap;
	
	OldTypesBase()
	{
		mOldTypeToJ2EETypeMap	= new HashMap<String,String>();
		mJ2EETypeToOldTypeMap	= new HashMap<String,String>();
		initMap();
	}
	
		void
	add(
		final String j2eeType,
		final String oldType )
	{
		mOldTypeToJ2EETypeMap.put( oldType, j2eeType );
		mJ2EETypeToOldTypeMap.put( j2eeType, oldType );
	}
	
	/**
		These are delegates that require a config name only, no other keys
		other than category=config
	 */
	abstract void        initMap();
	
	/**
		Determine the j2eeType associated with the ObjectName for an "old" 
		config MBean
		
		@param oldObjectName
	 */
		public String
	oldTypeToJ2EEType(
		final String		oldType,
		final ObjectName	oldObjectName )
	{
		String j2eeType	= null;
		
		if ( oldType.equals( oldObjectName.getKeyProperty( OLD_TYPE_PROP ) ) )
		{
			j2eeType	= oldObjectNameToJ2EEType( oldObjectName );
		}
		else
		{
			j2eeType	= mOldTypeToJ2EETypeMap.get( oldType );
		}
		return( j2eeType );
	}
	
	/**
		Determine the j2eeType associated with the ObjectName for an "old" 
		config MBean
		
		@param oldObjectName
	 */
		public String
	oldObjectNameToJ2EEType( final ObjectName oldObjectName )
	{
		final String	oldType	= oldObjectName.getKeyProperty( OLD_TYPE_PROP );
		if ( oldType == null )
		{
			System.err.println( "no j2eeType for: " + oldObjectName );
			throw new IllegalArgumentException( oldObjectName.toString() );
		}
		
		String	j2eeType	= null;
	
	/*
		if ( oldType.equals( "ssl" ) )
		{
			// ambiguous without parent
			if ( oldObjectName.getKeyProperty( "ssl-client-config" ) != null )
			{
				j2eeType	= XTypes.IIOP_SSL_CLIENT_CONFIG;
			}
			else
			{
				j2eeType	= XTypes.SSL_CONFIG;
			}
		}
		else
	*/
		{
			j2eeType = mOldTypeToJ2EETypeMap.get( oldType );
			if ( j2eeType == null )
			{
				throw new IllegalArgumentException( oldType );
			}
		}
		
		assert( j2eeType != null );
		return( j2eeType );
	}
	
	public final static String OLD_TYPE_PROP	= "type";
	
	/**
		By default, the mapping is done using only the OLD_TYPE_PROP field.
	 */
		public String
	oldTypeToJ2EEType( final ObjectName objectName )
	{
		return oldTypeToJ2EEType( objectName.getKeyProperty( OLD_TYPE_PROP ) );
	}
	
		public String
	oldTypeToJ2EEType( final String oldType )
	{
		return mOldTypeToJ2EETypeMap.get(oldType);
	}
	
		public String
	j2eeTypeToOldType( final String j2eeType )
	{
		return mJ2EETypeToOldTypeMap.get( j2eeType );
	}

		public Map<String,String>
	oldPropsToNewProps( final Map<String,String> oldProps )
	{
		final Map<String,String> newProps = new HashMap<String,String>();
		for( final String oldProp : oldProps.keySet() )
		{
			final String newProp	=mOldTypeToJ2EETypeMap.get( oldProp );
			if ( newProp != null )
			{
				newProps.put( newProp, oldProps.get( oldProp ) );
			}
		}
		return newProps;
	}
}








