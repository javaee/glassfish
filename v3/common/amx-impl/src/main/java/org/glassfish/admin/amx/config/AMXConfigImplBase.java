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
package org.glassfish.admin.amx.config;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ThrowableMapper;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.TypeCast;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.RefConfig;
import com.sun.appserv.management.config.RefConfigReferent;
import com.sun.appserv.management.config.AnyPropertyConfig;
import com.sun.appserv.management.config.PropertyConfig;
import com.sun.appserv.management.config.SystemPropertyConfig;
import com.sun.appserv.management.config.AMXCreateInfo;

import com.sun.appserv.management.config.SystemPropertiesAccess;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.helper.RefHelper;

import org.jvnet.hk2.config.ConfigBeanProxy;

import org.glassfish.admin.amx.mbean.AMXImplBase;
import org.glassfish.admin.amx.mbean.Delegate;
import org.glassfish.admin.amx.mbean.ContainerSupport;
import org.glassfish.admin.amx.util.Issues;
import org.glassfish.admin.amx.util.SingletonEnforcer;

import org.glassfish.admin.amx.mbean.MBeanInfoCache;
import org.glassfish.admin.amx.util.AMXConfigInfoResolver;
import org.glassfish.admin.amx.loader.AMXConfigVoid;

import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;

import org.glassfish.api.amx.AMXConfigInfo;


/**
	Base class from which all AMX Config MBeans should derive (but not "must").
	<p>
 */
