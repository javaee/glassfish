package com.sun.enterprise.naming.impl;

import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.jvnet.hk2.component.Habitat;

import javax.naming.NamingException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Mahesh Kannan
 *         Date: Mar 4, 2008
 */
public class NamedNamingObjectManager {

    private static AtomicBoolean gotAllNamedProxies
            = new AtomicBoolean(false);

    private static List<NamedNamingObjectProxy> proxies = new ArrayList<NamedNamingObjectProxy>();


    public static void checkAndLoadProxies(Habitat habitat)
        throws NamingException {
        if (! gotAllNamedProxies.get()) {
            if (habitat != null) {
                synchronized (gotAllNamedProxies) {
                    if (!gotAllNamedProxies.get()) {
                        GlassfishNamingManager nm =
                                habitat.getByContract(GlassfishNamingManager.class);
                        for (NamedNamingObjectProxy proxy : habitat.getAllByContract(NamedNamingObjectProxy.class)) {
                            //System.out.println("Got NamedNamingObjectProxy: " + proxy.getClass().getName());
                            proxies.add(proxy);
                        }
                        gotAllNamedProxies.set(true);
                    }
                }
            }
        }
    }

    public static Object tryNamedProxies(String name)
        throws NamingException {

        Object obj = null;
        for (NamedNamingObjectProxy proxy : proxies) {
            obj = proxy.handle(name);
            if (obj != null) {
                break;
            }
        }

        return obj;
    }
}
