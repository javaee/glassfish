package org.glassfish.flashlight.client;

import org.glassfish.external.probe.provider.annotations.ProbeParam;

import java.lang.reflect.Method;

/**
 * @author Mahesh Kannan
 *         Date: Jul 20, 2008
 */
public class EjbContainerListener {

    @ProbeListener("ejb:container::entry")
    public void foo(@ProbeParam("method")Method m, @ProbeParam("beanName")String beanName) {
        System.out.println("Got callback for: " + beanName);
    }
    
    @ProbeListener("ejb:container::entry")
    public void foo2(@ProbeParam("beanName")String beanName,
                     @ProbeParam("$appName")String applicationName,
                     @ProbeParam("method")Method m) {
        System.out.println("Got callback for: " + applicationName
                + "[" + beanName + "]");
    }

}
