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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.Notification;
import javax.management.MBeanServerNotification;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.NotCompliantMBeanException;
import javax.management.MBeanRegistration;
import javax.management.NotificationListener;
import javax.management.relation.MBeanServerNotificationFilter;


import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.util.misc.ListUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.client.ProxyFactory;

import com.sun.appserv.management.config.ConfigElement;

import com.sun.enterprise.management.config.AMXConfigImplBase;
import com.sun.enterprise.management.support.OldConfigTypes;
import com.sun.enterprise.management.support.ObjectNames;
import com.sun.enterprise.management.support.BootUtil;
import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.support.TypeInfo;
import com.sun.enterprise.management.support.TypeInfos;
import com.sun.enterprise.management.support.SystemInfoImpl;
import com.sun.enterprise.management.support.AMXDebugSupport;

import com.sun.appserv.management.util.jmx.MBeanProxyHandler;
import com.sun.appserv.management.util.jmx.MBeanServerConnectionSource;
import com.sun.appserv.management.util.jmx.stringifier.MBeanInfoStringifier;
import com.sun.appserv.management.util.jmx.stringifier.NotificationStringifier;


import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
            
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;

/**
	Loads AMX MBeans based on ConfigBeans.
 */
final class AMXLoader implements NotificationListener
{
    private final MBeanServer   mServer;
    private final String        mJMXDomain;
    
    private final ConfigDelegateFactory mDelegateFactory;
		
	private ObjectNames		    mObjectNames;
	
    private final Map<ObjectName,ConfigBean>    mObjectNameToConfigBean;
    
	
        public
    AMXLoader(
        final MBeanServer           server,
        final ConfigDelegateFactory delegateFactory )
        throws Exception
    {
        mServer = server;
        
		new AMXDebugSupport( mServer );
		
        mDelegateFactory    = delegateFactory;
        mJMXDomain  = BootUtil.getInstance().getAMXJMXDomainName();
        
	    mObjectNames    = ObjectNames.getInstance( getAMXJMXDomainName() );
	
        mObjectNameToConfigBean  = Collections.synchronizedMap( new HashMap<ObjectName,ConfigBean>() );
        
		loadSystemInfo( server );
			
		final MBeanServerNotificationFilter filter	=
			new MBeanServerNotificationFilter();
        filter.enableAllObjectNames();
		JMXUtil.listenToMBeanServerDelegate( mServer, this, filter, null );
    }
	
	    protected void
	sdebug( Object o )
	{
	    debug( o );
	    System.out.println( "" + o );
	}
	
		public String
	getAMXJMXDomainName()
	{
		return( BootUtil.getInstance().getAMXJMXDomainName() );
	}
	
        private ConfigContext
    getConfigContext()
    {
        return mDelegateFactory.getConfigContext();
    }
    
        private void
    debug( final Object o )
    {
        AMXDebug.getInstance().getOutput( "AMXLoader" ).println( o );
    }
    
		public void
	handleNotification(
		final Notification	notifIn, 
		final Object		handback) 
	{
		final String	type	= notifIn.getType();
		
		if ( notifIn instanceof MBeanServerNotification )
		{
		    final ObjectName    objectName  = ((MBeanServerNotification)notifIn).getMBeanName();
		    
            if ( type.equals( MBeanServerNotification.REGISTRATION_NOTIFICATION  ) )
            {
                handleMBeanRegistered( objectName );
            }
            else if ( type.equals( MBeanServerNotification.UNREGISTRATION_NOTIFICATION  ) )
            {
                handleMBeanUnregistered( objectName );
            }
		}
	}
	
	
		public void
	handleMBeanRegistered( final ObjectName	objectName )
	{
	}
	
		public void
	handleMBeanUnregistered( final ObjectName	objectName )
	{
	    mObjectNameToConfigBean.remove( objectName );
	}
     
    
    	protected Class
	getImplClass( final String j2eeType)
	{
		final TypeInfo	info	= TypeInfos.getInstance().getInfo( j2eeType );
		assert( info != null );
		
		return( info.getImplClass() );
	}
	
	private static final Class[]    DELEGATE_CONSTRUCTOR_SIG    = new Class[]
	{
	    Delegate.class,
	};
	
