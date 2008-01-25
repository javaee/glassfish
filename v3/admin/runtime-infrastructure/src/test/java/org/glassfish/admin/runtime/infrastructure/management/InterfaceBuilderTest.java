package org.glassfish.admin.runtime.infrastructure.management;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.glassfish.admin.runtime.infrastructure.management.InterfaceBuilder;

/**
 * @author Sreenivas Munnangi
 */
public class InterfaceBuilderTest {

    @Test
    public void createMBean() {
        String objectname =  "a:b=c";
        System.out.println("TestRTProto");
        try {
            InterfaceBuilder.createMBean(new Annotated(), objectname);
            System.out.println("Created MBean, invoking opeartion ...");
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Object obj = mbs.invoke(new ObjectName(objectname), "sayMSRHello", null, null);
            assertNull(obj);
        } catch (Exception e) {
            System.out.println("Caught exception ...");
            e.printStackTrace();
            fail("Encountered exception ...");
        }
        //assertTrue(domain != null);
    }

}
