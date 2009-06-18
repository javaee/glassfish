/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.amx.intf.config;

import org.glassfish.admin.amx.annotation.Stability;
import org.glassfish.admin.amx.annotation.Taxonomy;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.AMXProxy;

/**
 * Static methods for various multi-step operations.
 * @author llc
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
public final class AMXConfigUtil
{
    private AMXConfigUtil() {}

    /**
        Get the Domain config MBean from any AMXProxy.
        @Param amx any AMXProxy
    */
    public static Domain getDomainConfig(final AMXProxy amx)
    {
        final DomainRoot domainRoot = amx.extra().proxyFactory().getDomainRootProxy(false);
        
        return domainRoot.child(Domain.class);
    }
        
     /**
        Get any Resource by name.
        @param any     any AMX proxy
        @param name    name of the resource
       */
        public static Resource
    getResourceByName(final AMXProxy any, final String name)
    {
        final Domain domainConfig = getDomainConfig(any);
        if ( domainConfig == null )
        {
            throw new IllegalArgumentException("NULL DOMAIN CONFIG" );
        }
        final Resources resources = domainConfig.getResources();
        
        Resource resourceConfig = null;
        for( final AMXProxy child : resources.childrenSet() )
        {
            if ( child.getName().equals(name) )
            {
                resourceConfig = child.as(Resource.class);
                break;
            }
        }
        return resourceConfig;
    }
    
      /**
        Get any {@link Application} by name.  Looks under Applications (first) and SystemApplications.
        @param any     any AMX proxy
        @param name    name of the resource
       */
        public static Application
    getApplicationByName(final AMXProxy any, final String name)
    {
        final Domain domainConfig = getDomainConfig(any);
        
        Application appConfig = domainConfig.getApplications().childrenMap(Application.class).get(name);
        if ( appConfig == null )
        {
            appConfig = domainConfig.getSystemApplications().childrenMap(Application.class).get(name);
        }
        
        return appConfig;
    }
}










