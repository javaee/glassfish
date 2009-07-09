/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.amx.impl.config;

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.SystemPropertyBag;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.impl.mbean.AMXImplBase;
import org.glassfish.admin.amx.impl.util.InjectedValues;
import org.glassfish.admin.amx.intf.config.BackendPrincipal;
import org.glassfish.admin.amx.intf.config.ConfigTools;
import org.glassfish.admin.amx.intf.config.ConnectorConnectionPool;
import org.glassfish.admin.amx.intf.config.Domain;
import org.glassfish.admin.amx.intf.config.Resources;
import org.glassfish.admin.amx.intf.config.SecurityMap;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.PropertyBag;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * TODO: fix the duplication of code for Property/SystemProperty by refactoring the config bean interfaces.
 */
public class ConfigToolsImpl extends AMXImplBase
{
    private static void debug(final String s)
    {
        System.out.println(s);
    }

    public ConfigToolsImpl(final ObjectName parent)
    {
        super(parent, ConfigTools.class);
    }

    private static Property findProperty(final List<Property> props, final String name)
    {
        for (final Property prop : props)
        {
            if (prop == null)
            {
                debug("WARNING: null Property object in List<Property>");
                continue;
            }
            if (prop.getName().equals(name))
            {
                return prop;
            }
        }
        return null;
    }

    private static SystemProperty findSystemProperty(final List<SystemProperty> props, final String name)
    {
        for (final SystemProperty prop : props)
        {
            if (prop.getName().equals(name))
            {
                return prop;
            }
        }
        return null;
    }

    /** base class for Property or SystemProperty support */
    private static abstract class AnyPropsSetter implements ConfigCode
    {
        protected final List<Map<String, String>> mNewProps;

        protected final boolean mClearAll;

        AnyPropsSetter(List<Map<String, String>> newProps, final boolean clearAll)
        {
            mNewProps = newProps;
            mClearAll = clearAll;
        }

        public Object run(ConfigBeanProxy... params)
                throws PropertyVetoException, TransactionFailure
        {
            if (params.length != 1)
            {
                throw new IllegalArgumentException();
            }
            final ConfigBeanProxy parent = params[0];

            final ConfigBean source = (ConfigBean) ConfigBean.unwrap(parent);
            final ConfigSupport configSupport = source.getHabitat().getComponent(ConfigSupport.class);

            return _run(parent, configSupport);
        }

        abstract Object _run(final ConfigBeanProxy parent, final ConfigSupport spt) throws PropertyVetoException, TransactionFailure;

    }

    private static final class PropsSetter extends AnyPropsSetter
    {
        public PropsSetter(List<Map<String, String>> newProps, final boolean clearAll)
        {
            super(newProps, clearAll);
        }

        public Object _run(final ConfigBeanProxy parent, final ConfigSupport configSupport)
                throws PropertyVetoException, TransactionFailure
        {
            final PropertyBag bag = (PropertyBag) parent;
            final List<Property> props = bag.getProperty();
            if (mClearAll)
            {
                props.clear();
            }

            for (final Map<String, String> newProp : mNewProps)
            {
                final String name = newProp.get("Name");
                final String value = newProp.get("Value");
                final String description = newProp.get("Description");

                // Better to modify the existing property, but how to join the transaction?
                // Remove conflicting Property first
                Property prop = findProperty(props, name);
                if (prop != null)
                {
                    props.remove(prop);
                }
                prop = parent.createChild(Property.class);
                prop.setName(name);
                prop.setValue(value);
                prop.setDescription(description);
                props.add(prop);
                debug("Created/updated property: " + name);
            }

            return null;
        }

    }

    private static final class SystemPropsSetter extends AnyPropsSetter
    {
        public SystemPropsSetter(List<Map<String, String>> newProps, final boolean clearAll)
        {
            super(newProps, clearAll);
        }

        public Object _run(final ConfigBeanProxy parent, final ConfigSupport configSupport)
                throws PropertyVetoException, TransactionFailure
        {
            final SystemPropertyBag bag = (SystemPropertyBag) parent;
            final List<SystemProperty> props = bag.getSystemProperty();
            if (mClearAll)
            {
                props.clear();
            }

            for (final Map<String, String> newProp : mNewProps)
            {
                final String name = newProp.get("Name");
                final String value = newProp.get("Value");
                final String description = newProp.get("Description");

                // Better to modify the existing property, but how to join the transaction?
                // Remove conflicting Property first
                SystemProperty prop = findSystemProperty(props, name);
                if (prop != null)
                {
                    props.remove(prop);
                }
                prop = parent.createChild(SystemProperty.class);
                prop.setName(name);
                prop.setValue(value);
                prop.setDescription(description);
                props.add(prop);
                debug("Created/updated system property: " + name);
            }

            return null;
        }

    }

