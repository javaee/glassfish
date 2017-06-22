package test;
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

/*
 * JVMInformationTest.java
 *
 * Created on July 22, 2005, 12:53 AM
 */
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.admin.mbeans.jvm.JVMInformationMBean;
import com.sun.enterprise.admin.server.core.AdminService;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

/**
 */
public class JVMInformationTest implements RemoteAdminQuicklookTest {

    private MBeanServerConnection mbsc;
    
    private static final String BACKEND_MBEAN_ON = AdminService.PRIVATE_MBEAN_DOMAIN_NAME + ":" + "category=monitor,type=JVMInformationCollector,server=server";
    private long start, end;
    /** Creates a new instance of JVMInformationTest */
    public JVMInformationTest() {
    }

    public void setMBeanServerConnection(MBeanServerConnection c) {
        this.mbsc = c;
    }

    public String test() {
            try {
            start = System.currentTimeMillis();
            dumpDASInfo();
            System.out.println("Gets the DAS VM Information");
            return ( SimpleReporterAdapter.PASS );
        } catch(final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            end = System.currentTimeMillis();
        }
}

    public String getName() {
        return ( this.getClass().getName() );
    }

    public long getExecutionTime() {
        return ( end - start );
    }
    
    String getMemoryInfo() throws Exception 
    {
        final ObjectName on = new ObjectName(BACKEND_MBEAN_ON);
        final JVMInformationMBean mbean = (JVMInformationMBean)MBeanServerInvocationHandler.newProxyInstance(mbsc, on, com.sun.enterprise.admin.mbeans.jvm.JVMInformationMBean.class, false);
        return mbean.getMemoryInformation(null);
    }
    private void dumpDASInfo() throws Exception {
        final ObjectName on = new ObjectName(BACKEND_MBEAN_ON);
        final JVMInformationMBean mbean = (JVMInformationMBean)MBeanServerInvocationHandler.newProxyInstance(mbsc, on, com.sun.enterprise.admin.mbeans.jvm.JVMInformationMBean.class, false);
        System.out.println(mbean.getThreadDump(null));
        System.out.println(mbean.getMemoryInformation(null));
        System.out.println(mbean.getClassInformation(null));
        System.out.println(mbean.getSummary(null));
        
    }
}
