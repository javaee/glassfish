/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.amx.impl.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.management.ObjectName;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;
import org.jvnet.hk2.config.ConfigBean;

/**
 * Registry mapping ConfigBean to ObjectName and vice-versa.
 */
@Taxonomy( stability=Stability.NOT_AN_INTERFACE )
public final class ConfigBeanRegistry {
    private static void debug( final String s ) { System.out.println(s); }
    
    public static final class MBeanInstance
    {
        public final ConfigBean mConfigBean;
        public final ObjectName mObjectName;
        public final Object     mImpl;
        public MBeanInstance( final ConfigBean cb, final ObjectName on, final Object impl )
        {
            mConfigBean = cb;
            mObjectName = on;
            mImpl = impl;
        }
    }
    
    private final ConcurrentMap<ConfigBean,MBeanInstance> mFromConfigBean;
    private final ConcurrentMap<ObjectName, MBeanInstance> mFromObjectName;
    
    private ConfigBeanRegistry() {
        mFromConfigBean = new ConcurrentHashMap<ConfigBean,MBeanInstance>();
        mFromObjectName = new ConcurrentHashMap<ObjectName, MBeanInstance>();
    }
    
    private static final ConfigBeanRegistry INSTANCE = new ConfigBeanRegistry();
    public static ConfigBeanRegistry getInstance() {
        return INSTANCE;
    }

    private MBeanInstance getMBeanInstance(final ObjectName objectName)
    {
        return mFromObjectName.get(objectName);
    }

    private MBeanInstance getMBeanInstance(final ConfigBean cb)
    {
        return mFromConfigBean.get(cb);
    }

    public synchronized void  add(final ConfigBean cb, final ObjectName objectName, final Object impl)
    {
        final MBeanInstance mb = new MBeanInstance(cb, objectName, impl);
        mFromConfigBean.put(cb, mb );
        mFromObjectName.put(objectName, mb);
        //debug( "ConfigBeanRegistry.add(): " + objectName );
    }

    public synchronized void  remove(final ObjectName objectName)
    {
        final MBeanInstance mb = mFromObjectName.get(objectName);
        if ( mb != null )
        {
            mFromObjectName.remove(objectName);
            mFromConfigBean.remove(mb.mConfigBean);
        }
        //debug( "ConfigBeanRegistry.remove(): " + objectName );

    }

    public ConfigBean getConfigBean(final ObjectName objectName)
    {
        final MBeanInstance mb = getMBeanInstance(objectName);
        return mb == null ? null: mb.mConfigBean;
    }
    
    public ObjectName getObjectName(final ConfigBean cb)
    {
        final MBeanInstance mb = getMBeanInstance(cb);
        return mb == null ? null: mb.mObjectName;
    }
    
    public Object getImpl(final ObjectName objectName)
    {
        final MBeanInstance mb = getMBeanInstance(objectName);
        return mb == null ? null: mb.mImpl;
    }
    
    public Object getImpl(final ConfigBean cb)
    {
        final MBeanInstance mb = getMBeanInstance(cb);
        return cb == null ? null: mb.mImpl;
    }
}




