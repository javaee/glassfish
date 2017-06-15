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
 * ObjectNameTest.java
 *
 * Created on July 19, 2005, 2:38 AM
*/

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.admin.mbeans.custom.CustomMBeanConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
public class ObjectNameTest implements RemoteAdminQuicklookTest {

    private MBeanServerConnection mbsc;
    
    private static final String BACKEND_MBEAN_ON = "com.sun.appserv:category=config,type=applications";
    private static final String SS_CUSTOM_MBEAN_CLASS = "testmbeans.SimpleStandard";
    private long start, end;
    
    /** Creates a new instance of ObjectNameTest */
    public ObjectNameTest() {
    }

    public long getExecutionTime() {
        return ( end - start );
    }

    public String getName() {
        return ( ObjectNameTest.class.getName() );
    }

    public void setMBeanServerConnection(final MBeanServerConnection c) {
        this.mbsc = c;
    }

    public String test() {
        try {
            start = System.currentTimeMillis();
            testInvalidObjectNames();
            testDuplicateObjectNames();
            testEquivalentObjectNames();
            System.out.println("Various ObjectName tests ...");
            return ( SimpleReporterAdapter.PASS );
        } catch(final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            end = System.currentTimeMillis();
        }
    }
    ///// Private /////
    private void testInvalidObjectNames() throws Exception {
        final String target1 = "server"; //the DAS
        final Map<String, String> params = new HashMap<String, String> ();
        params.put(CustomMBeanConstants.IMPL_CLASS_NAME_KEY, SS_CUSTOM_MBEAN_CLASS);
        
        //invalid domain
        String invalidDomainNameON = "foo:bar=baz";
        params.put(CustomMBeanConstants.OBJECT_NAME_KEY, invalidDomainNameON);
        
        try {
            invokeCustomMBeanCreationMethod(target1, params, new HashMap<String, String> ());
        } catch (final Exception e) {
            //exception should be thrown and should be squelched as test passes in that case.
            System.out.println(e.getMessage());
        }
        //use of pattern
        invalidDomainNameON = "foo:bar=*,name=baz";
        params.put(CustomMBeanConstants.OBJECT_NAME_KEY, invalidDomainNameON);
        
        try {
            invokeCustomMBeanCreationMethod(target1, params, new HashMap<String, String> ());
        } catch (final Exception e) {
            //exception should be thrown and should be squelched as test passes in that case.
            System.out.println(e.getMessage());
        }        
    }
    
    private void testDuplicateObjectNames() throws Exception {
        final String target1 = "server"; //the DAS
        final Map<String, String> params = new HashMap<String, String> ();
        params.put(CustomMBeanConstants.IMPL_CLASS_NAME_KEY, SS_CUSTOM_MBEAN_CLASS);
        
        final String name = "custom" + System.currentTimeMillis();
        params.put(CustomMBeanConstants.NAME_KEY, name);

        String on = "user:bar=baz" + System.currentTimeMillis();
        params.put(CustomMBeanConstants.OBJECT_NAME_KEY, on);
        
        invokeCustomMBeanCreationMethod(target1, params, new HashMap<String, String> ());
        
        try {
            invokeCustomMBeanCreationMethod(target1, params, new HashMap<String, String> ());
        } catch (final Exception e) {
            //exception should be thrown and should be squelched as test passes in that case.
            System.out.println(e.getMessage());
        }        
    }

    private void testEquivalentObjectNames() throws Exception {
        final String target1 = "server"; //the DAS
        final Map<String, String> params = new HashMap<String, String> ();
        params.put(CustomMBeanConstants.IMPL_CLASS_NAME_KEY, SS_CUSTOM_MBEAN_CLASS);
        
        final String name = "custom" + System.currentTimeMillis();
        params.put(CustomMBeanConstants.NAME_KEY, name);

        final String p1 = "bar=baz" + System.currentTimeMillis();
        final String p2 = "baz=bar" + System.currentTimeMillis();
        String on = "user:" + p1 + "," + p2;
        params.put(CustomMBeanConstants.OBJECT_NAME_KEY, on);
        
        invokeCustomMBeanCreationMethod(target1, params, new HashMap<String, String> ());
        
        try {
            on = "user:"+ p2 + "," + p1; //this is same as previous, hence the following should fail
            params.put(CustomMBeanConstants.OBJECT_NAME_KEY, on);
            params.put(CustomMBeanConstants.NAME_KEY, name + System.currentTimeMillis());
            invokeCustomMBeanCreationMethod(target1, params, new HashMap<String, String> ());
        } catch (final Exception e) {
            //exception should be thrown and should be squelched as test passes in that case.
            System.out.println(e.getMessage());
        }        
    }

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
