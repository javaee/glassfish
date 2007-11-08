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
import javax.management.MBeanServerConnection;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.admin.mbeans.custom.CustomMBeanConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;

/*
 * SimpleStandardCustomMBeanTest.java
 *
 * Created on July 2, 2005, 2:27 AM
*/

public class SimpleStandardCustomMBeanTest implements RemoteAdminQuicklookTest {

    private MBeanServerConnection mbsc;
    
    private static final String BACKEND_MBEAN_ON = "com.sun.appserv:category=config,type=applications";
    private static final String SS_CUSTOM_MBEAN_CLASS = "testmbeans.SimpleStandard";
    private long start, end;
    
    /**
     * Creates a new instance of SimpleStandardCustomMBeanTest 
     */
    public SimpleStandardCustomMBeanTest() {
    }

    public long getExecutionTime() {
        return ( end - start ) ;
    }

    public String getName() {
        return ( this.getClass().getName() );
    }

    public void setMBeanServerConnection(MBeanServerConnection c) {
        this.mbsc = c;
    }

    public String test() {
        try {
            start = System.currentTimeMillis();
            testCreateDeleteListBasic();
            System.out.println("This test makes sure that SimpleStandard, which is a standard MBean can be properly created, deleted \nWhen this test is successful, an mbean is created, made sure it is created, and then deleted");
            return ( SimpleReporterAdapter.PASS );
        } catch(final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            end = System.currentTimeMillis();
        }
    }
    
    private void testCreateDeleteListBasic() throws Exception {
        final String target = null;
        final Map<String, String> params = new HashMap<String, String> ();
        params.put(CustomMBeanConstants.IMPL_CLASS_NAME_KEY, SS_CUSTOM_MBEAN_CLASS);
        final Map<String, String> attributes = new HashMap<String, String> ();
        final String name = invokeCustomMBeanCreationMethod(target, params, attributes);
        System.out.println("MBean created in domain: " + name);
        List<String> names = invokeListRegisteredMBeansMethod(target);
        //this should contain the mbean just created
        if (!names.contains(name)) {
            throw new RuntimeException("MBean got created, but is not listed ..." + name);
        }
        else {
            System.out.println("The list shows recently created MBean: " + name);
        }
        final String deleted = invokeDeleteMBeanMethod(target, name);
        System.out.println("MBean deleted from domain: " + deleted);
        names = invokeListRegisteredMBeansMethod(target);
        //this should NOT contain the mbean just created
        if (names.contains(deleted)) {
            throw new RuntimeException("The MBean is deleted, but list incorrectly shows it ..." + deleted);
        }
        else {
            System.out.println("The deleted MBean is correctly removed from returned list ...." + deleted);
        }
    }
    ///// Private /////
    
    private String invokeCustomMBeanCreationMethod(final String target, final Map<String, String> params, final Map<String, String> attributes) throws Exception {
        final ObjectName on         = new ObjectName(BACKEND_MBEAN_ON);
        final String oper           = "createMBean";
        final Object[] operParams   = new Object[]{ target, params, attributes };
        final String[] operSign     = new String[]{ String.class.getName(), Map.class.getName(), Map.class.getName() };
        return ( (String) mbsc.invoke(on, oper, operParams, operSign) );
    }
    private List<String> invokeListRegisteredMBeansMethod(final String target) throws Exception {
        final ObjectName on         = new ObjectName(BACKEND_MBEAN_ON);
        final String oper           = "listMBeanNames";
        final Object[] operParams   = new Object[]{ target };
        final String[] operSign     = new String[]{ String.class.getName() };
        return ( (List<String>) mbsc.invoke(on, oper, operParams, operSign) );
    }
    private String invokeDeleteMBeanMethod(final String target, final String name) throws Exception {
        final ObjectName on         = new ObjectName(BACKEND_MBEAN_ON);
        final String oper           = "deleteMBean";
        final Object[] operParams   = new Object[]{ target, name };
        final String[] operSign     = new String[]{ String.class.getName(), String.class.getName() };
        return ( (String) mbsc.invoke(on, oper, operParams, operSign) );
        
    }
    ///// Private /////
}