public class AMXConfigImplBase extends AMXImplBase
	implements AMXConfig
{
    private final Class<?> mSupplementaryInterface;
    
		public
	AMXConfigImplBase( 
        final String        j2eeType,
        final String        fullType,
        final ObjectName    parentObjectName,
		final Class<? extends AMX> theInterface,
        final  Class<?>     supplementaryInterface,
		final Delegate		delegate )
	{
		super( j2eeType, fullType, parentObjectName, theInterface, delegate );
        
        mSupplementaryInterface = supplementaryInterface;
	}
	
    @Override
        protected MBeanInfo
	modifyMBeanInfo( final MBeanInfo defaultInfo )
	{
        MBeanInfo info = super.modifyMBeanInfo( defaultInfo );
        
        if ( mSupplementaryInterface != null )
        {
            final MBeanInfo supplementaryInfo = MBeanInfoCache.getOtherMBeanInfo( mSupplementaryInterface );
            
            info = JMXUtil.mergeMBeanInfos( defaultInfo, supplementaryInfo );
        }
        
		return( info );
	}
    
    
        public Set<String>
    getContaineeJ2EETypes()
    {
        final Set<String> j2eeTypes = super.getContaineeJ2EETypes();
        
        // since properties are optional (might or might not be currently present),
        // add the j2eeType if they are supported.
        if ( supportsProperties() )
        {
            j2eeTypes.add( PropertyConfig.J2EE_TYPE );
        }
        
        // since system properties are optional (might or might not be currently present),
        // add the j2eeType if they are supported.
        if ( supportsSystemProperties() )
        {
            j2eeTypes.add( SystemPropertyConfig.J2EE_TYPE );
        }
        
        return j2eeTypes;
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
		
    /*
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
    */

	@Override
		protected final Set<String>
	getSuperfluousMethods()
	{
	    final Set<String>   items   = super.getSuperfluousMethods();
	    
	    final Method[]  methods = this.getClass().getMethods();
	    for( final Method m : methods )
	    {
	        final String    name    = m.getName();
	        
	        if (    isConfigFactoryGetter( name ) ||  // CONFIG_FACTORY
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
    
    
    	protected final <T extends AnyPropertyConfig> Map<String,String>
	asNameValuePairs( final Map<String,T> items )
	{
        final Map<String,String>  result = new HashMap<String,String>();
        for( final String name : items.keySet() )
        {
            final AnyPropertyConfig any = items.get(name);
            final String value = any.getValue();
            result.put( name, value );
        }
        
        return result;
    }
    
		protected final <T extends AnyPropertyConfig>  AnyPropertyConfig
	getAnyPropertyConfig( Map<String,T> props, final String propertyName )
	{
        return props.get(propertyName);
	}
    
		protected final <T extends AnyPropertyConfig>  String
	getPropertyValue( Map<String,T> props, final String propertyName )
	{
        final AnyPropertyConfig prop = props.get(propertyName);
        return prop == null ? null : prop.getValue();
	}
    
        private void
    validateNameValue( final String propertyName, final String propertyValue )
    {
		validatePropertyName( propertyName );
		if ( propertyValue == null  )
		{
			throw new IllegalArgumentException( "null" );
		}
    }
    
        private final DelegateToConfigBeanDelegate
    getConfigDelegate()
    {
        return DelegateToConfigBeanDelegate.class.cast( getDelegate() );
    }
    
    
        private final ConfigBean
    getConfigBean()
    {
        return getConfigDelegate().getConfigBean();
    }


//========================================================================================
    protected Map<String,PropertyConfig> getPropertyConfigMap() { return getSelf(PropertiesAccess.class).getPropertyConfigMap(); }
    
    	public Map<String,String>
	getProperties( )
	{
        return asNameValuePairs( getPropertyConfigMap() );
	}
	
		public String[]
	getPropertyNames( )
	{
		return( GSetUtil.toStringArray( getPropertyConfigMap().keySet() ) );
	}
	
		public String
	getPropertyValue( String propertyName )
	{   
        return getPropertyValue( getPropertyConfigMap(), propertyName );
    }
	
		public final void
	setPropertyValue(
		final String propertyName,
		final String propertyValue )
	{
		validateNameValue( propertyName, propertyValue );
        
        final PropertyConfig prop =  getPropertyConfigMap().get( propertyName );
        if ( prop != null )
        {
            prop.setValue( propertyValue );
        }
        else
        {
            createProperty( propertyName, propertyValue );
        }
	}
	
		public final boolean
	existsProperty( final String propertyName )
	{
		return getPropertyConfigMap().keySet().contains( propertyName );
	}
	
		public final void
	removeProperty( final String propertyName )
	{
        // reinvoke with non-deprecated auto-generic impl
        getSelf( PropertiesAccess.class ).removePropertyConfig( propertyName );
	}
	
		public final void
	createProperty( final String propertyName, final String propertyValue )
	{
        // reinvoke with non-deprecated auto-generic impl
        getSelf( PropertiesAccess.class ).createPropertyConfig( propertyName, propertyValue );
	}

		public final String
	getGroup()
	{
		return( AMX.GROUP_CONFIGURATION );
	}
	
//========================================================================================
    protected Map<String,SystemPropertyConfig> getSystemPropertyConfigMap() { return getSelf(SystemPropertiesAccess.class).getSystemPropertyConfigMap(); }

		public Map<String,String>
	getSystemProperties( )
	{
        return asNameValuePairs( getSystemPropertyConfigMap() );
	}
	
		public String[]
	getSystemPropertyNames( )
	{
		return( GSetUtil.toStringArray( getSystemPropertyConfigMap().keySet() ) );
	}
	
		public String
	getSystemPropertyValue( final String propertyName )
	{
        return getPropertyValue( getSystemPropertyConfigMap(), propertyName );
	}
	
		public final void
	setSystemPropertyValue(
		final String propertyName,
		final String propertyValue )
	{
		validateNameValue( propertyName, propertyValue );
        
        final SystemPropertyConfig prop =  getSystemPropertyConfigMap().get( propertyName );
        if ( prop != null )
        {
            prop.setValue( propertyValue );
        }
        else
        {
            createSystemProperty( propertyName, propertyValue );
        }
	}
	
		public final boolean
	existsSystemProperty( final String propertyName )
	{
		return getSystemPropertyConfigMap().keySet().contains( propertyName );
	}
	
		public final void
	removeSystemProperty( String propertyName )
	{
        // reinvoke with non-deprecated auto-generic impl
        getSelf( SystemPropertiesAccess.class ).removeSystemPropertyConfig( propertyName );
	}
	
		public final void
	createSystemProperty( String propertyName, String propertyValue )
	{
        // reinvoke with non-deprecated auto-generic impl
        getSelf( SystemPropertiesAccess.class ).createSystemPropertyConfig( propertyName, propertyValue );
	}
	
//========================================================================================
    
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
  
    
     
    
    static private final String CREATE = "create";
    static private final String CREATE_PREFIX  = CREATE;
    static private final String REMOVE_PREFIX  = "remove";
    static private final String CONFIG_SUFFIX  = "Config";
    static private final String FACTORY_SUFFIX  = "Factory";
    
    static private final Class[]   STRING_SIG  = new Class[] { String.class };
    
    /**
        Generic removal of any config contained by this config.
     */
        public final void
    removeConfig( final String j2eeType, final String name )
    {
        Issues.getAMXIssues().notDone( "AMXConfigImplBase(): support for removing config" );
        /*
        if ( name == null )
        {
            throw new IllegalArgumentException();
        }

	    final Map<String,ObjectName>    items   = getContaineeObjectNameMap( j2eeType );
	    final ObjectName objectName   = preRemove( items, name );
        
        if ( ! removeConfigWithFactory( objectName ) )
        {
            cdebug( "removeConfigWithFactory failed, using removeConfigWithMethod" );
            removeConfigWithMethod( objectName );
        }
        */
        throw new RuntimeException( "support for removing config not implemented" );
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
    isConfigFactoryGetter( final String operationName )
    {
        return operationName.startsWith( GET_PREFIX ) &&
	            operationName.endsWith( FACTORY_SUFFIX ) &&
                (! operationName.equals( "getProxyFactory" ) );
    }    

        private static Class<? extends ConfigBeanProxy>[]
    getSubTypes( final ConfigBean cb )
    {
        try
        {
            return (Class<? extends ConfigBeanProxy>[])
                ConfigSupport.getSubElementsTypes( cb );
        }
        catch( ClassNotFoundException e )
        {
            // OK
        }
        return new Class[0];
    }

        private Class<? extends ConfigBeanProxy>
    j2eeTypeToConfigBeanProxy( final String j2eeType )
    {
        final ConfigBean cb = getConfigBean();
        
//cdebug( "j2eeTypeToConfigBeanProxy: looking for interface for j2eeType " + j2eeType );
        Class<? extends ConfigBeanProxy> result = null;
        
        final String amxVoid = AMXConfigVoid.class.getName();
         final Class<? extends ConfigBeanProxy>[] candidates = getSubTypes( cb );
         for ( final Class<? extends ConfigBeanProxy> candidate : candidates )
         {
            final AMXConfigInfo amxConfigInfo = candidate.getAnnotation(AMXConfigInfo.class);
            if ( amxConfigInfo != null && ! amxVoid.equals(amxConfigInfo.amxInterfaceName()) )
            {
                final AMXConfigInfoResolver resolver = new AMXConfigInfoResolver( amxConfigInfo );
//cdebug( "j2eeTypeToConfigBeanProxy: class " + candidate.getName() + " has AMXConfigInfo  " + amxConfigInfo );
                if ( j2eeType.equals( resolver.j2eeType() ) )
                {
//cdebug( "j2eeTypeToConfigBeanProxy: FOUND " + candidate.getName() );
                    result = candidate;
                    break;
                }
            }
            else
            {
                //cdebug( "j2eeTypeToConfigBeanProxy: no AMXConfigInfo for " + candidate.getName()  );
            }
         }
         
         if ( result == null )
         {
            final Set<String> names = cb.getLeafElementNames();
//cdebug( "j2eeTypeToConfigBeanProxy: getLeafElementNames = " + CollectionUtil.toString(names) );
         }
        
        return result;
    }
    
        private Method
    getCreateMethod(
        final String    operationName,
        final Class[]   signature )
    {
        final Class<? extends AMXConfig> myInterface = getInterface();
        final Method m = ClassUtil.findMethod( myInterface, operationName, signature );
        return m;
    }
    
    
    /**
        Check arguments thoroughly for validity.
     */
        private void
    rejectBadArgs( 
        final Object[] args,
        final int      numRequiredArgs,
        final String[] paramNames,
        final Map<String,Object> optionalAttrs )
    {
        // verify that there aren't more arguments than parameter names
        if ( numRequiredArgs != 0 )
        {
            if ( args.length > paramNames.length )
            {
                throw new IllegalArgumentException( "More arguments than parameter names" );
            }
        }
        
        // verify that only legal types exist in the optionalAttrs array
        if ( optionalAttrs != null )
        {
            // verify that optional attributes are not redundant with required ones
            if ( paramNames != null )
            {
                final Set<String> temp = GSetUtil.newUnmodifiableStringSet( paramNames );
                temp.retainAll( optionalAttrs.keySet() );
                // there should be nothing in the set
                if ( temp.size() != 0 )
                {
                    throw new IllegalArgumentException(
                    "Optional attributes may not override required ones.  Duplicated attributes: {" + CollectionUtil.toString(temp) + "}" );
                }
                }
            
            for( final String key : optionalAttrs.keySet() )
            {
                final Object value = optionalAttrs.get(key);
                // is null legal?
                if ( value != null )
                {
                    final Class<?> theClass = value.getClass();
                    if ( theClass != String.class && theClass != Boolean.class &&
                        theClass != Integer.class && theClass != Long.class )
                    {
                        throw new IllegalArgumentException( "Illegal attribute class: " + theClass.getName() );
                    }
                }
            }
        }
        
    }
        
    private static void cdebug( final String s ) { System.out.println(s); }

        protected ObjectName
   createConfig(
        final String operationName,
        final Object[] args,
        String[]	   types)
        throws ClassNotFoundException, org.jvnet.hk2.config.TransactionFailure
   {
    setAMXDebug(true);
        if ( ! isCreateConfig( operationName ) )
        {
            throw new IllegalArgumentException( "Illegal method name for create: " + operationName );
        }
        
        ObjectName  result  = null;
        
        /*
          Determine if this create has an optional Map as the last argument; could be of the form:
                createFooConfig(p1, p2, ..., Map optional)
                createFooConfig(p1, p2, ..., pN)
                createFooConfig(optional)
                createFooConfig()
         */
        Map<String,Object> optionalAttrs = null;
        int numRequiredArgs = args.length;
        if ( args.length >= 1 )
        {
            Object lastArg = args[args.length-1];
            if ( lastArg instanceof Map )
            {
                optionalAttrs   = TypeCast.checkMap( TypeCast.asMap(lastArg), String.class, Object.class);
                numRequiredArgs = args.length - 1;
            }
        }
cdebug( "createConfig: " + operationName + ", args = " + StringUtil.toString(args) + ", types = " + StringUtil.toString(types) + " ===> numRequiredArgs = " + numRequiredArgs + ", optionalAttrs = " + MapUtil.toString(optionalAttrs) );
        
        final Class[] signature = ClassUtil.signatureFromClassnames(types);
        final Method m = getCreateMethod( operationName, signature );
        if ( m == null )
        {
            throw new IllegalArgumentException( "Can't find method " + operationName );
        }
        if ( ! AMXConfig.class.isAssignableFrom(  m.getReturnType() ) )
        {
            throw new IllegalArgumentException( "Class " + m.getReturnType().getName() + " is not a subclass of AMXConfig" );
        }
        
        final Class<? extends AMXConfig> returnType = (Class<? extends AMXConfig>)m.getReturnType();
        
        final String j2eeType = Util.getJ2EEType( returnType );
cdebug( "createConfig: j2eeType = " + j2eeType + ", return type = " + returnType.getName() );
        
        // Verify that the j2eeType matches the type expected from the operation name
        final String altJ2EEType = XTypes.PREFIX + operationName.substring( CREATE_PREFIX.length(), operationName.length() );
        if ( ! j2eeType.equals(altJ2EEType) )
        {
            throw new RuntimeException( "j2eeType " + j2eeType + " != " + altJ2EEType );
        }
                
        AMXCreateInfo amxCreateInfo = m.getAnnotation( AMXCreateInfo.class );
        if ( amxCreateInfo == null )
        {
            // if the Method has no AMXCreateInfo, accept the defaults from the Class
            amxCreateInfo = returnType.getAnnotation( AMXCreateInfo.class );
        }
        if ( amxCreateInfo == null )
        {
            // this is OK if there are no ordered parameters eg no parameters or an optional map only
            if ( numRequiredArgs != 0 )
            {
                throw new IllegalArgumentException(
                    "Method " + operationName + " must be annotated with " + AMXCreateInfo.class.getName() );
            }
        }
        
        final String[] paramNames = amxCreateInfo.paramNames();
        cdebug( "createConfig:  paramNames = {" + StringUtil.toString(paramNames) + "}" );
        rejectBadArgs( args, numRequiredArgs, paramNames, optionalAttrs );
            
        final Class<? extends ConfigBeanProxy>  newItemClass = j2eeTypeToConfigBeanProxy( j2eeType );
        if ( newItemClass == null )
        {
            throw new IllegalArgumentException( "Can't find class for j2eeType " + j2eeType );
        }
        
        final Map<String, String> allAttrs = new HashMap<String, String>();
        
        // set the optional attributes, if any, first so that required ones overwrite
        // (we are checking in rejectBadArgs(), but this order makes it more robust)
        if ( optionalAttrs != null )
        {
            for ( final String attrName : optionalAttrs.keySet() )
            {
                final String value = "" + optionalAttrs.get(attrName);  // force it into a String
                allAttrs.put( attrName, value );
            }
        }
        // set the required attributes: the last one might or might not be a Map of optional ones
        for ( int i = 0; i < numRequiredArgs; ++i ) {
            final String value = "" + args[i];  // force value into a String
            allAttrs.put( paramNames[i], value );
        }
  
cdebug( "createConfig:  creating new ConfigBean of class = " + newItemClass.getName());
        final ConfigBean newConfigBean = ConfigSupport.createAndSet( getConfigBean(), newItemClass, allAttrs);
cdebug( "createConfig:  CREATED new ConfigBean of class " + newItemClass.getName() + " = " + newConfigBean );

        final AMXConfigLoader  amxLoader = SingletonEnforcer.get( AMXConfigLoader.class );
        amxLoader.handleConfigBean( newConfigBean, true );
            
        final ObjectName objectName = newConfigBean.getObjectName();
        
cdebug( "createConfig:  ObjectName:  " + JMXUtil.toString(objectName) );
        return objectName;
   }
    
		protected boolean
	mySleep( final long millis )
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
        final ContainerSupport containerSupport = getContainerSupport();
        final ObjectName    objectName  = containerSupport.getContaineeObjectName( j2eeType );
        if ( objectName == null )
        {
            throw new RuntimeException( new InstanceNotFoundException( j2eeType ) );
        }
	    preRemove( objectName );
        
        final String simpleInterfaceName    =
            operationName.substring( REMOVE_PREFIX.length(), operationName.length());
            
        //createConfigFactory( simpleInterfaceName ).remove( objectName );
        throw new RuntimeException( "removeConfig() not supported" );
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
                cdebug( "removeConfig: found name match: " + Util.getObjectName( containee ) );
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

	    if ( isRemoveConfig( operationName, args, types ) )
	    {
            /*
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
            */
	    }
	    else if ( isCreateConfig( operationName ) )
	    {
	        try
	        {
	            result  = createConfig( operationName, args, types);
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





