    /*
    public void testSetProps() {
    final List<Map<String,String>> props = new ArrayList<Map<String,String>>();

    final ConfigTools tools = getDomainRootProxy().getExt().child(ConfigTools.class);

    props.add( MapUtil.newMap("Name", "test1", "Value", "value1", "Description", "desc1") );
    props.add( MapUtil.newMap("Name", "test2", "Value", "value2", "Description", "desc2") );
    props.add( MapUtil.newMap("Name", "test3", "Value", "value3", "Description", "desc3") );

    //final ObjectName target = getDomainRootProxy().getDomain().objectName();
    final ObjectName target = JMXUtil.newObjectName( "v3:pp=/domain/configs/config[server-config],type=web-container" );

    setProperties( target, props, false );
    setSystemProperties( target, props, false );
    }
     */
    public Object test()
    {
        final Domain domain = getDomainRootProxy().getDomain().as(Domain.class);
        final Resources resources = domain.getResources();
        
        // remove any existing test element
        final String NAME = "test";
        try
        {
        resources.removeChild( Util.deduceType(ConnectorConnectionPool.class), NAME );
        }
        catch( final Exception e )
        {
            //e.printStackTrace();
        }
        
        // create a new ConnectorConnectionPool with a SecurityMap containing a BackendPrincipal
        final Map<String,Object> params = new HashMap<String,Object>();
        params.put( "Name", NAME );
        params.put( "ResourceAdapterName", NAME );
        params.put( "ConnectionDefinitionName", NAME );
        params.put( "SteadyPoolSize", 23 ); // check that it works
        
        final Map<String,Object> securityParams = new HashMap<String,Object>();
        securityParams.put( "Name", NAME );
        params.put( Util.deduceType(SecurityMap.class), securityParams );
        
        final Map<String,Object> backendParams = new HashMap<String,Object>();
        backendParams.put( "UserName", "testUser" );
        backendParams.put( "Password", "testPassword" );
        securityParams.put( Util.deduceType(BackendPrincipal.class), backendParams );
        
        final AMXConfigProxy result = resources.createChild( Util.deduceType(ConnectorConnectionPool.class), params);
        
        return result.objectName();
    }

    // dummy interface for creating a proxy
    interface PropertyBagProxy extends ConfigBeanProxy, PropertyBag
    {
    }

    public void setProperties(
            final ObjectName parent,
            final List<Map<String, String>> props,
            final boolean clearAll)
    {
        if (parent == null || props == null)
        {
            throw new IllegalArgumentException();
        }

        final ConfigBean configBean = ConfigBeanRegistry.getInstance().getConfigBean(parent);
        if (configBean == null)
        {
            throw new IllegalArgumentException("" + parent);
        }

        final PropertyBagProxy proxy = configBean.getProxy(PropertyBagProxy.class);
        if (!PropertyBag.class.isAssignableFrom(proxy.getClass()))
        {
            throw new IllegalArgumentException("ConfigBean " + configBean.getProxyType().getName() + " is not a PropertyBag");
        }

        final PropsSetter propsSetter = new PropsSetter(props, clearAll);
        try
        {
            ConfigSupport.apply(propsSetter, proxy);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void clearProperties(final ObjectName parent)
    {
        setProperties(parent, new ArrayList<Map<String, String>>(), true);
    }

    // dummy interface for creating a proxy
    interface SystemPropertyBagProxy extends ConfigBeanProxy, SystemPropertyBag
    {
    }

    public void clearSystemProperties(final ObjectName parent)
    {
        setSystemProperties(parent, new ArrayList<Map<String, String>>(), true);
    }

    public void setSystemProperties(
            final ObjectName parent,
            final List<Map<String, String>> props,
            final boolean clearAll)
    {
        if (parent == null || props == null)
        {
            throw new IllegalArgumentException();
        }

        final ConfigBean configBean = ConfigBeanRegistry.getInstance().getConfigBean(parent);
        if (configBean == null)
        {
            throw new IllegalArgumentException("" + parent);
        }

        final SystemPropertyBagProxy proxy = configBean.getProxy(SystemPropertyBagProxy.class);
        if (!SystemPropertyBag.class.isAssignableFrom(proxy.getClass()))
        {
            throw new IllegalArgumentException("ConfigBean " + configBean.getProxyType().getName() + " is not a SystemPropertyBag");
        }

        final SystemPropsSetter propsSetter = new SystemPropsSetter(props, clearAll);
        try
        {
            ConfigSupport.apply(propsSetter, proxy);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    /** works only for @Configured types */
    private String[] getTypesImplementing( final Class<?> clazz)
    {
        final DomDocument domDoc = new DomDocument(InjectedValues.getInstance().getHabitat());

        try
        {
            final List<ConfigModel> models = domDoc.getAllModelsImplementing(clazz);
            final String[] names = new String[ models == null ? 0 : models.size()];
            if ( models != null )
            {
                int i = 0;
                for (final ConfigModel model : models)
                {
                    names[i] = model.getTagName();
                    ++i;
                }
            }

            return names;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public String[] getConfigNamedTypes()
    {
        return getTypesImplementing(Named.class);
    }

    public String[] getConfigResourceTypes()
    {
        return getTypesImplementing(Resource.class);
    }
    

    /*
    public String[] getConfigTypes()
    {
        final DomDocument domDoc = new DomDocument(InjectedValues.getInstance().getHabitat());
        final ConfigModel model = domDoc.getModelByElementName("domain");
        if ( model == null )
        {
            throw new IllegalArgumentException( "Can't get model for domain" );
        }
        
        final Set<String> names = model.getElementNames();
        final Set<String> all = new HashSet<String>();
        all.addAll( names );
        // need recursion here, but is it useful?
        for( final String name : names )
        {
            final ConfigModel model2 = domDoc.getModelByElementName(name);
            all.addAll( model2.getElementNames() );
        }
        
        final String[] result = new String[ all.size() ];
        all.toArray( result );
        
        return result;
    }
    */
}















