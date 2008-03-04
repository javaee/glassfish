package org.glassfish.admingui.util;

import java.util.Map;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.client.ProxyFactory;

import java.lang.management.ManagementFactory;
import javax.faces.context.FacesContext;
import javax.management.MBeanServer;


public class AMXUtil {

    private static DomainRoot domainRoot = null;

    private AMXUtil() {} //dummy constructor, all static methods.

    private static final MBeanServer mMBeanServer = ManagementFactory.getPlatformMBeanServer();

    public static DomainRoot getDomainRoot() {
        if (domainRoot == null){
            domainRoot = ProxyFactory.getInstance( mMBeanServer ).getDomainRoot();
            domainRoot.waitAMXReady();
        }
        return domainRoot;
    }

    public static AMXRoot getAmxRoot(){
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        return (AMXRoot) sessionMap.get("_AMXROOT");
    }
}
