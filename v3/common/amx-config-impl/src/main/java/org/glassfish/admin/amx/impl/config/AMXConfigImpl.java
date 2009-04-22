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
package org.glassfish.admin.amx.impl.config;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.glassfish.admin.amx.core.AMXConstants;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.impl.config.AttributeResolverHelper;
import org.glassfish.admin.amx.impl.config.RefHelper;
import static org.glassfish.admin.amx.intf.config.AnonymousElementList.*;
import org.glassfish.admin.amx.impl.mbean.AMXImplBase;
import org.glassfish.admin.amx.impl.util.Issues;
import org.glassfish.admin.amx.impl.util.MBeanInfoSupport;
import org.glassfish.admin.amx.impl.util.SingletonEnforcer;
import org.glassfish.admin.amx.impl.util.UnregistrationListener;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.intf.config.AMXCreateInfo;
import org.glassfish.admin.amx.config.AttributeResolver;
import static org.glassfish.admin.amx.impl.config.ConfigBeanJMXSupport.*;
import org.glassfish.admin.amx.config.DefaultValues;
import org.glassfish.admin.amx.intf.config.PropertiesAccess;
import org.glassfish.admin.amx.intf.config.PropertyConfig;
import org.glassfish.admin.amx.intf.config.RefConfig;
import org.glassfish.admin.amx.intf.config.RefConfigReferent;
import org.glassfish.admin.amx.intf.config.SystemPropertiesAccess;
import org.glassfish.admin.amx.intf.config.SystemPropertyConfig;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.ListUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.TypeCast;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.RetryableException;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.WriteableView;


/**
	Base class from which all AMX Config MBeans should derive (but not "must").
	<p>
 */
