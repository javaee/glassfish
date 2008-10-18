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

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.*;
import com.sun.appserv.management.helper.RefHelper;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.*;

import org.glassfish.admin.amx.util.AttributeResolverHelper;
import org.glassfish.admin.amx.mbean.MBeanInfoCache;

import org.glassfish.admin.amx.dotted.V3Pathname;
import org.glassfish.admin.amx.mbean.AMXImplBase;
import org.glassfish.admin.amx.mbean.ContainerSupport;
import org.glassfish.admin.amx.mbean.Delegate;
import org.glassfish.admin.amx.util.AMXConfigInfoResolver;
import org.glassfish.admin.amx.util.SingletonEnforcer;
import com.sun.appserv.management.util.jmx.UnregistrationListener;
import org.glassfish.admin.amx.util.Issues;

import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.WriteableView;
import org.jvnet.hk2.config.TransactionFailure;

import javax.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.jvnet.hk2.config.Dom;

/**
	Base class from which all AMX Config MBeans should derive (but not "must").
	<p>
 */
public class AMXConfigImplBase extends AMXImplBase
	implements AMXConfig, DefaultValues  // and others more conveniently implemented generically
{
    private final Class<?> mSupplementaryInterface;
    
		public
	AMXConfigImplBase( 
        final String        j2eeType,
        final String        fullType,
        final ObjectName    parentObjectName,
		final Class<? extends AMX> theInterface,
        final  Class<?>     supplementaryInterface,
		final DelegateToConfigBeanDelegate		delegate )
	{
		super( j2eeType, fullType, parentObjectName, theInterface, delegate );
        
        mSupplementaryInterface = supplementaryInterface;
        
	}

    
    private static ConfiguredHelper getConfiguredHelper(final Class<? extends ConfigBeanProxy> intf )
    {
        return ConfiguredHelperRegistry.getInstance(intf);
    }
    
        private String
    getTypeString()
    {
        final Class<? extends ConfigBeanProxy> intf = getConfigBean().getProxyType();
        final Package pkg = intf.getPackage();
        String result = intf.getName().substring( pkg.getName().length() + 1, intf.getName().length() );
        result = Dom.convertName(result);
        return result;
    }
    
    /**
        The actual name could be different than the 'name' property in the ObjectName if it
        contains characters that are illegal for an ObjectName.
     */
    @Override
		public String
	getName()
	{
        final ConfigBean cb = getConfigBean();
        
        return AMXConfigLoader.getName( cb, null );
	}
      
    @Override
         protected String
    _getPathnameType()
    {
        return V3Pathname.hyphenate(getTypeString());
    }


        public Set<String>
    getContaineeJ2EETypes()
    {
        final Set<String> j2eeTypes = super.getContaineeJ2EETypes();
                
        final ContainedTypeInfo info = new ContainedTypeInfo( getConfigBean() );
        j2eeTypes.addAll( info.findAllContainedJ2EETypes().keySet() );
        
        return j2eeTypes;
    }

    /**
        A subclass may override this any allow any name variant to map to the AMX
        Attribute name as found in the MBeanInfo.
     */
    @Override
        protected String
    asAMXAttributeName( final String name )
    {
        String amxName = super.asAMXAttributeName(name);
        
        //cdebug( "asAMXAttributeName: " + name );
        if ( JMXUtil.getMBeanAttributeInfo( getMBeanInfo(), amxName ) == null )
        {
            final AttrInfo info = getConfigDelegate().getAttrInfo_AMX(name);
        
            if ( info != null )
            {
                amxName = info.amxName();
                //cdebug( "asAMXAttributeName: found match: " + info );
            }
            else
            {
            //cdebug( "asAMXAttributeName: no match: " + name );
            }
        }
        return name;
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
	
	@Override
		protected final Set<String>
	getSuperfluousMethods()
	{
	    final Set<String>   items   = super.getSuperfluousMethods();
	    
	    final Method[]  methods = this.getClass().getMethods();
	    for( final Method m : methods )
	    {
	        final String    name    = m.getName();
	        
	        if (   isRemoveConfig( name ) ||
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
    
        private final ConfigBeanProxy
    getConfigBeanProxy()
    {
        return getConfigBean().getProxy( getConfigBean().getProxyType() );
    }

		public final String
	getGroup()
	{
		return( AMX.GROUP_CONFIGURATION );
	}


    protected Map<String,PropertyConfig> getPropertyConfigMap()
    {
        return getSelf(PropertiesAccess.class).getPropertyConfigMap();
    }
	
    protected Map<String,SystemPropertyConfig> getSystemPropertyConfigMap()
    {
        return getSelf(SystemPropertiesAccess.class).getSystemPropertyConfigMap();
    }
        
    /**
        Resolve a template String.  See {@link AttributeResolver} for details.
     */
        public String
    resolveAttributeValue( final String varString )
    {
        if ( ! AttributeResolverHelper.needsResolving(varString) )
        {
            return varString;
        }
        
        return new AttributeResolverHelper(this).resolve(varString); 
    }
    
        public String
    resolveAttribute( final String attrName )
    {
        try
        {
            final Object value = getAttribute(attrName);
            return resolveAttributeValue( value == null ? null : "" + value );
        }
        catch( final AttributeNotFoundException e )
        {
            System.out.println( "resolveAttribute: Attribute not found: " + attrName );
            return null;
        }
    }

        public Boolean
    resolveBoolean( final String attrName) 
    {
        return Boolean.parseBoolean( resolveAttribute(attrName) );
    }

        public Integer
    resolveInteger( final String attrName) 
    {
        return Integer.parseInt( resolveAttribute(attrName) );
    }

        public Long
    resolveLong( final String attrName) 
    {
        return Long.parseLong( resolveAttribute(attrName) );
    }
    
    
        public AttributeList
    resolveAttributes( final String[] attrNames )
    {
        Issues.getAMXIssues().notDone( "resolveAttributes: use annotations to create the correct type" );
                
        final AttributeList attrs = getAttributes(attrNames);
        final AttributeList resolvedAttrs = new AttributeList();
        for (final Object o : attrs )
        {
            Attribute r = (Attribute)o;
            // allow non-String attributes
            final Object value = r.getValue();
            if ( (value instanceof String) && AttributeResolverHelper.needsResolving((String)value) )
            {
                final String resolvedValue = resolveAttributeValue((String)value);
                // TODO: use annotation to determine correct type
                r = new Attribute( r.getName(), resolvedValue );
            }

            resolvedAttrs.add( r );
        }
        
        return resolvedAttrs;
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
	
    /*
	    private String
	getSimpleInterfaceName( final AMX amx )
    {
        final String fullInterfaceName  = Util.getExtra( amx ).getInterfaceName();
        final String interfaceName   = ClassUtil.stripPackageName( fullInterfaceName );
        
        return interfaceName;
    }
    */
    
    
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
        Generic removal of RefConfig.
        protected void
    removeRefConfig( final String j2eeType, final String name )
    {
        removeConfig( j2eeType, name );
    }
     */
    
    
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
    isGenericCreateConfig( final String operationName)
    {
        // not quite suffficient, but will do for anticipated cases
        try
        {
            final Class[] types = new Class[] { String.class, Map.class };
            return ConfigCreator.class.getMethod(operationName, types) != null;
        }
        catch( final NoSuchMethodException e )
        {
            return false;
        }
    }
     
        private Method
    getCreateMethod(
        final String    operationName,
        final String[]  types )
        throws ClassNotFoundException
    {
        final Class[] signature = ClassUtil.signatureFromClassnames(types);
        
        final Class<AMXConfig> myInterface = TypeCast.asClass(getInterface());
        final Method m = ClassUtil.findMethod( myInterface, operationName, signature );
        
        if ( m == null )
        {
            throw new IllegalArgumentException( "Can't find method " + operationName );
        }
        
        if ( ! AMXConfig.class.isAssignableFrom(  m.getReturnType() ) )
        {
            throw new IllegalArgumentException( "Class " + m.getReturnType().getName() + " is not a subclass of AMXConfig" );
        }
        
        return m;
    }

        private void
    checkPropertiesSupported(
        final Class<? extends AMXConfig>  intf,
        final CreateConfigArgSupport      argSpt )
    {
        // check for illegal use of properties on configs that don't have them
        if ( argSpt.getProperties().keySet().size() != 0 &&
            ! PropertiesAccess.class.isAssignableFrom( intf ) )
        {
            throw new IllegalArgumentException(
                "Properties specified, but not supported by " + intf.getName() );
        }
        // check for illegal use of system properties on configs that don't have them
        if ( argSpt.getSystemProperties().keySet().size() != 0 &&
            ! SystemPropertiesAccess.class.isAssignableFrom( intf ) )
        {
            throw new IllegalArgumentException(
                "System properties specified, but not supported by " + intf.getName() );
        }
    }
    
        private AMXCreateInfo
    getAMXCreateInfo(
        final Method m,
        final Class<? extends AMXConfig> intf,
        final int numArgs )
    {     
        // check the method first, then the interface.
        AMXCreateInfo amxCreateInfo = m.getAnnotation( AMXCreateInfo.class );
        if ( amxCreateInfo == null )
        {
            // if the Method has no AMXCreateInfo, accept the defaults from the Class
            amxCreateInfo = intf.getAnnotation( AMXCreateInfo.class );
            if ( amxCreateInfo == null )
            {
                cdebug( "No AMXCreateInfo found for interface " + intf.getName() );
            }
        }
        
        // if not on the method, check the interface
        if ( amxCreateInfo == null )
        {
            // this is OK if there are no ordered parameters eg no parameters or an optional map only
            if ( numArgs != 0 )
            {
                throw new IllegalArgumentException(
                    "Method " + m.getName() + " must be annotated with " + AMXCreateInfo.class.getName() );
            }
        }
        return amxCreateInfo;
    }
    
        private void
    checkJ2EETypeMatches( final String j2eeType, final String operationName )
    {
        // Verify that the j2eeType matches the type expected from the operation name
        final String altJ2EEType = XTypes.PREFIX + operationName.substring( CREATE_PREFIX.length(), operationName.length() );
        if ( ! j2eeType.equals(altJ2EEType) )
        {
            throw new IllegalArgumentException( "checkJ2EETypeMatches: j2eeType " + j2eeType + " != " + altJ2EEType );
        }
    }
    
    
        static private List<ConfigSupport.AttributeChanges>
    toAttributeChanges(Map<String, Object> values)
    {
        final List<ConfigSupport.AttributeChanges> changes = new ArrayList<ConfigSupport.AttributeChanges>();
        
        if ( values != null )
        {
            for( final String name : values.keySet() ) {
                final Object value = values.get(name);
                
                if ( value == null || (value instanceof String) )
                {
                    changes.add( new ConfigSupport.SingleAttributeChange(name, (String)value) );
                }
                else
                {
                    changes.add( new ConfigSupport.MultipleAttributeChanges(name, (String[])value) );
                }
            }
        }
        return changes;
    }
    
    private ObjectName finishCreate(
        final Class<? extends ConfigBeanProxy>  elementClass,
        final CreateConfigArgSupport argSpt,
        final  List<ConfigSupport.AttributeChanges>  changes)
    {
        ConfigBean newConfigBean = null;
        final PropertiesCallback  callback = new PropertiesCallback( argSpt.getProperties(), argSpt.getSystemProperties() );
        try
        {
            newConfigBean = ConfigSupport.createAndSet( getConfigBean(), elementClass, changes, callback);
        }
        catch( Throwable t )
        {
            cdebug( ExceptionUtil.toString(t) );
            throw new RuntimeException( t );
        }

        //----------------------
        //
        // Force a synchronous processing of the new ConfigBean into an AMX MBean
        //
        final AMXConfigLoader  amxLoader = SingletonEnforcer.get( AMXConfigLoader.class );
        amxLoader.handleConfigBean( newConfigBean, true );
        final ObjectName objectName = newConfigBean.getObjectName();
        
        //
        // Set the properties and system properties.  Ideally, this should be part of the original
        // transaction, but doing so would require creating sub-elements of the newly-created element,
        // an undertaking that is more involved.
        //
        final AMXConfig newAMX = AMXConfig.class.cast( getProxyFactory().getProxy( objectName ) );
        setAllProperties( newAMX, argSpt.getProperties(), argSpt.getSystemProperties() );
    
        return objectName;
    }

    /**
        Create a sub-element generically.
     */
        protected ObjectName
    createConfigGeneric(
        final String operationName,
        final Object[] args,
        String[]	   types)
        throws ClassNotFoundException, TransactionFailure
    {
        if ( types.length != 2)
        {
            throw new IllegalArgumentException("unexpected signature for create" );
        }
        final String elementType = (String)args[0];
        // for now, the only allowed values are of type String
        final Map<String,Object> params = TypeCast.checkMap(TypeCast.asMap(args[1]), String.class, Object.class);
        
        final Class<? extends ConfigBeanProxy>  newItemClass =
            ConfigSupport.getElementTypeByName( getConfigBeanProxy(), elementType );
        if ( newItemClass == null )
        {
            throw new IllegalArgumentException( "ConfigBean of type " + getConfigBean().getProxyType() +
                " does not support sub-element of type " + elementType );
        }
        
        //--------------
        
        final CreateConfigArgSupport argSpt = new CreateConfigArgSupport( operationName, params );
        final List<ConfigSupport.AttributeChanges> changes = toAttributeChanges(params);
        
        return finishCreate( newItemClass, argSpt, changes);
    }

        protected ObjectName
   createConfig(
        final String operationName,
        final Object[] args,
        String[]	   types)
        throws ClassNotFoundException, TransactionFailure
   {
        if ( ! isCreateConfig( operationName ) )
        {
            throw new IllegalArgumentException( "Illegal method name for create: " + operationName );
        }
        ObjectName  result  = null;
        
        //
        // Parse out the arguments
        //
        final CreateConfigArgSupport argSpt = new CreateConfigArgSupport( operationName, args, types );
        
        final Method m = getCreateMethod( operationName, types );
        final Class<? extends AMXConfig> returnType = TypeCast.asClass(m.getReturnType());
        checkPropertiesSupported( returnType, argSpt );
        
        final String j2eeType = Util.getJ2EEType( returnType );
        checkJ2EETypeMatches( j2eeType, operationName );
        cdebug( "createConfig: j2eeType = " + j2eeType + ", return type = " + returnType.getName() );
                        
        final AMXCreateInfo amxCreateInfo = getAMXCreateInfo( m, returnType, argSpt.numArgs() );
        final String[] paramNames = amxCreateInfo.paramNames();
        cdebug( "createConfig:  paramNames = {" + StringUtil.toString(paramNames) + "}" );
        argSpt.addExplicitAttrs( paramNames );
    
        final ContainedTypeInfo   subInfo = new ContainedTypeInfo( getConfigBean() );
        final Class<? extends ConfigBeanProxy>  newItemClass = subInfo.getConfigBeanProxyClassFor(j2eeType);
        if ( newItemClass == null )
        {
            throw new IllegalArgumentException( "Can't find ConfigBean @Configured class for AMX j2eeType " + j2eeType );
        }
        
        final AMXConfigInfoResolver resolver = subInfo.getAMXConfigInfoResolverFor( j2eeType );
        if ( resolver.amxInterface() != returnType )
        {
            throw new IllegalArgumentException();
        }
  
        cdebug( "calling ConfigSupport.createAndSet() " );
        ConfigBean newConfigBean = null;
        
        final List<ConfigSupport.AttributeChanges> changes = toAttributeChanges(argSpt.getAttrs());
        final ObjectName objectName = finishCreate( newItemClass, argSpt, changes);
    
        return objectName;
   }
   
    private static final class PropertiesCallback implements ConfigSupport.TransactionCallBack<WriteableView>
    {
        private final Map<String,String> mProperties;
        private final Map<String,String> mSystemProperties;
        
        public PropertiesCallback( 
            final Map<String,String> properties, 
            final Map<String,String> systemProperties )
        {
            mProperties         = properties;
            mSystemProperties   = systemProperties;
        }
        
        public void performOn(final WriteableView item) throws TransactionFailure
        {
            if ( false ) {
                // code in progress
                /*
                if ( mProperties.keySet().size() != 0 )
                {
                    Class<ConfigBeanProxy> propClass = null;
                    
                    try {
                        propClass = (Class<ConfigBeanProxy>)Class.forName("com.sun.enterprise.config.serverbeans.Property");
                    }
                    catch( final ClassNotFoundException e )
                    {
                        throw new TransactionFailure("Can't find com.sun.enterprise.config.serverbeans.Property");
                    }
                        
                    for( final String propertyName : mProperties.keySet() )
                    {
                        final String propertyValue = mProperties.get(propertyName);
                        
                        final ConfigBeanProxy propChild = item.allocateProxy(propClass);
                        final ConfigBean child = (ConfigBean)Dom.unwrap(propChild);
                        final ConfigBeanProxy childW = ConfigSupport.getWriteableView(propChild);
                        
                        //ConfigModel.Property modelProp = NameMappingHelper.getConfigModel_Property( child, "name");
                        childW.setter( modelProp, propertyName, String.class);
                        
                        //modelProp = NameMappingHelper.getConfigModel_Property( child, "value");
                        childW.setter( modelProp, propertyValue, String.class);
                    }
                }
                
                if ( mSystemProperties.keySet().size() != 0 )
                {
                    // figure out plain properties first
                }
                */
            
            }
        }
    }

    
    /**
        This should be done in one transaction...
     */
        private void
    setAllProperties(
        final AMXConfig amxConfig,
        final Map<String,String> properties, 
        final Map<String,String> systemProperties )
    {
        if ( properties.keySet().size() != 0 )
        {
            final PropertiesAccess pa = PropertiesAccess.class.cast( amxConfig );
            for( final String propName : properties.keySet() )
            {
                final String propValue = properties.get(propName);
                cdebug ("################## Creating property " + propName + " = " + propValue );
                try
                {
                    pa.createPropertyConfig( propName, propValue );
                }
                catch ( Throwable t )
                {
                    cdebug( ExceptionUtil.toString(ExceptionUtil.getRootCause(t)));
                }
            }
        }
        
        if ( systemProperties.keySet().size() != 0 )
        {
            final SystemPropertiesAccess pa = SystemPropertiesAccess.class.cast( amxConfig );
            for( final String propName : systemProperties.keySet() )
            {
                final String propValue = systemProperties.get(propName);
                debug ("################## Creating System property " + propName + " = " + propValue );
                pa.createSystemPropertyConfig( propName, propValue );
            }
        }
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
        Generic removal of any config contained by this config.
     */
        public final void
    removeConfig( final ObjectName containeeObjectName )
    {
        final ContainerSupport containerSupport = getContainerSupport();

	    preRemove( containeeObjectName );
        
        final AMXConfigImplBase child = (AMXConfigImplBase)get__ObjectRef( containeeObjectName );
        try
        {
cdebug( "REMOVING config of class " + child.getConfigBean().getProxyType().getName() + " from  parent of type " + 
    getConfigBean().getProxyType().getName() + ", ObjectName = " + JMXUtil.toString(containeeObjectName) );
            ConfigSupport.deleteChild( getConfigBean(), child.getConfigBean() );
        }
        catch( final TransactionFailure tf )
        {
            throw new RuntimeException( "Transaction failure deleting " + JMXUtil.toString(containeeObjectName), tf );
        }

        // NOTE: MBeans unregistered asynchronously by AMXConfigLoader
        // enforce synchronous semantics to clients by waiting until this happens
        final UnregistrationListener myListener = new UnregistrationListener( getMBeanServer(), containeeObjectName);
        final long TIMEOUT_MILLIS = 10 * 1000;

        final boolean unregisteredOK = myListener.waitForUnregister( TIMEOUT_MILLIS );
        if ( ! unregisteredOK )
        {
            throw new RuntimeException( "Something went wrong unregistering MBean " + JMXUtil.toString(containeeObjectName) );
        }
        
        
        //sendConfigRemovedNotification( containeeObjectName );
    }

    /**
        Generic removal of any config contained by this config.
     */
        public final void
    removeConfig( final String j2eeType, final String name )
    {
        final ContainerSupport containerSupport = getContainerSupport();
        final ObjectName    containeeObjectName  = containerSupport.getContaineeObjectName( j2eeType, name);
        if ( containeeObjectName == null )
        {
            throw new RuntimeException( new InstanceNotFoundException( "No MBean named " + name + " of j2eeType " + j2eeType + " found." ) );
        }
        removeConfig( containeeObjectName );
    }


   /**
        Remove config for a singleton Containee.
    */
      protected void
   removeConfig( final String operationName)
   {
        final String        j2eeType    = operationNameToJ2EEType( operationName );
        final ContainerSupport containerSupport = getContainerSupport();
        final ObjectName    containeeObjectName  = containerSupport.getContaineeObjectName( j2eeType );
        if ( containeeObjectName == null )
        {
            throw new RuntimeException( new InstanceNotFoundException( j2eeType ) );
        }
        removeConfig( containeeObjectName );
   }
   
   /**
        Remove config for a named Containee.
    */
      protected void
   removeConfig(
        final String   operationName,
        final Object[] args,
        String[]	   types)
        throws InvocationTargetException
   {
        if ( args == null || args.length == 0 )
        {
cdebug( "removeConfig: by operation name only" );
            // remove a singleton
            removeConfig( operationName );
        }
        else if ( args.length == 1 )
        {
cdebug( "removeConfig: by operationName + name" );
            // remove by name, type is implicit in method name
            removeConfig( operationNameToJ2EEType(operationName), (String)args[0] );
        }
        else if ( args.length == 2 )
        {
cdebug( "removeConfig: by  j2eeType + name" );
            // generic form
            if ( ! operationName.equals( "removeConfig" ) )
            {
                throw new IllegalArgumentException();
            }
            removeConfig( (String)args[0], (String)args[1] );
        }
        else
        {
            throw new IllegalArgumentException();
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
	        try
	        {
                removeConfig( operationName, args, types );
	        }
	        catch( InvocationTargetException e )
	        {
	            throw new MBeanException( e );
	        }
	    }
	    else if ( isGenericCreateConfig( operationName ) )
	    {
	        try
	        {
	            result  = createConfigGeneric( operationName, args, types);
	        }
	        catch( Exception e )
	        {
	            throw new MBeanException( e );
	        }
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
	
        public String[]
    getAnonymousElementList( final String elementName )
    {
        return getConfigDelegate().getAnonymousElementList(elementName);
    }
    
        public String[]
    modifyAnonymousElementList(
        final String   elementName,
        final String   cmd,
        final String[] values)
    {
        return getConfigDelegate().modifyAnonymousElementList(elementName, cmd, values);
    }
    
	/**
		Get the name of the config in which this MBean lives.
		
		@return config name, or null if not in a config
	 */
		public String
	getConfigName()
	{
		return getKeyProperty( XTypes.CONFIG_CONFIG );
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
    
    public String getDefaultValue( final String amxName )
    {
        try
        {
            final Class<? extends ConfigBeanProxy> myIntf = getConfigBean().getProxyType();
            //cdebug( "AMXConfigImplBase.getDefaultValue: " + amxName + " for " + myIntf.getName() );
            
            final Map<String,String> defaultValues = getDefaultValuesXMLNames( myIntf );
            //cdebug( "defaultValues for " + myIntf.getName() + ": " + MapUtil.toString(defaultValues) );
            
            String xmlName = getXMLAttributeName( getJ2EEType(), amxName );
            if ( xmlName == null )
            {
                //cdebug( "AMXConfigImplBase.getDefaultValue(): no xml name found for: " + amxName );
                // could be a deprecated value we choose not to have in AMX
                xmlName = amxName;
            }
            return defaultValues.get( xmlName );
        }
        catch( Throwable t ) {
            t.printStackTrace();
            return null;
        }
    }
    
    /**
        Get the default values, keyed by the XML element name.  The interface might be
        for this MBean or one of its children; it doesn't matter
     */
        private static final Map<String,String>
    getDefaultValuesXMLNames( final Class<? extends ConfigBeanProxy> intf )
    {
        return getConfiguredHelper(intf).getDefaultValues();
    }
    
        public final Map<String,String>
    getDefaultValues( final boolean useAMXAttributeName )
    {
        final Map<String,String> m = getDefaultValuesXMLNames( getConfigBean().getProxyType() );
        return useAMXAttributeName ? toAMXAttributeNames( getJ2EEType(), m) : m;
    }
    
        public final Map<String,String>
    getDefaultValues( final String j2eeTypeIn )
    {
        return getDefaultValues( j2eeTypeIn, true );
    }
    
    public String getXMLAttributeName( final String j2eeType, final String name )
    {
        return getNameMapping(j2eeType, true).getXMLName( name, true);
    }

    public String getAMXAttributeName( final String j2eeType, final String name )
    {
        return getNameMapping(j2eeType, true).getAMXName( name );
    }


    private NameMapping getNameMapping( final String j2eeType, final boolean deduce )
    {
        NameMapping m = NameMappingRegistry.getInstance(j2eeType);
        
        if ( m == null && deduce )
        {
            //
            // This case occurs when no AMX MBean has yet been registered.  It must
            // be done using only the available interfaces. Use case is for when the GUI
            // creates a new element that does not yet exist anywhere.
            //
            final Class<? extends ConfigBeanProxy> intf = getConfigBeanProxyClassForContainedType(j2eeType);
            if ( intf == null )
            {
                throw new IllegalArgumentException( "j2eeType '" + j2eeType + "'  is not a containee of " + getJ2EEType() );
            }
            
            // Now we need the AMX interface and we need to map AMX names to config names
            final ConfiguredHelper helper = ConfiguredHelperRegistry.getInstance(intf);
            
            // get all the AMX attribute names
            final Class<? extends AMXConfig> amxIntf = helper.getAMXInterface();
            final MBeanInfo info = MBeanInfoCache.getAMXMBeanInfo(amxIntf);
            final String[] attrNamesArrary = JMXUtil.getAttributeNames(info.getAttributes());
            final Set<String>   attrNames = GSetUtil.newStringSet(attrNamesArrary);
            if ( amxIntf == AMXGenericConfig.class )
            {
                attrNames.addAll( helper.getImpliedAMXNames() );
            }
            
            m = new NameMapping(j2eeType);
            
            for( final String amxAttrName : attrNames )
            {
                final String xmlName = helper.findXMLName(amxAttrName);
                if ( xmlName != null )
                {
                    m.pairNames( amxAttrName, xmlName );
        System.out.println( "DEDUCED: " + amxAttrName + " ===> " + xmlName );
                }
            }
            
            
            m = NameMappingRegistry.addInstance( m );
        }
        return m;
    }
    
        private Class<? extends ConfigBeanProxy>
    getConfigBeanProxyClassForContainedType( final String containeeJ2EEType )
    {
        final ContainedTypeInfo   info = new ContainedTypeInfo( getConfigBean() );
        final Class<? extends ConfigBeanProxy>  intf = info.getConfigBeanProxyClassFor( containeeJ2EEType );
        if ( intf == null )
        {
            throw new IllegalArgumentException( "Illegal containee j2eeType: " + containeeJ2EEType );
        }
        return intf;
    }
        
        public final Map<String,String>
    getDefaultValues( final String j2eeType, final boolean useAMXAttributeName )
    {
        final Class<? extends ConfigBeanProxy> intf = getConfigBeanProxyClassForContainedType( j2eeType );
        
        final Map<String,String> m = getDefaultValuesXMLNames(intf);
        return useAMXAttributeName ? toAMXAttributeNames( j2eeType, m) : m;
    }
    
    
        private Map<String,String>
    toAMXAttributeNames( final String j2eeType, final Map<String,String> xmlMap )
    {
        final Map<String,String> m = new HashMap<String,String>();
        
        for( final String xmlName : xmlMap.keySet() )
        {
            final String amxName = getAMXAttributeName( j2eeType, xmlName);
            final String value = xmlMap.get(xmlName);
            if ( amxName == null )
            {
                System.out.println( "No corresponding AMX attribute name for xml attribute " + xmlName );
                // if the AMX interface is out of date with respect to the underlying config, return
                // the name as-is
                m.put(xmlName, value);
            }
            else
            {
                m.put(amxName, value);
            }
        }
        return m;
    }
    
    /**
        Make sure the AMX to XML and vice-versa mapping is in place
     */
    
    @Override
    protected synchronized void
	postRegisterHook( Boolean registrationSucceeded )
	{
		super.postRegisterHook( registrationSucceeded );
		
        if ( registrationSucceeded.booleanValue() )
        {
            if ( getNameMapping(getJ2EEType(), false) == null )
            {
                final DelegateToConfigBeanDelegate delegate = getConfigDelegate();
                final String[] attrNames = getAttributeNames();
                
                delegate.initNameMapping( attrNames );
            }
        }
	}
    
    /**
        Issue an AttributeChangeNotification.  The name is the xml name; this method should
        be called only from "bubble up" code from the lower level ConfigBean stuff.
        @see AMXConfigLoader
     */
        void
    issueAttributeChangeForXmlAttrName(
        final String     xmlAttrName,
        final Object     oldValue,
        final Object     newValue,
        final long       whenChanged )
    {
        String attrType = String.class.getName();
        String amxAttrName = getAMXAttributeName( getJ2EEType(), xmlAttrName );
        if ( amxAttrName == null )
        {
            cdebug( "issueAttributeChangeForXmlAttrName: can't find AMX name for: " + xmlAttrName + ", using xmlName for now" );
            amxAttrName = xmlAttrName;
        }
        else
        {
            attrType = getAttributeType(amxAttrName);
        }
        
        if ( oldValue != newValue )
        {
			sendAttributeChangeNotification( "", amxAttrName, attrType, whenChanged, oldValue, newValue );
        }
    }
    
    
    /**
        The dotted name for config should be the xml name.
     */
    @Override
        protected String
    attributeNameToPathNameValueName( final String amxAttrName )
    {
        final String xmlName = getXMLAttributeName( getJ2EEType(), amxAttrName );
        return xmlName == null ? super.attributeNameToPathNameValueName(amxAttrName) : xmlName;
    }



}





















