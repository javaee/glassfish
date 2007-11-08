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
package com.sun.enterprise.management.config;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;

import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ThrowableMapper;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.RefConfig;
import com.sun.appserv.management.config.ResourceConfig;
import com.sun.appserv.management.config.ModuleConfig;
import com.sun.appserv.management.config.RefConfigReferent;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.ResourceRefConfigReferent;
import com.sun.appserv.management.config.DeployedItemRefConfigReferent;
import com.sun.appserv.management.config.ClusterRefConfigReferent;
import com.sun.appserv.management.config.ServerRefConfigReferent;
import com.sun.appserv.management.config.Description;
import com.sun.appserv.management.config.Enabled;
import com.sun.appserv.management.config.ObjectType;

import com.sun.appserv.management.config.DeployedItemRefConfigCR;
import com.sun.appserv.management.config.ServerRefConfigCR;
import com.sun.appserv.management.config.ClusterRefConfigCR;
import com.sun.appserv.management.config.ResourceRefConfigCR;
import com.sun.appserv.management.config.SystemPropertiesAccess;

import com.sun.appserv.management.base.XTypesMapper;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.helper.RefHelper;

import com.sun.enterprise.management.support.AMXImplBase;
import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.support.DelegateBase;
import com.sun.enterprise.management.support.AMXAttributeNameMapper;

import com.sun.enterprise.management.config.ConfigFactory;
import com.sun.enterprise.management.support.oldconfig.OldProperties;
import com.sun.enterprise.management.support.oldconfig.OldSystemProperties;

/**
	Base class from which all AMX Config MBeans should derive (but not "must").
	<p>
 */
