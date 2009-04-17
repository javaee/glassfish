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

import org.glassfish.admin.amx.util.AMXDebug;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.jmx.NotificationBuilder;
import org.glassfish.admin.amx.util.Output;

import javax.management.*;
import java.util.*;


/**
	This MBean is used to test the CustomMBean functionality.
	It is not an AMX MBean.
 */
public final class TestDummy
    implements TestDummyMBean, DynamicMBean, MBeanRegistration
{
    private final NotificationBroadcasterSupport    mBroadcaster;
    
	// all Attributes live in a Map
	private final Map<String,Object>	mAttributes;
	private MBeanInfo	                mMBeanInfo;
    private Output  mDebug;
    
    private MBeanServer     mServer;
    private ObjectName      mSelfObjectName;
		
    
		public
	TestDummy( )
	{
		mAttributes	= Collections.synchronizedMap( new HashMap<String,Object>() );
		mMBeanInfo	= null;
		
		mDebug  = AMXDebug.getInstance().getOutput( getDebugID() );
		//AMXDebug.getInstance().setDebug( getDebugID(), true );
		
		mBroadcaster    = new NotificationBroadcasterSupport();
	}
	
	    public String
	getAttr1()
	{
	    return (String)mAttributes.get( "Attr1" );
	}
	
	    public void
	setAttr1( final String value )
	{
	    setAttribute( "Attr1", value);
	}
	
	    public String
	getAttr2()
	{
	    return (String)mAttributes.get( "Attr2" );
	}
	
	    public void
	setAttr2( final String value )
	{
	    setAttribute( "Attr2", value);
	}
	
	    private void
	debug( final Object o )
	{
	    mDebug.println( o );
	}
		
		private String
    getDebugID()
    {
        return this.getClass().getName();
    }
    
		public void
	addAttribute( final String name, final Object value )
	{
		if ( name == null || name.length() == 0 )
		{
		    debug( "Illegal Attribute name: " + name );
			throw new IllegalArgumentException( );
		}
		
		mAttributes.put( name, value );
		mMBeanInfo	= null;
		debug( "added Attribute: " + name );
	}
	
		public void
	removeAttribute( final String name )
	{
		mAttributes.remove( name );
		mMBeanInfo	= null;
		debug( "removed Attribute: " + name );
	}
	
		private synchronized MBeanInfo
	createMBeanInfo()
	{
		debug( "createMBeanInfo");
		final MBeanInfo	baseMBeanInfo	=
		    MBeanInfoConverter.getInstance().convert( TestDummy.class, null );
		
		final List<MBeanAttributeInfo>	dynamicAttrInfos	= new ArrayList<MBeanAttributeInfo>();

		int	i = 0;
		for( final String name : mAttributes.keySet() )
		{
			final Object	value	= mAttributes.get( name );
			final String	type	= value == null ?
			    String.class.getName() : value.getClass().getName();
			
			final MBeanAttributeInfo info	=
			    new MBeanAttributeInfo( name, type, "dynamically-added Attribute",
										true, true, false );
			dynamicAttrInfos.add( info );
		}
		
		final MBeanAttributeInfo[]  dynInfos    = new MBeanAttributeInfo[ dynamicAttrInfos.size() ];
		dynamicAttrInfos.toArray( dynInfos );
		
		final MBeanAttributeInfo[]	attrInfos	=
			JMXUtil.mergeMBeanAttributeInfos( dynInfos, baseMBeanInfo.getAttributes() );
		
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
	
		public Object
	getAttribute( final String name )
	    throws AttributeNotFoundException
	{
	    if ( ! mAttributes.containsKey( name ) )
	    {
	        throw new AttributeNotFoundException( name );
	    }
	    
		return( mAttributes.get( name ) );
	}
	
		public AttributeList
	getAttributes( final String[] names )
	{
	    final AttributeList attrs   = new AttributeList();
	    
	    if ( names != null )
	    {
    	    for( int i = 0; i < names.length; ++i )
    	    {
    	        try
    	        {
    	            final Object result = getAttribute( names[ i ] );
    	            attrs.add( new Attribute( names[ i ], result ) );
    	        }
    	        catch( AttributeNotFoundException e )
    	        {
    	        }
    	    }
	    }
	    
	    return attrs;
	}
	
		public AttributeList
	setAttributes( final AttributeList attrs )
	{
	    throw new RuntimeException(
	        "TestDummy: setAttributes() not yet implemented" );
	}
	
		public Object
	invoke(
	    final String    methodName,
	    final Object[]  args,
	    final String[]  signature )
	{
	    Object      result  = null;
	    
        if(args == null)
            throw new RuntimeException("internal Error -- no args");

        final int   numArgs = args.length;
	    
        if ( "addAttribute".equals( methodName ) && numArgs == 2 )
	    {
	        addAttribute( (String)args[ 0 ], args[ 1 ] );
	    }
	    else if ( "removeAttribute".equals( methodName ) && numArgs == 1 )
	    {
	        removeAttribute( (String)args[ 0 ] );
	    }
	    else if ( numArgs == 2 &&
	        "emitNotifications".equals( methodName ) )
	    {
	        final String    type    = (String)args[0];
	        final int       howMany = (Integer)args[1];
	        result  = emitNotifications( type, howMany );
	    }
	    else
	    {
	        throw new RuntimeException( "invoke: no such method " + methodName );
	    }
	    return result;
	}
	
		public void
	setAttribute( final String name, final Object value )
	{
		debug( "setAttribute" + name + "=" + value );
		
		addAttribute( name, value );
	}
	
		public void
	setAttribute( final Attribute attr )
	{
		setAttribute( attr.getName(), attr.getValue() );
	}
	
		protected ObjectName
	preRegisterModifyName(
		final MBeanServer	server,
		final ObjectName	nameIn )
	{
	    //final String EXTRA  = ",epoch=" + System.currentTimeMillis();
	    final String EXTRA  = "";
	    
		final ObjectName	nameOut	= Util.newObjectName( nameIn.toString() + EXTRA );

		return( nameOut );
	}
	
	
		public ObjectName
	preRegister(
		final MBeanServer	server,
		final ObjectName	nameIn)
		throws Exception
	{
	    mServer = server;
		mSelfObjectName	= preRegisterModifyName( server, nameIn );
		
		return( mSelfObjectName );
	}
	
		public void
	postRegister( Boolean registrationSucceeded )
	{
	}
	
		public void
	preDeregister()
	{
	}
	
		public void
	postDeregister()
	{
	}
	
	
	    public long
	emitNotifications(
	    final String notifType,
	    final int    howMany )
	{
	    final NotificationBuilder builder   =
	        new NotificationBuilder( notifType, mSelfObjectName );
	        
	    final long start    = System.currentTimeMillis();
	    
	    for( int i = 0; i < howMany; ++i )
	    {
	        final Notification  notif = builder.buildNew( "test Notification" );
	        
	        sendNotification( notif );
	    }
	    
	    final long elapsed = System.currentTimeMillis() - start;
	    return elapsed;
	}
	
	    public void
	addNotificationListener(final NotificationListener	listener )
	{
		mBroadcaster.addNotificationListener( listener, null, null );
	}
	
		public void
	addNotificationListener(
		final NotificationListener	listener,
		final NotificationFilter	filter,
		final Object				handback)
	{
		mBroadcaster.addNotificationListener( listener, filter, handback );
	}

		public void
	removeNotificationListener( final NotificationListener listener)
		throws ListenerNotFoundException
	{
 		mBroadcaster.removeNotificationListener( listener );
	}
 
		public void
	removeNotificationListener(
		final NotificationListener	listener,
		final NotificationFilter	filter,
		final Object				handback)
		throws ListenerNotFoundException
	{
		mBroadcaster.removeNotificationListener( listener, filter, handback );
	}

		public void
 	sendNotification( final Notification notification)
 	{
 		mBroadcaster.sendNotification( notification );
 	}
 	
 	    public MBeanNotificationInfo[]
 	getNotificationInfo()
 	{
 	    return new MBeanNotificationInfo[0];
 	}
}














