/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.amx.impl.config;

import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.SystemPropertyBag;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;
import org.glassfish.admin.amx.impl.mbean.AMXImplBase;
import org.glassfish.admin.amx.intf.config.ConfigTools;
import org.glassfish.admin.amx.util.MapUtil;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.PropertyBag;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * TODO: fix the duplication of code for Property/SystemProperty by refactoring the config bean interfaces.
 */
public class ConfigToolsImpl extends AMXImplBase {
    private static void debug( final String s ) { System.out.println(s); }
    
    public ConfigToolsImpl(final ObjectName parent) {
        super(parent, ConfigTools.class);
    }
        
    private static Property findProperty( final List<Property> props, final String name )
    {
        for( final Property prop : props )
        {
            if ( prop == null )
            {
                debug( "WARNING: null Property object in List<Property>" );
                continue;
            }
            if ( prop.getName().equals(name) )
            {
                return prop;
            }
        }
        return null;
    }
    
    private static SystemProperty findProperty( final List<SystemProperty> props, final String name )
    {
        for( final SystemProperty prop : props )
        {
            if ( prop.getName().equals(name) )
            {
                return prop;
            }
        }
        return null;
    }
    
    /** base class for Property or SystemProperty support */
    private static abstract class AnyPropsSetter implements ConfigCode
    {
        protected final List<Map<String,String>> mNewProps;
        protected final boolean mClearAll;
    
        AnyPropsSetter( List<Map<String,String>>  newProps, final boolean clearAll)
        {
            mNewProps = newProps;
            mClearAll = clearAll;
        }
        
        public Object run(ConfigBeanProxy... params)
            throws PropertyVetoException, TransactionFailure
        {
            if ( params.length != 1 ) throw new IllegalArgumentException();
            final ConfigBeanProxy parent = params[0];
            
            final ConfigBean source = (ConfigBean) ConfigBean.unwrap(parent);
            final ConfigSupport configSupport = source.getHabitat().getComponent(ConfigSupport.class);
            
            return _run(parent, configSupport);
        }
        abstract Object _run(final ConfigBeanProxy parent, final ConfigSupport spt) throws PropertyVetoException, TransactionFailure;
    }
    
    private static final class PropsSetter extends AnyPropsSetter
    {
        public PropsSetter( List<Map<String,String>>  newProps, final boolean clearAll)
        {
            super( newProps, clearAll );
        }
        
        public Object _run(final ConfigBeanProxy parent, final ConfigSupport configSupport)
            throws PropertyVetoException, TransactionFailure
        {
            final PropertyBag bag = (PropertyBag)parent;
            final List<Property> props = bag.getProperty();
            if ( mClearAll )
            {
                props.clear();
            }
            
            for( final Map<String,String> newProp : mNewProps )
            {
                final String name  = newProp.get("Name");
                final String value = newProp.get("Value");
                final String description = newProp.get("Description");
                
                // Better to modify the existing property, but how to join the transaction?
                // Remove conflicting Property first
                Property prop = findProperty( props, name );
                if ( prop != null )
                {
                    props.remove(prop);
                }
                prop = parent.createChild(Property.class);
                prop.setName(name);
                prop.setValue(value);
                prop.setDescription(description);
                props.add(prop);
                debug( "Created/updated property: " + name );
            }
                            
            return null;
        }
    }
    
    private static final class SystemPropsSetter extends AnyPropsSetter
    {
        public SystemPropsSetter( List<Map<String,String>>  newProps, final boolean clearAll)
        {
            super( newProps, clearAll );
        }
        
        public Object _run(final ConfigBeanProxy parent, final ConfigSupport configSupport)
            throws PropertyVetoException, TransactionFailure
        {
            final SystemPropertyBag bag = (SystemPropertyBag)parent;
            final List<SystemProperty> props = bag.getSystemProperty();
            if ( mClearAll )
            {
                props.clear();
            }
            
            for( final Map<String,String> newProp : mNewProps )
            {
                final String name  = newProp.get("Name");
                final String value = newProp.get("Value");
                final String description = newProp.get("Description");
                
                // Better to modify the existing property, but how to join the transaction?
                // Remove conflicting Property first
                SystemProperty prop = findProperty( props, name );
                if ( prop != null )
                {
                    props.remove(prop);
                }
                prop = parent.createChild(SystemProperty.class);
                prop.setName(name);
                prop.setValue(value);
                prop.setDescription(description);
                props.add(prop);
                debug( "Created/updated system property: " + name );
            }
                            
            return null;
        }
    }

    
    public void test() {
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
    
    // dummy interface for creating a proxy
    interface PropertyBagProxy extends ConfigBeanProxy, PropertyBag
    {
    }

        public void
    setProperties(
        final ObjectName parent,
        final List<Map<String,String>>  props,
        final boolean clearAll)
    {
        if ( parent == null || props == null) throw new IllegalArgumentException();
        
        final ConfigBean configBean = ConfigBeanRegistry.getInstance().getConfigBean(parent);
        if ( configBean == null ) throw new IllegalArgumentException( ""+ parent );
        
        final PropertyBagProxy proxy = configBean.getProxy(PropertyBagProxy.class);
        if ( ! PropertyBag.class.isAssignableFrom( proxy.getClass() ) )
        {
            throw new IllegalArgumentException( "ConfigBean " + configBean.getProxyType().getName() + " is not a PropertyBag" );
        }
        
        final PropsSetter propsSetter = new PropsSetter( props, clearAll);
        try
        {
            ConfigSupport.apply( propsSetter, proxy );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
        public void
    clearProperties(final ObjectName parent)
    {
        setProperties(parent, new ArrayList<Map<String,String>>(), true);
    }
    
    
    // dummy interface for creating a proxy
    interface SystemPropertyBagProxy extends ConfigBeanProxy, SystemPropertyBag
    {
    }
    
    
        public void
    clearSystemProperties(final ObjectName parent)
    {
        setSystemProperties(parent, new ArrayList<Map<String,String>>(), true);
    }
    
        public void
    setSystemProperties(
        final ObjectName parent,
        final List<Map<String,String>>  props,
        final boolean clearAll)
    {
        if ( parent == null || props == null) throw new IllegalArgumentException();
        
        final ConfigBean configBean = ConfigBeanRegistry.getInstance().getConfigBean(parent);
        if ( configBean == null ) throw new IllegalArgumentException( ""+ parent );
        
        final SystemPropertyBagProxy proxy = configBean.getProxy(SystemPropertyBagProxy.class);
        if ( ! SystemPropertyBag.class.isAssignableFrom( proxy.getClass() ) )
        {
            throw new IllegalArgumentException( "ConfigBean " + configBean.getProxyType().getName() + " is not a SystemPropertyBag" );
        }
        
        final SystemPropsSetter propsSetter = new SystemPropsSetter( props, clearAll);
        try
        {
            ConfigSupport.apply( propsSetter, proxy );
        }
        catch( final Exception e )
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}









