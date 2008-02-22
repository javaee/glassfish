
package org.glassfish.admin.amx;


import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBean;

import com.sun.appserv.management.config.AMXConfig;


/**
 * @author llc
 */
@Service //(name="AMXConfigRegistrar")
public final class AMXConfigRegistrar implements CageBuilder
{
    private volatile AMXConfigLoader  mConfigLoader;
    
    @SuppressWarnings("unchecked")
    final ConfigBean asConfigBean( final Object o )
    {
        return (o instanceof ConfigBean) ? (ConfigBean)o : null;
    }
    
        public
    AMXConfigRegistrar()
    {
        //debug( "#### AMXConfigRegistrar.AMXConfigRegistrar  #####" );
        
        mConfigLoader = new AMXConfigLoader();
    }
    
    private static void debug( final String s ) { System.out.println(s); }
    
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


















