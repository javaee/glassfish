package org.glassfish.admin.amx;

import javax.management.ObjectName;

import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;

import org.jvnet.hk2.config.ConfigBean;


/**
 * @author llc
 */
@Service
public class AMXConfigObjectNameBuilder  {
    public ObjectName getObjectName(Inhabitant<?> i, String[] hint) {
    
        // get the Inhabitant metadata from AMXConfigInfo
        final String classname  = AMXConfigInfo.class.getName();
        final String j2eeType = i.metadata().getOne( classname + ".j2eeType");
        final String nameHint = i.metadata().getOne( classname + ".nameHint");
        
        final ConfigBean configBean = asConfigBean( i.get() );
        // TODO
        return null;
    }
    
    
    @SuppressWarnings("unchecked")
    final ConfigBean asConfigBean( final Object o )
    {
        return (o instanceof ConfigBean) ? (ConfigBean)o : null;
    }

}
