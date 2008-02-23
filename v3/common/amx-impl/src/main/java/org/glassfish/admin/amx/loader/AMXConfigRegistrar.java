

package org.glassfish.admin.amx.loader;


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
    private volatile AMXConfigLoader  mConfigLoader;
        
        public
    AMXConfigRegistrar()
    {
        //debug( "#### AMXConfigRegistrar.AMXConfigRegistrar  #####" );
        
        mConfigLoader = new AMXConfigLoader();
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
            mConfigLoader.handleConfigBean( cb );
        }
    }
    
        public AMXConfigLoader
    getAMXConfigLoader()
    {
        return mConfigLoader;
    }
}


