public class AMXConfigImplBase extends AMXImplBase
	implements AMXConfig
{
		protected
	AMXConfigImplBase( Delegate	delegate )
	{
		super( delegate );
	}
	
	    protected boolean
	supportsProperties()
	{
	    return PropertiesAccess.class.isAssignableFrom( getInterface() );
	}
	
	    protected boolean
	supportsSystemProperties()
	{
	    return SystemPropertiesAccess.class.isAssignableFrom( getInterface() );
	}
	
	/**
	    Verify that the AMX support for system properties is the same
	    as its Delegate.
	 */
	    protected final void
	checkPropertiesAccessSupport()
	{
        if ( getDelegate() != null)
        {
            final boolean   delegateHasProperties   = delegateSupportsProperties();
            
            if ( delegateHasProperties != supportsProperties() )
            {
                final String msg = getJ2EEType() + ": " +
                    "delegateSupportsProperties=" + delegateHasProperties +
                    ", but supportsProperties=" + supportsProperties();
                // implementation error
                logWarning( msg );
                throw new Error( msg );
            }
        }
        else if ( supportsProperties() )
        {
            final String msg    =  getJ2EEType() + ": " +
                "AMX interface supports properties, but has no delegate";
            logSevere( msg );
            throw new Error( msg );
        }
	}
	
	    private boolean
	delegateSupportsSystemProperties()
	{
        boolean supports   = true;
        final OldSystemProperties old    = getOldSystemProperties();
        try
        {
            final AttributeList props    =  old.getSystemProperties();
            supports   = true;
        }
        catch( Exception e )
        {
            supports   = false;
        }
        return supports;
	}
	
	    private boolean
	delegateSupportsProperties()
	{
        boolean supports   = true;
        final OldProperties old    = getOldProperties();
        try
        {
          final AttributeList props    =  old.getProperties();
          supports   = true;
        }
        catch( Exception e )
        {
            supports   = false;
        }
        return supports;
	}
	
	/**
	    Verify that the AMX support for properties is the same
	    as its Delegate.
	 */
	    protected final void
	checkSystemPropertiesAccessSupport()
	{
        if ( getDelegate() != null)
        {
            final boolean   delegateSupports    = delegateSupportsSystemProperties();
            
            if ( delegateSupports != supportsSystemProperties() )
            {
                final String msg = getJ2EEType() + ": " +
                    "delegateSupportsSystemProperties=" + delegateSupports +
                    ", but supportsSystemProperties=" + supportsSystemProperties();
                // implementation error
                logWarning( msg );
                throw new Error( msg );
            }
        }
        else if ( supportsSystemProperties() )
        {
            final String msg    =  getJ2EEType() + ": " +
                "AMX interface supports system properties, but has no delegate";
            logSevere( msg );
            throw new Error( msg );
        }
	}
	
	    protected final void
	checkInterfaceSupport(
	    final Class<?>     theInterface,
	    final String       attributeToCheck )
	{
        if ( getDelegate() != null)
        {
            final boolean   delegateSupports  =
                getDelegate().supportsAttribute( attributeToCheck );
            
            final boolean   supported   = theInterface.isAssignableFrom( getInterface() );
            
            if ( delegateSupports != supported )
            {
                final String msg    = "ERROR: " + getJ2EEType() + ": " +
                    "AMX interface does not match Delegate capabilities for " +
                    "interface " + theInterface.getName() + ", " +
                    "delegate support = " + delegateSupports +
                    ", AMX support = " + supported;
                logSevere( msg );
                throw new Error( msg );
            }
        }
	}

	
		protected final Set<String>
	getSuperfluousMethods()
	{
	    final Set<String>   items   = super.getSuperfluousMethods();
	    
	    final Method[]  methods = this.getClass().getMethods();
	    for( final Method m : methods )
	    {
	        final String    name    = m.getName();
	        
	        if (    isConfigFactoryGetter( name ) ||
	                isRemoveConfig( name ) ||
	                isCreateConfig( name ) )
	        {
	            if ( m.getParameterTypes().length <= 1 )
	            {
	                items.add( name );
	            }
	        }
	    }
	    
	    return items;
	}
	
		protected final void
	implCheck()
	{
	    super.implCheck();
	    
	    // not sure how to implement these checks in offline mode
	    if ( ! com.sun.enterprise.management.support.BootUtil.getInstance().getOffline() )
	    {
    	    checkPropertiesAccessSupport();
    	    checkSystemPropertiesAccessSupport();
	    }
	    
	    checkInterfaceSupport( Description.class, "Description" );
	    checkInterfaceSupport( ObjectType.class, "ObjectType" );
	    checkInterfaceSupport( Enabled.class, "Enabled" );
    }
    
		private static void
	validatePropertyName( final String propertyName )
	{
		if ( propertyName == null ||
			propertyName.length() == 0 )
		{
			throw new IllegalArgumentException( "Illegal property name: " +
				StringUtil.quote( propertyName ) );
		}
	}
	
		protected OldSystemProperties
	getOldSystemProperties()
	{	
		if ( ! haveDelegate() )
		{
			final String msg	= "system properties not supported (no delegate) by " +
									quote( getObjectName() );
			
			throw new IllegalArgumentException( msg );
		}
		
		return( new OldSystemPropertiesImpl( getDelegate() ) );
	}
	
		public Map<String,String>
	getSystemProperties( )
	{
		final AttributeList	props	= getOldSystemProperties().getSystemProperties();
		
		final Map<String,String> result	= JMXUtil.attributeListToStringMap( props );
		
		return result;
	}
	
		public String[]
	getSystemPropertyNames( )
	{
		final Set<String>	names	= getSystemProperties().keySet();
		
		return( GSetUtil.toStringArray( names ) );
	}
	
		public String
	getSystemPropertyValue( String propertyName )
	{
		return( getOldSystemProperties().getSystemPropertyValue( propertyName ) );
	}
	
		public final void
	setSystemPropertyValue(
		final String propertyName,
		final String propertyValue )
	{
		validatePropertyName( propertyName );
		
		if ( propertyValue == null  )
		{
			throw new IllegalArgumentException( "" + null );
		}

		final Attribute		attr	= new Attribute( propertyName, propertyValue );
		
		getOldSystemProperties().setSystemProperty( attr );
	}
	
		public final boolean
	existsSystemProperty( String propertyName )
	{
		validatePropertyName( propertyName );
		
		return( GSetUtil.newSet( getSystemPropertyNames() ).contains( propertyName ) );
	}
	
		public final void
	removeSystemProperty( String propertyName )
	{
		validatePropertyName( propertyName );
		
		getOldSystemProperties().setSystemProperty( new Attribute( propertyName, null ) );
	}
	
		public final void
	createSystemProperty( String propertyName, String propertyValue )
	{
		setSystemPropertyValue( propertyName, propertyValue );
	}
	
	
	
	
	/**
		Get the old mbean's properties API.
	 */
		protected OldProperties
	getOldProperties()
	{	
		if ( ! haveDelegate() )
		{
			final String msg	= "properties not supported (no delegate) by " +
									quote( getObjectName() );
			
			throw new IllegalArgumentException( msg );
		}
		
		return( new OldPropertiesImpl( getDelegate() ) );
	}
	
		public Map<String,String>
	getProperties( )
	{
		final AttributeList	props	= getOldProperties().getProperties();
		
		return JMXUtil.attributeListToStringMap( props );
	}
	
	
		public String[]
	getPropertyNames( )
	{
		final Set<String>	names	= getProperties().keySet();
		
		return( GSetUtil.toStringArray( names ) );
	}
	
		public String
	getPropertyValue( String propertyName )
	{
		return( getOldProperties().getPropertyValue( propertyName ) );
	}
	
		public final void
	setPropertyValue(
		final String propertyName,
		final String propertyValue )
	{
		validatePropertyName( propertyName );
		
		if ( propertyValue == null  )
		{
			throw new IllegalArgumentException( "null" );
		}

		final Attribute		attr	= new Attribute( propertyName, propertyValue );
		
		getOldProperties().setProperty( attr );
	}
	
		public final boolean
	existsProperty( String propertyName )
	{
		validatePropertyName( propertyName );
		
		return( GSetUtil.newSet( getPropertyNames() ).contains( propertyName ) );
	}
	
		public final void
	removeProperty( String propertyName )
	{
		validatePropertyName( propertyName );
		
		getOldProperties().setProperty( new Attribute( propertyName, null ) );
	}
	
		public final void
	createProperty( String propertyName, String propertyValue )
	{
		validatePropertyName( propertyName );
		
		setPropertyValue( propertyName, propertyValue );
	}
	
		public final String
	getGroup()
	{
		return( AMX.GROUP_CONFIGURATION );
	}
	
	
	    public MBeanNotificationInfo[]
	getNotificationInfo()
	{
	    final MBeanNotificationInfo[]   superInfos = super.getNotificationInfo();
	    
		// create a NotificationInfo for AttributeChangeNotification
		final String description	= "";
		final String[]	notifTypes	= new String[] { AttributeChangeNotification.ATTRIBUTE_CHANGE };
		final MBeanNotificationInfo	attributeChange = new MBeanNotificationInfo(
				notifTypes,
				AttributeChangeNotification.class.getName(),
				description );
	
		final MBeanNotificationInfo[]	selfInfos	=
			new MBeanNotificationInfo[]	{ attributeChange };

		final MBeanNotificationInfo[]	allInfos	=
			JMXUtil.mergeMBeanNotificationInfos( superInfos, selfInfos );
			
	    return allInfos;
	}
	
	    private String
	getSimpleInterfaceName( final AMX amx )
    {
        final String fullInterfaceName  = Util.getExtra( amx ).getInterfaceName();
        final String interfaceName   = ClassUtil.stripPackageName( fullInterfaceName );
        
        return interfaceName;
    }
    
    
	/**
	    Do anything necessary prior to removing an AMXConfig.
	    <p>
	    We have the situation where some of the com.sun.appserv MBeans
	    behave by auto-creating references, even in EE, but not auto-removing,
	    though in PE they are always auto-removed.  So the algorithm varies
	    by both release (PE vs EE) and by MBean.  This is hopeless.
	    <p>
	    So first we attempt remove all references to the AMXCOnfig (if any).
	    This will fail in PE, and may or may not fail in EE; we just ignore
	    it so long as there is only one failure.
	 */
	    protected void
    preRemove( final ObjectName objectName )
    {
        final AMXConfig amxConfig = getProxy( objectName, AMXConfig.class );
        
        if ( amxConfig instanceof RefConfigReferent )
        {
            debug( "*** Removing all references to ", objectName );
            
            final Set<RefConfig>  failures    =
                RefHelper.removeAllRefsTo( (RefConfigReferent)amxConfig, true );
            if( failures.size() != 0 )
            {
                debug( "FAILURE removing references to " + objectName  + ": " +
                    CollectionUtil.toString( Util.toObjectNames( failures ) ) );
            }
	    }
	    else
	    {
            debug( "*** not a RefConfigReferent: ", objectName );
	    }
    }
    
	/**
	    Make sure the item exists, then call preRemove( ObjectName ).
	 */
		protected ObjectName
    preRemove(
        final Map<String,ObjectName>    items,
        final String                    name )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException( "null name" );
        }

        final ObjectName    objectName  = items.get( name );
        if ( objectName == null )
        {
            throw new IllegalArgumentException( "Item not found: " + name );
        }
        
        preRemove( objectName );
        
        return objectName;
    }
  
    
    /**
        Remove the config by finding its ConfigFactory.
        The caller must have already called preRemove().
        @return true for success.
     */
        protected final boolean
    removeConfigWithFactory( final ObjectName objectName )
    {
        ConfigFactory factory    = null;
        
        boolean     attempted   = false;
        try
        {
            final AMXConfig amxConfig   = getProxy( objectName, AMXConfig.class );
            final String interfaceName   = getSimpleInterfaceName( amxConfig );
            debug( "removeConfigWithFactory: " + objectName );
            
            factory  = createConfigFactory( interfaceName );
        }
        catch( Exception e )
        {
            debug( ExceptionUtil.toString( e ) );
        }
        
        if ( factory != null )
        {
            attempted   = true;
                
            // some factories have remove(), because they remove a singleton
            // instance, and some have remove( ObjectName )
            try
            {
                final Method m  = factory.getClass().getMethod( "remove", (Class[])null );
                if ( m != null )
                {
                    m.invoke( factory, (Object[])null );
                }
            }
            catch( NoSuchMethodException e )
            {
                factory.remove( objectName );
            }
            catch( Exception e )
            {
                throw new RuntimeException( e );
            }
        }
        
        return attempted;
    }
    
    
    static private final String CREATE = "create";
    static private final String CREATE_PREFIX  = CREATE;
    static private final String REMOVE_PREFIX  = "remove";
    static private final String CONFIG_SUFFIX  = "Config";
    static private final String FACTORY_SUFFIX  = "Factory";
    
    static private final Class[]   STRING_SIG  = new Class[] { String.class };
    
    /**
        Remove the config, if possible, by finding a method of the
        appropriate name.  Usually, removeConfigWithFactory()
        should have been used instead.
        <p>
        The caller must have already called preRemove().
        <p>
        A RuntimeException is thrown if an appropriate method cannot
        be found.
     */
        protected final void
    removeConfigWithMethod( final ObjectName objectName )
    {
        final AMXConfig amxConfig   = getProxy( objectName, AMXConfig.class );
        final String interfaceName   = getSimpleInterfaceName( amxConfig );
        if ( ! interfaceName.endsWith( CONFIG_SUFFIX ) )
        {
            throw new IllegalArgumentException(
                "Interface doesn't end in " + CONFIG_SUFFIX + ": " + interfaceName );
        }
            
        // do it generically by constructing the expected method name,
        // and then calling it.
        final String operationName = REMOVE_PREFIX + interfaceName;
        debug( "removing config generically by calling ", operationName, "()" );
        try
        {
			final Method m	=
			    this.getClass().getDeclaredMethod( operationName, STRING_SIG);
			
			m.invoke( this, amxConfig.getName() );
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
     }
    
    
    /**
        Generic removal of any config contained by this config.
     */
        public final void
    removeConfig( final String j2eeType, final String name )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException();
        }

	    final Map<String,ObjectName>    items   = getContaineeObjectNameMap( j2eeType );
	    final ObjectName objectName   = preRemove( items, name );
        
        if ( ! removeConfigWithFactory( objectName ) )
        {
            debug( "removeConfigWithFactory failed, using removeConfigWithMethod" );
            removeConfigWithMethod( objectName );
        }
    }
    
    /**
        Generic removal of RefConfig.
     */
        protected void
    removeRefConfig( final String j2eeType, final String name )
    {
        removeConfig( j2eeType, name );
    }
    
    
        private boolean
    isRemoveConfig( final String operationName)
    {
        return operationName.startsWith( REMOVE_PREFIX ) &&
	        operationName.endsWith( CONFIG_SUFFIX );
    }
    
        private boolean
    isRemoveConfig(
		String 		operationName,
		Object[]	args,
		String[]	types )
    {
        final int   numArgs = args == null ? 0 : args.length;
        
        boolean isRemove    = numArgs <= 1 && isRemoveConfig( operationName );
        if ( isRemove && numArgs == 1 )
        {
            isRemove    = types[0].equals( String.class.getName() );
        }
        return isRemove;
    }
   
        private boolean
    isCreateConfig( final String operationName)
    {
        return operationName.startsWith( CREATE_PREFIX ) &&
	        operationName.endsWith( CONFIG_SUFFIX );
    }
    
        private boolean
    isConfigFactoryGetter(
		String 		operationName,
		Object[]	args,
		String[]	types )
    {
        final int   numArgs = args == null ? 0 : args.length;
        
        return numArgs == 0  && isConfigFactoryGetter( operationName );
    }
    
        private boolean
    isConfigFactoryGetter( final String operationName )
    {
        return operationName.startsWith( GET_PREFIX ) &&
	            operationName.endsWith( FACTORY_SUFFIX ) &&
                (! operationName.equals( "getProxyFactory" ) );
    }
    
        protected ObjectName
   createConfig(
        final String simpleInterfaceName,
        final Object[] args,
        String[]	   types)
        throws NoSuchMethodException, IllegalAccessException,
        InvocationTargetException, ClassNotFoundException, InstantiationException
   {
        ObjectName  result  = null;
    
        final Class[]   sig = ClassUtil.signatureFromClassnames( types );
            
        final ConfigFactory factory = createConfigFactory( simpleInterfaceName );
        if ( factory == null )
        {
            // look for the appropriate method
            final String createMethodName   = CREATE + simpleInterfaceName;
            final Method m   = this.getClass().getMethod( createMethodName, sig);
            if ( m == null )
            {
                throw new RuntimeException( "Can't find ConfigFactory for " + simpleInterfaceName );
            }
        }
        else
        {
			final Method createMethod	=
			    factory.getClass().getDeclaredMethod( CREATE, sig);
			if ( createMethod != null )
			{
			    result  = (ObjectName)createMethod.invoke( factory, args );
			}
			else
			{
			    final String msg    = "Can't find method " + CREATE +
			        " in factory " + factory.getClass().getName();
			    
			    throw new NoSuchMethodException( msg );
			}
        }
        
        return result;
   }
   
    
    private static final Set<String> CR_PREFIXES =
        GSetUtil.newUnmodifiableStringSet(
            "create", "remove"
        );
        
  
     /**
        Return the simple (no package) classname associated
        with certain operations:
        <ul>
        <li>removeAbcConfig => AbcConfig</li>
        <li>createAbcConfig => AbcConfig</li>
        <li>getAbcConfig => AbcConfig</li>
        </ul>
    */
        protected String
	operationNameToSimpleClassname( final String operationName )
    {
        return StringUtil.findAndStripPrefix( CR_PREFIXES, operationName );
    }
    
	    protected String
	operationNameToJ2EEType( final String operationName )
    {
        String  j2eeType   = null;
        
        if ( isRemoveConfig( operationName ) ||
              isCreateConfig( operationName )  )
        {
            j2eeType   = XTypes.PREFIX + operationNameToSimpleClassname( operationName );
        }
        else
        {
            j2eeType    = super.operationNameToJ2EEType( operationName );
        }
        return j2eeType;
    }
    
    
   /**
        Remove config for a singleton Containee.
    */
      protected void
   removeConfig( final String operationName)
   {
        final String        j2eeType    = operationNameToJ2EEType( operationName );
        final ObjectName    objectName  = getContaineeObjectName( j2eeType );
        if ( objectName == null )
        {
            throw new RuntimeException( new InstanceNotFoundException( j2eeType ) );
        }
	    preRemove( objectName );
        
        final String simpleInterfaceName    =
            operationName.substring( REMOVE_PREFIX.length(), operationName.length());
            
        createConfigFactory( simpleInterfaceName ).remove( objectName );
   }
   
   /**
        Remove config for a named Containee.
    */
      protected void
   removeConfig(
        final String operationName,
        final Object[] args,
        String[]	   types)
        throws InvocationTargetException
   {
        final String name    = (String)args[ 0 ];
        final String simpleInterfaceName    =
            operationName.substring( REMOVE_PREFIX.length(), operationName.length());
            
        final Set<? extends AMX>  containees  = getFactoryContainer().getContaineeSet();
        ObjectName  objectName  = null;
        for( final AMX containee : containees )
        {
            if ( containee.getName().equals( name ) )
            {
                debug( "removeConfig: found name match: " + Util.getObjectName( containee ) );
                if ( getSimpleInterfaceName( containee ).equals( simpleInterfaceName ) )
	            {
	                objectName  = Util.getObjectName( containee );
	                break;
	            }
	            debug( getSimpleInterfaceName( containee ), " != ", simpleInterfaceName );
            }
        }
        
        if ( objectName != null )
        {
            final AMX   amx = getProxy( objectName, AMX.class);
                
            removeConfig( amx.getJ2EEType(), amx.getName() );
        }
        else
        {
    		throw new IllegalArgumentException( "Not found: " + name );
        }
   }
   
        protected String
   getFactoryPackage()
   {
        // same package as the MBean implementation
        return this.getClass().getPackage().getName();
   }
   
   /**
        Create a ConfigFactory or return null if couldn't be created.
    */
        protected ConfigFactory
   createConfigFactory( final String simpleClassname )
   {
        ConfigFactory factory   = null;
        
        try
        {
            final String    classname   = getFactoryPackage() + "." +
                                            simpleClassname + FACTORY_SUFFIX;
            
            final Class factoryClass    = ClassUtil.getClassFromName( classname );
    		final Constructor constructor	= factoryClass.getConstructor( FACTORY_CONSTRUCTOR_SIG );
    		
    		if ( constructor != null )
    		{
                factory   = (ConfigFactory)constructor.newInstance( new Object[] { this } );
            }
            else
            {
                throw new RuntimeException( "No ConfigFactory found for " + classname );
            }
        }
        catch( Exception e )
        {
            debug( ExceptionUtil.toString( e ) );
            throw new RuntimeException( e );
        }
        return factory;
   }
   
   
	private static final Class[]    FACTORY_CONSTRUCTOR_SIG    = new Class[]
	{
	    ConfigFactoryCallback.class,
	};
	
    /**
        Automatically figure out get<abc>Factory(), 
        create<Abc>Config(), remove<Abc>Config().
        
     */
    	protected Object
	invokeManually(
		String 		operationName,
		Object[]	args,
		String[]	types )
		throws MBeanException, ReflectionException, NoSuchMethodException, AttributeNotFoundException
	{
	    final int   numArgs = args == null ? 0 : args.length;
	    
	    Object  result  = null;
	    
	    debugMethod( operationName, args );
	    
	    if ( isConfigFactoryGetter( operationName, args, types ) &&
	         ConfigFactoryCallback.class.isAssignableFrom( this.getClass() ) )
	    {
	        debug( "looking for factory denoted by " + operationName );
	        result  = createConfigFactory( operationName );
			if ( result == null )
			{
	            debug( "FAILED TO FIND factory denoted by " + operationName );
	            result  = super.invokeManually( operationName, args, types );
			}
	    }
	    else if ( isRemoveConfig( operationName, args, types ) )
	    {
	        try
	        {
	            if ( numArgs == 0 )
	            {
	                // a single, possibly unnamed containee
	                removeConfig( operationName );
	            }
	            else
	            {
	                removeConfig( operationName, args, types );
	            }
	        }
	        catch( InvocationTargetException e )
	        {
	            throw new MBeanException( e );
	        }
	    }
	    else if ( isCreateConfig( operationName ) )
	    {
	        // name will be of the form create<XXX>Config
	        final String simpleInterfaceName  =
	            operationName.substring( CREATE_PREFIX.length(), operationName.length() );
	            
	        try
	        {
	            result  = createConfig( simpleInterfaceName, args, types);
	        }
	        catch( Exception e )
	        {
	            throw new MBeanException( e );
	        }
	    }
	    else
	    {
	        result  = super.invokeManually( operationName, args, types );
	    }
	    return result;
	}
	
	
	/**
		Get the name of the config in which this MBean lives.
		
		@return config name, or null if not in a config
	 */
		public String
	getConfigName()
	{
		return( (String)getKeyProperty( XTypes.CONFIG_CONFIG ) );
	}
	
		public void
	sendConfigCreatedNotification( final ObjectName configObjectName )
	{
		sendNotification( AMXConfig.CONFIG_CREATED_NOTIFICATION_TYPE,
		    AMXConfig.CONFIG_REMOVED_NOTIFICATION_TYPE,
			AMXConfig.CONFIG_OBJECT_NAME_KEY, configObjectName );
	}
	
		public void
	sendConfigRemovedNotification( final ObjectName configObjectName )
	{
		sendNotification( AMXConfig.CONFIG_REMOVED_NOTIFICATION_TYPE,
		    AMXConfig.CONFIG_REMOVED_NOTIFICATION_TYPE,
			AMXConfig.CONFIG_OBJECT_NAME_KEY, configObjectName );
	}
    
    
    public String getDefaultValue( final String name )
    {
        try
        {
            return getDelegate().getDefaultValue( name );
        }
        catch( Throwable t )
        {
            throw new RuntimeException( t );
        }
    }
}





















