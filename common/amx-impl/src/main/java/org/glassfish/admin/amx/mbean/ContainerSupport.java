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
package org.glassfish.admin.amx.mbean;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import javax.management.MBeanException;


import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.AMX;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.jmx.JMXUtil;


/**
	Encapsulates support code needed for AMX MBeans implementing {@link Container}.
*/
public final class ContainerSupport
{
    protected static void debug( final String s ) { System.out.println(s); }
    
    private final MBeanServer   mMBeanServer;
    private final ObjectName    mOwnerObjectName;
    private final ConcurrentMap<String, Set<ObjectName>> mContainees = new ConcurrentHashMap<String, Set<ObjectName>>();
    
    public ContainerSupport( final MBeanServer mbeanServer, final ObjectName objectName)
    {
        mMBeanServer    = mbeanServer;
        mOwnerObjectName = objectName;
        
        if ( mMBeanServer == null || objectName == null )
        {
            throw new IllegalArgumentException();
        }
    }
    
        public void
    containeeRegistered( final ObjectName objectName )
    {
        addContainee( objectName );
    }

        public void
    containeeUnregistered( final ObjectName objectName )
    {
        removeContainee( objectName );
    }
    
       public synchronized void
    addContainee( final ObjectName objectName)
    {
        final String j2eeType = Util.getJ2EEType( objectName );
        
        Set<ObjectName> items = mContainees.get(j2eeType);
        if ( items == null )
        {
            items = new HashSet<ObjectName>();
        }
        items.add( objectName );
        mContainees.put( j2eeType, items );
        
        //debug( "addContainee: " + JMXUtil.toString(objectName) + " to " + JMXUtil.toString(mOwnerObjectName));
    }
    
       public synchronized void
    removeContainee( final ObjectName objectName)
    {
        final String j2eeType = Util.getJ2EEType( objectName );
        final Set<ObjectName> items = mContainees.get(j2eeType);
        items.remove( objectName );
        //debug( "removeContainee: " + JMXUtil.toString(objectName) + " from " + JMXUtil.toString(mOwnerObjectName));
    }

    
		public synchronized Set<String>
	getContaineeJ2EETypes()
	{
        // don't just return the internal key set; it might not be Serializable!
        final HashSet<String> items = new HashSet<String>( mContainees.keySet() );
        return items;
	}
    
    
		public synchronized Set<ObjectName>
	getContaineeObjectNameSet( final String j2eeType )
	{
        Set<ObjectName> result = mContainees.get( j2eeType );
        if ( result == null )
        {
            result = Collections.emptySet();
        }
        else
        {
            result = Collections.unmodifiableSet( result );
        }
        
        return result;
	}

		public Set<ObjectName>
	getContaineeObjectNameSet(final Set<String>		j2eeTypes )
	{
        final Set<ObjectName>   result = new HashSet<ObjectName>();
        
        for( final String j2eeType : j2eeTypes )
        {
            result.addAll( getContaineeObjectNameSet( j2eeType ) );
        }
        return result;
    }

    	public final ObjectName
	getContaineeObjectName( final String j2eeType )
	{
		final Set<ObjectName>	children	= getContaineeObjectNameSet( j2eeType );
		
		ObjectName	result	= null;
		
		if ( children.size() == 1 )
		{
			result	= GSetUtil.getSingleton( children );
		}
		else if ( children.size() == 0 )
		{
			//debug( "getContaineeObjectName: no children of type " + j2eeType );
			result	= null;
		}
		else
		{
			throw new UnsupportedOperationException( "getContaineeObjectName" );
		}
				
		return( result );
	}
    
		public final ObjectName
	getNamedChildObjectName( final String	name)
	{
		return( getContaineeObjectName( getChildJ2EEType(), name ) );
	}

	
		
		public final ObjectName
	getContaineeObjectName(
		final String	j2eeType,
		final String	name)
	{
		final Set<ObjectName>	candidates	= getContaineeObjectNameSet( j2eeType );
		
		final Set<ObjectName> matching	=
		    JMXUtil.findByProperty( candidates, AMX.NAME_KEY, name );
		
		final ObjectName	result	= (matching.size() == 0) ?
			null : GSetUtil.getSingleton( matching );
		
		return( result );
	}
	
