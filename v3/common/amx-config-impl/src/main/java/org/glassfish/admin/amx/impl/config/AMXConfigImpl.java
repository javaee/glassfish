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
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.AbstractQueue;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.concurrent.LinkedBlockingDeque;
import javax.management.*;

import org.glassfish.admin.amx.annotation.Stability;
import org.glassfish.admin.amx.annotation.Taxonomy;
import static org.glassfish.admin.amx.core.AMXConstants.*;
import org.glassfish.admin.amx.core.AMXProxy;
import static org.glassfish.admin.amx.config.AMXConfigConstants.*;
import static org.glassfish.admin.amx.impl.config.ConfigBeanJMXSupport.*;


import org.glassfish.admin.amx.impl.config.AttributeResolverHelper;
import org.glassfish.admin.amx.impl.mbean.AMXImplBase;
import org.glassfish.admin.amx.impl.util.Issues;
import org.glassfish.admin.amx.impl.util.MBeanInfoSupport;
import org.glassfish.admin.amx.impl.util.SingletonEnforcer;
import org.glassfish.admin.amx.impl.util.UnregistrationListener;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.config.AttributeResolver;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.ListUtil;
import org.glassfish.admin.amx.util.TypeCast;
import org.glassfish.admin.amx.util.jmx.JMXUtil;

import static org.glassfish.admin.amx.intf.config.AnonymousElementList.*;


import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.RetryableException;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.WriteableView;

/**
Base class from which all AMX Config MBeans should derive (but not "must").
<p>
 */
@Taxonomy(stability = Stability.NOT_AN_INTERFACE)
public class AMXConfigImpl extends AMXImplBase
{
    private final ConfigBean mConfigBean;

    /** MBeanInfo derived from the AMXConfigProxy interface, always the same */
    private static MBeanInfo configMBeanInfo;

