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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/ConfigFactory.java,v 1.18 2007/05/05 05:23:17 tcfujii Exp $
 * $Revision: 1.18 $
 * $Date: 2007/05/05 05:23:17 $
 */
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.lang.reflect.Method;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.InstanceNotFoundException;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.QueryMgr;

import com.sun.appserv.management.DomainRoot;

import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.misc.Output;

import com.sun.appserv.management.util.jmx.MBeanRegistrationListener;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.CommonConfigKeys;

import com.sun.enterprise.management.support.ParamNameMapper;
import com.sun.enterprise.management.support.LoaderMBean;
/*
import com.sun.enterprise.management.support.OldConfigTypes;
import com.sun.enterprise.management.support.OldTypeToJ2EETypeMapper;
import com.sun.enterprise.management.support.oldconfig.OldConfigProxies;
import com.sun.enterprise.management.support.oldconfig.OldResourcesMBean;
*/


import com.sun.appserv.management.base.AMXDebug;


import com.sun.enterprise.util.Issues;

/**
	Factory for creating and removing configs.
 */
public class ConfigFactory
{
	private final ConfigFactoryCallback		mCallbacks;
	private ParamNameMapper	mParamNameMapper = null;
    private final Output    mDebug;
    
	/**
		Whenever a name is required during creation of a new config item,
		this key is what is used within the Map. Usually, however, the name
		is passed as the first argument in createXXX().
	 */
    protected final static String CONFIG_NAME_KEY = "Name";
    
		public 
	ConfigFactory( final ConfigFactoryCallback	callbacks) 
	{
		mCallbacks	= callbacks;
		
		mDebug  = AMXDebug.getInstance().getOutput( getClass().getName() );
	}
	
	    protected final MBeanServer
	 getMBeanServer()
	 {
	    return getCallbacks().getMBeanServer();
	 }
	
	 	protected final void
	debug(final Object o)
	{
        mDebug.println( o );
	}
	
	
		protected final ConfigFactoryCallback
	getCallbacks()
	{
		return( mCallbacks );
	}
	
	
		protected static void
	putNonNull(
		final Map<String,String>		m,
		final String	key,
		final Object	value)
	{
		if ( value != null )
		{
			m.put( key, "" + value );
		}
	}
	
		protected final String
	getConfigName()
	{
		return( getCallbacks().getConfigName() );
	}
	
	    protected final String
    getContainerName()
    {
        final String name   = getFactoryContainer().getName();
        if ( name == null )
        {
            throw new IllegalArgumentException();
        }
        return name;
    }
    
		protected final Logger
	getLogger()
	{
		return( getCallbacks().getLogger() );
	}

		protected final DomainRoot
	getDomainRoot()
	{
		return( getCallbacks().getDomainRoot() );
	}
	
		protected final QueryMgr
	getQueryMgr()
	{
		return( getDomainRoot().getQueryMgr() );
	}
	
		protected final DomainConfig
	getDomainConfig()
	{
		return( getDomainRoot().getDomainConfig() );
	}
	
		protected final Container
	getFactoryContainer()
	{
		return( getCallbacks().getFactoryContainer() );
	}
	
/*
		protected final OldConfigProxies
	getOldConfigProxies()
	{
		return( getCallbacks().getOldConfigProxies() );
	}
*/
	
	    protected final void
	remove(  )
	{
	    throw new RuntimeException( "form 'remove()' no longer supported" );
	}
	
