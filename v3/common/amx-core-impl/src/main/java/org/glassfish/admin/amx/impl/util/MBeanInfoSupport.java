/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.amx.impl.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.base.Singleton;
import org.glassfish.admin.amx.core.AMXConstants;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.AMX_SPI;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.api.amx.AMXMBeanMetadata;
import static org.glassfish.admin.amx.core.AMXConstants.*;

/**
 *
 * @author llc
 */
public final class MBeanInfoSupport {
    private MBeanInfoSupport() {}

    private static void debug(final Object o)
    {
        System.out.println(o.toString() );
    }

    private static MBeanInfo amxspiMBeanInfo = null;
    public static synchronized MBeanInfo getAMX_SPIMBeanInfo()
    {
        if ( amxspiMBeanInfo == null ) {
            amxspiMBeanInfo = MBeanInfoSupport.getMBeanInfo(AMX_SPI.class);
        }
        return amxspiMBeanInfo;
    }

        public static <T extends AMX_SPI> MBeanInfo
    getMBeanInfo( final Class<T> intf )
    {
		final Map<String,Method>	getters			= new HashMap<String,Method>();
		final Map<String,Method>	setters			= new HashMap<String,Method>();
		final Map<String,Method>	getterSetters	= new HashMap<String,Method>();
		final Set<Method>	operations		= new HashSet<Method>();

        findInterfaceMethods(intf, getters, setters, getterSetters, operations);

        if ( ! AMX_SPI.class.isAssignableFrom(intf))
        {
            findInterfaceMethods(AMX_SPI.class, getters, setters, getterSetters, operations);
        }


		final List<MBeanAttributeInfo>	attrsList	=
			generateMBeanAttributeInfos( getterSetters.values(), getters.values(), setters.values() );

		final MBeanOperationInfo[]	operationInfos	= generateMBeanOperationInfos( operations );

		final MBeanConstructorInfo[]	constructorInfos	= null;
		final MBeanNotificationInfo[]	notificationInfos	= null;

        // might or might not have metadata
        final AMXMBeanMetadata meta = intf.getAnnotation(AMXMBeanMetadata.class);

        final boolean singleton = Singleton.class.isAssignableFrom(intf) || (meta != null && meta.singleton());
        final String  pathPart = Util.deduceType(intf);
        final String  group = AMXConstants.GROUP_OTHER;
        final boolean isLeaf = meta != null && meta.leaf();
        final boolean supportsAdoption = ! isLeaf;

        if ( isLeaf )
        {
            JMXUtil.remove( attrsList, AMXConstants.ATTR_CHILDREN);
        }

        final Descriptor d = mbeanDescriptor( true,
            intf,
            singleton,
            pathPart,
            group,
            supportsAdoption,
            null
        );
        
        

		final MBeanAttributeInfo[]	attrInfos	= new MBeanAttributeInfo[ attrsList.size() ];
		attrsList.toArray( attrInfos );
 
		final MBeanInfo	mbeanInfo	= new MBeanInfo(
				intf.getName(),
				intf.getName(),
				attrInfos,
				constructorInfos,
				operationInfos,
				notificationInfos,
                d );
        //debug( "MBeanInfoSupport.getMBeanInfo(): " + mbeanInfo );

		return( mbeanInfo );
    }

