/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package test;

/*
 * PrimitiveDataTypeMBeanTest.java
 *
 * Created on July 9, 2005, 11:01 PM
 */

/**
 *
 */
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.admin.mbeans.custom.CustomMBeanConstants;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class PrimitiveDataTypeMBeanTest implements RemoteAdminQuicklookTest {
    
    private long start, end;
    MBeanServerConnection mbsc = null;
    private static final String SS_CUSTOM_MBEAN_CLASS = "testmbeans.PrimitiveStandard";
    private static final String BACKEND_MBEAN_ON = "com.sun.appserv:category=config,type=applications";
    /** Creates a new instance of PrimitiveDataTypeMBeanTest */
    public PrimitiveDataTypeMBeanTest() {
    }

    public long getExecutionTime() {
        return ( end - start );
    }

    public String getName() {
        return ( PrimitiveDataTypeMBeanTest.class.getName() );
    }

    public void setMBeanServerConnection(final MBeanServerConnection c) {
        this.mbsc = c;
    }

    public String test() {
        try {
            start = System.currentTimeMillis();
            testCreatePrimitiveDataTypeMBean();
            System.out.println("This test makes sure that an MBean ");
            return ( SimpleReporterAdapter.PASS );
        } catch(final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            end = System.currentTimeMillis();
        }    
    }
    
    /** This test tests the PrimitiveStandardMBean
     */
    private void testCreatePrimitiveDataTypeMBean() throws Exception {
        final String target = null;
        final Map<String, String> params = new HashMap<String, String> ();
        params.put(CustomMBeanConstants.IMPL_CLASS_NAME_KEY, SS_CUSTOM_MBEAN_CLASS);
        params.put(CustomMBeanConstants.NAME_KEY, "custom" + System.currentTimeMillis());
        final Map<String, String> attributes = getAttributeMapForPrimitives();
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
    }
    ///// Private /////
    
    private Map<String, String> getAttributeMapForPrimitives() {
        final Map<String, String> attributes = new HashMap<String, String> ();
        final Random r = new Random();
        String v  = Boolean.toString(r.nextBoolean());
        String n  = "State";
        attributes.put(n, v);
        
        v = Integer.toString(r.nextInt());
        n = "Rank";
        attributes.put(n, v);
        
        v = Long.toString(r.nextLong());
        n = "Time";
        attributes.put(n, v);
        
        final byte[] bytes = new byte[1];
        r.nextBytes(bytes);
        v = Byte.toString(bytes[0]);
        n = "Length";
        attributes.put(n, v);
        
        /*v = Character.toString((char)r.nextInt());
        n = "ColorCode";
        attributes.put(n, v);*/
        
        v = Float.toString(r.nextFloat());
        n = "AnnualPercentRate";
        attributes.put(n, v);
        
        v = Double.toString(r.nextDouble());
        n = "Temperature";
        attributes.put(n, v);
        
        v = Short.toString((short)r.nextInt());
        n = "Characters";
        attributes.put(n, v);
        
        v = generateA2ZRandomString('a', 'z');
        n = "Name";
        attributes.put(n, v);
        
        v = DateFormat.getDateInstance().format(new Date());
        n = "StartDate";
        attributes.put(n, v);
        
        v = "foo:" + generateA2ZRandomString('a', 'm') + "=" + generateA2ZRandomString('n', 'z');
        n = "ResourceObjectName";
        attributes.put(n, v);
        
        return ( attributes );
    }
    private String generateA2ZRandomString(final int lo, final int hi) {
        final Random r = new Random();
        final int N = 26;
        final byte b[] = new byte[N];
        for (int i = 0; i < N; i++) {
            int x = 0;
            do {
                x = r.nextInt(hi);
            } while (x <= hi && lo >= x);
            b[i] = (byte)x;
        }
        return ( new String (b) );
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
