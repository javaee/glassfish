
package org.glassfish.admin.amx;


import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;


import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBean;

import com.sun.appserv.management.config.AMXConfig;


/**
    Called when ConfigBeans come into the habitat.  They are passed along
    to the AMXConfigLoader, which might queue them (if AMX has not been requested)
    or might register them as MBeans (if AMX has been requested already).
    
 * @author llc
 */
@Service //(name="AMXConfigRegistrar")
public final class AMXConfigRegistrar implements CageBuilder
{
    @Extract("AMXConfigRegistrar-AMXPendingConfigBeans")
    private final LinkedBlockingQueue<ConfigBean> mPendingConfigBeans = new LinkedBlockingQueue<ConfigBean>();
        
        public
    AMXConfigRegistrar()
    {
        //debug( "#### AMXConfigRegistrar.AMXConfigRegistrar  #####" );
    }
    
    private static void debug( final String s ) { System.out.println(s); }
    
    
    /**
        @return a ConfigBean, or null if it's not a ConfigBean
     */
    @SuppressWarnings("unchecked")
    final ConfigBean asConfigBean( final Object o )
    {
        return (o instanceof ConfigBean) ? (ConfigBean)o : null;
    }

    public void onEntered(Inhabitant<?> inhabitant)
    {
        final ConfigBean cb = asConfigBean(inhabitant);
        if ( cb != null )
        {
            Class<? extends ConfigBeanProxy> parentClass = null;
            final Class<? extends ConfigBeanProxy> cbClass = cb.getProxyType();
            final ConfigBean parent = asConfigBean( cb.parent() );
            if ( parent != null )
            {
                parentClass = parent.getProxyType();
            }
            //debug( "RECEIVED: " + cbClass.getName()  +
            //    ", PARENT = " + ((parentClass == null) ? "null" : parentClass.getName()) +
            //    ", parent Object Name = " + parent.getObjectName() );
            
            mPendingConfigBeans.add( cb );
        }
    }
}


