		public static void
	findInterfaceMethods( final Class<?> intf,
        final Map<String,Method> getters,
        final Map<String,Method> setters,
        final Map<String,Method> getterSetters,
        final Set<Method> operations
        )
	{
		final Method[]	methods	= intf.getMethods();

		for( final Method method : methods )
		{
			final String	methodName	= method.getName();
            
            final ManagedAttribute managedAttr = method.getAnnotation(ManagedAttribute.class);
            final ManagedOperation managedOp = method.getAnnotation(ManagedOperation.class);

            if ( managedAttr != null )
            {
                String attrName = null;
                if ( JMXUtil.isIsOrGetter( method ) )
                {
                    attrName	= JMXUtil.getAttributeName( method );
                    getters.put( attrName, method );
                    //debug( "findInterfaceMethods: getter: " + attrName );
                }
                else if ( JMXUtil.isSetter( method ) )
                {
                    attrName	= JMXUtil.getAttributeName( method );
                    setters.put( attrName, method );
                    //debug( "findInterfaceMethods: setter: " + attrName );
                }
                else
                {
                    //debug( "findInterfaceMethods: ignore: " + attrName );
                    // ignore
                }

                if ( (attrName != null) &&
                        getters.containsKey( attrName ) &&
                        setters.containsKey( attrName ) )
                {
                    final Method	getter	= getters.get( attrName );
                    final Class<?>	getterType	= getter.getReturnType();
                    final Class<?>	setterType	= setters.get( attrName ).getParameterTypes()[ 0 ];

                    if ( getterType == setterType )
                    {
                        getters.remove( attrName );
                        setters.remove( attrName );
                        getterSetters.put( attrName, getter );
                    //debug( "findInterfaceMethods: getter/setter: " + attrName );
                    }
                    else
                    {
                        throw new IllegalArgumentException( "Attribute " + attrName +
                            "has type " + getterType.getName() + " as getter but type " +
                            setterType.getName() + " as setter" );
                    }
                }
            }
			else if ( managedOp != null )
			{
				operations.add( method );
			}
		}

		/*
		java.util.Iterator	iter	= null;
		trace( "-------------------- getterSetters -------------------" );
		iter	= getterSetters.values().iterator();
		while ( iter.hasNext() )
		{
			trace( ((Method)iter.next()).getNameProp() + ", " );
		}
		trace( "-------------------- getters -------------------" );
		iter	= getters.values().iterator();
		while ( iter.hasNext() )
		{
			trace( ((Method)iter.next()).getNameProp() + ", " );
		}
		trace( "-------------------- setters -------------------" );
		iter	= setters.values().iterator();
		while ( iter.hasNext() )
		{
			trace( ((Method)iter.next()).getNameProp() + ", " );
		}
		*/
	}


    /**
     * Return MBeanAttributeInfo for the method.  If it's a getter,
     * it's marked as read-only, if it's a setter, it's marked as read/write.
     * @param m
     * @return
     */
    public static MBeanAttributeInfo attributeInfo( final Method m ){
        final ManagedAttribute managed = m.getAnnotation(ManagedAttribute.class);
        if ( managed == null) return null;

        final String methodName = m.getName();
        
        final Description d = m.getAnnotation(Description.class);
        final String description = d == null ? "n/a" : d.value();
        
        String attrName = JMXUtil.getAttributeName(m);
        final boolean isGetter = JMXUtil.isGetter(m);
        final boolean isSetter = JMXUtil.isSetter(m);
        final boolean isIs = JMXUtil.isIs(m);

        final MBeanAttributeInfo info =
                new MBeanAttributeInfo( attrName, m.getReturnType().getName(),
                    description, isGetter, isSetter, isIs, null);

        return info;
    }

    public static Class<?> returnType( final Class<?> clazz )
    {
        Class<?> type = clazz;
        if ( AMXProxy.class.isAssignableFrom(clazz))
        {
            type = ObjectName.class;
        }
        return type;
    }

		private static List<MBeanAttributeInfo>
	generateAttributeInfos(
		final Collection<Method> methods,
		final boolean	read,
		final boolean	write)
	{
		final List<MBeanAttributeInfo>	infos	= new ArrayList<MBeanAttributeInfo>();

		for( final Method m : methods )
		{
			final String	methodName	= m.getName();

            final String description = getDescription(m);

            String attrName = JMXUtil.getAttributeName(m);
            final boolean isIs = JMXUtil.isIs(m);

            // methods returning AMXProxy should return ObjectName
            Class<?> returnType = m.getReturnType();
            if ( AMXProxy.class.isAssignableFrom(returnType))
            {
                returnType = ObjectName.class;
            }

			final MBeanAttributeInfo info = new MBeanAttributeInfo(
				attrName,
				returnType(m.getReturnType()).getName(),
				methodName,
				read,
				write,
				JMXUtil.isIs(m) );
			infos.add( info );
            //debug( "Added MBeanAttributeInfo for: " + attrName );
		}

		return( infos );
	}