		protected Object
	newImpl(
	    final String    j2eeType,
	    final Delegate  delegate )
		throws Exception
	{
		Object	impl	= null;
		
		final Class implClass	= getImplClass( j2eeType );
		
		try
		{
			Constructor constructor	= implClass.getConstructor( DELEGATE_CONSTRUCTOR_SIG );
			
			if ( constructor != null )
			{
				impl = constructor.newInstance( new Object[] { delegate } );
			}
		}
		catch( Exception e )
		{
		    final Throwable rootCause   = ExceptionUtil.getRootCause( e );
			debug( "newImpl: exception creating new impl: "  + e + "\n" +
			    ExceptionUtil.getStackTrace( rootCause ) );
			throw e;
		}
		
		return( impl );
	}
	

        private String
    oldTypeToJ2EEType( final String type )
    {
        return OldConfigTypes.getInstance().oldTypeToJ2EEType( type );
    }
    
        public ObjectName
    loadAMX( final ConfigBean configBean )
        throws Exception
    {
        final ConfigBeanHelperFactory   factory =
            ConfigBeanHelperFactory.getInstance( mDelegateFactory.getConfigContext() );
        
        final ConfigBeanHelper  helper    = factory.getHelper( configBean ); 
            
        final String            xPath     = helper.getXPath();
        final List<String[]>    propsList =
            helper.getAllObjectNameProps( OldConfigTypes.getIgnoreTypes() );
        
        final String[]  firstPair   = propsList.iterator().next();
        final String    type      = firstPair[ 0 ];
        final String    foundName = firstPair[ 1 ];
        if ( type == null ||
            OldConfigTypes.getIgnoreTypes().contains( type ) )
        {
            return null;
        }
        
        final String j2eeType   = oldTypeToJ2EEType( type );
        final String name    = foundName == null ?
                        ObjectNames.getSingletonName( j2eeType ) : foundName;
        String  props   = Util.makeRequiredProps( j2eeType, name);
        //debug( type + "=" + name + " => " + props );
        
        for( final String[] pair : propsList )
        {
            final String ancestorType       = pair[ 0 ];
            final String ancestorNameFound  = pair[ 1 ];
            if ( ! OldConfigTypes.getIgnoreTypes().contains( ancestorType ) )
            {
                final String ancestorJ2EEType   = oldTypeToJ2EEType( ancestorType );
                final String ancestorName    = ancestorNameFound == null ?
                                ObjectNames.getSingletonName( j2eeType ) : ancestorNameFound;
                final String prop        = Util.makeProp( ancestorJ2EEType, ancestorName );
                
                props   = Util.concatenateProps( props, prop );
            }
            
        }
        
       // debug( type + "=" + name + " => " + props );
            
        ObjectName  objectName  = Util.newObjectName( mJMXDomain, props );
        
        final ConfigDelegate delegate    =
            mDelegateFactory.createConfigDelegate( configBean );
            
        final AMXConfigImplBase amx = (AMXConfigImplBase)newImpl( j2eeType, delegate );
        
        mObjectNameToConfigBean.put( objectName, configBean );
        objectName  = mServer.registerMBean( amx, objectName ).getObjectName();
        
        return objectName;
    }
    
    	
	