    private static synchronized MBeanInfo getAMXConfigMBeanInfo()
    {
        if (configMBeanInfo == null)
        {
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

    private static MBeanInfo createMBeanInfo(final ConfigBean cb)
    {
        Class<? extends ConfigBeanProxy> intf = cb.getProxyType();
        MBeanInfo newInfo = mInfos.get(intf);
        if (newInfo != null)
        {
            return newInfo;
        }

        final ConfigBeanJMXSupport spt = ConfigBeanJMXSupportRegistry.getInstance(cb);
        final MBeanInfo info = spt.getMBeanInfo();

        final List<MBeanAttributeInfo> attrInfos = ListUtil.newListFromArray(info.getAttributes());
        final MBeanInfo spiInfo = MBeanInfoSupport.getAMX_SPIMBeanInfo();

        // make a list so we can remove "Children" attribute if this MBean cannot have any
        final List<MBeanAttributeInfo> spiAttrInfos = ListUtil.newListFromArray(spiInfo.getAttributes());
        if (spt.isLeaf())
        {
            JMXUtil.remove(spiAttrInfos, ATTR_CHILDREN);
        }

        // Add in the AMX_SPI attributes, replacing any with the same name
        for (final MBeanAttributeInfo attrInfo : spiAttrInfos)
        {
            // remove existing info
            final String attrName = attrInfo.getName();
            final MBeanAttributeInfo priorAttrInfo = JMXUtil.remove(attrInfos, attrName);

            // special case the Name attribute to preserve its metadata
            if (attrName.equals(ATTR_NAME) && priorAttrInfo != null)
            {
                final Descriptor mergedD = JMXUtil.mergeDescriptors(attrInfo.getDescriptor(), priorAttrInfo.getDescriptor());

                final MBeanAttributeInfo newAttrInfo = new MBeanAttributeInfo(attrName,
                        attrInfo.getType(), attrInfo.getDescription(), attrInfo.isReadable(), attrInfo.isWritable(), attrInfo.isIs(), mergedD);

                attrInfos.add(newAttrInfo);
            }
            else
            {
                attrInfos.add(attrInfo);
            }
        }

        final List<MBeanOperationInfo> operationInfos = ListUtil.newListFromArray(info.getOperations());
        operationInfos.addAll(ListUtil.newListFromArray(getAMXConfigMBeanInfo().getOperations()));

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
                info.getDescriptor());

        mInfos.putIfAbsent(intf, newInfo);

        return newInfo;
    }

    public AMXConfigImpl(
            final ObjectName parentObjectName,
            final ConfigBean configBean)
    {
        this(parentObjectName, AMXConfigProxy.class, configBean);
    }

    public AMXConfigImpl(
            final ObjectName parentObjectName,
            final Class<? extends AMXProxy> theInterface,
            final ConfigBean configBean)
    {
        super(parentObjectName, createMBeanInfo(configBean));

        mConfigBean = configBean;

        // eager initialization, it will be needed momentarily
        getConfigBeanJMXSupport();
    }

    @Override
    protected void setAttributeManually(final Attribute attr)
            throws AttributeNotFoundException, InvalidAttributeValueException
    {
        final AttributeList attrList = new AttributeList();
        attrList.add(attr);

        final Map<String, Object> oldValues = new HashMap<String, Object>();
        final AttributeList successList = setAttributesInConfigBean(attrList, oldValues);
        if (successList.size() == 0)
        {
            throw new AttributeNotFoundException(attr.getName());
        }
    }

    /**
    Note that the default implementation sets attributes one at a time, but that
    MBeans with transactional requirements (eg configuration) may wish to set them as a group.
     */
    @Override
    public AttributeList setAttributes(final AttributeList attrs)
    {
        final AttributeList successList = new AttributeList();

        final Map<String, Object> oldValues = new HashMap<String, Object>();
        final AttributeList delegateSuccess = setAttributesInConfigBean(attrs, oldValues);
        successList.addAll(delegateSuccess);

        return (successList);
    }

    /**
    The actual name could be different than the 'name' property in the ObjectName if it
    contains characters that are illegal for an ObjectName.
     */
    @Override
    public String getName()
    {
        final ConfigBean cb = getConfigBean();

        String name = AMXConfigLoader.getName(cb);

        return name == null ? NO_NAME : name;
    }

    private final ConfigBean getConfigBean()
    {
        return mConfigBean;
    }

    private final ConfigBeanProxy getConfigBeanProxy()
    {
        return getConfigBean().getProxy(getConfigBean().getProxyType());
    }

    /**
    Resolve a template String.  See {@link AttributeResolver} for details.
     */
    public String resolveAttributeValue(final String varString)
    {
        if (!AttributeResolverHelper.needsResolving(varString))
        {
            return varString;
        }

        return new AttributeResolverHelper(getSelf(AMXConfigProxy.class)).resolve(varString);
    }

    public String resolveAttribute(final String attrName)
    {
        try
        {
            final Object value = getAttribute(attrName);
            return resolveAttributeValue(value == null ? null : "" + value);
        }
        catch (final AttributeNotFoundException e)
        {
            System.out.println("resolveAttribute: Attribute not found: " + attrName + " on " + getObjectName());
            return null;
        }
    }

    public Boolean resolveBoolean(final String attrName)
    {
        return Boolean.parseBoolean(resolveAttribute(attrName));
    }

    public Integer resolveInteger(final String attrName)
    {
        return Integer.parseInt(resolveAttribute(attrName));
    }

    public Long resolveLong(final String attrName)
    {
        return Long.parseLong(resolveAttribute(attrName));
    }

    public AttributeList resolveAttributes(final String[] attrNames)
    {
        Issues.getAMXIssues().notDone("resolveAttributes: use annotations to create the correct type");

        final AttributeList attrs = getAttributes(attrNames);
        final AttributeList resolvedAttrs = new AttributeList();
        for (final Object o : attrs)
        {
            Attribute r = (Attribute) o;
            // allow non-String attributes
            final Object value = r.getValue();
            if ((value instanceof String) && AttributeResolverHelper.needsResolving((String) value))
            {
                final String resolvedValue = resolveAttributeValue((String) value);
                // TODO: use annotation to determine correct type
                r = new Attribute(r.getName(), resolvedValue);
            }

            resolvedAttrs.add(r);
        }

        return resolvedAttrs;
    }

//========================================================================================
    /**
    Convert incoming parameters to HK2 data structures and sub-element Maps.
     */
    static private void toAttributeChanges(
            final Map<String, Object> values,
            final List<ConfigSupport.AttributeChanges> changes,
            final Map<String, Map<String, Object>> subs)
    {
        if (values != null)
        {
            for (final String nameAsProvided : values.keySet())
            {
                final Object value = values.get(nameAsProvided);

                final String xmlName = ConfigBeanJMXSupport.toXMLName(nameAsProvided);

                // auto-convert specific basic types to String
                if (value == null ||
                    (value instanceof String) ||
                    (value instanceof Number) ||
                    (value instanceof Boolean))
                {
                    //System.out.println( "toAttributeChanges: " + xmlName + " = " + value );
                    final String valueString = value == null ? null : "" + value;
                    final ConfigSupport.SingleAttributeChange change = new ConfigSupport.SingleAttributeChange(xmlName, valueString);
                    changes.add(change);
                }
                else if (value instanceof Map)
                {
                    //System.out.println( "toAttributeChanges: Map?!!!!!!!!!!!!!" );
                    // sub-element
                    final Map<String, Object> m = TypeCast.checkMap(Map.class.cast(value), String.class, Object.class);
                    subs.put(xmlName, m);
                }
                else if (value instanceof String[])
                {
                    //System.out.println( "toAttributeChanges: MultipleAttributeChanges" );
                    changes.add(new ConfigSupport.MultipleAttributeChanges(xmlName, (String[]) value));
                }
                else
                {
                    throw new IllegalArgumentException("Value of class " + value.getClass().getName() + " not supported for attribute " + nameAsProvided);
                }
            }
        }
    }

    private ObjectName finishCreate(
            final Class<? extends ConfigBeanProxy> elementClass,
            final List<ConfigSupport.AttributeChanges> changes,
            final Map<String, Map<String, Object>> subs)
            throws ClassNotFoundException, TransactionFailure
    {
        if (subs.keySet().size() != 0)
        {
            cdebug("Ignoring sub-elements: " + CollectionUtil.toString(subs.keySet(), ", "));
        }

        ConfigBean newConfigBean = null;
        final SubElementsCallback callback = new SubElementsCallback(subs);
        try
        {
            newConfigBean = ConfigSupport.createAndSet(getConfigBean(), elementClass, changes, callback);
        }
        catch (Throwable t)
        {
            cdebug(ExceptionUtil.toString(t));
            throw new RuntimeException(t);
        }

        //----------------------
        //
        // Force a synchronous processing of the new ConfigBean into an AMX MBean
        //
        final AMXConfigLoader amxLoader = SingletonEnforcer.get(AMXConfigLoader.class);
        amxLoader.handleConfigBean(newConfigBean, true);
        final ObjectName objectName = ConfigBeanRegistry.getInstance().getObjectName(newConfigBean);

        // final AMXConfigProxy newAMX = AMXConfigProxy.class.cast(getProxyFactory().getProxy(objectName, AMXConfigProxy.class));

        return objectName;
    }

    public ObjectName createChild(final String type, final Map<String, Object> params)
    {
        final Class<? extends ConfigBeanProxy> intf = getConfigBeanProxyClassForContainedType(type);
        if (intf == null)
        {
            throw new IllegalArgumentException("ConfigBean of type " + getConfigBean().getProxyType() +
                                               " does not support sub-element of type " + type);
        }

        try
        {
            return createChild(intf, params);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private static Map<String, Object> paramsToMap(final Object[] params)
    {
        final Map<String, Object> m = new HashMap<String, Object>();

        for (int i = 0; i < params.length; i += 2)
        {
            final String name = (String) params[i];

            final Object value = params[i + 1];

            // interpret an Object[] as meaning to create a sub-map
            if (value == null || value instanceof String)
            {
                m.put(name, value);
            }
            else if (value instanceof Object[])
            {
                m.put(name, paramsToMap((Object[]) value));
            }
            else
            {
                // let it be dealt with further on...
                m.put(name, value);
            }
        }
        return m;
    }

    public ObjectName createChild(final String type, final Object[] params)
    {
        if ((params.length % 2) != 0)
        {
            throw new IllegalArgumentException();
        }

        return createChild(type, paramsToMap(params));
    }

    /*
    public ObjectName test()
    {
    final Object[] args = {
    "name", "test1",
    "idle-thread-timeout-seconds", "900",
    "MAX-QUEUE-SIZE", "3",
    "min-thread-pool-size", "10",
    "min-thread-pool-size", "30",
    "classname", "com.sun.grizzly.http.StatsThreadPool"
    };
    return createChild( "thread-pool", args );
    }
     */
    private ObjectName createChild(final Class<? extends ConfigBeanProxy> intf, final Map<String, Object> params)
            throws ClassNotFoundException, TransactionFailure
    {
        //cdebug( "createChild: " + intf.getName() + ", params =  " + MapUtil.toString(params) );

        final ConfigBeanJMXSupport spt = ConfigBeanJMXSupportRegistry.getInstance(intf);
        if (!spt.isSingleton())
        {
            if (params == null)
            {
                throw new IllegalArgumentException("Named element requires at least its name");
            }
            final Set<String> requiredAttrs = spt.requiredAttributeNames();
            //cdebug( "createChild: " + intf.getName() + ", requiredAttributeNames =  " + CollectionUtil.toString(requiredAttrs, ", ") );
            for (final String reqName : requiredAttrs)
            {
                final String xmlName = ConfigBeanJMXSupport.toXMLName(reqName);
                // allow either the Attribute name or the xml name at this stage
                if (!(params.containsKey(reqName) || params.containsKey(xmlName)))
                {
                    throw new IllegalArgumentException("Required attribute missing: " + reqName);
                }
            }
        }

        final List<ConfigSupport.AttributeChanges> changes = new ArrayList<ConfigSupport.AttributeChanges>();
        final Map<String, Map<String, Object>> subs = new HashMap<String, Map<String, Object>>();
        toAttributeChanges(params, changes, subs);

        return finishCreate(intf, changes, subs);
    }

    /**
    Callback to create sub-elements (recursively) on a newly created child element.
     */
    private static final class SubElementsCallback implements ConfigSupport.TransactionCallBack<WriteableView>
    {
        private final Map<String, Map<String, Object>> mSubs;

        public SubElementsCallback(final Map<String, Map<String, Object>> subs)
        {
            mSubs = subs;
        }

        public void performOn(final WriteableView item) throws TransactionFailure
        {
            // create each sub-element, recursively
            for (final String type : mSubs.keySet())
            {
                final Map<String, Object> attrs = mSubs.get(type);

                cdebug("Ignoring sub-element creation for: " + type);

                // FIXME TODO: create a child of the specified type, set all its attributes,
                // recursively create any children the same way, etc

                for (final String attrName : attrs.keySet())
                {
                    /*
                    final String attrValue = mProperties.get(propertyName);

                    final ConfigBeanProxy propChild = item.allocateProxy(propClass);
                    final ConfigBean child = (ConfigBean)Dom.unwrap(propChild);
                    final ConfigBeanProxy childW = ConfigSupport.getWriteableView(propChild);

                    //ConfigModel.Property modelProp = getConfigModel_Property( "name");
                    childW.setter( modelProp, propertyName, String.class);

                    //modelProp = NameMappingHelper.getConfigModel_Property( "value");
                    childW.setter( modelProp, propertyValue, String.class);
                    }
                     */
                }
            }
        }

    }

    public void removeChild(final String type)
    {
        final ObjectName child = child(type);
        if (child == null)
        {
            throw new RuntimeException(new InstanceNotFoundException("No MBean of type " + type + " found."));
        }

        remove(child);
    }

    public void removeChild(final String type, final String name)
    {
        final ObjectName child = child(type, name);
        if (child == null)
        {
            throw new RuntimeException(new InstanceNotFoundException("No MBean named " + name + " of type " + type + " found."));
        }

        remove(child);
    }

    private final void remove(final ObjectName childObjectName)
    {
        try
        {
            final ConfigBean childConfigBean = ConfigBeanRegistry.getInstance().getConfigBean(childObjectName);


            try
            {
                cdebug("REMOVING config of class " + childConfigBean.getProxyType().getName() + " from  parent of type " +
                       getConfigBean().getProxyType().getName() + ", ObjectName = " + JMXUtil.toString(childObjectName));
                ConfigSupport.deleteChild(this.getConfigBean(), childConfigBean);
            }
            catch (final TransactionFailure tf)
            {
                throw new RuntimeException("Transaction failure deleting " + JMXUtil.toString(childObjectName), tf);
            }

            // NOTE: MBeans unregistered asynchronously by AMXConfigLoader
            // enforce synchronous semantics to clients by waiting until this happens
            //  the listener is smart enough not to wait if it's already unregistered
            final UnregistrationListener myListener = new UnregistrationListener(getMBeanServer(), childObjectName);
            final long TIMEOUT_MILLIS = 10 * 1000;
            final boolean unregisteredOK = myListener.waitForUnregister(TIMEOUT_MILLIS);
            if (!unregisteredOK)
            {
                throw new RuntimeException("Something went wrong unregistering MBean " + JMXUtil.toString(childObjectName));
            }
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Problem deleting " + childObjectName, e);
        }
    }

    private Object invokeDuckMethod(
            final ConfigBeanJMXSupport.DuckTypedInfo info,
            Object[] args)
            throws MBeanException
    {
        try
        {
            //cdebug( "invokeDuckMethod(): invoking: " + info.name() + " on " + info.method().getDeclaringClass() );

            if (!info.method().getDeclaringClass().isAssignableFrom(getConfigBeanProxy().getClass()))
            {
                throw new IllegalArgumentException("invokeDuckMethod: " + getConfigBean().getProxyType() + " not asssignable to " + info.method().getDeclaringClass());
            }

            Object result = info.method().invoke(getConfigBeanProxy(), args);
            result = translateResult(result);
            
            // cdebug( "invokeDuckMethod(): invoked: " + info.name() + ", got " + result );

            return result;
        }
        catch (final Exception e)
        {
            throw new MBeanException(e);
        }
    }
    
    private ObjectName getObjectName( final ConfigBeanProxy cbp )
    {
        final Dom dom = Dom.unwrap(cbp);
        
        if ( dom instanceof ConfigBean )
        {
            return ConfigBeanRegistry.getInstance().getObjectName( (ConfigBean)dom );
        }
        
        // we can't return a Dom over the wire
        return null;
    }

    /**
        Convert results that contain local ConfigBeanProxy into ObjectNames.
        Ignore other items, passing through unchanged.
     */
    private Object translateResult(final Object result )
    {
        // short-circuit the common case
        if ( result instanceof String ) return result;
        
        Object out = result;

        // ConfigBean types must be mapped back to ObjectName; they can't go across the wire
            
        if ( result instanceof ConfigBeanProxy )
        {
            out = getObjectName( (ConfigBeanProxy)result );
        }
        else if ( result instanceof Collection )
        {
            final Collection<Object> c = (Collection)result;
            final Collection<Object> translated = new ArrayList<Object>();
            for( final Object item : c )
            {
                translated.add( translateResult(item) );
            }
            
            if ( result instanceof Set )
            {
                out = new HashSet<Object>(translated);
            }
            else if ( result instanceof AbstractQueue )
            {
                out = new LinkedBlockingDeque(translated);
            }
            else
            {
                out = translated;
            }
        }
        else if ( result instanceof Map )
        {
            final Map resultMap = (Map)result;
            Map outMap = new HashMap();
            for( final Object key : resultMap.keySet() )
            {
                outMap.put( translateResult(key), translateResult( resultMap.get(key) ) );
            }
            out = outMap;
        }
        else if ( result.getClass().isArray() )
        {
            final Class<?> componentType = result.getClass().getComponentType();
            if ( ConfigBeanProxy.class.isAssignableFrom(componentType) )
            {
                final Object[] items = (Object[])result;
                final ObjectName[] objectNames = new ObjectName[items.length];
                for( int i = 0; i < items.length; ++i )
                {
                    objectNames[i]  = getObjectName( (ConfigBeanProxy)items[i] );
                }
                out = objectNames;
            }
        }
        
        return out;
    }
    
    /**
    Automatically figure out get<abc>Factory(),
    create<Abc>Config(), remove<Abc>Config().

     */
    @Override
    protected Object invokeManually(
            String operationName,
            Object[] args,
            String[] types)
            throws MBeanException, ReflectionException, NoSuchMethodException, AttributeNotFoundException
    {
        final int numArgs = args == null ? 0 : args.length;

        Object result = null;
        debugMethod(operationName, args);

        ConfigBeanJMXSupport.DuckTypedInfo duckTypedInfo = null;
        final ConfigBeanJMXSupport spt = getConfigBeanJMXSupport();
        if ((duckTypedInfo = getConfigBeanJMXSupport().findDuckTyped(operationName, types)) != null)
        {
            result = invokeDuckMethod(duckTypedInfo, args);
        }
        else
        {
            result = super.invokeManually(operationName, args, types);
        }
        return result;
    }

    public void sendConfigCreatedNotification(final ObjectName configObjectName)
    {
        sendNotification(CONFIG_CREATED_NOTIFICATION_TYPE,
                CONFIG_REMOVED_NOTIFICATION_TYPE,
                CONFIG_OBJECT_NAME_KEY, configObjectName);
    }

    public void sendConfigRemovedNotification(final ObjectName configObjectName)
    {
        sendNotification(CONFIG_REMOVED_NOTIFICATION_TYPE,
                CONFIG_REMOVED_NOTIFICATION_TYPE,
                CONFIG_OBJECT_NAME_KEY, configObjectName);
    }

    private final ConfigBeanJMXSupport getConfigBeanJMXSupport()
    {
        return ConfigBeanJMXSupportRegistry.getInstance(getConfigBean());
    }

    private static final Map<String, String> getDefaultValues(final Class<? extends ConfigBeanProxy> intf, boolean useAMXAttributeNames)
    {
        return ConfigBeanJMXSupportRegistry.getInstance(intf).getDefaultValues(useAMXAttributeNames);
    }

    public final Map<String, String> getDefaultValues(final String type, final boolean useAMXAttributeNames)
    {
        final Class<? extends ConfigBeanProxy> intf = getConfigBeanProxyClassForContainedType(type);

        return getDefaultValues(intf, useAMXAttributeNames);
    }

    public final Map<String, String> getDefaultValues(final boolean useAMXAttributeNames)
    {
        return getDefaultValues(mConfigBean.getProxyType(), useAMXAttributeNames);
    }

    private Class<? extends ConfigBeanProxy> getConfigBeanProxyClassForContainedType(final String type)
    {
        final ConfigBeanJMXSupport spt = getConfigBeanJMXSupport();
        if (spt == null)
        {
            throw new IllegalArgumentException("Can't find ConfigBean @Configured class for AMX type " + type);
        }
        // return spt.getConfigBeanProxyClassFor(type);

        return ConfigBeanJMXSupportRegistry.getConfigBeanProxyClassFor(spt, type);
    }

    @Override
    protected String[] attributeNameToType(final String attributeName)
    {
        return new String[]
                {
                    Util.typeFromName(attributeName), attributeName
                };
    }

    @Override
    protected Object getAttributeManually(final String name)
            throws AttributeNotFoundException, ReflectionException, MBeanException
    {
        return getAttributeFromConfigBean(name);
    }


//-------------------------------------------------------------
    /**
    Get an Attribute.  This is a bit tricky, because the target can be an XML attribute,
    an XML string element, or an XML list of elements.
     */
    protected final Object getAttributeFromConfigBean(final String amxName)
    {
        Object result = null;

        final MBeanAttributeInfo attrInfo = getAttributeInfo(amxName);
        final String xmlName = ConfigBeanJMXSupport.xmlName(attrInfo, amxName);
        final boolean isAttribute = ConfigBeanJMXSupport.isAttribute(attrInfo);

        if (isAttribute)
        {
            result = mConfigBean.rawAttribute(xmlName);
        }
        else if (ConfigBeanJMXSupport.isElement(attrInfo))
        {
            if (String.class.getName().equals(attrInfo.getType()))
            {
                final List<?> leaf = mConfigBean.leafElements(xmlName);
                if (leaf != null)
                {
                    try
                    {
                        result = (String) leaf.get(0);
                    }
                    catch (final Exception e)
                    {
                        // doesn't exist, return null
                    }
                }
            }
            else if (attrInfo.getType() == String[].class.getName())
            {
                //final String elementClass = (String)d.getFieldValue( DESC_ELEMENT_CLASS );

                final List<?> leaf = mConfigBean.leafElements(xmlName);
                if (leaf != null)
                {
                    // verify that it is List<String> -- no other types are supported in this way
                    final List<String> elems = TypeCast.checkList(leaf, String.class);
                    result = CollectionUtil.toArray(elems, String.class);
                }
            }
            else
            {
                throw new IllegalArgumentException("getAttributeFromConfigBean: unsupported return type: " + attrInfo.getType());
            }
        }
        //debug( "Attribute " + amxName + " has class " + ((result == null) ? "null" : result.getClass()) );
        return result;
    }

    private static final class MyTransactionListener implements TransactionListener
    {
        private final List<PropertyChangeEvent> mChangeEvents = new ArrayList<PropertyChangeEvent>();

        private final ConfigBean mTarget;

        MyTransactionListener(final ConfigBean target)
        {
            mTarget = target;
        }

        public void transactionCommited(List<PropertyChangeEvent> changes)
        {
            // include only events that match the desired config bean; other transactions
            // could generate events on other ConfigBeans. For that matter, it's unclear
            // why more than one transaction on the same ConfigBean couldn't be "heard" here.
            for (final PropertyChangeEvent event : changes)
            {
                final Object source = event.getSource();
                if (source instanceof ConfigBeanProxy)
                {
                    final Dom dom = Dom.unwrap((ConfigBeanProxy) source);
                    if (dom instanceof ConfigBean)
                    {
                        if (mTarget == (ConfigBean) dom)
                        {
                            mChangeEvents.add(event);
                        }
                    }
                }
            }
        }

        public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes)
        {
            // amx probably does not care that some changes were not processed successfully
            // and will require a restart
        }

        List<PropertyChangeEvent> getChangeEvents()
        {
            return mChangeEvents;
        }

    };

    /**
    Make a Map keyed by the property name of the PropertyChangeEvent, verifying that each
    name is non-null.
     */
    private Map<String, PropertyChangeEvent> makePropertyChangeEventMap(final List<PropertyChangeEvent> changeEvents)
    {
        final Map<String, PropertyChangeEvent> m = new HashMap<String, PropertyChangeEvent>();

        for (final PropertyChangeEvent changeEvent : changeEvents)
        {
            if (changeEvent.getPropertyName() == null)
            {
                throw new IllegalArgumentException("PropertyChangeEvent property names must be specified");
            }

            m.put(changeEvent.getPropertyName(), changeEvent);
        }
        return m;
    }

    private void joinTransaction(final Transaction t, final WriteableView writeable)
            throws TransactionFailure
    {
        if (!writeable.join(t))
        {
            t.rollback();
            throw new TransactionFailure("Cannot enlist " + writeable.getProxyType() + " in transaction", null);
        }
    }

    private static void commit(final Transaction t)
            throws TransactionFailure
    {
        try
        {
            t.commit();
        }
        catch (final RetryableException e)
        {
            t.rollback();
            throw new TransactionFailure(e.getMessage(), e);
        }
        catch (final TransactionFailure e)
        {
            cdebug("failure, not retryable...");
            t.rollback();
            throw e;
        }
    }

    static <T extends ConfigBeanProxy> WriteableView getWriteableView(final T s, final ConfigBean sourceBean)
            throws TransactionFailure
    {
        final WriteableView f = new WriteableView(s);
        if (sourceBean.getLock().tryLock())
        {
            return f;
        }
        throw new TransactionFailure("Config bean already locked " + sourceBean, null);
    }

    private static Type getCollectionGenericType()
    {
        try
        {
            return ConfigSupport.class.getDeclaredMethod("defaultPropertyValue", (Class[]) null).getGenericReturnType();
        }
        catch (NoSuchMethodException e)
        {
            // not supposed to happen, throw any reasonabl exception
            throw new IllegalArgumentException();
        }
    }

    private static boolean isCollectionCmd(final String s)
    {
        return s != null &&
               (s.equals(OP_ADD) || s.equals(OP_REMOVE) || s.equals(OP_REPLACE));
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
    private List<String> handleCollection(
            final WriteableView writeable,
            final ConfigModel.Property prop,
            final String cmd,
            final List<String> argValues)
    {
        if (!isCollectionCmd(cmd))
        {
            throw new IllegalArgumentException("" + cmd);
        }

        final Object o = writeable.getter(prop, getCollectionGenericType());
        final List<String> masterList = TypeCast.checkList(TypeCast.asList(o), String.class);

        //cdebug( "Existing values: {" + CollectionUtil.toString( masterList ) + "}");
        //cdebug( "Arg values: {" + CollectionUtil.toString( argValues ) + "}");

        if (cmd.equals(OP_REPLACE))
        {
            masterList.retainAll(argValues);
            for (final String s : argValues)
            {
                if (!masterList.contains(s))
                {
                    masterList.add(s);
                }
            }
        //cdebug( "Master list after OP_REMOVE: {" + CollectionUtil.toString( masterList ) + "}");
        }
        else if (cmd.equals(OP_REMOVE))
        {
            masterList.removeAll(argValues);
        //cdebug( "Master list after OP_REMOVE: {" + CollectionUtil.toString( masterList ) + "}");
        }
        else if (cmd.equals(OP_ADD))
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
        final Transaction mTransaction;

        final ConfigBean mConfigBean;

        final WriteableView mWriteable;

        public Applyer(final ConfigBean cb) throws TransactionFailure
        {
            this(cb, new Transaction());
        }

        public Applyer(final ConfigBean cb, final Transaction t)
                throws TransactionFailure
        {
            mConfigBean = cb;
            mTransaction = t;

            final ConfigBeanProxy readableView = cb.getProxy(cb.getProxyType());
            mWriteable = getWriteableView(readableView, cb);
        }

        protected void makeChanges()
                throws TransactionFailure
        {
        }

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

    protected ConfigModel.Property getConfigModel_Property(final String xmlName)
    {
        final ConfigModel.Property cmp = mConfigBean.model.findIgnoreCase(xmlName);
        if (cmp == null)
        {
            throw new IllegalArgumentException("Illegal name: " + xmlName);
        }
        return cmp;
    }

    private final class ModifyCollectionApplyer extends Applyer
    {
        private volatile List<String> mResult;

        private final String mElementName;

        private final String mCmd;

        private final String[] mValues;

        public ModifyCollectionApplyer(
                final ConfigBean cb,
                final String elementName,
                final String cmd,
                final String[] values)
                throws TransactionFailure
        {
            super(cb);
            mElementName = elementName;
            mCmd = cmd;
            mValues = values;
            mResult = null;
        }

        protected void makeChanges()
                throws TransactionFailure
        {
            final ConfigModel.Property prop = getConfigModel_Property(mElementName);
            mResult = handleCollection(mWriteable, prop, mCmd, ListUtil.asStringList(mValues));
        }

    }

    private final class MakeChangesApplyer extends Applyer
    {
        private final Map<String, Object> mChanges;

        public MakeChangesApplyer(
                final ConfigBean cb,
                final Map<String, Object> changes)
                throws TransactionFailure
        {
            super(cb);
            mChanges = changes;
        }

        protected void makeChanges()
                throws TransactionFailure
        {
            for (final String xmlName : mChanges.keySet())
            {
                final Object value = mChanges.get(xmlName);
                final ConfigModel.Property prop = getConfigModel_Property(xmlName);

                if (prop.isCollection())
                {
                    final List<String> results = handleCollection(mWriteable, prop, OP_REPLACE, ListUtil.asStringList(value));
                }
                else if (value == null || (value instanceof String))
                {
                    mWriteable.setter(prop, value, String.class);
                }
                else
                {
                    throw new TransactionFailure("Illegal data type for attribute " + xmlName + ": " + value.getClass().getName());
                }
            }
        }

    }

    private void apply(
            final ConfigBean cb,
            final Map<String, Object> changes)
            throws TransactionFailure
    {
        final MakeChangesApplyer mca = new MakeChangesApplyer(mConfigBean, changes);
        mca.apply();
    }

    private Map<String, Object> mapNamesAndValues(
            final Map<String, Object> amxAttrs,
            final Map<String, Object> noMatch)
    {
        final Map<String, Object> xmlAttrs = new HashMap<String, Object>();

        final Map<String, MBeanAttributeInfo> attrInfos = getAttributeInfos();

        for (final String amxAttrName : amxAttrs.keySet())
        {
            final Object valueIn = amxAttrs.get(amxAttrName);

            final MBeanAttributeInfo attrInfo = attrInfos.get(amxAttrName);
            if (attrInfo == null)
            {
                debug("WARNING: setAttributes(): no MBeanAttributeInfo found for: " + amxAttrName);
                noMatch.put(amxAttrName, valueIn);
                continue;
            }
            final String xmlName = ConfigBeanJMXSupport.xmlName(attrInfo, amxAttrName);

            if (xmlName != null)
            {
                final Object value = valueIn == null ? null : "" + valueIn;
                if (value != valueIn)
                {
                    //debug( "Attribute " + amxAttrName + " auto converted from " + valueIn.getClass().getName() + " to " + value.getClass().getName() );
                }

                // We accept only Strings, String[] or null
                if (valueIn == null || (value instanceof String))
                {
                    xmlAttrs.put(xmlName, (String) value);
                }
                else if (false /*isCollection(xmlName)*/)
                {
                    if ((valueIn instanceof String[]) || (valueIn instanceof List))
                    {
                        xmlAttrs.put(xmlName, ListUtil.asStringList(valueIn));
                    }
                    else
                    {
                        noMatch.put(amxAttrName, valueIn);
                    }
                }
                else
                {
                    noMatch.put(amxAttrName, valueIn);
                }
            // debug( "Attribute " + amxAttrName + "<=>" + xmlName + " is of class " + ((value == null) ? null : value.getClass().getName()) );
            }
            else
            {
                debug("WARNING: setAttributes(): no xmlName match found for AMX attribute: " + amxAttrName);
                noMatch.put(amxAttrName, valueIn);
            }
        }

        return xmlAttrs;
    }

    public AttributeList setAttributesInConfigBean(final AttributeList attrsIn, final Map<String, Object> oldValues)
    {
        oldValues.clear();

        // now map the AMX attribute names to xml attribute names
        final Map<String, Object> amxAttrs = JMXUtil.attributeListToValueMap(attrsIn);
        final Map<String, Object> notMatched = new HashMap<String, Object>();
        final Map<String, Object> xmlAttrs = mapNamesAndValues(amxAttrs, notMatched);

        if (notMatched.keySet().size() != 0)
        {
            cdebug("setAttributes: failed to map these AMX attributes: {" + CollectionUtil.toString(notMatched.keySet(), ", ") + "}");
        }

        final AttributeList successfulAttrs = new AttributeList();

        final Transactions transactions = mConfigBean.getHabitat().getComponent(Transactions.class);

        if (xmlAttrs.size() != 0)
        {
            //cdebug( "DelegateToConfigBeanDelegate.setAttributes(): " + attrsIn.size() + " attributes: {" +
            //     CollectionUtil.toString(amxAttrs.keySet()) + "} mapped to xml names {" + CollectionUtil.toString(xmlAttrs.keySet()) + "}");

            final MyTransactionListener myListener = new MyTransactionListener(mConfigBean);
            transactions.addTransactionsListener(myListener);

            // results should contain only those that succeeded which will be all or none
            // depending on whether the transaction worked or not
            try
            {
                final MakeChangesApplyer mca = new MakeChangesApplyer(mConfigBean, xmlAttrs);
                mca.apply();

                // use 'attrsIn' vs 'attrs' in case not all values are 'String'
                successfulAttrs.addAll(attrsIn);
            }
            catch (final TransactionFailure tf)
            {
                // empty results -- no Exception should be thrown per JMX spec
                cdebug(ExceptionUtil.toString(tf));
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

    /*
    public MBeanNotificationInfo[] getNotificationInfo()
    {
    final MBeanNotificationInfo[] superInfos = super.getNotificationInfo();

    // create a NotificationInfo for AttributeChangeNotification
    final String description = "";
    final String[] notifTypes = new String[]
    {
    AttributeChangeNotification.ATTRIBUTE_CHANGE
    };
    final MBeanNotificationInfo attributeChange = new MBeanNotificationInfo(
    notifTypes,
    AttributeChangeNotification.class.getName(),
    description);

    final MBeanNotificationInfo[] selfInfos =
    new MBeanNotificationInfo[]
    {
    attributeChange
    };

    final MBeanNotificationInfo[] allInfos =
    JMXUtil.mergeMBeanNotificationInfos(superInfos, selfInfos);

    return allInfos;
    }
     */
}





