		public Map<String,Map<String,ObjectName>>
	getMultiContaineeObjectNameMap( final Set<String> j2eeTypesIn )
	{
		// if Set is null, it means all types
		final Set<String>	j2eeTypes	= j2eeTypesIn == null ? getContaineeJ2EETypes() : j2eeTypesIn;
			
		final Map<String,Map<String,ObjectName>>	m	=
		    new HashMap<String,Map<String,ObjectName>>();
		
		for( final String j2eeType : j2eeTypes )
		{
			final Map<String,ObjectName>	nameMap	= getContaineeObjectNameMap( j2eeType );
			if ( nameMap.keySet().size() != 0 )
			{
				m.put( j2eeType, nameMap );
			}
		}
		
		return( m );
	}
	
    static private final Map<String,ObjectName> EMPTY_MAP = Collections.emptyMap();
    
		public final Map<String,ObjectName>
	getContaineeObjectNameMap( final String j2eeType )
	{
		if ( ! getContaineeJ2EETypes().contains( j2eeType ) )
		{
            return EMPTY_MAP;
		}

		final Set<ObjectName>	objectNames	= getContaineeObjectNameSet( j2eeType );
		
		Map<String,ObjectName>	result	= Collections.emptyMap();
		
		if ( objectNames.size() != 0 )
		{
			result	= Util.createObjectNameMap( objectNames );
		}
		assert( result.keySet().size() == objectNames.size() );
		
		return( result );
	}    
		public Set<ObjectName>
	getByNameContaineeObjectNameSet(
		final Set<String>		j2eeTypes,
		final String            name )
	{
        final Set<ObjectName> items = getContaineeObjectNameSet( j2eeTypes );
        
		final Set<ObjectName>		result	= new HashSet<ObjectName>();
        for( final ObjectName objectName : items )
		{
			if ( Util.getName( objectName ).equals( name ) )
			{
				result.add( objectName );
			}
		}
		return( result );
	}
	
    
	/**
		Get the names of all child objects, which may be of more
		than one type.
		
		@return Set containing all child ObjectNames
	*/
		public final Set<ObjectName>
	getContaineeObjectNameSet()
	{
		final String            selfType	= Util.getJ2EEType(mOwnerObjectName);
		final Set<ObjectName>	allChildren	= new HashSet<ObjectName>();
		
		for( final String j2eeType : getContaineeJ2EETypes() )
		{
			final Set<ObjectName>	childrenOfType	= getContaineeObjectNameSet( j2eeType );
			
			allChildren.addAll( childrenOfType );
		}
		
		return( allChildren );
	}
    
		public String
	getChildJ2EEType()
	{
		final Set<String>	types	= getContaineeJ2EETypes();
		if ( types.size() != 1 )
		{
			debug( "getChildJ2EEType failing on: " + mOwnerObjectName + ", got this many children: " + types.size() );
			throw new IllegalArgumentException( SmartStringifier.toString( types ) );
		}
		
		return( GSetUtil.getSingleton( types ) );
	}
    
    
	
	/**
		@return String[] containing names of all children of specified type
	*/
		public final String[]
	getChildNames()
	{
		return( getContaineeNamesOfType( getChildJ2EEType() ) );
	}
	
	/**
		@return String[] containing names of all children of specified type
	*/
		public final String[]
	getContaineeNamesOfType( final String j2eeType )
	{
		final Set<ObjectName> objectNames	= getContaineeObjectNameSet( j2eeType );
				
		return( getNamePropertyValues( objectNames ) );
	}
	
	
	/**
		Get the name of a child MBean, assuming there is only one kind,
		and there is never more than one.
		
		@return ObjectName of child, or null if not found
	*/
		public ObjectName
	getOnlyChildObjectName()
	{
		return( getContaineeObjectName( getChildJ2EEType() ) );
	}
    
    	
	/**
		Extract the value of the "name" key for each ObjectName and place
		it into an array.
		
		@return String[] containing values of "name" property, one for each ObjectName
	*/
		protected final String[]
	getNamePropertyValues( final Set<ObjectName> objectNameSet )
	{
		return( JMXUtil.getKeyProperty( AMX.NAME_KEY, objectNameSet ) );
	}
		


}















