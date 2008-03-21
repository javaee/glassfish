

package org.glassfish.admin.amx.config;


import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBean;

import com.sun.appserv.management.config.AMXConfig;
import org.glassfish.admin.amx.util.SingletonEnforcer;


/**
    Called when ConfigBeans come into the habitat.  They are passed along
    to the AMXConfigLoader, which might queue them (if AMX has not been requested)
    or might register them as MBeans (if AMX has been requested already).
    
 * @author llc
 */
@Service(name="AMXConfigRegistrar")
public final class AMXConfigRegistrar implements CageBuilder
{
    private static void debug( final String s ) { System.out.println(s); }
    private AMXConfigLoader  mConfigLoader;
    
    /**
        Singleton: there should be only one instance and hence a private constructor.
        But the framework using this wants to instantiate things with a public constructor.
     */
    public AMXConfigRegistrar()
    {
        SingletonEnforcer.register( this.getClass(), this );
        
        mConfigLoader = new AMXConfigLoader();
    }
    
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
            final ConfigBean parent = asConfigBean(cb.parent());
        //debug( "AMXConfigRegistrar.onEntered: " + cb.getProxyType().getName() + " with parent " + (parent == null ? "null" : parent.getProxyType().getName()) );
        
            mConfigLoader.handleConfigBean( cb );
        }
    }
    
        public AMXConfigLoader
    getAMXConfigLoader()
    {
        return mConfigLoader;
    }
}


