        protected WaitForUnregistrationListener
    newWaitForUnregistrationListener( final ObjectName objectName )
    {
        WaitForUnregistrationListener   listener = null;
        try
        {
            final String name =
                "WaitForUnregistrationListener for " + JMXUtil.toString(objectName);
            
            listener    = new WaitForUnregistrationListener( name, objectName );
            listener.startListening();
            return listener;
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
    
    protected final class WaitForUnregistrationListener extends MBeanRegistrationListener
    {
        private final ObjectName        mTarget;
        private final long              mStart;
        private volatile long           mElapsed;
        private final CountDownLatch    mLatch;
        private final boolean           mWasUnregistered = false;

        private WaitForUnregistrationListener(
            final String name,
            final ObjectName objectName )
            throws InstanceNotFoundException, java.io.IOException
        {
            super( name, getMBeanServer(), null );
            mTarget     = objectName;
            mLatch      = new CountDownLatch(1);
            mStart      = System.currentTimeMillis();
            mElapsed    = -1;
        }
        
        public ObjectName   getTarget()     { return mTarget; }
        public long         getElapsed()    { return mElapsed; }
        protected void mbeanRegistered( final ObjectName objectName ) {}
        
            protected void
        mbeanUnregistered( final ObjectName objectName )
        {
            if ( mTarget.equals( objectName ) )
            {
                mElapsed    = System.currentTimeMillis() - mStart;
                mLatch.countDown();
                cleanup();
            }
        }
        
            private boolean
        await( final long timeoutMillis )
        {
            try
            {
                mLatch.await( timeoutMillis, TimeUnit.MILLISECONDS );
            }
            catch ( final InterruptedException e )
            {
            }
            
            final boolean   gotRegistration = mElapsed >= 0;
            if ( ! gotRegistration )
            {
                mElapsed    = timeoutMillis;
            }
            
            return gotRegistration;
        }
    
            public void
         waitForUnregistration( )
         {
            final long MAX_WAIT_MILLIS  = 15 * 1000;    // 15 seconds, very generous
            
            final boolean success = await( MAX_WAIT_MILLIS );
            
            if ( ! success )
            {
                throw new RuntimeException( "MBean " + JMXUtil.toString( getTarget() ) +
                " failed to unregister in " + getElapsed() + " ms, " + 
                    "isRegistered=" + getMBeanServer().isRegistered( getTarget() ) );
            }
            
            if ( getElapsed() > 1000 )
            {
               getLogger().info( "ConfigFactory.waitForUnregistration: MBean " +
                    JMXUtil.toString( getTarget() ) + " unregistered in " + getElapsed() + " ms" );
            }
         }
     }
     
	/** 
	    Remove the config represented by the AMX MBean with the
	    specified ObjectName.
	 */
		public final void
	remove( final ObjectName objectName )
	{
		if ( objectName == null )
		{
			throw new RuntimeException( new InstanceNotFoundException() );
		}
		
		final WaitForUnregistrationListener l  =
		    newWaitForUnregistrationListener( objectName );
		
		internalRemove( objectName );
		
		// do not return to caller until the MBean actually is unregistered!
		l.waitForUnregistration();
		
		getCallbacks().sendConfigRemovedNotification( objectName );
	}
	
		protected void
	removeByName( final String	name )
	{
		throw new UnsupportedOperationException( "removeByNameInternal" );
	}
	
		protected void
	internalRemove( final ObjectName	objectName )
	{
		removeByName( Util.getName( objectName ) );
	}
	
	/**
		Get the names of parameters that don't map via the default algorithms.
	 */
		protected Map<String,String>
	getParamNameOverrides()
	{
		// none, by default
		return( Collections.emptyMap() );
	}


		protected synchronized ParamNameMapper
	getParamNameMapper()
	{
		if ( mParamNameMapper == null )
		{
			mParamNameMapper	=
				new ParamNameMapper( getParamNameOverrides() );
		}
		
		return( mParamNameMapper );
	}
	
		protected boolean
	getBooleanOption( final Map<String,String> m, final String key )
	{
		boolean	value	= false;
		final Object	obj	= (m == null) ? null : m.get( key );
		if ( obj != null )
		{
			if ( obj instanceof Boolean )
			{
				value	= ((Boolean)obj).booleanValue();
			}
			else if ( obj instanceof String )
			{
				value	= Boolean.valueOf( (String)obj ).booleanValue();
			}
			else
			{
				throw new IllegalArgumentException( "Illegal value for Boolean " + key );
			}
			
		}
		return( value );
	}
	
		protected boolean
	requireValidReferences( final Map<String,String> options )
	{
		return ! getBooleanOption( options, CommonConfigKeys.IGNORE_MISSING_REFERENCES_KEY );
	}
	
	
	private final static Class[]	ATTRS_ONLY_SIG	=
		new Class[] { AttributeList.class };
	private final static Class[]	ATTRS_AND_PROPS_SIG	=
		new Class[] { AttributeList.class, Properties.class };
		
	
		protected final Method
	findAnyMethod( String methodName, final Class[] sig )
	{
		Method	m	= null;
		try
		{
			m	= this.getClass().getDeclaredMethod( methodName, sig );
		}
		catch( NoSuchMethodException e )
		{
			// ok, doesn't exist
		}
		
		return( m );
	}

		protected final ObjectName
	createChild( final Map<String,String>		params )
	{
		return( createNamedChild( null, params ) );
	}
	
	/**
		Create an "old" child MBean of the specified name using the specified parameters,
		and then create a corresponding AMX MBean.
		The parameters need not include the name; it will be added.
		<p>
		The parameters are first translated to an AttributeList and Properties. See
		translateParams() for details.
		
		@return the ObjectName of the new AMX MBean
		
	 */
		protected final ObjectName
	createNamedChild(
		final String	name,
		final Map<String,String>		params )
	{
		debug( "createNamedChild: " + name + ":\n{\n" + stringify( params ) + "\n}");
		
		final AttributeList		attrs	= new AttributeList();
		final Properties		props	= new Properties();
		
		translateParams( params, attrs, props );
		
		debug( "createNamedChild: translated attrs:\n{\n" + stringify( attrs ) + "\n}");
		if ( props.keySet().size() != 0 )
		{
		    debug( "createNamedChild: translated props:\n" + stringify( props ) );
		}
		
		ObjectName	oldObjectName		= null;
		boolean		propertiesHandled	= false;

		if ( findAnyMethod( "createOldChildConfig", ATTRS_AND_PROPS_SIG ) != null )
		{
		    debug( "createNamedChild: calling createOldChildConfig using attrs\n" +
		        stringify( attrs ) + "\nand props \n" + stringify( props ) );
		        
			oldObjectName	= createOldChildConfig( attrs, props );
			propertiesHandled = true;
		}
		else if ( findAnyMethod( "createOldChildConfig", ATTRS_ONLY_SIG ) != null )
		{
		    debug( "createNamedChild: calling createOldChildConfig using attrs\n" +
		        stringify( attrs ));
		        
			oldObjectName	= createOldChildConfig( attrs );
		}
		else
		{
			throw new UnsupportedOperationException( "createOldChildConfig" );
		}
		
		return finish( oldObjectName, propertiesHandled ? null : props );
	}
	   
	   
	     protected final ObjectName
	 finish(
	    final ObjectName oldObjectName,
	    final Properties props)
	 {
		assert( oldObjectName != null );
		debug( "createNamedChild: created: " + StringUtil.quote( oldObjectName.toString() ) );
		
		if ( ! getMBeanServer().isRegistered( oldObjectName ) )
		{
			throw new RuntimeException( new InstanceNotFoundException( oldObjectName.toString() ) );
		}
		
		// need the new AMX name to set properties
		final ObjectName	amxName	= getCallbacks().getLoader().sync( oldObjectName );
		debug( "createNamedChild: amx object name: " + StringUtil.quote( amxName.toString() ) );

		if ( props != null && props.size() > 0)
		{
		    debug( "Setting properties: " + stringify( props ) );
			setAllProperties( amxName, props );
		}

        // ensure that all sub-elements are also present
        getCallbacks().getLoader().waitAll();
        
		getCallbacks().sendConfigCreatedNotification( amxName );
		return( amxName );
	 }
	
	
	protected static final Set<String> NO_OPTIONAL_KEYS	= Collections.emptySet();
	/**
		  By default, assume there are no optional keys.
	 */
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( NO_OPTIONAL_KEYS );
	}
	
		protected void
	checkLegalOptions( final Map<String,String> options )
	{
		if ( options != null )
		{
			final Set<String>	legalKeys	= getLegalOptionalCreateKeys();
			if ( legalKeys != null )
			{
			    debug( "Legal optional keys: " + stringify( legalKeys ) );
			}
			if ( legalKeys != null )
			{
				// remove all legal Attribute keys
				final Map<String,String> remaining	= new HashMap<String,String>( options );
				remaining.keySet().removeAll( legalKeys );
				
				final HashMap<String,String>	illegal	= new HashMap<String,String>();
				
				// find all non-properties; these are illegal
				final Iterator	iter	= remaining.keySet().iterator();
				while ( iter.hasNext() )
				{
					final String	key	= (String)iter.next();
					if ( ! key.startsWith( PropertiesAccess.PROPERTY_PREFIX ) )
					{
						illegal.put( key, remaining.get( key ) );
					}
				}

				if ( illegal.size() != 0 )
				{
				    final String msg    = "Illegal optional keys: " +
						MapUtil.toString( illegal, ",");
				
				    debug( msg );
					throw new IllegalArgumentException( msg );
				}
			}
		}
		else
		{
			// no options, so they're legal!
		}
	}

	/**
		Initialize a Map of parameters consisting of attributes and properties.
		First the optional parameters are inserted.
		Then the name/value pairs in the Object[] are added, possibly overwriting any
		values that were redundantly specified in the optional map.
		
		@param name
		@param required name/value pairings; even ones are keys, odd ones are values
		@param optional	additional name/value pairings
	 */
		protected final Map<String,String>
	initParams(
		final String	name,
		final String[]	required,
		final Map<String,String>		optional )
	{
		checkLegalOptions( optional );
		
		final Map<String,String>	m	= initParams( required, optional );

		if ( name == null || name.length() == 0 )
		{
			throw new IllegalArgumentException( "Illegal to have null or empty name" );
		}
		
		m.put( CONFIG_NAME_KEY, name );
		return( m );
	}
	
	/**
		Validate parameters.  The params Map contains all parameters, including
		Attributes and properties as 
		
		@param params
	 */
		protected final void
	validateParams( final Map<String,?> params )
	{
		for( final String name : params.keySet() )
		{
			final Object	value	= params.get( name );
			
			validateParam( name, value );
		}
	}
	
	/**
		Validate a parameter to a create() or createAbc() method.
		Subclasses should verify that the parameter name is legal,
		and optionally validate its value as well.
		
		@param name
		@param value
	 */
		protected final void
	validateParam(
		final String	name,
		final Object	value )
	{
		if ( name == null || name.length() == 0 )
		{
			throw new IllegalArgumentException( "parameter name must be non-null and non-empty" );
		}
		
		if ( name.startsWith( PropertiesAccess.PROPERTY_PREFIX ) )
		{
			if ( ! (value instanceof String) )
			{
				throw new IllegalArgumentException( "Property value must be string: " +
					name + ", class =" + value.getClass().getName() );
			}
		}
	}
	
	
	/**
		Initalize parameters, no name supplied.
	 */
		protected final Map<String,String>
	initParams(
		final String[]	required,
		final Map<String,String> optionalIn )
	{
	    // guarantee that all arguments are Strings;
	    // clients can be using old version of the interfaces and/or non-generic
	    // versions of Map
	    final Map<String,String> optional = MapUtil.toStringStringMap( optionalIn );
	    
		final Map<String,String>	m	= new HashMap<String,String>();
		
		if ( optional != null )
		{
			m.putAll( optional );
			m.remove( CommonConfigKeys.IGNORE_MISSING_REFERENCES_KEY );
		}
		if ( required != null )
		{
			m.putAll( MapUtil.newMap( required ) );
		}
		
		validateParams( m );
		
		return( m );
	}
	
	/**
		Initalize parameters, no name supplied.
	 */
		protected final Map<String,String>
	initParams( final Map<String,String>		optional )
	{
		return( initParams( (String[])null, optional ) );
	}
	
	
	/**
		Translate the Map of parameters to Attributes and Properties.  AttibuteNames
		are mapped to the underlying names.  Property names are not mapped, but the
		PROPERTY_PREFIX is stripped.
		
		@param paramsToTranslate
		@param attrsOut
		@param propsOut
	 */
		protected final void
	translateParams(
		final Map<String,String>			paramsToTranslate,
		final AttributeList	attrsOut,
		final Properties	propsOut )
	{
		final ParamNameMapper	mapper	= getParamNameMapper();
		
		final Iterator	iter	= paramsToTranslate.keySet().iterator();
		final String	propertyPrefix	= PropertiesAccess.PROPERTY_PREFIX;
		while( iter.hasNext() )
		{
			final String	key	= (String)iter.next();
			final Object	value	= paramsToTranslate.get( key );
			
			if ( key.startsWith( propertyPrefix ) )
			{
				final String	name	=
					key.substring( propertyPrefix.length(), key.length() );
					
				propsOut.put( name, value );
			}
			else
			{
				final String	translatedName	= mapper.mangleAttributeName( key );
				
				final Attribute	attr	= new Attribute( translatedName, value );
				
				attrsOut.add( attr );
			}
		}
	}

	/**
		Given a set of translated Attributes, create the appropriate type of child
		and return its "old" config ObjectName
		
		@param	translatedAttrs	
		@return ObjectName of newly-created child
	 */
		protected ObjectName
	createOldChildConfig(
		final AttributeList translatedAttrs )
	{
		throw new UnsupportedOperationException( "createOldChildConfig( AttributeList )" );
	}
	
	/**
		Given a set of translated Attributes, create the appropriate type of child
		and return its "old" config ObjectName
		
		@param	translatedAttrs	
		@param	props	Properties to also use	
		@return ObjectName of newly-created child
	 */
		protected ObjectName
	createOldChildConfig(
		final AttributeList translatedAttrs,
		final Properties	props )
	{
		throw new UnsupportedOperationException( "createOldChildConfig( AttributeList, Properties)" );
	}
	
		protected final void
	trace( Object o )
	{
	    debug( o );
	}
	
		protected String
	stringify( Object o )
	{
		return( SmartStringifier.toString( o ) );
	}
	
	/**
		Given a set of translated Attributes, create the appropriate type of child
		and return its "old" config ObjectName

		@param	oldChildType
		@param	translatedAttrs	
		@return ObjectName of newly-created child
	 */
		protected ObjectName
	createOldChildByType( 
		final String		oldChildType, 
		final AttributeList translatedAttrs )
	{
		throw new UnsupportedOperationException( "createOldChildByType( String, AttributeList )" );
	}
	
	/**
		Given a set of translated Attributes, create the appropriate type of child
		and return its "old" config ObjectName

		@param	oldChildType
		@param	translatedAttrs	
		@param	props	Properties to also use	
		@return ObjectName of newly-created child
	 */
		protected ObjectName
	createOldChildByType(
		final String		oldChildType, 
		final AttributeList translatedAttrs,
		final Properties	props )
	{
		throw new UnsupportedOperationException( "createOldChildByType( String, AttributeList, Properties )" );
	}
	
		private static final Class[] TYPE_AND_ATTRS_AND_PROPS_SIG = 
		new Class[] { String.class, AttributeList.class, Properties.class };

	private static final Class[] TYPE_AND_ATTRS_SIG = 
		new Class[] { String.class, AttributeList.class };

	
		protected final ObjectName
	createChildByType(
		final String childJ2EEType,
		final Map<String,String> params )
	{
        final String msg ="ConfigFactory.createChildByType: not implemented";
        Issues.getAMXIssues().notDone( msg );
        throw new RuntimeException( msg );
    }
    
/*
		protected final ObjectName
	createChildByType(
		final String childJ2EEType,
		final Map<String,String> params )
	{
		//assert getChildInfos().contains (childJ2EEType) : 
			//childJ2EEType + " is not a valid child of " + getSelfJ2EEType ();

		final String oldType = getOldTypeToJ2EETypeMapper().j2eeTypeToOldType( childJ2EEType );

		assert oldType != null;

		final Properties	props	= new Properties();
		final AttributeList	attrs	= new AttributeList();

		translateParams( params, attrs, props );

		ObjectName	oldObjectName		= null;
		boolean		propertiesHandled	= false;

		if ( findAnyMethod( "createOldChildByType", TYPE_AND_ATTRS_AND_PROPS_SIG ) != null )
		{
			oldObjectName	= createOldChildByType( oldType, attrs, props );
			propertiesHandled = true;
		}
		else if ( findAnyMethod( "createOldChildByType", TYPE_AND_ATTRS_SIG ) != null )
		{
			oldObjectName	= createOldChildByType( oldType, attrs );
		}
		else if ( findAnyMethod( "createOldChildByType", ATTRS_ONLY_SIG ) != null )
		{
			oldObjectName	= createOldChildConfig( attrs );
		}
		else
		{
			throw new UnsupportedOperationException( "createOldChildByType" );
		}
		
		assert( oldObjectName != null );
		debug( "createChildByType: oldObjectName: " + StringUtil.quote( oldObjectName.toString() ) );

		// need the new AMX name to set properties
		final ObjectName	amxName	= getCallbacks().getLoader().sync( oldObjectName );
		debug( "createChildByType: amx object name: " + StringUtil.quote( amxName.toString() ) );

		if ( props.size() > 0 && !propertiesHandled )
		{
			setAllProperties( amxName, props );
		}

		getCallbacks().sendConfigCreatedNotification( amxName );
		return( amxName );
	}
*/
	


		protected final void
	setAllProperties(
		final ObjectName	amxObjectName,
		final Properties	props )
	{
		final PropertiesAccess	proxy	= PropertiesAccess.class.cast(
		    getCallbacks().getProxyFactory().getProxy( amxObjectName, AMX.class) );
		
		setAllProperties( proxy, props );
	}
	
	/**
		For each property in props , call setPropertyValue()
	 */
		protected final void
	setAllProperties(
		final PropertiesAccess	target,
		final Properties		props )
	{
		final Iterator	iter	= props.keySet().iterator();
		
		while ( iter.hasNext() )
		{
			final String	key	= (String)iter.next();
			
			target.setPropertyValue( key, (String)props.get( key ) );
		}
	}


/*
		protected OldTypeToJ2EETypeMapper 
	getOldTypeToJ2EETypeMapper()
	{
		return OldConfigTypes.getInstance();
	}
*/
	

        
		protected ObjectName
	syncNewAMXMBeanWithOld(ObjectName oldObjectName)
	{
		final ObjectName amxName = getCallbacks().getLoader().sync( oldObjectName );
		
		getCallbacks().sendConfigCreatedNotification( amxName );
		return amxName;
	}
	
	
/*
		final OldResourcesMBean
	getOldResourcesMBean()
	{
		return( getOldConfigProxies().getOldResourcesMBean( ) );
	}
*/
	
		protected static String
	quote( Object o )
	{
		return( StringUtil.quote( o.toString() ) );
	}
	
	
	
		protected void
	checkNonEmptyString( final String s, final String name )
	{
		if (  s == null ||  s.length() == 0 )
		{
			throw new IllegalArgumentException( "Parameter may not be null or empty: " +
				quote( name ) );
		}
	}
		
	    protected final Object
	getValue( final Map<String,String> m, final String key )
	{
	    return (m == null) ? null : m.get( key );
	}
	
	    protected final String
	getString( final Map<String,String> m, final String key )
	{
	    return (String)getValue( m, key );
	}
	
	    protected final Boolean
	getBoolean(
	    final Map<String,String> m,
	    final String             key,
	    final Boolean            defaultValue )
	{
	    final Object    value   = getValue( m, key );
	    
	    return (value == null) ? defaultValue : Boolean.valueOf( "" + value );
	}
	
	
		protected boolean
	sleepMillis( final long millis )
	{
		boolean	interrupted	= false;
		
		try
		{
			Thread.sleep( millis );
		}
		catch( InterruptedException e )
		{
			Thread.interrupted();
			interrupted	= true;
		}
		
		return interrupted;
	}
	
	    protected AMX
	requireItem(
	    final String j2eeType,
	    final String name )
	{
	    final AMX item  = getFactoryContainer().getContainee( j2eeType, name );
	    if ( item == null )
	    {
	        throw new IllegalArgumentException( j2eeType + "=" + name );
	    }
	    return item;
	}
}



