public class AMXConfigImpl extends AMXImplBase
	implements DefaultValues  // and others more conveniently implemented generically
{
	private final ConfigBean        mConfigBean;
    
    /** MBeanInfo derived from the AMXConfigProxy interface, always the same */
    private static MBeanInfo configMBeanInfo;
    private static synchronized MBeanInfo getAMXConfigMBeanInfo()
    {
        if ( configMBeanInfo == null ) {
            configMBeanInfo = MBeanInfoSupport.getMBeanInfo(AMXConfigProxy.class);
        }
        return configMBeanInfo;
    }
    
    /**
     * We save time and space by creating exactly one MBeanInfo for any given config interface;
     * it can be shared among all instances since it is invariant.
     */
    private static final ConcurrentMap<Class<? extends ConfigBeanProxy>, MBeanInfo> mInfos =
            new ConcurrentHashMap<Class<? extends ConfigBeanProxy>, MBeanInfo>();    
    
    private static MBeanInfo createMBeanInfo( final ConfigBean cb )
    {
        Class<? extends ConfigBeanProxy> intf = cb.getProxyType();
        MBeanInfo newInfo = mInfos.get(intf);
        if ( newInfo != null )
        {
            return newInfo;
        }
        
        final ConfigBeanJMXSupport spt = ConfigBeanJMXSupportRegistry.getInstance(cb);
        final MBeanInfo info = spt.getMBeanInfo();

        final List<MBeanAttributeInfo> attrInfos = ListUtil.newListFromArray( info.getAttributes() );
        final MBeanInfo spiInfo = MBeanInfoSupport.getAMX_SPIMBeanInfo();
        
        // make a list so we can remove "Children" if this MBean cannot have any
        final List<MBeanAttributeInfo>  spiAttrInfos = ListUtil.newListFromArray( spiInfo.getAttributes() );
        if ( spt.isLeaf() )
        {
            JMXUtil.remove( spiAttrInfos, AMXConstants.ATTR_CHILDREN);
        }
        // Add in the AMX_SPI attributes, replacing any with the same name
        for( final MBeanAttributeInfo attrInfo : spiAttrInfos )
        {
            final MBeanAttributeInfo before = JMXUtil.remove( attrInfos, attrInfo.getName() );
            attrInfos.add( attrInfo );
            // may need to merge some Descriptor fields...
            //if ( before == null ) continue
            //final Descriptor descBefore = before.getDescriptor();;
        }
        
        final List<MBeanOperationInfo> operationInfos = ListUtil.newListFromArray( info.getOperations() );
        operationInfos.addAll( ListUtil.newListFromArray( getAMXConfigMBeanInfo().getOperations() ) );

        final MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[attrInfos.size()];
        attrInfos.toArray(attrs);
        
        final MBeanOperationInfo[] operations = new MBeanOperationInfo[operationInfos.size()];
        operationInfos.toArray(operations);

        newInfo = new MBeanInfo(
                info.getClassName(),
                info.getDescription(),
                attrs,
                info.getConstructors(),
                operations,
                info.getNotifications(),
                info.getDescriptor() );

        mInfos.putIfAbsent( intf, newInfo);

        return newInfo;
    }
    
		public
	AMXConfigImpl(
        final ObjectName    parentObjectName,
        final ConfigBean    configBean)
	{
        this( parentObjectName, AMXConfigProxy.class, configBean );
    }
    
		public
	AMXConfigImpl(
        final ObjectName    parentObjectName,
		final Class<? extends AMXProxy> theInterface,
        final ConfigBean    configBean)
	{
		super( parentObjectName, createMBeanInfo(configBean) );

		mConfigBean	= configBean;
        
        // eager initialization, it will be needed momentarily
        getConfigBeanJMXSupport();
	}
    
	public void	delegateFailed( Throwable t ) {}
	
 
    
     @Override
		protected void
	setAttributeManually( final Attribute attr )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
        final AttributeList attrList = new AttributeList();
        attrList.add( attr );
        
        final Map<String,Object> oldValues = new HashMap<String,Object>();
        final AttributeList successList = setAttributesInConfigBean( attrList, oldValues);
        if ( successList.size() == 0 )
        {
            throw new AttributeNotFoundException( attr.getName() );
        }
    }
    
       
	/**
		Note that the default implementation sets attributes one at a time, but that
        MBeans with transactional requirements (eg configuration) may wish to set them as a group.
	*/
    @Override
		public AttributeList
	setAttributes( final AttributeList attrs )
	{
		final AttributeList	successList	= new AttributeList();

        final Map<String,Object> oldValues = new HashMap<String,Object>();
        final AttributeList delegateSuccess = setAttributesInConfigBean( attrs, oldValues );
        successList.addAll( delegateSuccess );
        
		return( successList );
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
        
        String name =  AMXConfigLoader.getName( cb );
        
        return name == null ? AMXConstants.NO_NAME : name;
	}

    /*
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

	
		protected static boolean
	hasElementName( final Class<?>	mbeanInterface )
	{
		return( NamedConfigElement.class.isAssignableFrom( mbeanInterface ) );
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
    
    
		protected final AnyPropertyConfig
	getAnyPropertyConfig( Map<String,AnyPropertyConfig> props, final String propertyName )
	{
        return props.get(propertyName);
	}
    
		protected final String
	getPropertyValue( Map<String,AnyPropertyConfig> props, final String propertyName )
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
     * */
    
    
    
        private final ConfigBean
    getConfigBean()
    {
        return mConfigBean;
    }
    
        private final ConfigBeanProxy
    getConfigBeanProxy()
    {
        return getConfigBean().getProxy( getConfigBean().getProxyType() );
    }


    protected Map<String,PropertyConfig> getProperty()
    {
        return getSelf(PropertiesAccess.class).getProperty();
    }
	
    protected Map<String,SystemPropertyConfig> getSystemProperty()
    {
        return getSelf(SystemPropertiesAccess.class).getSystemProperty();
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
        
        return new AttributeResolverHelper( getSelf(AMXConfigProxy.class) ).resolve(varString);
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
	    Do anything necessary prior to removing an AMXConfigProxy.
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
        final AMXConfigProxy amxConfig = getProxy( objectName, AMXConfigProxy.class );
        
        if ( amxConfig instanceof RefConfigReferent )
        {
            debug( "*** Removing all references to ", objectName );
            
            final Set<RefConfig>  failures    =
                RefHelper.removeAllRefsTo( (RefConfigReferent)amxConfig, true );
            if( failures.size() != 0 )
            {
                debug( "FAILURE removing references to " + objectName  );
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
    static private final String CREATE_CHILD  = "createChild";
    static private final String REMOVE_CHILD  = "removeChild";
    
    static private final Class[]   STRING_SIG  = new Class[] { String.class };
    
    /**
        Generic removal of RefConfig.
        protected void
    removeRefConfig( final String type, final String name )
    {
        removeConfig( type, name );
    }
     */
    
    
        private boolean
    isRemoveConfig( final String operationName)
    {
        return operationName.equals( REMOVE_CHILD );
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
        return operationName.equals( CREATE_CHILD );
    }
    
        private boolean
    isGenericCreateConfig( final String operationName)
    {
        // not quite suffficient, but will do for anticipated cases
        try
        {
            final Class[] types = new Class[] { String.class, Map.class };
            return AMXConfigProxy.class.getMethod(operationName, types) != null;
        }
        catch( final NoSuchMethodException e )
        {
            return false;
        }
    }
     
//         private Method
//     getCreateMethod(
//         final String    operationName,
//         final String[]  types )
//         throws ClassNotFoundException
//     {
//             /*
//         final Class[] signature = ClassUtil.signatureFromClassnames(types);
//         
//         final Class<AMXConfigProxy> myInterface = TypeCast.asClass(getInterface());
//         final Method m = ClassUtil.findMethod( myInterface, operationName, signature );
//         
//         if ( m == null )
//         {
//             throw new IllegalArgumentException( "Can't find method " + operationName );
//         }
//         
//         if ( ! AMXConfigProxy.class.isAssignableFrom(  m.getReturnType() ) )
//         {
//             throw new IllegalArgumentException( "Class " + m.getReturnType().getName() + " is not a subclass of AMXConfigProxy" );
//         }
//         
//         return m;
//              * */
//             return null;
//     }
// 
//         private void
//     checkPropertiesSupported(
//         final Class<? extends AMXConfigProxy>  intf,
//         final CreateConfigArgSupport      argSpt )
//     {
//         // check for illegal use of properties on configs that don't have them
//         if ( argSpt.getProperties().keySet().size() != 0 &&
//             ! PropertiesAccess.class.isAssignableFrom( intf ) )
//         {
//             throw new IllegalArgumentException(
//                 "Properties specified, but not supported by " + intf.getName() );
//         }
//         // check for illegal use of system properties on configs that don't have them
//         if ( argSpt.getSystemProperties().keySet().size() != 0 &&
//             ! SystemPropertiesAccess.class.isAssignableFrom( intf ) )
//         {
//             throw new IllegalArgumentException(
//                 "System properties specified, but not supported by " + intf.getName() );
//         }
//     }
//     
//         private AMXCreateInfo
//     getAMXCreateInfo(
//         final Method m,
//         final Class<? extends AMXConfigProxy> intf,
//         final int numArgs )
//     {     
//         // check the method first, then the interface.
//         AMXCreateInfo amxCreateInfo = m.getAnnotation( AMXCreateInfo.class );
//         if ( amxCreateInfo == null )
//         {
//             // if the Method has no AMXCreateInfo, accept the defaults from the Class
//             amxCreateInfo = intf.getAnnotation( AMXCreateInfo.class );
//             if ( amxCreateInfo == null )
//             {
//                 cdebug( "No AMXCreateInfo found for interface " + intf.getName() );
//             }
//         }
//         
//         // if not on the method, check the interface
//         if ( amxCreateInfo == null )
//         {
//             // this is OK if there are no ordered parameters eg no parameters or an optional map only
//             if ( numArgs != 0 )
//             {
//                 throw new IllegalArgumentException(
//                     "Method " + m.getName() + " must be annotated with " + AMXCreateInfo.class.getName() );
//             }
//         }
//         return amxCreateInfo;
//     }
//     
//         private void
//     checkThatTypeMatches( final String type, final String operationName )
//     {
//         // Verify that the type matches the type expected from the operation name
//         final String altJ2EEType = operationName.substring( CREATE_PREFIX.length(), operationName.length() );
//         if ( ! type.equals(altJ2EEType) )
//         {
//             throw new IllegalArgumentException( "checkThatTypeMatches: type " + type + " != " + altJ2EEType );
//         }
//     }
    
    
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
        throws ClassNotFoundException, TransactionFailure
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
        final AMXConfigProxy newAMX = AMXConfigProxy.class.cast( getProxyFactory().getProxy( objectName, AMXConfigProxy.class) );
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
        final String type = (String)args[0];
        // for now, the only allowed values are of type String
        final Map<String,Object> params = TypeCast.checkMap(TypeCast.asMap(args[1]), String.class, Object.class);
        
        final Class<? extends ConfigBeanProxy>  intf = getConfigBeanProxyClassForContainedType(type);
        if ( intf == null )
        {
            throw new IllegalArgumentException( "ConfigBean of type " + getConfigBean().getProxyType() +
                " does not support sub-element of type " + type );
        }
        
        return createChild( intf, params );
    }
    
        private ObjectName
    createChild( final Class<? extends ConfigBeanProxy> intf, final Map<String,Object> params )
        throws ClassNotFoundException, TransactionFailure
    {
        final ConfigBeanJMXSupport spt = ConfigBeanJMXSupportRegistry.getInstance( intf );
        if ( ! spt.isSingleton() )
        {
            if ( params == null )
            {
                throw new IllegalArgumentException( "Named element requires at least its name" );
            }
            final Set<String> requiredAttrs = spt.requiredAttributeNames();
            for( final String s : requiredAttrs )
            {
                if ( ! params.containsKey(s) )
                {
                    throw new IllegalArgumentException( "Required attribute missing: " + s );
                }
            }
        }
        
        final CreateConfigArgSupport argSpt = new CreateConfigArgSupport( params );
        final List<ConfigSupport.AttributeChanges> changes = toAttributeChanges(params);
        
        return finishCreate( intf, argSpt, changes);
    }
    
        private ObjectName
    createPropertyChild( final String type, final String name, final String value )
        throws ClassNotFoundException, TransactionFailure
    {
        final Class<? extends ConfigBeanProxy>  intf =
            ConfigSupport.getElementTypeByName( getConfigBeanProxy(), type );
            
        final Map<String,Object> attrs = new HashMap<String,Object>();
        attrs.put( AMXConstants.ATTR_NAME, name );
        attrs.put( "Value", value );
        return createChild( intf, attrs );
    }



//        protected ObjectName
//    createConfig(
//         final String operationName,
//         final Object[] args,
//         String[]	   types)
//         throws ClassNotFoundException, TransactionFailure
//    {
//         if ( ! isCreateConfig( operationName ) )
//         {
//             throw new IllegalArgumentException( "Illegal method name for create: " + operationName );
//         }
//         ObjectName  result  = null;
//         
//         //
//         // Parse out the arguments
//         //
//         final CreateConfigArgSupport argSpt = new CreateConfigArgSupport( operationName, args, types );
//         
//         final Method m = getCreateMethod( operationName, types );
//         final Class<? extends AMXConfigProxy> returnType = TypeCast.asClass(m.getReturnType());
//         checkPropertiesSupported( returnType, argSpt );
//         
//         final String type = Util.deduceType( returnType );
//         checkThatTypeMatches( type, operationName );
//         cdebug( "createConfig: type = " + type + ", return type = " + returnType.getName() );
//                         
//         final AMXCreateInfo amxCreateInfo = getAMXCreateInfo( m, returnType, argSpt.numArgs() );
//         final String[] paramNames = amxCreateInfo.paramNames();
//         cdebug( "createConfig:  paramNames = {" + StringUtil.toString(paramNames) + "}" );
//         argSpt.addExplicitAttrs( paramNames );
//     
//         final ConfigBeanJMXSupport spt = ConfigBeanJMXSupportRegistry.getInstance( getConfigBean().getProxyType() );
//         if ( spt == null )
//         {
//             throw new IllegalArgumentException( "Can't find ConfigBean @Configured class for AMX type " + type );
//         }
//         final Class<? extends ConfigBeanProxy>  newItemClass = spt.getConfigBeanProxyClassFor(type);
//         
//         //final ContainedTypeInfo   subInfo = new ContainedTypeInfo( getConfigBean() );
//         //final Class<? extends ConfigBeanProxy>  newItemClass = subInfo.getConfigBeanProxyClassFor(type);
//         //if ( newItemClass == null )
//         //{
//        //     throw new IllegalArgumentException( "Can't find ConfigBean @Configured class for AMX type " + type );
//         ///}
//         
//     /*
//         final AMXConfigInfoResolver resolver = subInfo.getAMXConfigInfoResolverFor( type );
//         if ( resolver.amxInterface() != returnType )
//         {
//             throw new IllegalArgumentException();
//         }
//     */
//   
//         cdebug( "calling ConfigSupport.createAndSet() " );
//         ConfigBean newConfigBean = null;
//         
//         final List<ConfigSupport.AttributeChanges> changes = toAttributeChanges(argSpt.getAttrs());
//         final ObjectName objectName = finishCreate( newItemClass, argSpt, changes);
//     
//         return objectName;
//    }
   
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
                        propClass = (Class<ConfigBeanProxy>)Class.forName("org.glassfish.api.admin.config.Property");
                    }
                    catch( final ClassNotFoundException e )
                    {
                        throw new TransactionFailure("Can't find org.glassfish.api.admin.config.Property");
                    }
                        
                    for( final String propertyName : mProperties.keySet() )
                    {
                        final String propertyValue = mProperties.get(propertyName);
                        
                        final ConfigBeanProxy propChild = item.allocateProxy(propClass);
                        final ConfigBean child = (ConfigBean)Dom.unwrap(propChild);
                        final ConfigBeanProxy childW = ConfigSupport.getWriteableView(propChild);
                        
                        //ConfigModel.Property modelProp = getConfigModel_Property( "name");
                        childW.setter( modelProp, propertyName, String.class);
                        
                        //modelProp = NameMappingHelper.getConfigModel_Property( "value");
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
        final AMXConfigProxy amxConfig,
        final Map<String,String> properties, 
        final Map<String,String> systemProperties )
        throws ClassNotFoundException, TransactionFailure
    {
        if ( properties.keySet().size() != 0 )
        {
            for( final String propName : properties.keySet() )
            {
                final String propValue = properties.get(propName);
                cdebug ("################## Creating property " + propName + " = " + propValue );
                createPropertyChild( PropertyConfig.AMX_TYPE, propName, propValue );
            }
        }
        
        if ( systemProperties.keySet().size() != 0 )
        {
            final SystemPropertiesAccess pa = SystemPropertiesAccess.class.cast( amxConfig );
            for( final String propName : systemProperties.keySet() )
            {
                final String propValue = systemProperties.get(propName);
                debug ("################## Creating System property " + propName + " = " + propValue );
                createPropertyChild( SystemPropertyConfig.AMX_TYPE, propName, propValue );
            }
        }
    }
    

    private static final Set<String> CR_PREFIXES =
        SetUtil.newUnmodifiableStringSet(
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
    
        protected void
    removeSelf()
    {
        final ObjectName objectName = getObjectName();
        final ConfigBean configBean = this.getConfigBean();
        
        try
        {
            cdebug( "REMOVING config of class " + configBean.getProxyType().getName() + " from  parent of type " + 
                getConfigBean().getProxyType().getName() + ", ObjectName = " + JMXUtil.toString(objectName) );
            ConfigSupport.deleteChild( configBean, this.getConfigBean() );
        }
        catch( final TransactionFailure tf )
        {
            throw new RuntimeException( "Transaction failure deleting " + JMXUtil.toString(objectName), tf );
        }

        // NOTE: MBeans unregistered asynchronously by AMXConfigLoader
        // enforce synchronous semantics to clients by waiting until this happens
        final UnregistrationListener myListener = new UnregistrationListener( getMBeanServer(), objectName);
        final long TIMEOUT_MILLIS = 10 * 1000;

        final boolean unregisteredOK = myListener.waitForUnregister( TIMEOUT_MILLIS );
        if ( ! unregisteredOK )
        {
            throw new RuntimeException( "Something went wrong unregistering MBean " + JMXUtil.toString(objectName) );
        }

    }
    
    /**
        Generic removal of any config contained by this config.
     */
        public final void
    removeConfig( final ObjectName childObjectName )
    {
	    preRemove( childObjectName );
        
        try
        {
            getMBeanServer().invoke( childObjectName, "removeSelf", null, null );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException("Problem deleting " + childObjectName, e);
        }
    }

    /**
        Generic removal of any config contained by this config.
     */
        public final void
    removeConfig( final String type, final String name )
    {
        final ObjectName    childObjectName  = child(type, name);
        if ( childObjectName == null )
        {
            throw new RuntimeException( new InstanceNotFoundException( "No MBean named " + name + " of type " + type + " found." ) );
        }
        removeConfig( childObjectName );
    }
    
	    protected String
	operationNameToType( final String operationName )
    {
        String  type   = null;
        
        if ( isRemoveConfig( operationName ) ||
              isCreateConfig( operationName )  )
        {
            type   = operationNameToSimpleClassname( operationName );
        }
        return type;
    }


   /**
        Remove config for a singleton Containee.
    */
      protected void
   removeConfig( final String operationName)
   {
        final String        type    = operationNameToType( operationName );
        final ObjectName    childObjectName  = child( type );
        if ( childObjectName == null )
        {
            throw new RuntimeException( new InstanceNotFoundException( type ) );
        }
        removeConfig( childObjectName );
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
//         if ( args == null || args.length == 0 )
//         {
//             cdebug( "removeConfig: by operation name only" );
//             // remove a singleton
//             removeConfig( operationName );
//         }
//         else
        if ( args.length == 1 )
        {
            cdebug( "removeConfig: by operationName + name" );
            // remove by name, type is implicit in method name
            removeConfig( operationNameToType(operationName), (String)args[0] );
        }
        else if ( args.length == 2 )
        {
            cdebug( "removeConfig: by  type + name" );
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
    
        private Object
    invokeDuckMethod(
        final ConfigBeanJMXSupport.DuckTypedInfo  info,
		Object[]	         args )
        throws MBeanException
    {
        try
        {
            cdebug( "invokeDuckMethod(): invoking: " + info.name() + " on " + info.method().getDeclaringClass() );
            
            if ( ! info.method().getDeclaringClass().isAssignableFrom( getConfigBeanProxy().getClass() ) )
            {
                throw new IllegalArgumentException( "invokeDuckMethod: " + getConfigBean().getProxyType() + " not asssignable to " + info.method().getDeclaringClass() );
            }
            
            final Object result = info.method().invoke( getConfigBeanProxy(), args);
            
            cdebug( "invokeDuckMethod(): invoked: " + info.name() + ", got " + result );
            
            return result;
        }
        catch( final Exception e )
        {
            throw new MBeanException(e);
        }
    }
    
    /**
        Automatically figure out get<abc>Factory(), 
        create<Abc>Config(), remove<Abc>Config().
        
     */
    @Override
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
        
        ConfigBeanJMXSupport.DuckTypedInfo duckTypedInfo = null;
        final ConfigBeanJMXSupport spt = getConfigBeanJMXSupport();
        if ( isGenericCreateConfig( operationName ) )
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
	    else if ( isRemoveConfig( operationName, args, types ) )
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
        else if ( (duckTypedInfo = getConfigBeanJMXSupport().findDuckTyped(operationName, types)) != null )
        {
            result = invokeDuckMethod( duckTypedInfo, args );
        }
	    else
	    {
	        result  = super.invokeManually( operationName, args, types );
	    }
	    return result;
	}
	
		public void
	sendConfigCreatedNotification( final ObjectName configObjectName )
	{
		sendNotification( AMXConfigProxy.CONFIG_CREATED_NOTIFICATION_TYPE,
		    AMXConfigProxy.CONFIG_REMOVED_NOTIFICATION_TYPE,
			AMXConfigProxy.CONFIG_OBJECT_NAME_KEY, configObjectName );
	}
	
		public void
	sendConfigRemovedNotification( final ObjectName configObjectName )
	{
		sendNotification( AMXConfigProxy.CONFIG_REMOVED_NOTIFICATION_TYPE,
		    AMXConfigProxy.CONFIG_REMOVED_NOTIFICATION_TYPE,
			AMXConfigProxy.CONFIG_OBJECT_NAME_KEY, configObjectName );
	}
    
    private final ConfigBeanJMXSupport getConfigBeanJMXSupport()
    {
        return ConfigBeanJMXSupportRegistry.getInstance( getConfigBean() );
    }
    
    
        private static final Map<String,String>
    getDefaultValues( final Class<? extends ConfigBeanProxy> intf, boolean useAMXAttributeNames)
    {
        return ConfigBeanJMXSupportRegistry.getInstance(intf).getDefaultValues( useAMXAttributeNames );
    }
    
        public final Map<String,String>
    getDefaultValues( final String type, final boolean useAMXAttributeNames )
    {
        final Class<? extends ConfigBeanProxy> intf = getConfigBeanProxyClassForContainedType( type );
        
        return getDefaultValues( intf, useAMXAttributeNames );
    }
    
        public final Map<String,String>
    getDefaultValues( final boolean useAMXAttributeNames )
    {
        return getDefaultValues( mConfigBean.getProxyType(), useAMXAttributeNames );
    }
           
        private Class<? extends ConfigBeanProxy>
    getConfigBeanProxyClassForContainedType( final String type )
    {
        final ConfigBeanJMXSupport spt = getConfigBeanJMXSupport();
        if ( spt == null )
        {
            throw new IllegalArgumentException( "Can't find ConfigBean @Configured class for AMX type " + type );
        }
        return spt.getConfigBeanProxyClassFor(type);
    }
    
//     private String getAMXAttributeName(final String type, final String xmlName )
//     {
//         // FIXME
//         System.out.println( "AMXConfigImpl.getAMXAttributeName() FIXME: " + xmlName);
//         return xmlName;
//     }
//     
//         private Map<String,String>
//     toAMXAttributeNames( final String type, final Map<String,String> xmlMap )
//     {
//         final Map<String,String> m = new HashMap<String,String>();
//         
//         for( final String xmlName : xmlMap.keySet() )
//         {
//             final String amxName = getAMXAttributeName( type, xmlName);
//             final String value = xmlMap.get(xmlName);
//             if ( amxName == null )
//             {
//                 System.out.println( "No corresponding AMX attribute name for xml attribute " + xmlName );
//                 // if the AMX interface is out of date with respect to the underlying config, return
//                 // the name as-is
//                 m.put(xmlName, value);
//             }
//             else
//             {
//                 m.put(amxName, value);
//             }
//         }
//         return m;
//     }
//     
//       /**
//         Issue an AttributeChangeNotification.  The name is the xml name; this method should
//         be called only from "bubble up" code from the lower level ConfigBean stuff.
//         @see AMXConfigLoader
//         */
//         void
//     issueAttributeChangeForXmlAttrName(
//         final String     xmlAttrName,
//         final Object     oldValue,
//         final Object     newValue,
//         final long       whenChanged )
//     {
//         String attrType = String.class.getName();
//         String amxAttrName = getAMXAttributeName( getType(), xmlAttrName );
//         if ( amxAttrName == null )
//         {
//             cdebug( "issueAttributeChangeForXmlAttrName: can't find AMX name for: " + xmlAttrName + ", using xmlName for now" );
//             amxAttrName = xmlAttrName;
//         }
//         else
//         {
//             attrType = getAttributeType(amxAttrName);
//         }
//         
//         if ( oldValue != newValue )
//         {
// 			sendAttributeChangeNotification( "", amxAttrName, attrType, whenChanged, oldValue, newValue );
//         }
//     }
    
    
    @Override
    protected String[] attributeNameToType( final String attributeName )
    {
        return new String[] { ConfigBeanJMXSupport.typeFromName(attributeName), attributeName };
    }


    @Override
		protected Object
	getAttributeManually( final String name )
		throws AttributeNotFoundException, ReflectionException, MBeanException
	{
        return getAttributeFromConfigBean( name );
    }


//-------------------------------------------------------------
    
    /**
        Get an Attribute.  This is a bit tricky, because the target can be an XML attribute,
        an XML string element, or an XML list of elements.
     */
		protected final Object
	getAttributeFromConfigBean( final String amxName )
	{
        Object result = null;
        
        final MBeanAttributeInfo attrInfo = getAttributeInfo(amxName);
        final String xmlName = ConfigBeanJMXSupport.xmlName( attrInfo, amxName );
        final boolean isAttribute = ConfigBeanJMXSupport.isAttribute(attrInfo);
        
        if ( isAttribute )
        {
            result = mConfigBean.rawAttribute( xmlName );
        }
        else if ( ConfigBeanJMXSupport.isElement(attrInfo) )
        {
            if ( String.class.getName().equals( attrInfo.getType() ) )
            {
                final List<?> leaf = mConfigBean.leafElements(xmlName);
                if ( leaf != null ) {
                    try
                    {
                        result = (String)leaf.get(0);
                    }
                    catch( final Exception e )
                    {
                        // doesn't exist, return null
                    }
                }
            }
            else if ( attrInfo.getType() == String[].class.getName() )
            {
                //final String elementClass = (String)d.getFieldValue( DESC_ELEMENT_CLASS );
            
                final List<?> leaf = mConfigBean.leafElements(xmlName);
                if ( leaf != null ) {
                    // verify that it is List<String> -- no other types are supported in this way
                    final List<String> elems = TypeCast.checkList( leaf, String.class );
                    result = CollectionUtil.toArray( elems, String.class);
                }
            }
            else {
                throw new IllegalArgumentException("getAttributeFromConfigBean: unsupported return type: " + attrInfo.getType() );
            }
        }
        //debug( "Attribute " + amxName + " has class " + ((result == null) ? "null" : result.getClass()) );
        return result;
	}
    
    private static final class MyTransactionListener implements TransactionListener
    {
        private final List<PropertyChangeEvent> mChangeEvents = new ArrayList<PropertyChangeEvent>();
        private final ConfigBean    mTarget;
        
        MyTransactionListener( final ConfigBean target ) { mTarget = target;}
        
        public void transactionCommited(List<PropertyChangeEvent> changes) {
            // include only events that match the desired config bean; other transactions
            // could generate events on other ConfigBeans. For that matter, it's unclear
            // why more than one transaction on the same ConfigBean couldn't be "heard" here.
            for( final PropertyChangeEvent event : changes )
            {
                final Object source = event.getSource();
                if ( source instanceof ConfigBeanProxy )
                {
                    final Dom dom = Dom.unwrap( (ConfigBeanProxy)source );
                    if ( dom instanceof ConfigBean )
                    {
                        if ( mTarget == (ConfigBean)dom )
                        {
                            mChangeEvents.add( event );
                        }
                    }
                }
            }
        }

        public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {
            // amx probably does not care that some changes were not processed successfully
            // and will require a restart
        }

        List<PropertyChangeEvent>  getChangeEvents() { return mChangeEvents; }
    };

    /**
        Make a Map keyed by the property name of the PropertyChangeEvent, verifying that each
        name is non-null.
     */
        private  Map<String,PropertyChangeEvent>
    makePropertyChangeEventMap( final List<PropertyChangeEvent> changeEvents )
    {
        final Map<String,PropertyChangeEvent>   m = new HashMap<String,PropertyChangeEvent>();
        
        for( final PropertyChangeEvent changeEvent : changeEvents )
        {
            if ( changeEvent.getPropertyName() == null )
            {
                throw new IllegalArgumentException( "PropertyChangeEvent property names must be specified" );
            }
            
            m.put( changeEvent.getPropertyName(), changeEvent );
        }
        return m;
    }
    
    private void joinTransaction( final Transaction t, final WriteableView writeable ) 
        throws TransactionFailure
    {
        if ( ! writeable.join(t) )
        {
            t.rollback();
            throw new TransactionFailure("Cannot enlist " + writeable.getProxyType() + " in transaction",null);
        }
    } 
        
        private static void
    commit( final Transaction t )
        throws TransactionFailure
    {
        try
        {
            t.commit();
        }
        catch ( final RetryableException e)
        {
            t.rollback();
            throw new TransactionFailure(e.getMessage(), e);
        }
        catch ( final TransactionFailure e) {
            cdebug("failure, not retryable...");
            t.rollback();
            throw e;
        }
    }
    
        static <T extends ConfigBeanProxy> WriteableView
     getWriteableView(final T s, final ConfigBean sourceBean)
        throws TransactionFailure {
        final WriteableView f = new WriteableView(s);
        if (sourceBean.getLock().tryLock()) {
            return f;
        }
        throw new TransactionFailure("Config bean already locked " + sourceBean, null);
    }
    
    private static Type getCollectionGenericType() 
    {
        try
        {
            return ConfigSupport.class.getDeclaredMethod("defaultPropertyValue", (Class[])null).getGenericReturnType();
        }
        catch( NoSuchMethodException e )
        {
            // not supposed to happen, throw any reasonabl exception
            throw new IllegalArgumentException();
        }
    }    
    
    private static boolean isCollectionCmd( final String s )
    {
        return s != null &&
            (s.equals(OP_ADD) || s.equals(OP_REMOVE) || s.equals(OP_REPLACE) );
    }
    

//         public String[]
//     getAnonymousElementList( final String elementName )
//     {
//         return (String[])getAttributeFromConfigBean( elementName );
//     }
//     
//         public String[]
//     modifyAnonymousElementList(
//         final String   elementName,
//         final String   cmd,
//         final String[] values)
//     {
//         //cdebug( "modifyAnonymousElementList: " + elementName + ", " + cmd + ", {" + StringUtil.toString(values) + "}" );
//         getAnonymousElementList(elementName); // force an error right away if it's a bad name
//         
//         final String xmlName = mNameMappingHelper.getXMLName(elementName, true);
//         try
//         {
//             final ModifyCollectionApplyer mca = new ModifyCollectionApplyer( mConfigBean, xmlName, cmd, values );
//             mca.apply();
//             return ListUtil.toStringArray(mca.mResult);
//         }
//         catch( final TransactionFailure e )
//         {
//             throw new RuntimeException( "Could not modify element collection " + elementName, e);
//         }
//     }
//     
//     
//         public String[]
//     modifyAnonymousElementList(
//         final String   elementName,
//         final String   cmd,
//         final String[] values)
//     {
//         return getConfigDelegate().modifyAnonymousElementList(elementName, cmd, values);
//    }


    /**
        Handle an update to a collection, returning the List<String> that results.
     */
        private List<String>
    handleCollection(
        final WriteableView writeable,
        final ConfigModel.Property prop,
        final String        cmd,
        final List<String>  argValues )
    {
        if ( ! isCollectionCmd(cmd) )
            throw new IllegalArgumentException(""+cmd);
            
        final Object o = writeable.getter(prop, getCollectionGenericType());
        final List<String> masterList = TypeCast.checkList( TypeCast.asList(o), String.class);
        
        //cdebug( "Existing values: {" + CollectionUtil.toString( masterList ) + "}");
        //cdebug( "Arg values: {" + CollectionUtil.toString( argValues ) + "}");

        if ( cmd.equals( OP_REPLACE ) )
        {
            masterList.retainAll( argValues );
            for( final String s : argValues )
            {
                if ( ! masterList.contains(s) )
                {
                    masterList.add(s);
                }
            }
            //cdebug( "Master list after OP_REMOVE: {" + CollectionUtil.toString( masterList ) + "}");
        }
        else if ( cmd.equals( OP_REMOVE ) )
        {
            masterList.removeAll( argValues );
            //cdebug( "Master list after OP_REMOVE: {" + CollectionUtil.toString( masterList ) + "}");
        }
        else if ( cmd.equals( OP_ADD ) )
        {
            // eliminate duplicates for now unless there is a good reason to allow them
            final List<String> temp = new ArrayList<String>(argValues);
            temp.removeAll(masterList);
            
            masterList.addAll(temp);
            //cdebug( "Master list after OP_ADD: {" + CollectionUtil.toString( masterList ) + "}");
        }
        else
        {
            throw new IllegalArgumentException(cmd);
        }
        
        //cdebug( "Existing values list before commit: {" + CollectionUtil.toString( masterList ) + "}");
        return new ArrayList<String>(masterList);
    }
    
    private class Applyer
    {
        final Transaction   mTransaction;
        final ConfigBean    mConfigBean;
        final WriteableView mWriteable;
        
        public Applyer( final ConfigBean cb ) throws TransactionFailure { this(cb, new Transaction()); }
        public Applyer( final ConfigBean cb, final Transaction t)
            throws TransactionFailure
        {
            mConfigBean = cb;
            mTransaction = t;
            
            final ConfigBeanProxy readableView = cb.getProxy( cb.getProxyType() );
            mWriteable = getWriteableView(readableView, cb );
        }
        
        protected void makeChanges() 
            throws TransactionFailure  {}
        
        final void apply()
            throws TransactionFailure
        {
            try
            {
                joinTransaction(mTransaction, mWriteable);
                
                makeChanges();
                
                commit(mTransaction);
            }
            finally
            {
                mConfigBean.getLock().unlock();
            }
        }
    }
    
        protected ConfigModel.Property
    getConfigModel_Property( final String xmlName ) {
        final ConfigModel.Property cmp = mConfigBean.model.findIgnoreCase(xmlName);
        if (cmp == null) {
            throw new IllegalArgumentException( "Illegal name: " + xmlName );
        }
        return cmp;
    }
    
    private final class ModifyCollectionApplyer extends Applyer
    {
        private volatile List<String> mResult;
        private final String   mElementName;
        private final String   mCmd;
        private final String[] mValues;
        
        public ModifyCollectionApplyer(
            final ConfigBean    cb,
            final String elementName,
            final String cmd,
            final String[] values )
            throws TransactionFailure
        {
            super( cb );
            mElementName = elementName;
            mCmd = cmd;
            mValues = values;
            mResult = null;
        }
        
        protected void makeChanges()
            throws TransactionFailure
        {
            final ConfigModel.Property prop = getConfigModel_Property(mElementName);
            mResult = handleCollection( mWriteable, prop, mCmd, ListUtil.asStringList(mValues));
        }
    }
    
    private final class MakeChangesApplyer extends Applyer
    {
        private final Map<String,Object> mChanges;
        
        public MakeChangesApplyer(
            final ConfigBean cb,
            final Map<String,Object> changes)
            throws TransactionFailure

        {
            super(cb);
            mChanges = changes;
        }
                
        protected void makeChanges()
            throws TransactionFailure
        {
            for ( final String xmlName : mChanges.keySet() )
            {
                final Object value = mChanges.get(xmlName);
                final ConfigModel.Property prop = getConfigModel_Property(xmlName);

                if ( prop.isCollection() )
                {
                    final List<String> results = handleCollection( mWriteable, prop, OP_REPLACE, ListUtil.asStringList(value) );
                }
                else if ( value == null || (value instanceof String) )
                {
                    mWriteable.setter( prop, value, String.class);
                }
                else
                {
                    throw new TransactionFailure( "Illegal data type for attribute " + xmlName + ": " + value.getClass().getName() );
                }
            }
        }
    }
    
    private void apply(
        final ConfigBean cb,
        final Map<String,Object> changes )
        throws TransactionFailure
    {
        final MakeChangesApplyer mca = new MakeChangesApplyer( mConfigBean, changes );
        mca.apply();
    }
        
   
       private Map<String,Object>
    mapNamesAndValues(
        final Map<String,Object> amxAttrs,
        final Map<String,Object> noMatch )
    {
        final Map<String,Object> xmlAttrs = new HashMap<String,Object>();
        
        final Map<String,MBeanAttributeInfo> attrInfos = getAttributeInfos();
        
        for( final String amxAttrName : amxAttrs.keySet() )
        {
            final Object valueIn = amxAttrs.get(amxAttrName);
            
            final MBeanAttributeInfo attrInfo = attrInfos.get(amxAttrName);
            if ( attrInfo == null )
            {
                debug( "WARNING: setAttributes(): no MBeanAttributeInfo found for: " + amxAttrName );
                noMatch.put( amxAttrName, valueIn);
                continue;
            }
            final String xmlName = ConfigBeanJMXSupport.xmlName( attrInfo, amxAttrName );
            
            if ( xmlName != null )
            {
                final Object value   = valueIn == null ? null : "" + valueIn;
                if ( value != valueIn )
                {
                    //debug( "Attribute " + amxAttrName + " auto converted from " + valueIn.getClass().getName() + " to " + value.getClass().getName() );
                }
                
                // We accept only Strings, String[] or null
                if ( valueIn == null || (value instanceof String))
                {
                    xmlAttrs.put( xmlName, (String)value);
                }
                else if ( false /*isCollection(xmlName)*/ )
                {
                    if ( (valueIn instanceof String[]) || (valueIn instanceof List) )
                    {
                        xmlAttrs.put( xmlName, ListUtil.asStringList(valueIn) );
                    }
                    else
                    {
                        noMatch.put( amxAttrName, valueIn );
                    }
                }
                else
                {
                    noMatch.put( amxAttrName, valueIn );
                }
               // debug( "Attribute " + amxAttrName + "<=>" + xmlName + " is of class " + ((value == null) ? null : value.getClass().getName()) );
            }
            else
            {
                debug( "WARNING: setAttributes(): no xmlName match found for AMX attribute: " + amxAttrName );
                noMatch.put( amxAttrName, valueIn );
            }
        }
        
        return xmlAttrs;
    }


		public AttributeList
	setAttributesInConfigBean( final AttributeList attrsIn, final Map<String,Object> oldValues )
	{
        oldValues.clear();
        
        // now map the AMX attribute names to xml attribute names
        final Map<String, Object> amxAttrs = JMXUtil.attributeListToValueMap( attrsIn );
        final Map<String,Object>  notMatched = new HashMap<String,Object>();
        final Map<String,Object>  xmlAttrs = mapNamesAndValues( amxAttrs, notMatched);
        
        if ( notMatched.keySet().size() != 0 )
        {
            cdebug( "setAttributes: failed to map these AMX attributes: {" + CollectionUtil.toString( notMatched.keySet(), ", ") + "}" );
        }
        
        final AttributeList successfulAttrs = new AttributeList();
        
        final Transactions transactions = mConfigBean.getHabitat().getComponent(Transactions.class);
        
        if ( xmlAttrs.size() != 0 )
        {
            //cdebug( "DelegateToConfigBeanDelegate.setAttributes(): " + attrsIn.size() + " attributes: {" +
           //     CollectionUtil.toString(amxAttrs.keySet()) + "} mapped to xml names {" + CollectionUtil.toString(xmlAttrs.keySet()) + "}");
            
            final MyTransactionListener  myListener = new MyTransactionListener( mConfigBean );
            transactions.addTransactionsListener(myListener);
                
            // results should contain only those that succeeded which will be all or none
            // depending on whether the transaction worked or not
            try
            {
                final MakeChangesApplyer mca = new MakeChangesApplyer( mConfigBean, xmlAttrs );
                mca.apply();

                // use 'attrsIn' vs 'attrs' in case not all values are 'String'
                successfulAttrs.addAll( attrsIn );
            }
            catch( final TransactionFailure tf )
            {
                // empty results -- no Exception should be thrown per JMX spec
                cdebug( ExceptionUtil.toString(tf) );
            }
            finally
            {
                transactions.waitForDrain();

                transactions.removeTransactionsListener(myListener);
            }
        
            // determine later the best way to handle AttributeChangeNotification
            // It can get ugly at this level; the config code will issue a different event
            // for every single <jvm-options> element (for example)
            /*
            if ( successfulAttrs.size() != 0 )
            {
                // verify that the size of the PropertyChangeEvent list matches
                final List<PropertyChangeEvent> changeEvents = myListener.getChangeEvents();
                if ( successfulAttrs.size() != changeEvents.size() )
                {
                    throw new IllegalStateException( "List<PropertyChangeEvent> size=" + changeEvents.size() +
                        " does not match the number of Attributes, size = " + successfulAttrs.size() );
                }
                
                //
                // provide details on old values for the caller. Note that config always returns
                // type 'String' which no ability to map back to 'Integer', etc, so the MBeanInfo info
                // of the MBean should not be using anything but String.
                // 
                final Map<String,PropertyChangeEvent> eventsMap = makePropertyChangeEventMap( changeEvents );
                final Map<String, String> attrsS = JMXUtil.attributeListToStringMap( successfulAttrs );
                
                // supply all the old values to caller using the AMX attribute name
                for( final String amxAttrName : attrsS.keySet() )
                {
                    final PropertyChangeEvent changeEvent = eventsMap.get( mNameMappingHelper.getXMLName( amxAttrName ) );
                    oldValues.put( amxAttrName, changeEvent.getOldValue() );
                }
            }
            */
        }
    
		return successfulAttrs;
	}

    
}





















