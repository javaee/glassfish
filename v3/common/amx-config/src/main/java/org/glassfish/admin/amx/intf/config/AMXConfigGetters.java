/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.amx.intf.config;

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.AMXProxy;

/**
 * Getters for common high-level config items.
 * @author llc
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
public final class AMXConfigGetters
{
    private final DomainRoot mDomainRoot;
    private final Domain     mDomainConfig;
    
    /**
        Pass any AMXProxy to initialize.
        @param amx any AMXProxy
     */
    public AMXConfigGetters( final AMXProxy amx)
    {
        mDomainRoot = amx.extra().proxyFactory().getDomainRootProxy();
        mDomainConfig = mDomainRoot.child(Domain.class);
    }
    
    public DomainRoot domainRoot()   {  return mDomainRoot; }
    public Domain     domainConfig() { return mDomainConfig; }
    public Resources  resources()    { return domainConfig().getResources(); }
    public Applications  applications()    { return domainConfig().getApplications(); }
    public SystemApplications  systemApplications()    { return domainConfig().getSystemApplications(); }

        
     /**
        Get any Resource by name.
        @param name    name of the resource
        @param clazz   interface to be applied to the resource
       */
        public <T extends Resource> T
    getResource(final String name, final Class<T> clazz )
    {
        for( final AMXProxy child : resources().childrenSet() )
        {
            if ( child.getName().equals(name) )
            {
                return child.as(clazz);
            }
        }
        return null;
    }
    
    public Resource getResource(final String name)
    {
        return getResource(name, Resource.class);
    }
    
    public Server getServer(final String name)
    {
        return child( domainConfig().getServers(), Server.class, name );
    }
    
    
      /**
        Get any {@link Application} by name.  Looks under Applications (first) and SystemApplications.
        @param name    name of the resource
        @param clazz   interface to be applied to the resource
       */
        public Application
    getApplication(final String name)
    {
        Application appConfig = applications().childrenMap(Application.class).get(name);
        if ( appConfig == null )
        {
            appConfig = systemApplications().childrenMap(Application.class).get(name);
        }
        return appConfig;
    }
    
    /**
        Get a named child of the specified interface.
     */
    public <T extends AMXProxy> T child(final AMXProxy parent, final Class<T> intf, final String name )
    {
        return parent.childrenMap( intf ).get(name);
    }
    
    public Config getConfig(final String name)
    {
        return child( domainConfig().getConfigs(), Config.class, name );
    }
}












