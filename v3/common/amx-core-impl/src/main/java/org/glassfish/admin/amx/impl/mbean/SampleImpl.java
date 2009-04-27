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
package org.glassfish.admin.amx.impl.mbean;

import org.glassfish.admin.amx.base.Sample;
import org.glassfish.admin.amx.util.jmx.JMXUtil;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.glassfish.admin.amx.util.CollectionUtil;


/**
	@see Sample
 */
public final class SampleImpl extends AMXImplBase
{
	// all Attributes live in a Map
	private final Map<String,Serializable>	mAttributes;
	private MBeanInfo	mMBeanInfo;
	
		public void
	emitNotifications( final Serializable data, final int numNotifs, final long interval )
	{
		if ( numNotifs <= 0 )
		{
			throw new IllegalArgumentException( "" + numNotifs );
		}
		
		new EmitterThread( data, numNotifs, interval ).start();
	}
		
		public
	SampleImpl(final ObjectName parentObjectName)
	{
        super( parentObjectName, Sample.class );
		mAttributes	= Collections.synchronizedMap( new HashMap<String,Serializable>() );
		mMBeanInfo	= null;
	}
		
		public void
	addAttribute( final String name, final Serializable value )
	{
		if ( name == null || name.length() == 0 )
		{
			throw new IllegalArgumentException( );
		}
		
		mAttributes.put( name, value );
		mMBeanInfo	= null;
	}
	
		public void
	removeAttribute( final String name )
	{
		mAttributes.remove( name );
		mMBeanInfo	= null;
	}
	
	
		public boolean
	getMBeanInfoIsInvariant()
	{
		return( false );
	}
	
		private synchronized MBeanInfo
	createMBeanInfo()
	{
		final MBeanInfo	baseMBeanInfo	= super.getMBeanInfo();
		
		final MBeanAttributeInfo[]	dynamicAttrInfos	=
			new MBeanAttributeInfo[ mAttributes.keySet().size() ];
		final Iterator	iter	= mAttributes.keySet().iterator();
		int	i = 0;
		while ( iter.hasNext() )
		{
			final String	name	= (String)iter.next();
			final Object	value	= mAttributes.get( name );
			final String	type	= value == null ? String.class.getName() : value.getClass().getName();
			
			dynamicAttrInfos[ i ]	= new MBeanAttributeInfo( name, type, "dynamically-added Attribute",
										true, true, false );
			++i;
		}
		
		final MBeanAttributeInfo[]	attrInfos	=
			JMXUtil.mergeMBeanAttributeInfos( dynamicAttrInfos, baseMBeanInfo.getAttributes() );
		
		return( JMXUtil.newMBeanInfo( baseMBeanInfo, attrInfos ) );
	}
	
		public synchronized MBeanInfo
	getMBeanInfo()
	{
		if ( mMBeanInfo == null )
		{
			mMBeanInfo	= createMBeanInfo();
		}
		
		return( mMBeanInfo );
	}
	
		protected Serializable
	getAttributeManually( final String name )
	{
		return( mAttributes.get( name ) );
	}
	
	
		protected void
	setAttributeManually( final Attribute attr )
	{
		mAttributes.put( attr.getName(), Serializable.class.cast( attr.getValue() ) );
	}
	
	
	
	private final class EmitterThread extends Thread
	{
		private final Serializable	mData;
		private final int		mNumNotifs;
		private final long		mIntervalMillis;
	
		public	EmitterThread( final Serializable data, final int numNotifs, final long intervalMillis )
		{
			mData			= data;
			mNumNotifs		= numNotifs;
			mIntervalMillis	= intervalMillis;
		}
	
			public void
		run()
		{
			for( int i = 0; i < mNumNotifs; ++i )
			{
				sendNotification( Sample.SAMPLE_NOTIFICATION_TYPE, Sample.USER_DATA_KEY, mData );
				
				try
				{
					Thread.sleep( mIntervalMillis );
				}
				catch( InterruptedException e )
				{
					break;
				}
			}
		}
	}
	
	
		public void
	uploadBytes( final byte[] bytes )
	{	
		// do nothing; just a bandwidth test
	}
	
	private final static int MEGABYTE	= 1024 * 1024;
		public byte[]
	downloadBytes( final int numBytes )
	{	
		if ( numBytes <0 || numBytes > 10 * MEGABYTE )
		{
			throw new IllegalArgumentException( "Illegal count: " + numBytes );
		}

		final byte[]	bytes	= new byte[ numBytes ];
		
		return( bytes );
	}
    
    public ObjectName[] getAllAMX()
    {
        return getAllAMXProxies();
    }
    
    // will turn into proxies by declaration in the proxy
    public ObjectName[] getAllAMXProxies()
    {
        final Set<ObjectName> all = getDomainRootProxy().getQueryMgr().queryAllObjectNameSet();
        
        return CollectionUtil.toArray( all, ObjectName.class );
    }
}