		public static List<MBeanAttributeInfo>
	generateMBeanAttributeInfos(
		final Collection<Method> getterSetters,
		final Collection<Method> getters,
		final Collection<Method> setters  )
	{
		final List<MBeanAttributeInfo>	attrsList	= new ArrayList<MBeanAttributeInfo>();

		attrsList.addAll( generateAttributeInfos( getterSetters, true, true ) );
		attrsList.addAll( generateAttributeInfos( getters, true, false ) );
		attrsList.addAll( generateAttributeInfos( setters, false, true ) );

		return( attrsList );
	}

        public static <T extends Annotation> T
    getAnnotation( final Annotation[] annotations, final Class<T> clazz)
    {
        T result = null;

        for( final Annotation a : annotations) {
            if ( a.annotationType() == clazz ){
                result = (T)a;
                break;
            }
        }
        return result;
    }
        
        private static String
    getDescription( final AnnotatedElement o )
    {
        final Description d = o.getAnnotation(Description.class);
        return d == null ? "n/a" : d.value();
    }

        public static MBeanParameterInfo[]
	parameterInfos( final Method method)
	{
        final Class<?>[] sig = method.getParameterTypes();
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        
		final MBeanParameterInfo[]	infos	= new MBeanParameterInfo[ sig.length ];
        
		for( int i = 0; i < sig.length; ++i )
		{
			final Class<?>	   paramClass	= sig[ i ];
            final Annotation[] annotations = paramAnnotations[i];

            String paramName = "p" + i;
            String description = paramClass.getName();
            final Description d = getAnnotation(annotations, Description.class);
            if ( d != null )
            {
                final String value = d.value();
                // interpret a '|' to mean name and description
                final int idx = value.indexOf("|");
                if ( idx > 0 )
                {
                    paramName = value.substring(0, idx);
                    description = value.substring(idx+1,value.length());
                }
                else
                {
                    description = value;
                }
            }
            
			final String type = paramClass.getName();

			final MBeanParameterInfo	info = new MBeanParameterInfo( paramName, type, description);
			infos[ i ]	= info;
		}

		return( infos );
	}

		public static MBeanOperationInfo[]
	generateMBeanOperationInfos(final Collection<Method> methods )
	{
		final MBeanOperationInfo[]	infos	= new MBeanOperationInfo[ methods.size() ];

		int	i = 0;
        for( final Method m : methods )
		{
            final ManagedOperation managed = m.getAnnotation(ManagedOperation.class);
            
			final String	methodName	= m.getName();
            final MBeanParameterInfo[] parameterInfos = parameterInfos(m);
            final int impact = managed == null ? MBeanOperationInfo.UNKNOWN : managed.impact();
            final String description = getDescription(m);
            final Descriptor descriptor = null;

			final MBeanOperationInfo	info = new MBeanOperationInfo(
				methodName,
				description,
				parameterInfos,
				returnType(m.getReturnType()).getName(),
				impact,
                descriptor
			);

			infos[ i ]	= info;
			++i;

		}

		return( infos );
	}

    public static DescriptorSupport mbeanDescriptor(
        final boolean immutable,
        final Class<?>  intf,
        final boolean singleton,
        final String  pathPart,
        final String  group,
        final boolean supportsAdoption,
        final String[] subTypes
        )
    {
        final DescriptorSupport desc = new DescriptorSupport();

        if ( pathPart == null || pathPart.length() == 0 ) {
            throw new IllegalArgumentException("pathPart may not be empty");
        }
        if ( intf == null || ! intf.isInterface() ) {
            throw new IllegalArgumentException("interface class must be an interface");
        }

        desc.setField( DESC_STD_IMMUTABLE_INFO, immutable );
        desc.setField( DESC_STD_INTERFACE_NAME, intf.getName() );
        desc.setField( DESC_IS_SINGLETON, singleton );
        //desc.setField( DESC_PATH_PART, pathPart );
        desc.setField( DESC_GROUP, group );
        desc.setField( DESC_SUPPORTS_ADOPTION, supportsAdoption );
        
        if ( subTypes != null ) {
            desc.setField( DESC_SUB_TYPES, subTypes );
        }

        return desc;
    }
}


