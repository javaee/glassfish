/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;


import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.proxy.AMXBooter;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.intf.config.ConfigConfig;
import org.glassfish.admin.amx.intf.config.ConfigsConfig;
import org.glassfish.admin.amx.intf.config.DomainConfig;


import org.jvnet.hk2.component.Habitat;

/**
 *
 * @author anilam
 */
public class V3AMX {
    private static V3AMX v3amx  ;
    private final DomainRoot domainRoot;
    private final DomainConfig domainConfig;
    private final ProxyFactory proxyFactory;

    private V3AMX(DomainRoot dd, MBeanServerConnection msc) {
        proxyFactory = ProxyFactory.getInstance(msc);
        domainRoot = dd;
        domainConfig =  domainRoot.getDomain().as(DomainConfig.class);
    }

     /**
     *	<p> Use this method to get the singleton instance of this object.</p>
     *
     *	<p> On the first invokation of this method, it will obtain the official
     *	    MBeanServer from the <code>Habitat</code>.  This will cause the
     *	    MBeanServer to initialize when this is called, if it hasn't already
     *	    been initialized.</p>
     */
    public synchronized static V3AMX getInstance() {
	if (v3amx == null) {
	    // Get the ServletContext
	    ServletContext servletCtx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();

        // Get the Habitat from the ServletContext
        Habitat habitat = (Habitat) servletCtx.getAttribute(
            org.glassfish.admingui.common.plugin.ConsoleClassLoader.HABITAT_ATTRIBUTE);

	    // Get the MBeanServer via the Habitat, we want the "official" one
	    MBeanServer mbs = (MBeanServer) habitat.getComponent(MBeanServer.class);
            if (mbs == null){
                 mbs = java.lang.management.ManagementFactory.getPlatformMBeanServer();
            }
            if (mbs == null){
                System.out.println("!!!!!!!!!!!!!!  Cannot get to MBeanServer");
            }
            AMXBooter.bootAMX(mbs);
            DomainRoot domainRoot = ProxyFactory.getInstance(mbs).getDomainRootProxy();
            v3amx = new V3AMX( domainRoot, mbs);
	}
        return v3amx;
    }


    public DomainConfig getDomainConfig(){
        return domainConfig;
    }

    public DomainRoot getDomainRoot(){
        return domainRoot;
    }

    public ConfigsConfig getConfigsConfig(){
        return domainConfig.getConfigs();
    }

    public ProxyFactory getProxyFactory(){
        return proxyFactory;
    }

    public AMXProxy getProxy( ObjectName objName){
        return proxyFactory.getProxy(objName);
    }

    public static ConfigConfig getServerConfig(String configName){
        if ((configName == null) || configName.equals(""))
                configName = "server-config";
        return V3AMX.getInstance().getConfigsConfig().getConfig().get(configName);
    }

}
