/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.amx.impl.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.management.ObjectName;
import org.jvnet.hk2.config.ConfigBean;

/**
 * Temporary registry mapping ConfigBean to ObjectName and vice-versa
 * until the earlier implementation is removed, freeing up the ObjectName
 * field of the ConfigBean.
 * @author llc
 */
public final class ConfigBeanRegistry {
    private static void debug( final String s ) { System.out.println(s); }
    
    private final ConcurrentMap<ConfigBean,ObjectName> mToObjectName;
    private final ConcurrentMap<ObjectName, ConfigBean> mToConfigBean;
    
    public ConfigBeanRegistry() {
        mToObjectName = new ConcurrentHashMap<ConfigBean,ObjectName>();
        mToConfigBean = new ConcurrentHashMap<ObjectName, ConfigBean>();
    }

    public synchronized void  add(final ConfigBean cb, final ObjectName objectName)
    {
        mToObjectName.put(cb, objectName);
        mToConfigBean.put(objectName, cb);
        //debug( "ConfigBeanRegistry.add(): " + objectName );
    }

    public synchronized void  remove(final ObjectName objectName)
    {
        final ConfigBean cb = mToConfigBean.get(objectName);
        mToObjectName.remove(objectName);
        if ( cb != null )
        {
            mToConfigBean.remove(cb);
        }
        //debug( "ConfigBeanRegistry.remove(): " + objectName );

    }

    public ConfigBean getConfigBean(final ObjectName objectName)
    {
        return mToConfigBean.get(objectName);
    }


    public ObjectName getObjectName(final ConfigBean cb)
    {
        return mToObjectName.get(cb);
    }

}