	    private Map<String,Object>
	getConfigBeanAttributes( final ConfigBean  configBean )
	{
	    final Map<String,Object>    pairs   = new HashMap<String,Object>();
	    
        try
        {
            final ConfigDelegate delegate    =
                mDelegateFactory.createConfigDelegate( configBean );
            
            final MBeanAttributeInfo[]  attrInfos   = delegate.getMBeanInfo().getAttributes();
            for( final MBeanAttributeInfo attrInfo : attrInfos )
            {
                final String    name    = attrInfo.getName();
                Object  value   = null;
                try
                {
                    value   = delegate.getAttribute( name );
                }
                catch( AttributeNotFoundException e )
                {
                    value   = "<NOT FOUND>";
                }
                pairs.put( name, value );
            }
        }
        catch( Exception e )
        {
            pairs.put( "EXCEPTION", e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return pairs;
	}
	
	
	    private void
	addBeanHierarchy(
	    final List<ConfigBean>  configBeans,
	    final ConfigBean        configBean )
	{
        // nulls exist in the array, strange but true
	    if ( configBean != null )
	    {
    	    configBeans.add( configBean );

            final ConfigBean[]  children    = configBean.getAllChildBeans();
            if ( children != null )
            {
                for( final ConfigBean child : children )
                {
                    addBeanHierarchy( configBeans, child );
                }
            }
        }
	}
	
	    private String
	configBeansToString( final List<ConfigBean> configBeans )
	{
        final String[]  xPaths  = new String[ configBeans.size() ];
        int i = 0;
        for( final ConfigBean configBean : configBeans )
        {
            xPaths[ i ] = "" + configBean.getXPath();
            ++i;
        }
        Arrays.sort( xPaths );
        
        final String NEWLINE    = System.getProperty( "line.separator" );
        return StringUtil.toString( NEWLINE, (Object[])xPaths );
	}
	
	
	    private ObjectName
	configBeanToAMX( final ConfigBean  configBean )
	{
	    assert( configBean != null );
	    
	    ObjectName    objectName    = null;
	    try
	    {
    	    objectName  = loadAMX( configBean );
	    }
	    catch( Exception e )
	    {
	       final Throwable rootCause    = ExceptionUtil.getRootCause( e );
	       
	       sdebug( "Exception of type " + rootCause.getClass().getName() + 
	       " trying to create AMXConfig for " +
	        configBean.getXPath() +
	        ": " + rootCause.getMessage()  );
	       sdebug( ExceptionUtil.getStackTrace( rootCause ) );
	    }
        
        return objectName;
	}
	
	    private List<ObjectName>
	configBeansToAMX( final List<ConfigBean> configBeans )
	{
        final List<ObjectName> objectNames   = new ArrayList<ObjectName>();
        for( final ConfigBean configBean : configBeans )
        {
            final ObjectName    objectName  = configBeanToAMX( configBean );
            if ( objectName != null )
            {
                objectNames.add( objectName );
            }
        }
        
        return objectNames;
	}
	
	    public void
	loadAll()
	    throws ConfigException
	{
	    loadDomainRoot();
	    
	    final ConfigContext configContext   = mDelegateFactory.getConfigContext();
	    assert( configContext != null );
	    
        final ConfigBean domainConfigBean = 
            configContext.exactLookup( ServerXPathHelper.XPATH_DOMAIN );
        assert( domainConfigBean != null );
        
        final List<ConfigBean>    configBeans   = new ArrayList<ConfigBean>();
        addBeanHierarchy( configBeans, domainConfigBean );
        debug( configBeansToString( configBeans ) );
        
        // make an AMX MBean for every applicable config bean
        final List<ObjectName> objectNames   = configBeansToAMX( configBeans );
        
        debug( "loadAll: loaded " + objectNames.size() + "MBeans:" );
        debug( CollectionUtil.toString( JMXUtil.objectNamesToStrings( objectNames ), "\n") );
	}

		private void
	loadDomainRoot()
	{
		try
		{
			final TypeInfo	info	=
				TypeInfos.getInstance().getInfo( XTypes.DOMAIN_ROOT );
			final Class			implClass	= info.getImplClass();
				
			final ObjectName	objectName	= mObjectNames.getDomainRootObjectName( );
				
			final Object	impl	= implClass.newInstance();
			mServer.registerMBean( impl, objectName );
		}
		catch( Exception e )
		{
			debug( ExceptionUtil.toString( e ) );
			throw new Error( e );
		}
	}
	
	
		public ObjectName
	loadSystemInfo( final MBeanServer server )
	    throws Exception
	{
		final BootUtil	bootUtil	= BootUtil.getInstance();
		
		final SystemInfoImpl	systemInfo	= new SystemInfoImpl( server, bootUtil );
		
		final ObjectName	tempName	=
		    mObjectNames.getSingletonObjectName( systemInfo.J2EE_TYPE );
		
	    final ObjectName objectName	=
	        mServer.registerMBean( systemInfo, tempName ).getObjectName();
		debug( "loaded SystemInfo as " + objectName );
		return( objectName );
	}
	
	    private ProxyFactory
	getProxyFactory()
	{
	    return ProxyFactory.getInstance( mServer );
	}
	
		public DomainRoot
	getDomainRoot()
	{
		return( getProxyFactory().getDomainRoot() ); 
	}
}








