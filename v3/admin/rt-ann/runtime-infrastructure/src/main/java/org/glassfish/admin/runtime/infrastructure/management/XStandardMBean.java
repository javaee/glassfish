package org.glassfish.admin.runtime.infrastructure.management;

import javax.management.*;
import java.lang.reflect.Proxy;

/**
 * Class description
 *
 * @author Sreenivas Munnangi
 */

public class XStandardMBean extends StandardMBean {

    private Object get;

    public <T> XStandardMBean (T impl, Class<T> intf) throws NotCompliantMBeanException {
        super(impl, intf);
        System.out.println("MSR: in XStandardMBean other constr ...");
    }
    @java.lang.Override
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        super.preRegister(server, name);
        System.out.println("MSR: in XStandardMBean preRegsiter ...");
        Object obj = this.getImplementation();
        MBeanInvocationHandler handler = (MBeanInvocationHandler) Proxy.getInvocationHandler(obj);
        // add instanceof tests to protect against exceptions
        Object userObj = handler.getWrapped();
        MBeanInjector.inject(userObj, server, name);
        System.out.println("MSR: in XStandardMBean preRegsiter ...after injection");
        return name;
    }
}
