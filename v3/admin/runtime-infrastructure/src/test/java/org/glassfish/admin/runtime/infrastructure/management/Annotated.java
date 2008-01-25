package org.glassfish.admin.runtime.infrastructure.management;

import javax.annotation.Resource;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.glassfish.admin.runtime.infrastructure.management.ManagedAttribute;
import org.glassfish.admin.runtime.infrastructure.management.ManagedOperation;

public class Annotated {

    @Resource
    public MBeanServer mbeanServer;
    
    @Resource
    public ObjectName objectName;

    @ManagedOperation
    public void sayMSRHello() {
        System.out.println("Hello, world");
        System.out.println("mbs = " + mbeanServer);
        System.out.println("on = " + objectName);
    }

    @ManagedAttribute
    public int getMSRX() {
        return x;
    }

    @ManagedAttribute
    public void setMSRX(int x) {
        this.x = x;
    }

    private int x;
}
    
